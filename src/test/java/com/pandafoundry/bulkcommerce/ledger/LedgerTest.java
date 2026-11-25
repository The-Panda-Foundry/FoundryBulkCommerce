package com.pandafoundry.bulkcommerce.ledger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Actual native SQLite: durable records, reopen, injected SQL failure, exclusivity and persistent recovery. */
public final class LedgerTest {
    private static int checks;
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    private static <T> T waitFor(CompletableFuture<T> future) throws Exception { return future.get(15, TimeUnit.SECONDS); }
    private static void fails(CompletableFuture<?> future, String message) throws Exception {
        boolean failed = false;
        try { waitFor(future); } catch (java.util.concurrent.ExecutionException expected) { failed = true; }
        check(failed, message);
    }
    private static LocalLedger.Intent intent(UUID id, UUID player) {
        return new LocalLedger.Intent(id, player, "Name 'quoted'", System.currentTimeMillis(), 2304, 2,
            "{\"schemaVersion\":1,\"before\":\"test snapshot\",\"paymentSpurs\":2}");
    }
    private static LocalLedger open(Path path) throws Exception {
        LocalLedger ledger = new LocalLedger(path, "staging-test");
        waitFor(ledger.open(() -> {})); return ledger;
    }
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("foundry-commerce-ledger-test-");
        UUID player = UUID.randomUUID(), first = UUID.randomUUID();
        Path main = directory.resolve("main.sqlite");
        LocalLedger ledger = new LocalLedger(main, "staging-test");
        check(!ledger.health().ready(), "not ready before durable startup");
        AtomicBoolean offCaller = new AtomicBoolean();
        String caller = Thread.currentThread().getName();
        var health = waitFor(ledger.open(() -> offCaller.set(!Thread.currentThread().getName().equals(caller))));
        check(offCaller.get() && health.ready() && !health.blocked(), "dependency and journal IO run on dedicated worker");
        waitFor(ledger.prepare(intent(first, player)));
        var row = waitFor(ledger.inspect(first)).orElseThrow();
        check(row.state().equals("PREPARED") && row.instance().equals("staging-test"), "durable intent includes source instance");
        check(row.intent().equals(intent(first, player).json()) && row.intentHash().length() == 64, "full evidence and hash retained");
        waitFor(ledger.outcome(first, "APPLIED_MEMORY", "{\"observed\":true}", false));
        check(waitFor(ledger.inspect(first)).orElseThrow().state().equals("APPLIED_MEMORY"), "outcome recorded separately from intent");
        var list = waitFor(ledger.recent(player, false, 100));
        check(list.size() == 1 && list.getFirst().intent().isEmpty(), "history is bounded and does not load snapshot payloads");
        check(waitFor(ledger.recent(UUID.randomUUID(), false, 25)).isEmpty(), "player filtering uses exact UUID");
        check(waitFor(ledger.recent(null, true, 25)).isEmpty(), "normal observed outcome is not an active recovery case");
        waitFor(ledger.close(true));
        ledger = open(main);
        check(!ledger.health().blocked(), "completed orderly shutdown can reopen");
        check(waitFor(ledger.inspect(first)).orElseThrow().playerName().equals("Name 'quoted'"), "prepared SQL preserves quoted strings");

        // A recorded in-memory success is still suspect after an unclean process exit.
        UUID second = UUID.randomUUID();
        waitFor(ledger.prepare(intent(second, player)));
        waitFor(ledger.outcome(second, "APPLIED_MEMORY", "{}", false));
        waitFor(ledger.close(false));
        ledger = open(main);
        check(ledger.health().blocked(), "unclean boot persists emergency containment");
        check(waitFor(ledger.inspect(second)).orElseThrow().recoveryRequired(), "unclean applied outcome needs manual review");
        check(!waitFor(ledger.inspect(first)).orElseThrow().recoveryRequired(), "earlier orderly-closed session is retained independently");
        check(waitFor(ledger.recent(null, true, 25)).size() == 1, "unresolved query surfaces affected transaction");
        fails(ledger.prepare(intent(UUID.randomUUID(), player)), "blocked ledger cannot authorise more mutations");
        waitFor(ledger.close(true));
        ledger = open(main);
        check(ledger.health().blocked(), "restart does not clear emergency breaker");
        waitFor(ledger.close(false));

        Path pending = directory.resolve("pending.sqlite");
        ledger = open(pending);
        UUID pendingId = UUID.randomUUID(); waitFor(ledger.prepare(intent(pendingId, player)));
        waitFor(ledger.close(false)); ledger = open(pending);
        check(ledger.health().blocked() && waitFor(ledger.inspect(pendingId)).orElseThrow().state().equals("PREPARED"), "pending intent survives without automatic replay");
        waitFor(ledger.close(false));

        Path cancelled = directory.resolve("cancelled.sqlite");
        ledger = open(cancelled); UUID cancelledId = UUID.randomUUID();
        waitFor(ledger.prepare(intent(cancelledId, player))); waitFor(ledger.outcome(cancelledId, "CANCELLED", "state changed", false));
        waitFor(ledger.close(false)); ledger = open(cancelled);
        check(!ledger.health().blocked(), "proven pre-mutation cancellation needs no economic recovery");
        waitFor(ledger.trip("test emergency")); waitFor(ledger.close(true)); ledger = open(cancelled);
        check(ledger.health().blocked(), "explicit circuit breaker persists without pending transactions");
        waitFor(ledger.close(false));

        Path duplicate = directory.resolve("duplicate.sqlite");
        ledger = open(duplicate); UUID duplicateId = UUID.randomUUID();
        waitFor(ledger.prepare(intent(duplicateId, player)));
        fails(ledger.prepare(intent(duplicateId, player)), "duplicate transaction identity cannot overwrite evidence");
        check(waitFor(ledger.recent(null, false, 25)).size() == 1, "one row per globally unique transaction ID");
        waitFor(ledger.close(false));

        Path writeFailure = directory.resolve("failure.sqlite");
        ledger = open(writeFailure); UUID failureId = UUID.randomUUID();
        waitFor(ledger.prepare(intent(failureId, player)));
        // Emulate an IO failure at the critical outcome boundary without adding a production failure command.
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + writeFailure); var statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER reject_outcome BEFORE UPDATE ON transactions BEGIN SELECT RAISE(FAIL,'injected outcome failure'); END");
        }
        fails(ledger.outcome(failureId, "APPLIED_MEMORY", "{}", false), "outcome write failure is observable");
        check(ledger.health().blocked() && waitFor(ledger.inspect(failureId)).orElseThrow().state().equals("PREPARED"), "failed outcome retains recovery obligation");
        waitFor(ledger.close(false));

        Path exclusive = directory.resolve("exclusive.sqlite");
        ledger = open(exclusive); LocalLedger other = new LocalLedger(exclusive, "staging-test");
        fails(other.open(() -> {}), "another owner cannot open the same ledger");
        waitFor(other.close(false)); waitFor(ledger.close(true));
        other = new LocalLedger(exclusive, "wrong-instance");
        fails(other.open(() -> {}), "instance identity mismatch fails closed");
        waitFor(other.close(false));

        LocalLedger unavailable = new LocalLedger(directory.resolve("unavailable.sqlite"), "staging-test");
        fails(unavailable.open(() -> { throw new IllegalStateException("unverified dependency"); }), "unverified dependency prevents ledger admission");
        check(!unavailable.health().ready() && unavailable.health().blocked(), "startup failure cannot authorise transactions");
        waitFor(unavailable.close(false));
        System.out.println("Native SQLite ledger: " + checks + " checks passed; test data=" + directory);
    }
}
