package com.pandafoundry.bulkcommerce.storage;

/** Bounded, non-sensitive diagnosis for a rejected request or skipped source. */
public final class SaleRejection extends IllegalStateException {
    public enum Reason {
        UNSUPPORTED_STORAGE, DUPLICATE_UUID, UNSUPPORTED_UPGRADE, INVALID_DATA,
        SCAN_LIMIT, SNAPSHOT_LIMIT, INSUFFICIENT_STOCK, VALIDATION_FAILURE
    }
    private final Reason reason;
    public SaleRejection(Reason reason, String explanation) { super(explanation); this.reason = reason; }
    public Reason reason() { return reason; }
}
