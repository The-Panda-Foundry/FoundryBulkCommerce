package com.pandafoundry.bulkcommerce.ledger;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** One local writer; no Minecraft objects, remote services or replay engine. */
public final class LocalLedger {
    public record Intent(UUID id, UUID player, String playerName, long createdAt, int quantity, int payment, String json) {}
    public record Row(String id, String instance, String boot, long createdAt, String player, String playerName,
                      int quantity, int payment, String state, boolean recoveryRequired, String intent,
                      String outcome, String intentHash) {}
    public record Health(boolean ready, boolean blocked, String instance, String problem, int queued) {}
    private final Path path;
    private final String configuredInstance;
    private final String boot = UUID.randomUUID().toString();
    private final ThreadPoolExecutor writer;
    private volatile boolean ready, blocked, closing;
    private volatile String instance = "starting", problem = "";
    private Connection connection;
    private FileChannel lockChannel;
    private FileLock lock;

    public LocalLedger(Path path, String configuredInstance) {
        this.path = path.toAbsolutePath();
        this.configuredInstance = configuredInstance;
        writer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(64), task -> {
            Thread thread = new Thread(task, "foundry-commerce-ledger");
            thread.setDaemon(false);
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());
    }

    public Health health() { return new Health(ready, blocked, instance, problem, writer.getQueue().size()); }
    public String bootId() { return boot; }

    public CompletableFuture<Health> open(Runnable verifyDependencies) {
        return io(() -> {
            verifyDependencies.run();
            Files.createDirectories(path.getParent());
            try { Files.setPosixFilePermissions(path.getParent(), java.nio.file.attribute.PosixFilePermissions.fromString("rwx------")); }
            catch (UnsupportedOperationException ignored) { /* Windows: use the hosting account's directory ACL. */ }
            lockChannel = FileChannel.open(path.resolveSibling("ledger.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            if (lock == null) throw new IllegalStateException("Ledger is already owned by another process");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            try { Files.setPosixFilePermissions(path, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")); }
            catch (UnsupportedOperationException ignored) { /* Directory ACL applies on platforms without POSIX permissions. */ }
            try (var statement = connection.createStatement()) {
                try (var result = statement.executeQuery("PRAGMA journal_mode=WAL")) {
                    if (!result.next() || !"wal".equalsIgnoreCase(result.getString(1))) throw new SQLException("WAL unavailable");
                }
                statement.execute("PRAGMA synchronous=FULL");
                statement.execute("PRAGMA busy_timeout=2500");
                try (var result = statement.executeQuery("PRAGMA synchronous")) {
                    if (!result.next() || result.getInt(1) != 2) throw new SQLException("FULL durability unavailable");
                }
                try (var result = statement.executeQuery("PRAGMA quick_check")) {
                    if (!result.next() || !"ok".equals(result.getString(1))) throw new SQLException("Ledger integrity check failed");
                }
                try (var result = statement.executeQuery("PRAGMA user_version")) {
                    if (!result.next() || result.getInt(1) > 1) throw new SQLException("Unsupported ledger schema");
                }
                statement.execute("CREATE TABLE IF NOT EXISTS metadata (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
                statement.execute("CREATE TABLE IF NOT EXISTS transactions (id TEXT PRIMARY KEY, instance_id TEXT NOT NULL, boot_id TEXT NOT NULL, created_at INTEGER NOT NULL, player_uuid TEXT NOT NULL, player_name TEXT NOT NULL, quantity INTEGER NOT NULL, payment INTEGER NOT NULL, state TEXT NOT NULL, recovery_required INTEGER NOT NULL DEFAULT 0, intent_json TEXT NOT NULL, outcome_json TEXT NOT NULL DEFAULT '', intent_sha256 TEXT NOT NULL)");
                statement.execute("CREATE INDEX IF NOT EXISTS tx_player_time ON transactions(player_uuid, created_at DESC)");
                statement.execute("CREATE INDEX IF NOT EXISTS tx_time ON transactions(created_at DESC)");
                statement.execute("CREATE INDEX IF NOT EXISTS tx_recovery ON transactions(recovery_required, state)");
                statement.execute("CREATE TABLE IF NOT EXISTS reconciliations (id TEXT PRIMARY KEY, transaction_id TEXT NOT NULL, recorded_at INTEGER NOT NULL, operator TEXT NOT NULL, notes TEXT NOT NULL)");
                statement.execute("PRAGMA user_version=1");
            }
            transaction(() -> {
                String previousInstance = meta("instance_id");
                instance = previousInstance == null ? (configuredInstance.isBlank() ? UUID.randomUUID().toString() : configuredInstance) : previousInstance;
                if (!configuredInstance.isBlank() && !configuredInstance.equals(instance)) throw new SQLException("Configured instance does not match this ledger");
                String previousBoot = meta("boot_id");
                if (previousBoot != null && !"1".equals(meta("clean_stop"))) {
                    try (var update = connection.prepareStatement("UPDATE transactions SET recovery_required=1 WHERE boot_id=? AND state IN ('PREPARED','APPLIED_MEMORY','ROLLED_BACK_MEMORY','RECOVERY_REQUIRED')")) {
                        update.setString(1, previousBoot); update.executeUpdate();
                    }
                }
                blocked = "1".equals(meta("breaker")) || unresolvedCount() > 0;
                putMeta("instance_id", instance); putMeta("boot_id", boot); putMeta("clean_stop", "0");
                putMeta("breaker", blocked ? "1" : "0");
                if (blocked) problem = "Manual reconciliation required; inspect unresolved records";
                return null;
            });
            ready = true;
            return health();
        });
    }

    public CompletableFuture<Void> prepare(Intent intent) {
        return io(() -> {
            if (!ready || blocked) throw new SQLException("Ledger is not accepting transactions");
            if (intent.quantity() <= 0 || intent.payment() <= 0) throw new IllegalArgumentException("Invalid economic intent");
            return transaction(() -> {
                try (var insert = connection.prepareStatement("INSERT INTO transactions(id,instance_id,boot_id,created_at,player_uuid,player_name,quantity,payment,state,intent_json,intent_sha256) VALUES(?,?,?,?,?,?,?,?,'PREPARED',?,?)")) {
                    insert.setString(1, intent.id().toString()); insert.setString(2, instance); insert.setString(3, boot);
                    insert.setLong(4, intent.createdAt()); insert.setString(5, intent.player().toString()); insert.setString(6, intent.playerName());
                    insert.setInt(7, intent.quantity()); insert.setInt(8, intent.payment()); insert.setString(9, intent.json());
                    insert.setString(10, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(intent.json().getBytes(java.nio.charset.StandardCharsets.UTF_8))));
                    insert.executeUpdate();
                }
                return null;
            });
        });
    }

    public CompletableFuture<Void> outcome(UUID id, String state, String outcome, boolean requiresRecovery) {
        if (!List.of("CANCELLED", "APPLIED_MEMORY", "ROLLED_BACK_MEMORY", "RECOVERY_REQUIRED").contains(state))
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid outcome"));
        if (requiresRecovery) blocked = true;
        return io(() -> transaction(() -> {
            try (var update = connection.prepareStatement("UPDATE transactions SET state=?,outcome_json=?,recovery_required=? WHERE id=? AND state='PREPARED'")) {
                update.setString(1, state); update.setString(2, outcome); update.setInt(3, requiresRecovery ? 1 : 0); update.setString(4, id.toString());
                if (update.executeUpdate() != 1) throw new SQLException("Outcome requires exactly one prepared intent");
            }
            if (requiresRecovery) { putMeta("breaker", "1"); putMeta("breaker_reason", "Transaction " + id + " requires recovery"); }
            return null;
        }));
    }

    public CompletableFuture<Void> trip(String reason) {
        blocked = true; problem = reason;
        return io(() -> transaction(() -> { putMeta("breaker", "1"); putMeta("breaker_reason", reason); return null; }));
    }

    public CompletableFuture<List<Row>> recent(UUID player, boolean unresolved, int limit) {
        int bounded = Math.max(1, Math.min(25, limit));
        String filter = player != null ? " WHERE player_uuid=?" : unresolved ? " WHERE recovery_required=1 OR state='PREPARED'" : "";
        return io(() -> {
            List<Row> rows = new ArrayList<>();
            try (var query = connection.prepareStatement("SELECT id,instance_id,boot_id,created_at,player_uuid,player_name,quantity,payment,state,recovery_required,'' AS intent_json,'' AS outcome_json,intent_sha256 FROM transactions" + filter + " ORDER BY created_at DESC,id LIMIT ?")) {
                int parameter = 1;
                if (player != null) query.setString(parameter++, player.toString());
                query.setInt(parameter, bounded);
                try (var result = query.executeQuery()) { while (result.next()) rows.add(row(result)); }
            }
            return List.copyOf(rows);
        });
    }

    public CompletableFuture<Optional<Row>> inspect(UUID id) {
        return io(() -> {
            try (var query = connection.prepareStatement("SELECT * FROM transactions WHERE id=?")) {
                query.setString(1, id.toString());
                try (var result = query.executeQuery()) { return result.next() ? Optional.of(row(result)) : Optional.empty(); }
            }
        });
    }

    private Row row(ResultSet r) throws SQLException {
        return new Row(r.getString("id"), r.getString("instance_id"), r.getString("boot_id"), r.getLong("created_at"),
            r.getString("player_uuid"), r.getString("player_name"), r.getInt("quantity"), r.getInt("payment"), r.getString("state"),
            r.getInt("recovery_required") != 0, r.getString("intent_json"), r.getString("outcome_json"), r.getString("intent_sha256"));
    }
    private int unresolvedCount() throws SQLException {
        try (var query = connection.createStatement(); var r = query.executeQuery("SELECT COUNT(*) FROM transactions WHERE recovery_required=1 OR state='PREPARED'")) { return r.next() ? r.getInt(1) : 0; }
    }
    private String meta(String key) throws SQLException {
        try (var query = connection.prepareStatement("SELECT value FROM metadata WHERE key=?")) {
            query.setString(1, key); try (var r = query.executeQuery()) { return r.next() ? r.getString(1) : null; }
        }
    }
    private void putMeta(String key, String value) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO metadata(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            statement.setString(1, key); statement.setString(2, value); statement.executeUpdate();
        }
    }
    private <T> T transaction(Callable<T> work) throws Exception {
        connection.setAutoCommit(false);
        try { T value = work.call(); connection.commit(); return value; }
        catch (Exception failure) {
            try { connection.rollback(); } catch (SQLException rollback) { failure.addSuppressed(rollback); }
            throw failure;
        } finally { connection.setAutoCommit(true); }
    }
    private void failure(Throwable error) {
        blocked = true; problem = error.getClass().getSimpleName();
        if (connection != null) {
            try { putMeta("breaker", "1"); putMeta("breaker_reason", problem); }
            catch (SQLException ignored) { /* A surviving PREPARED/unclean boot is itself a recovery obligation. */ }
        }
    }
    private <T> CompletableFuture<T> io(Callable<T> work) {
        if (closing) return CompletableFuture.failedFuture(new IllegalStateException("Ledger is closing"));
        CompletableFuture<T> result = new CompletableFuture<>();
        try {
            writer.execute(() -> {
                try { result.complete(work.call()); }
                catch (Exception | LinkageError error) { failure(error); result.completeExceptionally(error); }
            });
        } catch (RejectedExecutionException full) {
            // No work was accepted. A caller must never mutate without a successful prepare acknowledgement.
            result.completeExceptionally(full);
        }
        return result;
    }

    /** Tests may request an unclean close; production calls this only after ServerStoppedEvent. */
    public CompletableFuture<Void> close(boolean orderly) {
        closing = true; ready = false;
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            writer.execute(() -> {
                Exception problem = null;
                try {
                    if (connection != null && orderly && !blocked && unresolvedCount() == 0)
                        transaction(() -> { putMeta("clean_stop", "1"); return null; });
                } catch (Exception error) { failure(error); problem = error; }
                finally {
                    try { if (connection != null) connection.close(); } catch (Exception error) { problem = error; }
                    try { if (lock != null) lock.release(); } catch (Exception error) { problem = error; }
                    try { if (lockChannel != null) lockChannel.close(); } catch (Exception error) { problem = error; }
                }
                if (problem == null) result.complete(null); else result.completeExceptionally(problem);
            });
        } catch (RejectedExecutionException full) { result.completeExceptionally(full); }
        writer.shutdown();
        return result;
    }
}
