package pl.mfelisiak.voucherstore.sales;

public interface Inventory {
    boolean isAvailable(String productId);
}
