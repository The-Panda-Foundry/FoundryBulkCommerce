package com.pandafoundry.bulkcommerce.transaction;

import java.util.List;

public final class TransactionTest {
    private static int checks;
    private static void check(boolean value, String name) {
        checks++;
        if (!value) throw new AssertionError(name);
    }
    private static final class Edit implements Transaction.Edit {
        int value = 100;
        final int take;
        boolean failApply, partial, failRestore, silent;
        Edit(int take) { this.take = take; }
        public boolean isBefore() { return value == 100; }
        public boolean isAfter() { return value == 100-take; }
        public void apply() {
            if (silent) return;
            value -= partial ? 1 : take;
            if (failApply) throw new IllegalStateException("injected removal failure");
        }
        public void restore() { if (failRestore) throw new IllegalStateException("injected restore failure"); value = 100; }
        public String describe() { return "fake source"; }
    }
    private static final class Wallet implements Transaction.Wallet {
        int balance = 500;
        boolean failCredit, creditThenThrow, silent, failRestore;
        Runnable onCredit = () -> {}, onRestore = () -> {};
        public boolean isBefore() { return balance == 500; }
        public void credit(int amount) {
            onCredit.run();
            if (failCredit) throw new IllegalStateException("injected payment failure");
            if (!silent) balance += amount;
            if (creditThenThrow) throw new IllegalStateException("injected failure after payment");
        }
        public boolean isCredited(int amount) { return balance == 500+amount; }
        public void restore() { onRestore.run(); if (failRestore) throw new IllegalStateException("injected compensation failure"); balance = 500; }
    }
    public static void main(String[] args) {
        check(Transaction.quantity(4500, 2304, 5000) == 2304, "complete lots");
        check(Transaction.quantity(Long.MAX_VALUE, 64, 1048576) == 1048576, "large availability bounded without overflow");
        check(Transaction.quantity(63, 64, 2304) == 0, "incomplete lot untouched");
        check(Transaction.quantity(5000, 2304, 2303) == 0, "limit smaller than lot");
        check(Transaction.quantity(5000, 2304, 4608) == 4608, "two lots");
        for (int lot = 1; lot <= 2304; lot++) {
            int quantity = Transaction.quantity(100000000000L, lot, 1048576);
            check(quantity <= 1048576 && quantity % lot == 0, "bounded lot property");
        }
        Edit a = new Edit(50), b = new Edit(25); Wallet w = new Wallet();
        final Edit first = a, second = b;
        w.onCredit = () -> check(first.isAfter() && second.isAfter(), "payment occurs only after all removals");
        var result = Transaction.commit(List.of(a,b), w, 30);
        check(result.successful() && a.value == 50 && b.value == 75 && w.balance == 530, "verified multi-source sale");
        for (int failure = 0; failure < 6; failure++) {
            a = new Edit(50); b = new Edit(25); w = new Wallet();
            switch (failure) {
                case 0 -> { b.failApply = true; b.partial = true; }
                case 1 -> b.silent = true;
                case 2 -> w.failCredit = true;
                case 3 -> w.creditThenThrow = true;
                case 4 -> w.silent = true;
                case 5 -> b.value = 99; // A stale plan must perform no writes.
            }
            result = Transaction.commit(List.of(a,b), w, 30);
            check(!result.successful() && !result.recoveryRequired(), "recoverable failure "+failure);
            check(a.value == 100 && b.value == (failure == 5 ? 99 : 100) && w.balance == 500, "exact rollback "+failure);
        }
        a = new Edit(50); w = new Wallet(); w.creditThenThrow = true;
        final Edit removal = a;
        w.onRestore = () -> check(removal.value == 50, "payment compensation precedes item restoration");
        result = Transaction.commit(List.of(a), w, 30);
        check(!result.successful() && !result.recoveryRequired() && a.value == 100 && w.balance == 500, "ordered compensation finishes exactly");
        a = new Edit(50); w = new Wallet(); w.creditThenThrow = true; w.failRestore = true;
        result = Transaction.commit(List.of(a), w, 30);
        check(result.recoveryRequired() && a.value == 50 && w.balance == 530, "uncertain payment must not refund items");
        a = new Edit(50); a.failApply = true; a.failRestore = true; w = new Wallet();
        result = Transaction.commit(List.of(a), w, 30);
        check(result.recoveryRequired() && w.balance == 500, "rollback failure surfaced; never paid");
        System.out.println("Transaction integrity: "+checks+" checks passed");
    }
}
