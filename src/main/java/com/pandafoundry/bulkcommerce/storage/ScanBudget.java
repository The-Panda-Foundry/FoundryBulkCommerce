package com.pandafoundry.bulkcommerce.storage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

/** Per-request work limits, discarded after planning. */
public final class ScanBudget {
    private final int slotLimit, byteLimit;
    private int slots, bytes;
    public ScanBudget(int slotLimit, int byteLimit) { this.slotLimit = slotLimit; this.byteLimit = byteLimit; }
    public void slot() { if (++slots > slotLimit) throw new IllegalStateException("Slot inspection limit exceeded"); }
    public void nbt(CompoundTag tag) {
        try {
            NbtIo.write(tag, new DataOutputStream(new OutputStream() {
                public void write(int value) throws IOException { add(1); }
                public void write(byte[] buffer, int offset, int length) throws IOException { add(length); }
                private void add(int count) throws IOException {
                    if (count > byteLimit - bytes) throw new IOException("NBT snapshot limit exceeded");
                    bytes += count;
                }
            }));
        } catch (IOException limit) { throw new IllegalStateException("NBT snapshot limit exceeded", limit); }
    }
}
