package com.pandafoundry.bulkcommerce.transaction;

import java.util.ArrayList;
import java.util.List;

/** Synchronous, bounded transaction. Adapters must snapshot before construction and never yield. */
public final class Transaction {
    private Transaction() {}

    public interface Edit {
        boolean isBefore();
        void apply();
        boolean isAfter();
        void restore();
        String describe();
        default String recoveryDetails() { return describe(); }
    }
    public interface Wallet {
        boolean isBefore();
        void credit(int amount);
        boolean isCredited(int amount);
        void restore();
    }
    public record Result(boolean successful, boolean recoveryRequired, RuntimeException failure) {}

    public static int quantity(long available, int lotSize, int limit) {
        if (available < 0 || lotSize <= 0 || limit <= 0) throw new IllegalArgumentException("Invalid sale bounds");
        return (int)(Math.min(available, (long)limit) / lotSize * lotSize);
    }

    public static Result commit(List<? extends Edit> edits, Wallet wallet, int payment) {
        if (edits.isEmpty() || payment <= 0) throw new IllegalArgumentException("Empty transaction");
        List<Edit> attempted = new ArrayList<>();
        boolean creditAttempted = false;
        try {
            if (!wallet.isBefore()) throw new IllegalStateException("Balance changed before sale");
            for (Edit edit : edits) if (!edit.isBefore()) throw new IllegalStateException("Stale source: " + edit.describe());
            for (Edit edit : edits) {
                attempted.add(edit); // Includes an adapter that throws after a partial write.
                edit.apply();
                if (!edit.isAfter()) throw new IllegalStateException("Removal not verified: " + edit.describe());
            }
            for (Edit edit : edits) if (!edit.isAfter()) throw new IllegalStateException("Source changed during removal");
            if (!wallet.isBefore()) throw new IllegalStateException("Balance changed during removal");
            creditAttempted = true;
            wallet.credit(payment);
            if (!wallet.isCredited(payment)) throw new IllegalStateException("Payment not verified");
            return new Result(true, false, null);
        } catch (RuntimeException failure) {
            // Never return items while a payment could still exist.
            boolean walletRestored;
            try {
                if (creditAttempted) wallet.restore();
                walletRestored = !creditAttempted || wallet.isBefore();
            } catch (RuntimeException recovery) {
                failure.addSuppressed(recovery);
                try { walletRestored = wallet.isBefore(); }
                catch (RuntimeException unknown) { failure.addSuppressed(unknown); walletRestored = false; }
            }
            if (!walletRestored) return new Result(false, true, failure);
            boolean recovered = true;
            for (int i = attempted.size() - 1; i >= 0; i--) {
                Edit edit = attempted.get(i);
                try { edit.restore(); }
                catch (RuntimeException recovery) { failure.addSuppressed(recovery); }
                try { if (!edit.isBefore()) recovered = false; }
                catch (RuntimeException unknown) { failure.addSuppressed(unknown); recovered = false; }
            }
            return new Result(false, !recovered, failure);
        }
    }
}
