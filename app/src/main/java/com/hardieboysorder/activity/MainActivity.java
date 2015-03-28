package com.hardieboysorder.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import com.hardieboysorder.R;

public class MainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        TabHost tabHost = getTabHost();

        TabSpec invoicesTabSpec = tabHost.newTabSpec("Invoices Tab");
        TabSpec catalogTabSpec = tabHost.newTabSpec("Catalog Tab");
        TabSpec optionsTabSpec = tabHost.newTabSpec("Options Tab");

        invoicesTabSpec.setIndicator("Invoices");
        invoicesTabSpec.setContent(new Intent(this, InvoicesTabActivity.class));

        catalogTabSpec.setIndicator("Catalog");
        catalogTabSpec.setContent(new Intent(this, CatalogTabActivity.class));

        optionsTabSpec.setIndicator("Options");
        optionsTabSpec.setContent(new Intent(this, OptionsTabActivity.class));

        tabHost.addTab(invoicesTabSpec);
        tabHost.addTab(catalogTabSpec);
        tabHost.addTab(optionsTabSpec);

        TextView invoicesTabText = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        invoicesTabText.setTextSize(25);

        TextView catalogTabText = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        catalogTabText.setTextSize(25);

        TextView optionsTabText = (TextView) tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        optionsTabText.setTextSize(25);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
