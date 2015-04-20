package com.hardieboysorder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.hardieboysorder.R;
import com.hardieboysorder.adapter.InvoiceItemAdapter;
import com.hardieboysorder.db.HardieboysOrderDB;
import com.hardieboysorder.model.Invoice;
import com.hardieboysorder.model.InvoiceItem;
import com.hardieboysorder.model.Item;
import com.hardieboysorder.widget.ItemButton;
import com.hardieboysorder.widget.NumberButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class InvoicesTabActivity extends Activity {

    HardieboysOrderDB db;
    TextView invoiceTextView, contactTextView, dateTextView, invoiceGrandTotalTextView;
    ListView invoiceItemListView;
    InvoiceItemAdapter invoiceItemAdapter;
    ImageButton backImageButton, forwardImageButton, contactImageButton, printImageButton;
    RelativeLayout itemButtonLayout;
    LinearLayout numberButtonLayout;
    ItemButton pressedItemButton;
    NumberButton pressedNumberButton;
    Invoice currentInvoice;
    int mostRecentInvoiceID, otherItemAmount;
    boolean comingFromContactSelect = false;
    boolean mConnected = false;
    static BixolonPrinter mBixolonPrinter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoices_tab_activity);

        db = new HardieboysOrderDB(this);
        //db.addTestData();

        initializeViews();
        initializeClickEvents();
        loadNumberButtons();

        mBixolonPrinter = new BixolonPrinter(this, mHandler, null);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!comingFromContactSelect) {
            loadItemButtons();
            loadMostRecentInvoice();
            loadInvoiceItems();
            handleNavButtons();
        }

        comingFromContactSelect = false;
    }

    private void initializeViews() {
        invoiceTextView = (TextView) findViewById(R.id.invoiceTextView);
        contactTextView = (TextView) findViewById(R.id.contactTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        invoiceGrandTotalTextView = (TextView) findViewById(R.id.invoiceGrandTotalTextView);
        invoiceItemListView = (ListView) findViewById(R.id.invoiceItemListView);
        backImageButton = (ImageButton) findViewById(R.id.backImageButton);
        forwardImageButton = (ImageButton) findViewById(R.id.forwardImageButton);
        contactImageButton = (ImageButton) findViewById(R.id.contactImageButton);
        printImageButton = (ImageButton) findViewById(R.id.printImageButton);
    }

    private void initializeClickEvents() {
        invoiceItemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDiscountDeleteDialog(view, invoiceItemAdapter.getItem(position));
                return false;
            }
        });

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInvoice.getInvoiceID() != 1) {
                    loadInvoice(db.getInvoice(currentInvoice.getInvoiceID() - 1));
                    loadInvoiceItems();
                }

                handleNavButtons();
            }
        });

        forwardImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInvoice.getInvoiceID() == mostRecentInvoiceID) {
                    if (currentInvoice.getContactID() != -1 || invoiceItemAdapter.getCount() > 0) {
                        Invoice newInvoice = new Invoice(-1, 0, new Date());

                        db.addInvoice(newInvoice);
                        loadMostRecentInvoice();
                        loadInvoiceItems();
                        handleNavButtons();
                    }
                } else {
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

        printImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.createBackupFile();
                getContactAddress(currentInvoice.getContactID());
                if (!mConnected) {
                    mBixolonPrinter.findUsbPrinters();
                    mConnected = true;
                }

                if (mConnected) {
                    printCurrentInvoice();
                }
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

            for (int y = 0; y < items.size(); y++) {
                Item currentItem = items.get(y);

                if (items.get(y).getIconOrder() == i + 1) {
                    newItemButton = new ItemButton(this, currentItem, id);
                    items.remove(currentItem);
                    hasIconOrder = true;
                    break;
                }
            }

            //Need to create a blank button for this space
            if (!hasIconOrder) {
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
                    ItemButton selectedItemButton = (ItemButton) v;

                    if (selectedItemButton.getItem() != null) {
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
                                if (otherItemAmount > 0) {
                                    newInvoiceItem.setQuantity(otherItemAmount);
                                } else {
                                    newInvoiceItem.setQuantity(pressedNumberButton.getNumber());
                                }
                                newInvoiceItem.setTotal(pressedItemButton.getItem().getPrice() * newInvoiceItem.getQuantity());
                                db.addInvoiceItem(newInvoiceItem);

                                double currentGrandTotal = currentInvoice.getGrandTotal();
                                currentGrandTotal += newInvoiceItem.getTotal();

                                currentInvoice.setGrandTotal(currentGrandTotal);
                                db.updateInvoice(currentInvoice);

                                invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));

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

            if (colCount == 0) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            } else {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, id - 1);
            }

            //If we are in any row except the top row, place in reference to the button above it
            if (rowCount != 0) {
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

    private void loadNumberButtons() {
        numberButtonLayout = (LinearLayout) findViewById(R.id.numberButtonLayout);

        for (int i = 1; i < 21; i++) {
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

                            double currentGrandTotal = currentInvoice.getGrandTotal();
                            currentGrandTotal += newInvoiceItem.getTotal();

                            currentInvoice.setGrandTotal(currentGrandTotal);
                            db.updateInvoice(currentInvoice);

                            invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));

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
        NumberButton otherNumberButton = new NumberButton(this, "*");
        otherNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOtherAmountDialog(v);
            }
        });
        numberButtonLayout.addView(otherNumberButton);


    }

    private void loadMostRecentInvoice() {
        currentInvoice = db.getMostRecentInvoice();

        if (currentInvoice == null) {
            currentInvoice = new Invoice(1, -1, 0, new Date());
            db.addInvoice(currentInvoice);
        }

        invoiceTextView.setText("#" + formatInvoiceForReceipt(currentInvoice.getInvoiceID()));
        contactTextView.setText(getContactName(currentInvoice.getContactID()));
        dateTextView.setText(new SimpleDateFormat("d-M-yyyy h:mm a").format(currentInvoice.getDate()));
        invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));
        mostRecentInvoiceID = currentInvoice.getInvoiceID();
    }

    private void loadInvoice(Invoice invoice) {
        currentInvoice = invoice;
        invoiceTextView.setText("#" + formatInvoiceForReceipt(currentInvoice.getInvoiceID()));
        contactTextView.setText(getContactName(currentInvoice.getContactID()));
        dateTextView.setText(new SimpleDateFormat("d-M-yyyy h:mm a").format(currentInvoice.getDate()));
        invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));
    }

    private void loadInvoiceItems() {
        ArrayList<InvoiceItem> invoiceItems = db.getInvoiceItemsForInvoice(currentInvoice.getInvoiceID());
        invoiceItemAdapter = new InvoiceItemAdapter(this);

        for (InvoiceItem invoiceItem : invoiceItems) {
            invoiceItemAdapter.add(invoiceItem);
            if (invoiceItem.getDiscount() > 0) {
                invoiceItemAdapter.add(new InvoiceItem(-22, invoiceItem.getInvoiceItemID(), -1, 0, invoiceItem.getDiscount(), invoiceItem.getTotal()));
            }
        }

        invoiceItemListView.setAdapter(invoiceItemAdapter);
    }

    private void handleNavButtons() {
        if (currentInvoice.getInvoiceID() == mostRecentInvoiceID) {
            forwardImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_new));
        } else {
            forwardImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_forward));
        }
    }

    private void showItemAssignDialog(View v) {
        final ItemButton selectedItemButton = (ItemButton) v;

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
        if (selectedItemButton.getItem() != null) {
            itemDesignAdapter.add(new Item(null, "(Clear)", 0, null, 0, 0));
        }

        ListView itemAssignListView = (ListView) itemAssignDialogView.findViewById(R.id.itemAssignListView);
        itemAssignListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = null;

                if (itemDesignAdapter.getItem(position).getDescription().equals("(Clear)")) {
                    item = selectedItemButton.getItem();
                    item.setIconOrder(0);
                } else {
                    if (selectedItemButton.getItem() != null) {
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

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (3):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    //Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] {ContactsContract.Data.DATA1}, ContactsContract.Data.CONTACT_ID + " = " + 1, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        currentInvoice.setContactID(Integer.parseInt(id));
                        db.updateInvoice(currentInvoice);
                        contactTextView.setText(getContactName(currentInvoice.getContactID()));
                        //String q = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                }

                comingFromContactSelect = true;
        }
    }

    private String getContactName(int contactID) {
        String name = "";

        if (contactID > 0) {
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

    private void showDiscountDeleteDialog(View view, final InvoiceItem invoiceItem) {
        final AlertDialog invoiceItemDiscountDeleteDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Invoice Item")
                .setMessage("What would you like to do with this item?")
                .setPositiveButton("Cancel", null)
                .setNegativeButton("Apply Discount", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showInvoiceItemDiscountDialog(invoiceItem);
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (invoiceItem.getInvoiceItemID() == -22) {
                            InvoiceItem invoiceItemWithDiscount = db.getInvoiceItem(invoiceItem.getInvoiceID());

                            currentInvoice.setGrandTotal(currentInvoice.getGrandTotal() + invoiceItem.getDiscountAmount());

                            invoiceItemWithDiscount.setDiscount(0);
                            db.updateInvoiceItem(invoiceItemWithDiscount);
                        } else {
                            if (invoiceItem.getDiscount() > 0) {
                                currentInvoice.setGrandTotal(currentInvoice.getGrandTotal() - (invoiceItem.getTotal() - invoiceItem.getDiscountAmount()));
                            } else {
                                currentInvoice.setGrandTotal(currentInvoice.getGrandTotal() - invoiceItem.getTotal());
                            }

                            db.deleteInvoiceItem(invoiceItem);
                        }


                        db.updateInvoice(currentInvoice);
                        invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));

                        loadInvoiceItems();
                    }
                })
                .create();

        invoiceItemDiscountDeleteDialog.show();
    }

    private void showInvoiceItemDiscountDialog(final InvoiceItem invoiceItem) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View invoiceItemDiscountView = layoutInflater.inflate(R.layout.invoiceitem_discount_dialog, null);

        final EditText discountEditText = (EditText) invoiceItemDiscountView.findViewById(R.id.invoiceItemDiscountEditText);

        final AlertDialog itemEditDialog = new AlertDialog.Builder(this)
                .setView(invoiceItemDiscountView)
                .setCancelable(true)
                .setTitle("Apply Discount %")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        invoiceItem.setDiscount(Integer.parseInt(discountEditText.getText().toString()));
                        double discountAmount = (invoiceItem.getTotal() * ((double) invoiceItem.getDiscount() / 100));

                        currentInvoice.setGrandTotal(currentInvoice.getGrandTotal() - discountAmount);
                        invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));

                        db.updateInvoiceItem(invoiceItem);
                        db.updateInvoice(currentInvoice);

                        loadInvoiceItems();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        itemEditDialog.show();
    }

    private void showOtherAmountDialog(final View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View otherAmountView = layoutInflater.inflate(R.layout.other_amount_dialog, null);

        final EditText otherAmountEditText = (EditText) otherAmountView.findViewById(R.id.otherAmountEditText);

        final AlertDialog itemEditDialog = new AlertDialog.Builder(this)
                .setView(otherAmountView)
                .setCancelable(true)
                .setTitle("Other Item Amount")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NumberButton selectedNumberButton = (NumberButton) v;
                        otherItemAmount = Integer.parseInt(otherAmountEditText.getText().toString());

                        if (pressedNumberButton != null) {
                            pressedNumberButton.getBackground().setAlpha(255);
                            pressedNumberButton = selectedNumberButton;
                            selectedNumberButton.getBackground().setAlpha(128);
                        } else {
                            //Assign as selected item button and then check if number button has also been
                            //selected. Add new invoice item if both buttons have been selected.
                            pressedNumberButton = selectedNumberButton;
                            selectedNumberButton.getBackground().setAlpha(128);

                            if (pressedItemButton != null && pressedNumberButton != null) {
                                InvoiceItem newInvoiceItem = new InvoiceItem();
                                newInvoiceItem.setInvoiceID(currentInvoice.getInvoiceID());
                                newInvoiceItem.setItemID(pressedItemButton.getItem().getItemID());
                                newInvoiceItem.setQuantity(otherItemAmount);
                                newInvoiceItem.setTotal(pressedItemButton.getItem().getPrice() * newInvoiceItem.getQuantity());
                                db.addInvoiceItem(newInvoiceItem);

                                double currentGrandTotal = currentInvoice.getGrandTotal();
                                currentGrandTotal += newInvoiceItem.getTotal();

                                currentInvoice.setGrandTotal(currentGrandTotal);
                                db.updateInvoice(currentInvoice);

                                invoiceGrandTotalTextView.setText("$" + String.format("%.2f", currentInvoice.getGrandTotal()));

                                pressedItemButton.getBackground().setAlpha(255);
                                pressedItemButton = null;
                                pressedNumberButton.getBackground().setAlpha(255);
                                otherItemAmount = 0;
                                pressedNumberButton = null;

                                loadInvoiceItems();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        itemEditDialog.show();
    }

    private void printCurrentInvoice() {

        int left = BixolonPrinter.ALIGNMENT_LEFT;
        int center = BixolonPrinter.ALIGNMENT_CENTER;
        int right = BixolonPrinter.ALIGNMENT_RIGHT;

        int bold = BixolonPrinter.TEXT_ATTRIBUTE_EMPHASIZED;
        bold |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int attribute = BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        int underline = BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
        underline |= BixolonPrinter.TEXT_ATTRIBUTE_UNDERLINE2;

        int large = BixolonPrinter.TEXT_SIZE_HORIZONTAL2;
        large |= BixolonPrinter.TEXT_SIZE_VERTICAL2;

        int size = BixolonPrinter.TEXT_SIZE_HORIZONTAL1;
        size |= BixolonPrinter.TEXT_SIZE_VERTICAL1;

        ArrayList<InvoiceItem> currentInvoiceItems = db.getInvoiceItemsForInvoice(currentInvoice.getInvoiceID());

        printLogo();
        mBixolonPrinter.lineFeed(2, false);
        mBixolonPrinter.printText("HARDIEBOYS BEVERAGES", center, bold, large, false);
        mBixolonPrinter.lineFeed(2, false);
        mBixolonPrinter.printText("PO BOX 27413", center, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("WELLINGTON", center, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("TEL 021647528", center, attribute, size, false);
        mBixolonPrinter.lineFeed(2, false);
        mBixolonPrinter.printText("TAX INVOICE GST NO.18475243", center, attribute, size, false);
        mBixolonPrinter.lineFeed(2, false);
        mBixolonPrinter.printText("DATE:       " + new SimpleDateFormat("d-M-yyyy h:mm a").format(new Date()), left, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("INVOICE No: ", left, attribute, size, false);
        mBixolonPrinter.printText(formatInvoiceForReceipt(currentInvoice.getInvoiceID()), bold, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("SOLD TO:    " + getContactName(currentInvoice.getContactID()).toUpperCase(), left, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);

        String[] cityStateArray = getContactAddress(currentInvoice.getContactID());

        mBixolonPrinter.printText("            " + cityStateArray[0].toUpperCase(), left, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("            " + cityStateArray[1].toUpperCase(), left, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("NAME CODE:  " + getContactNickname(currentInvoice.getContactID()), left, attribute, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("TYPE:       INVOICE", left, attribute, size, false);
        mBixolonPrinter.lineFeed(4, false);
        mBixolonPrinter.printText("QTY", left, underline, size, false);
        mBixolonPrinter.printText("   ", left, attribute, size, false);
        mBixolonPrinter.printText("DESC", left, underline, size, false);
        mBixolonPrinter.printText("                ", right, attribute, size, false);
        mBixolonPrinter.printText("UNIT", left, underline, size, false);
        mBixolonPrinter.printText("      ", left, attribute, size, false);
        mBixolonPrinter.printText("PRICE", left, underline, size, false);
        mBixolonPrinter.lineFeed(1, false);

        for (InvoiceItem invoiceItem : currentInvoiceItems) {
            mBixolonPrinter.printText(invoiceItem.getPrintOutLine(), left, attribute, size, false);
            mBixolonPrinter.lineFeed(1, false);
        }

        String subtotalString = "SUBTOTAL: $" + String.format("%.2f", currentInvoice.getGrandTotal());
        while (subtotalString.length() < 42) {
            subtotalString = " " + subtotalString;
        }

        mBixolonPrinter.lineFeed(2, false);
        mBixolonPrinter.printText("TOTAL INVOICE", left, bold, size, false);
        mBixolonPrinter.lineFeed(1, false);
        mBixolonPrinter.printText("   (GST INC):", left, bold, size, false);
        String totalString = "$" + String.format("%.2f", currentInvoice.getGrandTotal());
        while (totalString.length() < 29) {
            totalString = " " + totalString;
        }
        mBixolonPrinter.printText(totalString, left, bold, size, false);
        mBixolonPrinter.lineFeed(4, false);
        mBixolonPrinter.printText("WHOLEFOOD IS HEALTHY FOOD!", center, bold, size, false);
        mBixolonPrinter.lineFeed(7, false);

        mBixolonPrinter.cutPaper(false);
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            //Log.d(TAG, "mHandler.handleMessage(" + msg + ")");

            switch (msg.what) {
                case BixolonPrinter.MESSAGE_USB_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(getApplicationContext(), "No connected device", Toast.LENGTH_SHORT).show();
                    } else {
                        showUsbDialog(InvoicesTabActivity.this, (Set<UsbDevice>) msg.obj, mUsbReceiver);
                    }
                    return true;
            }
            return false;
        }
    });

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                mBixolonPrinter.connect();
                Toast.makeText(getApplicationContext(), "Found USB device", Toast.LENGTH_SHORT).show();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mBixolonPrinter.disconnect();
                Toast.makeText(getApplicationContext(), "USB device removed", Toast.LENGTH_SHORT).show();
                mConnected = false;
            }

        }
    };

    static void showUsbDialog(final Context context, final Set<UsbDevice> usbDevices, final BroadcastReceiver usbReceiver) {
        final String[] items = new String[usbDevices.size()];
        int index = 0;
        for (UsbDevice device : usbDevices) {
            items[index++] = "Device name: " + device.getDeviceName() + ", Product ID: " + device.getProductId() + ", Device ID: " + device.getDeviceId();
        }

        new AlertDialog.Builder(context).setTitle("Connected USB printers")
                .setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mBixolonPrinter.connect((UsbDevice) usbDevices.toArray()[which]);

                        // listen for new devices
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                        context.registerReceiver(usbReceiver, filter);
                    }
                }).show();
    }

    private String getContactNickname(int contactId) {
        String nickname;

        if (contactId <= 0) {
            return "";
        }

        try {
            Cursor cur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data.DATA1}, ContactsContract.Data.CONTACT_ID + " = " + contactId, null, null);

            int nicknameIndex = cur.getColumnIndex(ContactsContract.Data.DATA1);

            if (cur.moveToFirst()) {
                nickname = cur.getString(nicknameIndex);
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        return nickname;
    }

    private String[] getContactAddress(int contactId) {
        Uri postal_uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
        Cursor postal_cursor  = getContentResolver().query(postal_uri,null,  ContactsContract.Data.CONTACT_ID + "="+contactId, null,null);
        String streetCityArray[] = new String[2];
        String street = "";
        String city = "";
        while(postal_cursor.moveToNext())
        {
            street = postal_cursor.getString(postal_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            city = postal_cursor.getString(postal_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
        }
        postal_cursor.close();

        streetCityArray[0] = street;
        streetCityArray[1] = city;

        return streetCityArray;
    }

    private String formatInvoiceForReceipt(int invoiceId){
        String outputString = String.valueOf(invoiceId);

        while(outputString.length() < 6){
            outputString = "0" + outputString;
        }

        return "A" + outputString;

    }

    private void printLogo() {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.logo);
        Bitmap bitmap = drawable.getBitmap();

        int mAlignment = BixolonPrinter.ALIGNMENT_CENTER;

        int width = 0;

        mBixolonPrinter.printBitmap(bitmap, mAlignment, width, 25, false, false, true);
    }
}
