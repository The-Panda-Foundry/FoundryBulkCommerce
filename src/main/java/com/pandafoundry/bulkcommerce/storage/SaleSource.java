package com.pandafoundry.bulkcommerce.storage;

import com.pandafoundry.bulkcommerce.transaction.Transaction;

public interface SaleSource {
    long available();
    /** Prepare an edit on copies; do not mutate live inventory. */
    Transaction.Edit remove(int amount);
}
