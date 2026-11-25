package com.pandafoundry.bulkcommerce;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.fml.ModList;

/** Capture mod paths on the server thread; perform all fingerprint IO on the ledger worker. */
public final class RuntimeCompatibility {
    private RuntimeCompatibility() {}
    public static Runnable verifier() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("numismatics", "d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767");
        expected.put("numismatics_treasury", "fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2");
        // Config can be reloaded: disabling the adapter must not bypass verification before a later enable.
        if (ModList.get().isLoaded("sophisticatedstorage")) {
            expected.put("sophisticatedstorage", "bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69");
            expected.put("sophisticatedcore", "33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39");
        }
        Map<Path, String> paths = new LinkedHashMap<>();
        expected.forEach((id, hash) -> {
            var file = ModList.get().getModFileById(id);
            if (file == null) throw new IllegalStateException("Missing compatibility reference: " + id);
            paths.put(file.getFile().getFilePath(), hash);
        });
        return () -> paths.forEach((path, expectedHash) -> {
            try (InputStream stream = Files.newInputStream(path)) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] buffer = new byte[65536];
                for (int count; (count = stream.read(buffer)) != -1;) digest.update(buffer, 0, count);
                if (!HexFormat.of().formatHex(digest.digest()).equals(expectedHash))
                    throw new IllegalStateException("Unverified compatibility reference: " + path.getFileName());
            } catch (Exception error) { throw new IllegalStateException("Compatibility fingerprint failed: " + path.getFileName(), error); }
        });
    }
}
