package com.hardieboysorder.model;

public class InvoiceItem {

    private int invoiceItemID;
    private int invoiceID;
    private int itemID;
    private int quantity;
    private int discount;
    private double total;
    private Item item;

    public InvoiceItem(){}

    public InvoiceItem(int invoiceID, int itemID, int quantity, int discount, double total){
        this.invoiceID = invoiceID;
        this.itemID = itemID;
        this.quantity = quantity;
        this.discount = discount;
        this.total = total;
    }

    public InvoiceItem(int invoiceItemID, int invoiceID, int itemID, int quantity, int discount, double total){
        this.invoiceItemID = invoiceItemID;
        this.invoiceID = invoiceID;
        this.itemID = itemID;
        this.quantity = quantity;
        this.discount = discount;
        this.total = total;
    }

    @Override
    public String toString(){
        return quantity + item.getDescription() + "$" + String.format("%.2f", total);
    }

    public int getInvoiceItemID() {
        return invoiceItemID;
    }

    public void setInvoiceItemID(int invoiceItemID) {
        this.invoiceItemID = invoiceItemID;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setItem(Item item){this.item = item;}

    public Item getItem(){return this.item;}

    public double getDiscountAmount(){
        return total * (double)discount / 100;
    }
}
