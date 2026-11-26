package com.pandafoundry.bulkcommerce.storage;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;

/** Exact reviewed classes in the fingerprinted dependency cohorts. No wrapper construction or upgrade hooks. */
public final class UpgradePolicy {
    private UpgradePolicy() {}
    private static final Set<String> ORDINARY=Set.of(
        "net.p3pp3rf1y.sophisticatedcore.upgrades.pickup.PickupUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.filter.FilterUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.compacting.CompactingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedstorage.init.ModItems$StorageCompactingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.SmeltingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoSmeltingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.SmokingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoSmokingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.BlastingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.cooking.AutoBlastingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.stonecutter.StonecutterUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.jukebox.JukeboxUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.alchemy.AlchemyUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.restock.RestockUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.deposit.DepositUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.refill.RefillUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.everlasting.EverlastingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.anvil.AnvilUpgradeItem",
        "net.p3pp3rf1y.sophisticatedbackpacks.upgrades.smithing.SmithingUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.battery.BatteryUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeItem",
        "net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeItem",
        "net.p3pp3rf1y.sophisticatedstorage.upgrades.hopper.HopperUpgradeItem"
    );
    public static int columns(ItemStack upgrade,String namespace) {
        if(upgrade.getCount()!=1||!BuiltInRegistries.ITEM.getKey(upgrade.getItem()).getNamespace().equals(namespace)
            ||!(upgrade.getItem() instanceof IUpgradeItem<?> item))throw unsupported();
        if(upgrade.getItem().getClass()==StackUpgradeItem.class){
            double multiplier=((StackUpgradeItem)upgrade.getItem()).getStackSizeMultiplier();
            if(!Double.isFinite(multiplier)||multiplier<=0)throw unsupported();
        }else if(!ORDINARY.contains(upgrade.getItem().getClass().getName()))throw unsupported();
        int columns=item.getInventoryColumnsTaken();
        if(columns<0||columns>2)throw unsupported();
        return columns;
    }
    private static SaleRejection unsupported(){return new SaleRejection(SaleRejection.Reason.UNSUPPORTED_UPGRADE,"Unreviewed, nested, virtual, infinite or identity-changing upgrade");}
}
