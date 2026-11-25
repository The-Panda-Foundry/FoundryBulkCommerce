package com.pandafoundry.bulkcommerce.mixin;

import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exact balances are needed because setBalance normalizes the overflow reserve. */
@Mixin(value = BankAccount.class, remap = false)
public interface BankAccountAccessor {
    @Accessor("balance") int foundry$getBalance();
    @Accessor("balance") void foundry$setBalance(int balance);
    @Accessor("additionalBalance") long foundry$getAdditionalBalance();
    @Accessor("additionalBalance") void foundry$setAdditionalBalance(long balance);
}
