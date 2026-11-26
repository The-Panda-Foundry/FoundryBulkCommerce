package com.pandafoundry.bulkcommerce;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.ModList;

/** Capture mod paths on the server thread; verify exact files/cohort on the ledger worker. */
public final class RuntimeCompatibility {
    private RuntimeCompatibility() {}
    private static final List<Map<String,String>> SOPHISTICATED_COHORTS=List.of(
        Map.of("sophisticatedcore","33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39",
            "sophisticatedstorage","bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69",
            "sophisticatedbackpacks","de4a51eb3757ee273b56a41620d6598cd3af8578a5a02c4b5a784e42ab4bee2b"),
        Map.of("sophisticatedcore","6273cfa446b338f165a9c799ddd78fcb320fad0b519a70d969165b7518d3bac8",
            "sophisticatedstorage","e11e7bbb8c4e4acf8b34bb0133c82d95c4e9c5dd843c46c21c8a482d9e40d80f",
            "sophisticatedbackpacks","8df6a8ebad2aebc71bc54e51eddd47fe931ed8bc338f7d1924e272ac72e38092")
    );
    static void verifySophisticatedCohort(Map<String,String> observed) {
        if(SOPHISTICATED_COHORTS.stream().noneMatch(cohort->observed.entrySet().stream().allMatch(entry->entry.getValue().equals(cohort.get(entry.getKey())))))
            throw new IllegalStateException("Unverified or mixed Sophisticated compatibility cohort");
    }
    public static Runnable verifier() {
        Map<String,String> fixed=new LinkedHashMap<>();
        fixed.put("numismatics","d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767");
        fixed.put("numismatics_treasury","fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2");
        if(ModList.get().isLoaded("curios"))fixed.put("curios","a45df2125c26219974aba7507ffc9afe7b83acc941a386af3faacb1cc0056fde");
        if(ModList.get().isLoaded("accessories"))fixed.put("accessories","10017a3da78ea63e9ece27a1ca32f8cf490362f348778cf8cb759e7282f3beb0");
        Map<String,Path> paths=new LinkedHashMap<>();
        fixed.keySet().forEach(id->paths.put(id,modPath(id)));
        // Config reloads must not allow an initially disabled integration to bypass exact verification.
        boolean storage=ModList.get().isLoaded("sophisticatedstorage"),backpacks=ModList.get().isLoaded("sophisticatedbackpacks");
        if(storage||backpacks)paths.put("sophisticatedcore",modPath("sophisticatedcore"));
        if(storage)paths.put("sophisticatedstorage",modPath("sophisticatedstorage"));
        if(backpacks)paths.put("sophisticatedbackpacks",modPath("sophisticatedbackpacks"));
        return ()->{
            Map<String,String> sophisticated=new LinkedHashMap<>();
            paths.forEach((id,path)->{
                String actual=hash(path);
                if(fixed.containsKey(id)){
                    if(!actual.equals(fixed.get(id)))throw new IllegalStateException("Unverified compatibility reference: "+path.getFileName());
                }else sophisticated.put(id,actual);
            });
            verifySophisticatedCohort(sophisticated);
            paths.values().forEach(path->FoundryBulkCommerce.LOGGER.info("[BulkCommerce] Compatibility SHA-256 verified: {}",path.getFileName()));
        };
    }
    private static Path modPath(String id){
        var file=ModList.get().getModFileById(id);
        if(file==null)throw new IllegalStateException("Missing compatibility reference: "+id);
        return file.getFile().getFilePath();
    }
    private static String hash(Path path){
        try(InputStream stream=Files.newInputStream(path)){
            MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[65536];
            for(int count;(count=stream.read(buffer))!=-1;)digest.update(buffer,0,count);
            return HexFormat.of().formatHex(digest.digest());
        }catch(Exception error){throw new IllegalStateException("Compatibility fingerprint failed: "+path.getFileName(),error);}
    }
}
