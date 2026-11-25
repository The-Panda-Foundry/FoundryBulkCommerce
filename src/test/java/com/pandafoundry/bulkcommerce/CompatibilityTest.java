package com.pandafoundry.bulkcommerce;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Structural compatibility check against the original bytes; this does not claim a live Mixin launch. */
public final class CompatibilityTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static ClassNode read(Path jar, String name) throws Exception {
        try (JarFile file = new JarFile(jar.toFile())) {
            ClassNode node = new ClassNode();
            new ClassReader(file.getInputStream(file.getJarEntry(name+".class"))).accept(node, 0);
            return node;
        }
    }
    public static void main(String[] args) throws Exception {
        ClassNode entry = new ClassNode();
        new ClassReader("com.pandafoundry.bulkcommerce.FoundryBulkCommerce").accept(entry, 0);
        var mod = entry.visibleAnnotations.stream().filter(a -> a.desc.equals("Lnet/neoforged/fml/common/Mod;")).findFirst().orElseThrow();
        check(!mod.values.contains("dist"), "entry point must load on both physical sides for integrated servers");
        try (var stream = CompatibilityTest.class.getResourceAsStream("/foundrybulkcommerce.mixins.json")) {
            var config = com.google.gson.JsonParser.parseString(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            check(!config.has("server") && !config.has("client"), "common server-class hooks must not be physically side-restricted");
            var names = config.getAsJsonArray("mixins").asList().stream().map(com.google.gson.JsonElement::getAsString).toList();
            check(names.containsAll(List.of("TreasurySellAllMixin", "BankAccountAccessor", "PlayerAdmissionMixin", "ServerShutdownMixin")) && names.size() == 4, "all four integrity hooks load in both distributions");
        }
        Path deps = Path.of(args[0]);
        var screen = read(deps.resolve("numismatics_treasury-1.0.2.jar"), "dev/yassou/numismaticstreasury/client/screen/ServerShopScreen");
        var sellAll = screen.methods.stream().filter(m -> m.name.equals("sellAll")).findFirst().orElseThrow();
        boolean sendsAction = false, checksLooseStock = false;
        for (var instruction : sellAll.instructions) if (instruction instanceof MethodInsnNode call) {
            sendsAction |= call.owner.equals("dev/yassou/numismaticstreasury/network/TreasuryNetwork") && call.name.equals("sendAction");
            checksLooseStock |= call.name.equals("countMatching");
        }
        check(sendsAction && !checksLooseStock, "Treasury Sell All sends the existing action without a loose-stock guard");
        var treasury = read(deps.resolve("numismatics_treasury-1.0.2.jar"), "dev/yassou/numismaticstreasury/network/ServerActionHandler");
        MethodNode trade = treasury.methods.stream().filter(m -> m.name.equals("tradeShop") && m.desc.equals("(Lnet/minecraft/server/level/ServerPlayer;Lcom/google/gson/JsonObject;Z)V")).findFirst().orElseThrow();
        check((trade.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE), "private static injection target");
        List<String> calls = new ArrayList<>();
        int counts = 0;
        for (var instruction : trade.instructions) if (instruction instanceof MethodInsnNode call) {
            if (call.owner.equals("dev/yassou/numismaticstreasury/server/InventoryUtil") && call.name.equals("countMatching")) {
                if (counts++ == 0) {
                    check(call.desc.equals("(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)I"), "exact count descriptor");
                    for (String guard : List.of("get", "near", "getBlockState", "getBlockEntity", "configured", "mode"))
                        check(calls.contains(guard), "guard before injection: "+guard);
                    check(!calls.contains("credit") && !calls.contains("removeMatching"), "injection precedes mutation/payment");
                }
            }
            calls.add(call.name);
        }
        check(counts == 2, "count invocation ordinal layout");
        var bank = read(deps.resolve("CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar"), "dev/ithundxr/createnumismatics/content/backend/BankAccount");
        check(bank.fields.stream().anyMatch(f -> f.name.equals("balance") && f.desc.equals("I") && (f.access & Opcodes.ACC_PRIVATE) != 0), "standard balance accessor");
        check(bank.fields.stream().anyMatch(f -> f.name.equals("additionalBalance") && f.desc.equals("J") && (f.access & Opcodes.ACC_PRIVATE) != 0), "reserve accessor");
        check(bank.methods.stream().anyMatch(m -> m.name.equals("markDirty") && m.desc.equals("()V")), "account dirty method");
        var storage = read(deps.resolve("sophisticatedstorage-1.21.1-1.6.2.2159.jar"), "net/p3pp3rf1y/sophisticatedstorage/block/ItemContentsStorage");
        check(storage.methods.stream().anyMatch(m -> m.name.equals("setStorageContents") && m.desc.equals("(Ljava/util/UUID;Lnet/minecraft/nbt/CompoundTag;)V")), "canonical storage write API");
        check(storage.methods.stream().anyMatch(m -> m.name.equals("getStorageContents") && m.desc.equals("(Ljava/util/UUID;)Ljava/util/Optional;")), "canonical storage read API");
        var core = read(deps.resolve("sophisticatedcore-1.21.1-1.5.7.2381.jar"), "net/p3pp3rf1y/sophisticatedcore/inventory/StorageWrapperRepository");
        check(core.methods.stream().anyMatch(m -> m.name.equals("getExistingStorageWrapper")), "cache lookup without allocation");
        System.out.println("Original JAR compatibility: "+checks+" structural checks passed (no runtime launch)");
    }
}
