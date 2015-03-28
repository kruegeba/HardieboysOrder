package com.hardieboysorder.model;

public class InvoiceItem {

    private int invoiceItemID;
    private int invoiceID;
    private int itemID;
    private int quantity;
    private int discount;
    private double total;

    public InvoiceItem(){}

    public InvoiceItem(int invoiceID, int itemID, int quantity, int discount, double total){
        this.invoiceID = invoiceID;
        this.itemID = itemID;
        this.quantity = quantity;
        this.discount = discount;
        this.total = total;
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
}
