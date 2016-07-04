package com.hardieboysorder.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hardieboysorder.R;
import com.hardieboysorder.model.InvoiceItem;

public class InvoiceItemAdapter extends BaseAdapter {

    private ArrayList<InvoiceItem> _data;
    Context _c;

    public InvoiceItemAdapter(Context c){
        _c = c;
        _data = new ArrayList<InvoiceItem>();
    }

    public InvoiceItemAdapter (ArrayList<InvoiceItem> data, Context c){
        _data = data;
        _c = c;
    }
    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return _data.size();
    }

    public void add(InvoiceItem invoiceItem){
        _data.add(invoiceItem);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public InvoiceItem getItem(int position) {
        // TODO Auto-generated method stub
        return _data.get(position);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = convertView;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(_c.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.invoice_item_row, null);

            TextView quantity = (TextView)v.findViewById(R.id.quantity);
            TextView description = (TextView)v.findViewById(R.id.description);
            TextView total = (TextView)v.findViewById(R.id.total);

            InvoiceItem invoiceItem = _data.get(position);

            if(invoiceItem.getInvoiceItemID() == -22){
                quantity.setText("");
                description.setText(invoiceItem.getDiscount() + "% Discount");
                total.setText("-$" + String.format("%.2f", invoiceItem.getDiscountAmount()));
            }else{
                quantity.setText(invoiceItem.getQuantity() + "");
                description.setText(invoiceItem.getItem().getDescription());
                total.setText("$" + String.format("%.2f", invoiceItem.getTotal()));
            }

        }

        return v;
    }

}