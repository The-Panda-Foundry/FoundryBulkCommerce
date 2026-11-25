package com.pandafoundry.bulkcommerce;

import com.mojang.authlib.GameProfile;
import com.pandafoundry.bulkcommerce.ledger.LedgerRuntime;
import com.pandafoundry.bulkcommerce.ledger.LocalLedger;
import com.pandafoundry.bulkcommerce.mixin.BankAccountAccessor;
import dev.yassou.numismaticstreasury.block.entity.ServerShopBlockEntity;
import dev.yassou.numismaticstreasury.block.entity.ShopMode;
import dev.yassou.numismaticstreasury.server.BankService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;

/** Captures actual server chat while the real Mixin, asynchronous ledger and settlement run. */
@GameTestHolder("foundrybulkcommerce") @PrefixGameTestTemplate(false)
public final class TransactionMessagesGameTest {
    private static final class RecordingPlayer extends FakePlayer {
        final List<String> messages=new ArrayList<>();
        RecordingPlayer(ServerLevel level){super(level,new GameProfile(UUID.randomUUID(),"TransactionChat"));}
        @Override public void sendSystemMessage(Component message){messages.add(message.getString());}
    }
    private static final class State {
        RecordingPlayer player;Map<UUID,ServerPlayer> players;BankAccountAccessor bank;BlockPos pos;
        boolean show,debug,enabled;int limit;CompletableFuture<List<LocalLedger.Row>> rows;
    }
    private static ItemStack box(){var box=new ItemStack(Items.SHULKER_BOX);box.set(DataComponents.CONTAINER,
        ItemContainerContents.fromItems(List.of(new ItemStack(Items.STONE,64),new ItemStack(Items.DIAMOND,7))));return box;}
    @SuppressWarnings("unchecked") private static void setup(GameTestHelper h,State state){
        h.assertTrue(!BulkConfig.SHOW_TRANSACTION_MESSAGES.getDefault(),"Routine chat defaults false");
        state.show=BulkConfig.SHOW_TRANSACTION_MESSAGES.get();state.debug=BulkConfig.DEBUG_TRANSACTIONS.get();state.enabled=BulkConfig.ENABLED.get();state.limit=BulkConfig.LIMIT.get();
        BulkConfig.LIMIT.set(2304);BulkConfig.ENABLED.set(true);BulkConfig.SHOW_TRANSACTION_MESSAGES.set(false);BulkConfig.DEBUG_TRANSACTIONS.set(false);
        state.player=new RecordingPlayer(h.getLevel());state.player.getInventory().clearContent();state.bank=(BankAccountAccessor)(Object)BankService.account(state.player);
        state.bank.foundry$setBalance(0);state.bank.foundry$setAdditionalBalance(0);
        try{var field=PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);state.players=(Map<UUID,ServerPlayer>)field.get(h.getLevel().getServer().getPlayerList());}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        state.players.put(state.player.getUUID(),state.player);state.pos=h.absolutePos(new BlockPos(1,1,1));
        h.getLevel().setBlockAndUpdate(state.pos,dev.yassou.numismaticstreasury.registry.TreasuryContent.NUMIS_SHOP.get().defaultBlockState());
        ((ServerShopBlockEntity)h.getLevel().getBlockEntity(state.pos)).configure(ShopMode.BUY_FROM_PLAYER,new ItemStack(Items.STONE),3,64);
        state.player.setPos(state.pos.getX()+0.5,state.pos.getY(),state.pos.getZ()+0.5);
    }
    private static void request(State state){BulkSales.reset();state.player.messages.clear();state.player.getInventory().setItem(0,box());NetheriteTransactionsGameTest.invoke(state.player,state.pos);}
    private static void settled(GameTestHelper h,State state,int balance){
        h.assertTrue(state.bank.foundry$getBalance()==balance&&state.bank.foundry$getAdditionalBalance()==0,"Exact payment independent of notification option");
        var contents=state.player.getInventory().getItem(0).get(DataComponents.CONTAINER);
        h.assertTrue(contents.getStackInSlot(0).isEmpty()&&contents.getStackInSlot(1).getCount()==7,"Verified extraction preserves unrelated contents");
    }
    @GameTest(template="empty",batch="transaction-messages",timeoutTicks=100000)
    public static void actualChatAndAsyncLedger(GameTestHelper h){
        State state=new State();h.startSequence()
            .thenWaitUntil(()->h.assertTrue(LedgerRuntime.admissionOpen(),"Ledger ready"))
            .thenExecute(()->{setup(h,state);request(state);h.assertTrue(BulkSales.hasActive()&&state.player.messages.isEmpty()&&state.bank.foundry$getBalance()==0,"Quiet preparation still awaits asynchronous durable intent");})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Quiet outcome durable"))
            .thenExecute(()->{settled(h,state,3);h.assertTrue(state.player.messages.isEmpty(),"No preparation, successful confirmation or reference in quiet chat");BulkConfig.SHOW_TRANSACTION_MESSAGES.set(true);request(state);h.assertTrue(state.player.messages.size()==1&&state.player.messages.getFirst().startsWith("Preparing bulk sale. Reference: "),"Explicit opt-in restores preparation reference");})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Visible outcome durable"))
            .thenExecute(()->{settled(h,state,6);h.assertTrue(state.player.messages.size()==2&&state.player.messages.getLast().startsWith("Sold 64 items for 3 Spurs. Reference: "),"Opt-in restores successful confirmation");String prepared=state.player.messages.getFirst().split("Reference: ")[1];h.assertTrue(state.player.messages.getLast().endsWith(prepared),"One unique reference across preparation and successful outcome");BulkConfig.SHOW_TRANSACTION_MESSAGES.set(false);BulkConfig.DEBUG_TRANSACTIONS.set(true);request(state);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Debug-on quiet transaction durable"))
            .thenExecute(()->{settled(h,state,9);h.assertTrue(state.player.messages.isEmpty(),"Diagnostic logging does not enable player chat");BulkConfig.DEBUG_TRANSACTIONS.set(false);BulkConfig.SHOW_TRANSACTION_MESSAGES.set(true);request(state);BulkConfig.SHOW_TRANSACTION_MESSAGES.set(false);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Message option changed during intent"))
            .thenExecute(()->{settled(h,state,12);h.assertTrue(state.player.messages.size()==1&&state.player.messages.getFirst().startsWith("Preparing"),"Disabling presentation during intent neither cancels sale nor reports success");request(state);BulkConfig.SHOW_TRANSACTION_MESSAGES.set(true);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Presentation enabled during intent"))
            .thenExecute(()->{settled(h,state,15);h.assertTrue(state.player.messages.size()==1&&state.player.messages.getFirst().startsWith("Sold "),"Enabling presentation during intent only changes final chat");BulkConfig.SHOW_TRANSACTION_MESSAGES.set(false);request(state);state.player.getInventory().setItem(18,state.player.getInventory().getItem(0));state.player.getInventory().setItem(0,ItemStack.EMPTY);})
            .thenWaitUntil(()->h.assertTrue(!BulkSales.hasActive(),"Stale movement cancelled"))
            .thenExecute(()->{
                h.assertTrue(state.bank.foundry$getBalance()==15&&state.player.messages.size()==1&&state.player.messages.getFirst().startsWith("Sale CANCELLED. Reference: "),"Cancellation remains visible with reference while quiet");
                h.assertTrue(state.player.getInventory().getItem(18).get(DataComponents.CONTAINER).getStackInSlot(0).getCount()==64,"Cancelled protected container untouched");
                BulkSales.reset();state.player.messages.clear();NetheriteTransactionsGameTest.invoke(state.player,state.pos);
                h.assertTrue(!BulkSales.hasActive()&&state.player.messages.size()==1&&state.player.messages.getFirst().contains("INSUFFICIENT_STOCK")&&state.bank.foundry$getBalance()==15,"Preparation rejection remains visible without mutation");
                BulkSales.reset();state.player.messages.clear();BulkConfig.ENABLED.set(false);NetheriteTransactionsGameTest.invoke(state.player,state.pos);
                h.assertTrue(state.player.messages.size()==1&&state.player.messages.getFirst().contains("disabled, busy or awaiting ledger recovery"),"Admission warning is never suppressed");BulkConfig.ENABLED.set(true);
                state.rows=LedgerRuntime.ledger().recent(state.player.getUUID(),false,25);
            }).thenWaitUntil(()->h.assertTrue(state.rows.isDone(),"Read-only ledger evidence ready"))
            .thenExecute(()->{
                var rows=state.rows.getNow(List.of());h.assertTrue(rows.size()==6&&rows.stream().filter(r->r.state().equals("APPLIED_MEMORY")).count()==5&&rows.stream().filter(r->r.state().equals("CANCELLED")).count()==1,"All five sales and stale cancellation recorded independently of chat");
                h.assertTrue(rows.stream().allMatch(r->r.quantity()==64&&r.payment()==3)&&rows.stream().map(LocalLedger.Row::id).distinct().count()==6,"Ledger quantities/payments and unique references preserved");
                var command=h.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("bulkcommerce");h.assertTrue(command!=null&&command.getChildren().size()==5,"All read-only administrator commands preserved");
                state.players.remove(state.player.getUUID());state.player.getInventory().clearContent();state.bank.foundry$setBalance(0);state.bank.foundry$setAdditionalBalance(0);BankService.existingAccount(state.player.getUUID()).markDirty();
                BulkConfig.SHOW_TRANSACTION_MESSAGES.set(state.show);BulkConfig.DEBUG_TRANSACTIONS.set(state.debug);BulkConfig.ENABLED.set(state.enabled);BulkConfig.LIMIT.set(state.limit);
                FoundryBulkCommerce.LOGGER.info("[BC021 notifications] quiet/visible/debug-independent/toggled-intent success, visible rejections, 6 unique ledger outcomes passed");
            }).thenSucceed();
    }
}
