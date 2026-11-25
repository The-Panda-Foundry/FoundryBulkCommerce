package com.pandafoundry.bulkcommerce.storage;
import java.util.List;
public record StorageScan(List<SaleSource> sources,List<Skipped> rejections) {
    public record Skipped(int slot,String item,SaleRejection.Reason reason) {}
    public int skipped(){return rejections.size();}
}
