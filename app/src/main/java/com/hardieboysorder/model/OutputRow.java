package com.hardieboysorder.model;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ben on 4/10/2015.
 */
public class OutputRow {

    private int invoiceID;
    private int date;
    private String type;
    private String nameCode;
    private double gross;
    private double net;
    private String itemCode;
    private int quantity;
    private int discount;
    private int contactID;
    private double itemPrice;

    public OutputRow(){

    }

    @Override
    public String toString(){
        StringBuilder outputString = new StringBuilder();

        outputString.append(formatInvoiceForReceipt(invoiceID));
        outputString.append("\t");
        try{
            outputString.append(new SimpleDateFormat("dd-MMM-yy").format(new Date(date * 1000L)));
            //outputString.append(new SimpleDateFormat("dd-MMM-yy").format(formattedDate));
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

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
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
        //double gstAmount = (gross * 3)/23;
        //double amount = gross - gstAmount;
        //this.gross = round(amount, 2);
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = calculateWithoutGST();
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
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

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    private double calculateWithoutGST(){
        double gstAmount = (itemPrice * 3) / 23;
        double amount = itemPrice - gstAmount;
        amount *= quantity;

        if(discount != 0) {
           double discountAmount = (amount * ((double)discount/100));
           amount -= discountAmount;
        }

        return round(amount, 2);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private String formatInvoiceForReceipt(int invoiceId){
        String outputString = String.valueOf(invoiceId);
        String orderPrefix = "";

        try{
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+File.separator+"HardieBoysOrderStart.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            orderPrefix = fileReader.readLine();
        }catch(Exception e){
        }

        while(outputString.length() < 6){
            outputString = "0" + outputString;
        }

        return orderPrefix + outputString;

    }
}
