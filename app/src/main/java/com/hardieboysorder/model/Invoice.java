package com.hardieboysorder.model;

import java.util.Date;

public class Invoice {

    private int invoiceID;
    private int contactID;
    private double grandTotal;
    private Date date;

    public Invoice(){ }

    public Invoice(int contactID, double grandTotal, Date date){
        this.contactID = contactID;
        this.grandTotal = grandTotal;
        this.date = date;
    }

    public Invoice(int invoiceID, int contactID, double grandTotal, Date date){
        this(contactID, grandTotal, date);
        this.invoiceID = invoiceID;
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
