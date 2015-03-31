package com.hardieboysorder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
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
import com.hardieboysorder.model.Item;
import com.hardieboysorder.widget.ItemButton;
import com.hardieboysorder.widget.NumberButton;

import java.util.ArrayList;
import java.util.Date;

public class InvoicesTabActivity extends Activity {

    HardieboysOrderDB db;
    RelativeLayout itemButtonLayout;
    LinearLayout numberButtonLayout;
    ItemButton pressedItemButton;
    Invoice currentInvoice;
    TextView invoiceIDTextView, contactTextView;
    ImageButton backImagebutton, forwardImageButton, contactImageButton, discountImageButton;
    Button printButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoices_tab_activity);

        db = new HardieboysOrderDB(this);
        //db.addTestData();

        loadInvoiceWidgets();
        loadNumberButtons();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadItemButtons();
        loadMostRecentInvoice();
    }

    private void loadInvoiceWidgets(){
        backImagebutton = (ImageButton)findViewById(R.id.backImageButton);
        invoiceIDTextView = (TextView)findViewById(R.id.invoiceIDTextView);
        forwardImageButton = (ImageButton)findViewById(R.id.forwardImageButton);
        contactImageButton = (ImageButton)findViewById(R.id.contactImageButton);
        contactTextView = (TextView)findViewById(R.id.contactTextView);
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
                            } else {
                                pressedItemButton = selectedItemButton;
                            }
                        } else {
                            //Assign as selected item button and then check if number button has also been
                            //selected. Add new invoice item if both buttons have been selected.
                            pressedItemButton = selectedItemButton;
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
            numberButtonLayout.addView(new NumberButton(this, i));
        }
        numberButtonLayout.addView(new NumberButton(this, "*"));
    }

    private void loadMostRecentInvoice(){
        currentInvoice = db.getMostRecentInvoice();

        if(currentInvoice == null){
            currentInvoice = new Invoice(-1, null, 0, new Date());
            db.addInvoice(currentInvoice);
        }

        invoiceIDTextView.setText("#" + currentInvoice.getInvoiceID());

    }

    private void loadInvoiceItems(int invoiceID){

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

}
