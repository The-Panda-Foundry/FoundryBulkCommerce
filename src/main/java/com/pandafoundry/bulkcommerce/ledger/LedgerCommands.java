package com.pandafoundry.bulkcommerce.ledger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pandafoundry.bulkcommerce.FoundryBulkCommerce;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Read-only, permission-level-two audit commands. Every database query runs on the ledger writer. */
public final class LedgerCommands {
    private LedgerCommands() {}
    public static void register(RegisterCommandsEvent event) {
        FoundryBulkCommerce.LOGGER.info("[BulkCommerce] Registered /bulkcommerce status, recent, player, inspect and unresolved (permission level 2; read-only)");
        event.getDispatcher().register(Commands.literal("bulkcommerce").requires(source -> source.hasPermission(2))
            .then(Commands.literal("status").executes(context -> {
                LocalLedger local = LedgerRuntime.ledger();
                say(context.getSource(), local == null ? "Ledger not started" : local.health().toString()); return 1;
            }))
            .then(Commands.literal("recent").executes(context -> list(context.getSource(), null, false, 10))
                .then(Commands.argument("limit", IntegerArgumentType.integer(1, 25)).executes(context -> list(context.getSource(), null, false, IntegerArgumentType.getInteger(context, "limit")))))
            .then(Commands.literal("player").then(Commands.argument("player", UuidArgument.uuid())
                .executes(context -> list(context.getSource(), UuidArgument.getUuid(context, "player"), false, 10))
                .then(Commands.argument("limit", IntegerArgumentType.integer(1, 25)).executes(context -> list(context.getSource(), UuidArgument.getUuid(context, "player"), false, IntegerArgumentType.getInteger(context, "limit"))))))
            .then(Commands.literal("unresolved").executes(context -> list(context.getSource(), null, true, 25)))
            .then(Commands.literal("inspect").then(Commands.argument("transaction", UuidArgument.uuid())
                .executes(context -> inspect(context.getSource(), UuidArgument.getUuid(context, "transaction"), 0))
                .then(Commands.argument("page", IntegerArgumentType.integer(1, 10000)).executes(context -> inspect(context.getSource(), UuidArgument.getUuid(context, "transaction"), IntegerArgumentType.getInteger(context, "page")))))));
    }
    private static int list(CommandSourceStack source, UUID player, boolean unresolved, int limit) {
        LocalLedger local = LedgerRuntime.ledger();
        if (local == null || !local.health().ready()) { say(source, "Ledger is unavailable; inspect the server log."); return 0; }
        local.recent(player, unresolved, limit).whenComplete((rows, error) -> reply(source, () -> {
            if (error != null) { failed(source, error); return; }
            if (rows.isEmpty()) say(source, "No matching transactions.");
            else rows.forEach(row -> say(source, summary(row)));
        }));
        return 1;
    }
    private static int inspect(CommandSourceStack source, UUID id, int page) {
        LocalLedger local = LedgerRuntime.ledger();
        if (local == null || !local.health().ready()) { say(source, "Ledger is unavailable; inspect the server log."); return 0; }
        local.inspect(id).whenComplete((record, error) -> reply(source, () -> {
            if (error != null) { failed(source, error); return; }
            if (record.isEmpty()) { say(source, "Transaction not found."); return; }
            var row = record.get();
            if (page == 0) {
                say(source, summary(row)); say(source, "instance=" + row.instance() + " boot=" + row.boot());
                say(source, "intent SHA-256=" + row.intentHash()); say(source, "outcome=" + row.outcome());
                say(source, "Use /bulkcommerce inspect " + id + " 1 for paginated full intent evidence.");
            } else {
                int start = (page - 1) * 2000;
                int pages = Math.max(1, (row.intent().length() + 1999) / 2000);
                if (start >= row.intent().length()) { say(source, "No such page; total pages=" + pages); return; }
                say(source, "Intent evidence " + id + " page " + page + "/" + pages);
                for (int offset = start; offset < Math.min(start + 2000, row.intent().length()); offset += 500)
                    say(source, row.intent().substring(offset, Math.min(Math.min(offset + 500, start + 2000), row.intent().length())));
            }
        }));
        return 1;
    }
    private static String summary(LocalLedger.Row row) {
        return row.id() + " " + Instant.ofEpochMilli(row.createdAt()) + " player=" + row.playerName() + " (" + row.player()
            + ") items=" + row.quantity() + " spurs=" + row.payment() + " state=" + row.state() + " recovery=" + row.recoveryRequired();
    }
    private static void failed(CommandSourceStack source, Throwable error) {
        FoundryBulkCommerce.LOGGER.error("Read-only ledger query failed", error);
        say(source, "Ledger query failed; check the server log. No economic action was performed.");
        LedgerRuntime.quarantine(source.getServer(), "Ledger query failure");
    }
    private static void reply(CommandSourceStack source, Runnable response) {
        if (!source.getServer().isStopped()) source.getServer().execute(response);
    }
    private static void say(CommandSourceStack source, String message) { source.sendSuccess(() -> Component.literal(message), false); }
}
