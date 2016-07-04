package com.hardieboysorder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hardieboysorder.R;
import com.hardieboysorder.db.HardieboysOrderDB;
import com.hardieboysorder.model.Item;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CatalogTabActivity extends Activity {

    private HardieboysOrderDB db;

    private ArrayAdapter<Item> itemListAdapter;
    private ListView itemListView;
    private TextView descriptionTextView, codeTextView, priceTextView, iconTextView;
    private TextView itemDescriptionTextView, itemCodeTextView, itemPriceTextView;
    private ImageButton iconImageButton, editItemIconImageButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_tab_activity);

        db = new HardieboysOrderDB(this);

        loadWidgets();

        makeWidgetsVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadAllItems();
    }

    private void loadWidgets(){
        itemListView = (ListView) findViewById(R.id.itemListView);
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //If user clicked on last item, it was the "+ Add New Item" row
                if(position == parent.getCount() - 1){
                    showItemEditDialog(new Item(), "New Item");
                }else{
                    Item selectedItem = itemListAdapter.getItem(position);
                    showItemInformation(selectedItem);
                    makeWidgetsVisible(true);
                }
            }
        });
        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Ignore if it's the "+ Add New Item" row
                if(position != parent.getCount() - 1){
                    final Item selectedItem = itemListAdapter.getItem(position);
                    showItemInformation(selectedItem);
                    showEditDeleteCancelDialog(selectedItem);
                }

                return false;
            }
        });

        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        codeTextView = (TextView) findViewById(R.id.codeTextView);
        priceTextView = (TextView) findViewById(R.id.priceTextView);
        iconTextView = (TextView) findViewById(R.id.iconTextView);
        itemDescriptionTextView = (TextView) findViewById(R.id.itemDescriptionTextView);
        itemCodeTextView = (TextView) findViewById(R.id.itemCodeTextView);
        itemPriceTextView = (TextView) findViewById(R.id.itemPriceTextView);
        iconImageButton = (ImageButton) findViewById(R.id.iconImageButton);
    }

    //Get all the active items and load them into the itemListView
    private void loadAllItems(){
        itemListAdapter = new ArrayAdapter<Item>(this, R.layout.listview_row, db.getAllActiveItems());
        itemListAdapter.add(new Item(null, "+ Add New Item", 0, null, 0, 0));
        itemListView.setAdapter(itemListAdapter);
    }

    //Delete item from all necessary places and update itemListView
    private void deleteItem(Item item){

        //We also need to decrease by 1 every item's icon order that is greater than the deleted
        //items so that they will still fit with the new amount of item buttons on the invoices tab.
        if(item.getIconOrder() > 0) {
            db.decreaseIconOrders(item);
        }

        //In order to maintain historical item data, we need to first check if the item
        //is used on any invoice/invoiceItems.  If it is not, then we can straight up delete it, but
        //if it is used on at least one invoice we have to flip the IsActive switch to false.
        if(db.isItemUsed(item)){
            item.setIsActive(0);
            db.updateItem(item);
        }else{
            db.deleteItem(item);
        }

        loadAllItems();
        makeWidgetsVisible(false);
    }

    //Delete item from all necessary places and update itemListView
    private void addItem(Item item){
        db.addItem(item);
        itemListAdapter.add(item);
        itemListAdapter.notifyDataSetChanged();
        showItemInformation(item);
        makeWidgetsVisible(true);
    }

    private void updateBusinessSignificantItem(Item item){
        Item newItem = new Item();

        //In order to maintain historical item data, we need to first check if the item
        //is used on any invoice/invoiceItems.  If it is not, then we can straight up update it, but
        //if it is used on at least one invoice we have to flip the IsActive switch to false and create
        // a new item.
        if(db.isItemUsed(item)){
            item.setIsActive(0);
            db.updateItem(item);

            //Recreate with everything except ID so a new one will be generated
            newItem.setDescription(item.getDescription());
            newItem.setCode(item.getCode());
            newItem.setPrice(item.getPrice());
            newItem.setIcon(item.getIcon());
            newItem.setIconOrder(item.getIconOrder());
            newItem.setIsActive(1);

            db.addItem(newItem);

            itemListAdapter.remove(item);
            itemListAdapter.add(newItem);
            showItemInformation(newItem);
        }else{
            db.updateItem(item);
            showItemInformation(item);
        }
    }

    //Load text views with selected item data
    private void showItemInformation(Item item){
        itemDescriptionTextView.setText(item.getDescription());
        itemCodeTextView.setText(item.getCode());
        itemPriceTextView.setText("$" + String.format("%.2f", item.getPrice()));
        if(item.getIcon() != null){
            iconImageButton.setImageBitmap(getImage(item.getIcon()));
        }else{
            iconImageButton.setImageBitmap(null);
        }
    }

    //Convert from bitmap to byte[] for storage in DB
    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    //Convert from byte[] to bitmap to show as image
    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    private void showEditDeleteCancelDialog(Item item){
        final Item selectedItem = item;

        AlertDialog.Builder editDeleteCancelDialogBuilder = new AlertDialog.Builder(this);
        editDeleteCancelDialogBuilder
                .setCancelable(false)
                .setTitle(selectedItem.getDescription())
                .setMessage("What would you like to do?")
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Do nothing, close dialog
                            }
                        })
                .setNegativeButton("Edit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showItemEditDialog(selectedItem, "Edit Item");
                            }
                        })
                .setNeutralButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteItem(selectedItem);
                            }
                        })
                .show();
    }

    private void showItemEditDialog(final Item originalItem, String title){
        final String dialogTitle = title;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View itemEditDialogView = layoutInflater.inflate(R.layout.item_edit_dialog, null);

        final EditText descriptionEditText = (EditText) itemEditDialogView.findViewById(R.id.editItemDescriptionEditView);
        descriptionEditText.setText(originalItem.getDescription());

        final EditText codeEditText = (EditText) itemEditDialogView.findViewById(R.id.editItemCodeEditText);
        codeEditText.setText(originalItem.getCode());

        final EditText priceEditText = (EditText) itemEditDialogView.findViewById(R.id.editItemPriceEditText);
        priceEditText.setText("$" + String.format("%.2f", originalItem.getPrice()));

        editItemIconImageButton = (ImageButton) itemEditDialogView.findViewById(R.id.editItemIconImageButton);
        if(originalItem.getIcon() != null){
            editItemIconImageButton.setImageBitmap(getImage(originalItem.getIcon()));
        }
        editItemIconImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile(v);
            }
        });

        final AlertDialog itemEditDialog = new AlertDialog.Builder(this)
                .setView(itemEditDialogView)
                .setCancelable(true)
                .setTitle(title)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        itemEditDialog.show();

        itemEditDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Item newItem = new Item();

                if (dialogTitle.equals("Edit Item")) {
                    if (validateItemInfo(descriptionEditText.getText().toString(), codeEditText.getText().toString(), priceEditText.getText().toString())) {
                        newItem.setItemID(originalItem.getItemID());

                        newItem.setDescription(descriptionEditText.getText().toString());
                        newItem.setCode(codeEditText.getText().toString());
                        newItem.setPrice(FormatPriceString(priceEditText.getText().toString()));
                        if(editItemIconImageButton.getDrawable() != null) {
                            newItem.setIcon(getBytes(((BitmapDrawable) editItemIconImageButton.getDrawable()).getBitmap()));
                        }
                        newItem.setIconOrder(originalItem.getIconOrder());
                        newItem.setIsActive(1);

                        //One of the business significant properties changed
                        if (!originalItem.getDescription().equals(newItem.getDescription()) || !originalItem.getCode().equals(newItem.getCode()) ||
                                originalItem.getPrice() != newItem.getPrice()) {
                            updateBusinessSignificantItem(newItem);
                        } else {
                            db.updateItem(newItem);
                            showItemInformation(newItem);
                        }

                        loadAllItems();
                        itemEditDialog.dismiss();
                    }
                } else {
                    if (validateItemInfo(descriptionEditText.getText().toString(), codeEditText.getText().toString(), priceEditText.getText().toString())) {
                        newItem.setDescription(descriptionEditText.getText().toString());
                        newItem.setCode(codeEditText.getText().toString());
                        newItem.setPrice(FormatPriceString(priceEditText.getText().toString()));
                        if (editItemIconImageButton.getDrawable() != null) {
                            newItem.setIcon(getBytes(((BitmapDrawable) editItemIconImageButton.getDrawable()).getBitmap()));
                        }
                        newItem.setIconOrder(0);
                        newItem.setIsActive(1);

                        addItem(newItem);
                        loadAllItems();
                        itemEditDialog.dismiss();
                    }
                }
            }
        });
        itemEditDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                itemEditDialog.dismiss();
            }
        });
    }

    private void openFile(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 41);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == 41 && resultCode == Activity.RESULT_OK) {

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
            }
            Bitmap bmp = null;
            try{
                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                editItemIconImageButton.setImageBitmap(bmp);
            }catch(FileNotFoundException e) {

            }catch(IOException e){

            }
        }
    }

    private void makeWidgetsVisible(boolean isVisible){
        if(!isVisible){
            descriptionTextView.setVisibility(View.GONE);
            itemDescriptionTextView.setVisibility(View.GONE);
            codeTextView.setVisibility(View.GONE);
            itemCodeTextView.setVisibility(View.GONE);
            priceTextView.setVisibility(View.GONE);
            itemPriceTextView.setVisibility(View.GONE);
            iconTextView.setVisibility(View.GONE);
            iconImageButton.setVisibility(View.GONE);
        }else{
            descriptionTextView.setVisibility(View.VISIBLE);
            itemDescriptionTextView.setVisibility(View.VISIBLE);
            codeTextView.setVisibility(View.VISIBLE);
            itemCodeTextView.setVisibility(View.VISIBLE);
            priceTextView.setVisibility(View.VISIBLE);
            itemPriceTextView.setVisibility(View.VISIBLE);
            iconTextView.setVisibility(View.VISIBLE);
            iconImageButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateItemInfo(String description, String code, String price){
        AlertDialog.Builder validationErrorDialog = new AlertDialog.Builder(this);
        validationErrorDialog.setTitle("Validation Error");
        validationErrorDialog.setCancelable(false);
        validationErrorDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing, close dialog
                    }
                });

        if(description.trim().equals("")){
            validationErrorDialog.setMessage("Description cannot be blank.");
            validationErrorDialog.show();
            return false;
        }else if(code.trim().equals("")){
            validationErrorDialog.setMessage("Code cannot be blank.");
            validationErrorDialog.show();
            return false;
        }else if(price.matches(".*\\d.*")){
            // contains a number
        } else{
            validationErrorDialog.setMessage("Price must contain at least one number.");
            validationErrorDialog.show();
            return false;
        }

        return true;
    }

    private double FormatPriceString(String priceString){
        return Double.parseDouble(priceString.replace("$", ""));
    }
}
