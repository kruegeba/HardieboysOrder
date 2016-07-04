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

    public String getPrintOutLine(){
        String quantityString = String.valueOf(quantity);
        String descriptionString = item.getDescription();
        String itemPriceString, totalPriceString;

        if(discount > 0){
            double discountedTotal =  total - (total * ((double)discount/100));
            totalPriceString = "$" + String.format("%.2f", discountedTotal);

            itemPriceString = "$" + String.format("%.2f", discountedTotal/(double)quantity);
        }else{
            itemPriceString = "$" + String.format("%.2f", item.getPrice());
            totalPriceString = "$" + String.format("%.2f", total);
        }

        //Will always be max length of 3 with 3 trailing space
        if(quantityString.length() == 1){
            quantityString = "  " + quantityString;
        }else if(quantityString.length() == 2){
            quantityString = " " + quantityString;
        }
        quantityString = quantityString + "   ";

        //Will always be max length of 17 with 3 trailing spaces
        if(descriptionString.length() > 17){
            descriptionString = descriptionString.substring(0, 17);
        }else{
            while(descriptionString.length() < 17){
                descriptionString = descriptionString + " ";
            }
        }
        descriptionString = descriptionString + "   ";

        //Will always be max of 7 with 1 trailing spaces
        while(itemPriceString.length() < 7){
            itemPriceString = itemPriceString + " ";
        }
        itemPriceString = itemPriceString + " ";

        if(totalPriceString.length() == 7){
            totalPriceString = " " + totalPriceString;
        }else if(totalPriceString.length() == 6){
            totalPriceString = "  " + totalPriceString;
        }else if(totalPriceString.length() == 5){
            totalPriceString = "   " + totalPriceString;
        }

        return quantityString + descriptionString + itemPriceString + totalPriceString;
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

    public String getInsertString(){
        String sql = "INSERT INTO InvoiceItem VALUES(";

        sql += invoiceItemID + ",";
        sql += invoiceID + ",";
        sql += itemID + ",";
        sql += quantity + ",";
        sql += discount + ",";
        sql += total;

        sql += ");";

        return sql;
    }
}
