package com.hardieboysorder.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.provider.ContactsContract;

import com.hardieboysorder.model.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HardieboysOrderDB extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "HardieboysOrderDB";
    private static final int DATABASE_VERSION = 1;

    public HardieboysOrderDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItemTableSQL = "CREATE TABLE Item ( " +
                "ItemID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Code TEXT, "+
                "Description TEXT, "+
                "Price INTEGER, "+
                "Icon BLOB, "+
                "IconOrder INTEGER, "+
                "IsActive INTEGER )";

        String createOrderTableSQL = "CREATE TABLE Invoice ( " +
                "InvoiceID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ContactID INTEGER, "+
                "Type TEXT, "+
                "GrandTotal INTEGER, "+
                "Date DATETIME )";

        String createOrderItemTableSQL = "CREATE TABLE InvoiceItem ( " +
                "InvoiceItemID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "InvoiceID INTEGER, "+
                "ItemID INTEGER, "+
                "Quantity INTEGER, "+
                "Discount INTEGER, "+
                "Total INTEGER )";

        db.execSQL(createItemTableSQL);
        db.execSQL(createOrderTableSQL);
        db.execSQL(createOrderItemTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Item");
        db.execSQL("DROP TABLE IF EXISTS Invoice");
        db.execSQL("DROP TABLE IF EXISTS InvoiceItem");

        this.onCreate(db);
    }

    public void addItem(Item item){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("Code", item.getCode());
        values.put("Description", item.getDescription());
        values.put("Price", item.getPrice());
        values.put("Icon", item.getIcon());
        values.put("IconOrder", item.getIconOrder());
        values.put("IsActive", item.getIsActive());

        // 3. insert
        db.insert("Item", // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Item getItem(int id){

        String[] columns = {"ItemID", "Code", "Description", "Price", "Icon", "IconOrder", "IsActive"};

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query("Item", // a. table
                        columns, // b. column names
                        " ItemID = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        Item item = new Item();
        item.setItemID(cursor.getInt(0));
        item.setCode(cursor.getString(1));
        item.setDescription(cursor.getString(2));
        item.setPrice(cursor.getDouble(3));
        item.setIcon(cursor.getBlob(4));
        item.setIconOrder(cursor.getInt(5));
        item.setIsActive(cursor.getInt(6));

        db.close();

        // 5. return item
        return item;
    }

    public ArrayList<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<Item>();

        // 1. build the query
        String query = "SELECT ItemID, Code, Description, Price, Icon, IconOrder, IsActive FROM Item";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Item item = null;
        if (cursor.moveToFirst()) {
            do {
                item = new Item();
                item.setItemID(cursor.getInt(0));
                item.setCode(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setPrice(cursor.getDouble(3));
                item.setIcon(cursor.getBlob(4));
                item.setIconOrder(cursor.getInt(5));
                item.setIsActive(cursor.getInt(6));

                // Add book to books
                items.add(item);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return items;
    }

    public ArrayList<Item> getAllActiveItems() {
        ArrayList<Item> items = new ArrayList<Item>();

        // 1. build the query
        String query = "SELECT ItemID, Code, Description, Price, Icon, IconOrder, IsActive FROM Item WHERE IsActive = 1 ORDER BY Description COLLATE NOCASE";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Item item = null;
        if (cursor.moveToFirst()) {
            do {
                item = new Item();
                item.setItemID(cursor.getInt(0));
                item.setCode(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setPrice(cursor.getDouble(3));
                item.setIcon(cursor.getBlob(4));
                item.setIconOrder(cursor.getInt(5));
                item.setIsActive(cursor.getInt(6));

                // Add book to books
                items.add(item);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return items;
    }

    public int updateItem(Item item) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("Code", item.getCode());
        values.put("Description", item.getDescription());
        values.put("Price", item.getPrice());
        values.put("Icon", item.getIcon());
        values.put("IconOrder", item.getIconOrder());
        values.put("IsActive", item.getIsActive());

        // 3. updating row
        int i = db.update("Item", //table
                values, // column/value
                "ItemID = ?", // selections
                new String[] { String.valueOf(item.getItemID()) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    public void deleteItem(Item item) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete("Item", //table name
                "ItemID = ?",  // selections
                new String[] { String.valueOf(item.getItemID()) }); //selections args

        // 3. close
        db.close();
    }

    public void decreaseIconOrders(Item item){
        ArrayList<Item> x = getAllActiveItems();

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE Item SET IconOrder = IconOrder - 1 WHERE IconOrder > " + item.getIconOrder();

        db.execSQL(sql);

        x = getAllActiveItems();

        db.close();
    }

    public void addInvoice(Invoice invoice){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("ContactID", invoice.getContactID());
        values.put("GrandTotal", invoice.getGrandTotal());
        values.put("Date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(invoice.getDate()));

        // 3. insert
        db.insert("Invoice", // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Invoice getInvoice(int id){

        String[] columns = {"InvoiceID", "ContactID", "GrandTotal", "Date"};

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query("Invoice", // a. table
                        columns, // b. column names
                        "InvoiceID = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        Invoice invoice = new Invoice();
        invoice.setInvoiceID(cursor.getInt(0));
        invoice.setContactID(cursor.getInt(1));
        invoice.setGrandTotal(cursor.getDouble(2));
        try{
            invoice.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(cursor.getString(3)));
        }catch(Exception e){
            //Ignore for now
        }

        db.close();

        // 5. return
        return invoice;
    }

    public ArrayList<Invoice> getAllInvoices() {
        ArrayList<Invoice> invoices = new ArrayList<Invoice>();

        // 1. build the query
        String query = "SELECT InvoiceID, ContactID, GrandTotal, Date FROM Invoice";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Invoice invoice = null;
        if (cursor.moveToFirst()) {
            do {
                invoice = new Invoice();
                invoice.setInvoiceID(cursor.getInt(0));
                invoice.setContactID(cursor.getInt(1));
                invoice.setGrandTotal(cursor.getDouble(2));
                try{
                    invoice.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(cursor.getString(3)));
                }catch(Exception e){
                    //Ignore for now
                }

                // Add book to books
                invoices.add(invoice);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return invoices;
    }

    public int updateInvoice(Invoice invoice) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("ContactID", invoice.getContactID());
        values.put("GrandTotal", invoice.getGrandTotal());
        values.put("Date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(invoice.getDate()));

        // 3. updating row
        int i = db.update("Invoice", //table
                values, // column/value
                "InvoiceID = ?", // selections
                new String[] { String.valueOf(invoice.getInvoiceID()) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    public void deleteInvoice(Invoice invoice) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete("Invoice", //table name
                "InvoiceID = ?",  // selections
                new String[] { String.valueOf(invoice.getInvoiceID()) }); //selections args

        // 3. close
        db.close();
    }

    public void addInvoiceItem(InvoiceItem invoiceItem){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("InvoiceID", invoiceItem.getInvoiceID());
        values.put("ItemID", invoiceItem.getItemID());
        values.put("Quantity", invoiceItem.getQuantity());
        values.put("Discount",invoiceItem.getDiscount());
        values.put("Total", invoiceItem.getTotal());

        // 3. insert
        db.insert("InvoiceItem", // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public InvoiceItem getInvoiceItem(int id){

        String[] columns = {"InvoiceItemID", "InvoiceID", "ItemID", "Quantity", "Discount", "Total"};

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query("InvoiceItem", // a. table
                        columns, // b. column names
                        "InvoiceItemID = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setInvoiceItemID(cursor.getInt(0));
        invoiceItem.setInvoiceID(cursor.getInt(1));
        invoiceItem.setItemID(cursor.getInt(2));
        invoiceItem.setQuantity(cursor.getInt(3));
        invoiceItem.setDiscount(cursor.getInt(4));
        invoiceItem.setTotal(cursor.getDouble(5));

        db.close();

        // 5. return
        return invoiceItem;
    }

    public ArrayList<InvoiceItem> getAllInvoiceItems() {
        ArrayList<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();

        // 1. build the query
        String query = "SELECT InvoiceItemID, InvoiceID, ItemID, Quantity, Discount, Total FROM InvoiceItem";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        InvoiceItem invoiceItem = null;
        if (cursor.moveToFirst()) {
            do {
                invoiceItem = new InvoiceItem();
                invoiceItem.setInvoiceItemID(cursor.getInt(0));
                invoiceItem.setInvoiceID(cursor.getInt(1));
                invoiceItem.setItemID(cursor.getInt(2));
                invoiceItem.setQuantity(cursor.getInt(3));
                invoiceItem.setDiscount(cursor.getInt(4));
                invoiceItem.setTotal(cursor.getDouble(5));

                // Add book to books
                invoiceItems.add(invoiceItem);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return invoiceItems;
    }

    public int updateInvoiceItem(InvoiceItem invoiceItem) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("InvoiceItemID", invoiceItem.getInvoiceItemID());
        values.put("InvoiceID", invoiceItem.getInvoiceID());
        values.put("ItemID", invoiceItem.getItemID());
        values.put("Quantity", invoiceItem.getQuantity());
        values.put("Discount", invoiceItem.getDiscount());
        values.put("Total", invoiceItem.getTotal());

        // 3. updating row
        int i = db.update("InvoiceItem", //table
                values, // column/value
                "InvoiceItemID = ?", // selections
                new String[] { String.valueOf(invoiceItem.getInvoiceItemID()) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    public void deleteInvoiceItem(InvoiceItem invoiceItem) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete("InvoiceItem", //table name
                "InvoiceItemID = ?",  // selections
                new String[] { String.valueOf(invoiceItem.getInvoiceItemID()) }); //selections args

        // 3. close
        db.close();
    }

    public void addTestData(){
        addItem(new Item("DGB330", "Dry Ginger Beer", 3.50, null, 1, 1));
        addItem(new Item("L330", "Lemonade", 3.50, null, 2, 1));
        addItem(new Item("LIME330", "Lime", 3.50, null, 3, 1));
        addItem(new Item("OJ2.5L", "Orange Juice 2.5 Litres", 18.75, null, 4, 1));
        addItem(new Item("OJ2L", "Orange Juice 2 Litres", 15.00, null, 5, 1));
        addItem(new Item("RGB330", "Regular Ginger Beer", 3.50, null, 6, 1));
        addItem(new Item("TANG2.5", "Tangelo Juice 2.5 Litres", 22.50, null, 7, 1));
        addItem(new Item("TANG2", "Tangelo Juice 2 Litres", 18.00, null, 8, 1));
        addItem(new Item("FRT5", "Freight at $5.00", 5.00, null, 9, 1));

        //addInvoice(new Invoice(3, "Credit Card", 10.50, new Date()));

        //addInvoiceItem(new InvoiceItem(1, 1, 3, 0, 10.50));
        //addInvoiceItem(new InvoiceItem(1, 1, 4, 0, 18.75));
    }

    public boolean isItemUsed(Item item){
        boolean result;

        // 1. build the query
        String query = "SELECT Item.ItemID FROM Item JOIN InvoiceItem ON Item.ItemID = InvoiceItem.ItemID WHERE Item.ItemID = " + item.getItemID();

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. see if we have anything
        if (cursor.getCount() > 0){
            result = true;
        }else{
            result = false;
        }

        db.close();

        return result;
    }

    public ArrayList<Item> getItemsWithNoIconOrder() {
        ArrayList<Item> items = new ArrayList<Item>();

        // 1. build the query
        String query = "SELECT ItemID, Code, Description, Price, Icon, IconOrder, IsActive FROM Item WHERE IsActive = 1 AND IconOrder = 0 ORDER BY Code COLLATE NOCASE";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Item item = null;
        if (cursor.moveToFirst()) {
            do {
                item = new Item();
                item.setItemID(cursor.getInt(0));
                item.setCode(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setPrice(cursor.getDouble(3));
                item.setIcon(cursor.getBlob(4));
                item.setIconOrder(cursor.getInt(5));
                item.setIsActive(cursor.getInt(6));

                // Add book to books
                items.add(item);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return items;
    }

    public Invoice getMostRecentInvoice(){

        Invoice invoice = null;
        String[] columns = {"InvoiceID", "ContactID", "GrandTotal", "Date"};

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT InvoiceID, ContactID, GrandTotal, Date FROM Invoice ORDER BY InvoiceID DESC LIMIT 1";

        // 2. build query
        Cursor cursor = db.rawQuery(sql, null);

        // 3. if we got results get the first one
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            invoice = new Invoice();
            invoice.setInvoiceID(cursor.getInt(0));
            invoice.setContactID(cursor.getInt(1));
            invoice.setGrandTotal(cursor.getDouble(2));
            try {
                invoice.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(cursor.getString(3)));
            } catch (Exception e) {
                //Ignore for now
            }
        }

        db.close();

        // 5. return
        return invoice;
    }

    public ArrayList<InvoiceItem> getInvoiceItemsForInvoice(int invoiceID) {
        ArrayList<InvoiceItem> invoiceItems = new ArrayList<InvoiceItem>();

        // 1. build the query
        String query = "SELECT InvoiceItemID, InvoiceID, ItemID, Quantity, Discount, Total FROM InvoiceItem WHERE InvoiceID = " + invoiceID;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        InvoiceItem invoiceItem = null;
        if (cursor.moveToFirst()) {
            do {
                invoiceItem = new InvoiceItem();
                invoiceItem.setInvoiceItemID(cursor.getInt(0));
                invoiceItem.setInvoiceID(cursor.getInt(1));
                invoiceItem.setItemID(cursor.getInt(2));
                invoiceItem.setQuantity(cursor.getInt(3));
                invoiceItem.setDiscount(cursor.getInt(4));
                invoiceItem.setTotal(cursor.getDouble(5));

                invoiceItem.setItem(getItem(invoiceItem.getItemID()));

                // Add book to books
                invoiceItems.add(invoiceItem);
            } while (cursor.moveToNext());
        }

        db.close();

        // return books
        return invoiceItems;
    }

    public ArrayList<OutputRow> getOutputRows(Date date) {
        ArrayList<OutputRow> outputRows = new ArrayList<OutputRow>();
        String dateString = "";
        try{
            dateString = new SimpleDateFormat("dd-MM-yyyy").format(date);
        }catch(Exception e){
            e.printStackTrace();
        }

        // 1. build the query
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("Inv.InvoiceID, ");
        query.append("Inv.Date, ");
        query.append("Inv.GrandTotal, ");
        query.append("Item.Code, ");
        query.append("InvItem.Quantity, ");
        query.append("InvItem.Discount, ");
        query.append("Inv.ContactID ");
        query.append("FROM ");
        query.append("Invoice Inv ");
        query.append("JOIN InvoiceItem InvItem ON Inv.InvoiceID = InvItem.InvoiceID ");
        query.append("JOIN Item Item ON InvItem.ItemID = Item.ItemID ");
        query.append("WHERE Inv.Date > '" + dateString + "' ");
        query.append("ORDER BY Inv.InvoiceID ASC");

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query.toString(), null);

        // 3. go over each row, build book and add it to list
        OutputRow outputRow = null;
        if (cursor.moveToFirst()) {
            do {
                outputRow = new OutputRow();

                outputRow.setInvoiceID(cursor.getInt(0));
                outputRow.setDate(cursor.getString(1));
                outputRow.setType("DI");
                outputRow.setGross(cursor.getDouble(2));
                outputRow.setItemCode(cursor.getString(3));
                outputRow.setQuantity(cursor.getInt(4));
                outputRow.setDiscount(cursor.getInt(5));
                outputRow.setContactID(cursor.getInt(6));

                outputRows.add(outputRow);
            } while (cursor.moveToNext());
        }

        db.close();

        return outputRows;
    }

    public ArrayList<OutputRow> getOutputRows(String startDate, String endDate) {
        ArrayList<OutputRow> outputRows = new ArrayList<OutputRow>();

        // 1. build the query
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("Inv.InvoiceID, ");
        query.append("Inv.Date, ");
        query.append("Inv.GrandTotal, ");
        query.append("Item.Code, ");
        query.append("InvItem.Quantity, ");
        query.append("InvItem.Discount, ");
        query.append("Inv.ContactID ");
        query.append("FROM ");
        query.append("Invoice Inv ");
        query.append("JOIN InvoiceItem InvItem ON Inv.InvoiceID = InvItem.InvoiceID ");
        query.append("JOIN Item Item ON InvItem.ItemID = Item.ItemID ");
        query.append("WHERE Inv.Date BETWEEN '" + startDate + "' AND '" + endDate + "' ");
        query.append("ORDER BY Inv.InvoiceID ASC");

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query.toString(), null);

        // 3. go over each row, build book and add it to list
        OutputRow outputRow = null;
        if (cursor.moveToFirst()) {
            do {
                outputRow = new OutputRow();

                outputRow.setInvoiceID(cursor.getInt(0));
                outputRow.setDate(cursor.getString(1));
                outputRow.setType("DI");
                outputRow.setGross(cursor.getDouble(2));
                outputRow.setItemCode(cursor.getString(3));
                outputRow.setQuantity(cursor.getInt(4));
                outputRow.setDiscount(cursor.getInt(5));
                outputRow.setContactID(cursor.getInt(6));

                outputRows.add(outputRow);
            } while (cursor.moveToNext());
        }

        db.close();

        return outputRows;
    }
}
