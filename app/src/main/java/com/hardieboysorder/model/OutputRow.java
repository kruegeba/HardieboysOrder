package com.hardieboysorder.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ben on 4/10/2015.
 */
public class OutputRow {

    private int invoiceID;
    private String date;
    private String type;
    private String nameCode;
    private double gross;
    private double net;
    private String itemCode;
    private int quantity;
    private int discount;
    private int contactID;

    public OutputRow(){

    }

    @Override
    public String toString(){
        StringBuilder outputString = new StringBuilder();

        outputString.append(String.valueOf(invoiceID));
        outputString.append("\t");
        try{
            Date formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date);
            outputString.append(new SimpleDateFormat("dd-MMM-yy").format(formattedDate));
        }catch(Exception e){
            e.printStackTrace();
        }
        outputString.append("\t");
        outputString.append(type);
        outputString.append("\t");
        outputString.append(nameCode);
        outputString.append("\t");
        outputString.append(gross);
        outputString.append("\t");
        outputString.append(net);
        outputString.append("\t");
        outputString.append(itemCode);
        outputString.append("\t");
        outputString.append(quantity);
        outputString.append("\t");
        outputString.append(discount);

        return outputString.toString();
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameCode() {
        return nameCode;
    }

    public void setNameCode(String nameCode) {
        this.nameCode = nameCode;
    }

    public double getGross() {
        return gross;
    }

    public void setGross(double gross) {
        this.gross = gross;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
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

    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }
}
