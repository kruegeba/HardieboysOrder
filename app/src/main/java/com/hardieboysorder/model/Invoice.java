package com.hardieboysorder.model;

import java.util.Date;

public class Invoice {

    private int invoiceID;
    private int contactID;
    private String type;
    private double grandTotal;
    private Date date;

    public Invoice(){}

    public Invoice(int invoiceID, String type, double grandTotal, Date date){
        this.invoiceID = invoiceID;
        this.type = type;
        this.grandTotal = grandTotal;
        this.date = date;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
