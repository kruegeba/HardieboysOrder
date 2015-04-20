package com.hardieboysorder.model;

public class Item {

    private int itemID;
    private String code;
    private String description;
    private double price;
    private byte[] icon;
    private int iconOrder;
    private int isActive;

    public Item(){}

    public Item(String code, String description, double price, byte[] icon, int iconOrder, int isActive){
        this.code = code;
        this.description = description;
        this.price = price;
        this.icon = icon;
        this.iconOrder = iconOrder;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public boolean equals(Item item){
        if(this.itemID == item.getItemID() &&
                this.code == item.getCode() &&
                this.description == item.getDescription() &&
                this.price == item.getPrice() &&
                this.icon == item.getIcon() &&
                this.iconOrder == item.getIconOrder() &&
                this.isActive == item.getIsActive()){
            return true;
        }else{
            return false;
        }
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public int getIconOrder() {
        return iconOrder;
    }

    public void setIconOrder(int iconOrder) {
        this.iconOrder = iconOrder;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getInsertString(){
        String sql = "INSERT INTO Item VALUES(";

        sql += itemID + ",";
        sql += "'" + code + "',";
        sql += "'" + description + "',";
        sql += price + ",";
        sql += "null,";
        sql += iconOrder + ",";
        sql += isActive;

        sql += ");";

        return sql;
    }
}
