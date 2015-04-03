package com.hardieboysorder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hardieboysorder.R;
import com.hardieboysorder.db.HardieboysOrderDB;
import com.hardieboysorder.model.Invoice;
import com.hardieboysorder.model.InvoiceItem;
import com.hardieboysorder.model.Item;
import com.hardieboysorder.widget.ItemButton;
import com.hardieboysorder.widget.NumberButton;

import java.util.ArrayList;
import java.util.Date;

public class InvoicesTabActivity extends Activity {

    HardieboysOrderDB db;
    TextView invoiceTextView, contactTextView, dateTextView;
    ListView invoiceItemListView;
    ArrayAdapter<InvoiceItem> invoiceItemAdapter;
    ImageButton backImageButton, forwardImageButton, contactImageButton, printImageButton;
    RelativeLayout itemButtonLayout;
    LinearLayout numberButtonLayout;
    ItemButton pressedItemButton;
    NumberButton pressedNumberButton;
    Invoice currentInvoice;
    int mostRecentInvoiceID;
    boolean comingFromContactSelect = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoices_tab_activity);

        db = new HardieboysOrderDB(this);
        //db.addTestData();

        initializeViews();
        initializeClickEvents();
        loadNumberButtons();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!comingFromContactSelect) {
            loadItemButtons();
            loadMostRecentInvoice();
            loadInvoiceItems();
            handleNavButtons();
            comingFromContactSelect = false;
        }
    }

    private void initializeViews(){
        invoiceTextView = (TextView)findViewById(R.id.invoiceTextView);
        contactTextView = (TextView)findViewById(R.id.contactTextView);
        dateTextView = (TextView)findViewById(R.id.dateTextView);
        invoiceItemListView = (ListView)findViewById(R.id.invoiceItemListView);
        backImageButton = (ImageButton)findViewById(R.id.backImageButton);
        forwardImageButton = (ImageButton)findViewById(R.id.forwardImageButton);
        contactImageButton = (ImageButton)findViewById(R.id.contactImageButton);
        printImageButton = (ImageButton)findViewById(R.id.printImageButton);
    }

    private void initializeClickEvents(){
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentInvoice.getInvoiceID() != 1){
                    loadInvoice(db.getInvoice(currentInvoice.getInvoiceID() - 1));
                    loadInvoiceItems();
                }

                handleNavButtons();
            }
        });

        forwardImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentInvoice.getInvoiceID() == mostRecentInvoiceID){
                    if(currentInvoice.getContactID() != -1 || invoiceItemAdapter.getCount() > 0){
                        Invoice newInvoice = new Invoice(-1, 0, new Date());

                        db.addInvoice(newInvoice);
                        loadMostRecentInvoice();
                        loadInvoiceItems();
                        handleNavButtons();
                    }
                }else{
                    loadInvoice(db.getInvoice(currentInvoice.getInvoiceID() + 1));
                    loadInvoiceItems();
                    handleNavButtons();
                }
            }
        });

        contactImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });
    }

    private void loadItemButtons() {
        itemButtonLayout = (RelativeLayout) findViewById(R.id.itemButtonLayout);
        itemButtonLayout.removeAllViews();
        ArrayList<Item> items = db.getAllActiveItems();
        int colCount = 0;
        int rowCount = 0;

        //# of columns
        int colSpan = 4;
        int itemCount = items.size();

        for (int i = 0; i < itemCount; i++) {
            int id = 1000 + i;

            ItemButton newItemButton = null;
            boolean hasIconOrder = false;

            for(int y = 0; y < items.size(); y++){
                Item currentItem = items.get(y);

                if(items.get(y).getIconOrder() == i + 1){
                    newItemButton = new ItemButton(this, currentItem, id);
                    items.remove(currentItem);
                    hasIconOrder = true;
                    break;
                }
            }

            //Need to create a blank button for this space
            if(!hasIconOrder){
                newItemButton = new ItemButton(this, id);
            }

            newItemButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showItemAssignDialog(v);
                    return false;
                }
            });
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemButton selectedItemButton = (ItemButton)v;

                    if(selectedItemButton.getItem() != null) {
                        if (pressedItemButton != null) {
                            //An item button has already been selected so switch to new item if
                            //it is different, or un-select if it is the same button.
                            if (pressedItemButton.getItem().equals(selectedItemButton.getItem())) {
                                pressedItemButton = null;
                                selectedItemButton.getBackground().setAlpha(255);
                            } else {
                                pressedItemButton.getBackground().setAlpha(255);
                                pressedItemButton = selectedItemButton;
                                selectedItemButton.getBackground().setAlpha(128);
                            }
                        } else {
                            //Assign as selected item button and then check if number button has also been
                            //selected. Add new invoice item if both buttons have been selected.
                            pressedItemButton = selectedItemButton;
                            selectedItemButton.getBackground().setAlpha(128);

                            if (pressedItemButton != null && pressedNumberButton != null) {
                                InvoiceItem newInvoiceItem = new InvoiceItem();
                                newInvoiceItem.setInvoiceID(currentInvoice.getInvoiceID());
                                newInvoiceItem.setItemID(pressedItemButton.getItem().getItemID());
                                newInvoiceItem.setQuantity(pressedNumberButton.getNumber());
                                newInvoiceItem.setTotal(pressedItemButton.getItem().getPrice() * newInvoiceItem.getQuantity());
                                db.addInvoiceItem(newInvoiceItem);

                                pressedItemButton.getBackground().setAlpha(255);
                                pressedItemButton = null;
                                pressedNumberButton.getBackground().setAlpha(255);
                                pressedNumberButton = null;

                                loadInvoiceItems();
                            }
                        }
                    }
                }
            });

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 20, 0, 0);

            if (colCount == 0){
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            }
            else{
                layoutParams.addRule(RelativeLayout.RIGHT_OF, id - 1);
            }

            //If we are in any row except the top row, place in reference to the button above it
            if (rowCount != 0){
                layoutParams.addRule(RelativeLayout.BELOW, id - colSpan);
            }

            newItemButton.setLayoutParams(layoutParams);
            itemButtonLayout.addView(newItemButton);

            if (colCount == colSpan - 1) {
                rowCount += 1;
            }

            colCount = (colCount + 1) % colSpan;
        }
    }

    private void loadNumberButtons(){
        numberButtonLayout = (LinearLayout)findViewById(R.id.numberButtonLayout);

        for(int i = 1; i < 21; i++){
            NumberButton newNumberButton = new NumberButton(this, i);
            newNumberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NumberButton selectedNumberButton = (NumberButton) v;

                    if (pressedNumberButton != null) {
                        //A number button has already been selected so switch to new item if
                        //it is different, or un-select if it is the same button.
                        if (pressedNumberButton.getNumber() == selectedNumberButton.getNumber()) {
                            pressedNumberButton = null;
                            selectedNumberButton.getBackground().setAlpha(255);
                        } else {
                            pressedNumberButton.getBackground().setAlpha(255);
                            pressedNumberButton = selectedNumberButton;
                            selectedNumberButton.getBackground().setAlpha(128);
                        }
                    } else {
                        //Assign as selected item button and then check if number button has also been
                        //selected. Add new invoice item if both buttons have been selected.
                        pressedNumberButton = selectedNumberButton;
                        pressedNumberButton.getBackground().setAlpha(128);

                        if (pressedItemButton != null && pressedNumberButton != null) {
                            InvoiceItem newInvoiceItem = new InvoiceItem();
                            newInvoiceItem.setInvoiceID(currentInvoice.getInvoiceID());
                            newInvoiceItem.setItemID(pressedItemButton.getItem().getItemID());
                            newInvoiceItem.setQuantity(pressedNumberButton.getNumber());
                            newInvoiceItem.setTotal(pressedItemButton.getItem().getPrice() * newInvoiceItem.getQuantity());
                            db.addInvoiceItem(newInvoiceItem);

                            pressedItemButton.getBackground().setAlpha(255);
                            pressedItemButton = null;
                            pressedNumberButton.getBackground().setAlpha(255);
                            pressedNumberButton = null;

                            loadInvoiceItems();
                        }
                    }
                }
            });
            numberButtonLayout.addView(newNumberButton);
        }
        numberButtonLayout.addView(new NumberButton(this, "*"));


    }

    private void loadMostRecentInvoice(){
        currentInvoice = db.getMostRecentInvoice();

        if(currentInvoice == null){
            currentInvoice = new Invoice(1, -1, 0, new Date());
            db.addInvoice(currentInvoice);
        }

        invoiceTextView.setText("#" + currentInvoice.getInvoiceID());
        contactTextView.setText(getContactName(currentInvoice.getContactID()));
        dateTextView.setText(currentInvoice.getDate().toString());
        mostRecentInvoiceID = currentInvoice.getInvoiceID();
    }

    private void loadInvoice(Invoice invoice){
        currentInvoice = invoice;
        invoiceTextView.setText("#" + currentInvoice.getInvoiceID());
        contactTextView.setText(getContactName(currentInvoice.getContactID()));
        dateTextView.setText(currentInvoice.getDate().toString());
    }

    private void loadInvoiceItems(){
        invoiceItemAdapter = new ArrayAdapter<InvoiceItem>(this, R.layout.listview_row, db.getInvoiceItemsForInvoice(currentInvoice.getInvoiceID()));
        invoiceItemListView.setAdapter(invoiceItemAdapter);
    }

    private void handleNavButtons(){
        if(currentInvoice.getInvoiceID() == mostRecentInvoiceID){
            forwardImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_new));
        }else{
            forwardImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_forward));
        }
    }

    private void showItemAssignDialog(View v){
        final ItemButton selectedItemButton = (ItemButton)v;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View itemAssignDialogView = layoutInflater.inflate(R.layout.item_assign_dialog, null);

        final AlertDialog itemAssignDialog = new AlertDialog.Builder(this)
                .setView(itemAssignDialogView)
                .setCancelable(true)
                .setTitle("Item Assign")
                .setMessage("Assign which item to this button?")
                .setNegativeButton("Cancel", null)
                .create();

        final ArrayAdapter<Item> itemDesignAdapter = new ArrayAdapter<Item>(this, R.layout.listview_row, db.getItemsWithNoIconOrder());
        if(selectedItemButton.getItem() != null){
            itemDesignAdapter.add(new Item(null, "(Clear)", 0, null, 0, 0));
        }

        ListView itemAssignListView = (ListView)itemAssignDialogView.findViewById(R.id.itemAssignListView);
        itemAssignListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = null;

                if(itemDesignAdapter.getItem(position).getDescription().equals("(Clear)")){
                    item = selectedItemButton.getItem();
                    item.setIconOrder(0);
                }else{
                    if(selectedItemButton.getItem() != null){
                        Item currentItemButtonItem = selectedItemButton.getItem();
                        currentItemButtonItem.setIconOrder(0);

                        db.updateItem(currentItemButtonItem);
                    }

                    item = itemDesignAdapter.getItem(position);
                    item.setIconOrder(selectedItemButton.getId() - 999);
                }

                db.updateItem(item);
                onResume();
                itemAssignDialog.dismiss();
            }
        });

        itemAssignListView.setAdapter(itemDesignAdapter);

        itemAssignDialog.show();
    }

    @Override public void onActivityResult(int reqCode, int resultCode, Intent data){ super.onActivityResult(reqCode, resultCode, data);

        switch(reqCode)
        {
            case (3):
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    //Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.Data.DATA1}, ContactsContract.Data.CONTACT_ID + " = " + 1, null, null);
                    if (c.moveToFirst())
                    {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        currentInvoice.setContactID(Integer.parseInt(id));
                        db.updateInvoice(currentInvoice);
                        contactTextView.setText(getContactName(currentInvoice.getContactID()));
                        //String q = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        comingFromContactSelect = true;
                    }
                }
        }
    }

    private String getContactName(int contactID){
        String name = "";

        if(contactID > 0){
            // Build the Uri to query to table
            Uri myPhoneUri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, contactID + "");

            // Query the table
            Cursor phoneCursor = managedQuery(
                    myPhoneUri, null, null, null, null);

            // Get the phone numbers from the contact
            for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {

                // Get a phone number
                //String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                name = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            }
        }

        return name;
    }

}
