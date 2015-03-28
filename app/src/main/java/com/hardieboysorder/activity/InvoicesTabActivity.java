package com.hardieboysorder.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hardieboysorder.R;
import com.hardieboysorder.db.HardieboysOrderDB;
import com.hardieboysorder.model.Item;
import com.hardieboysorder.widget.ItemButton;

import java.util.ArrayList;

public class InvoicesTabActivity extends Activity {

    HardieboysOrderDB db;
    RelativeLayout itemButtonLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoices_tab_activity);

        db = new HardieboysOrderDB(this);
        //db.addTestData();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Reload the item buttons on each "onResume" in case item details where changed on the
        //catalog tab.
        loadItemButtons();
    }

    private void loadItemButtons() {
        itemButtonLayout = (RelativeLayout) findViewById(R.id.itemButtonLayout);
        itemButtonLayout.removeAllViews();
        ArrayList<Item> items = db.getAllActiveItems();
        int colCount = 0;
        int rowCount = 0;

        //# of columns
        int colSpan = 4;

        for (int i = 0; i < items.size(); i++) {
            int id = 1000 + i;
            ItemButton newItemButton = new ItemButton(this, items.get(i));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 20, 0, 0);
            newItemButton.setId(id);
            newItemButton.setMaxWidth(400);
            newItemButton.setMaxHeight(360);
            newItemButton.setAdjustViewBounds(true);

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
}
