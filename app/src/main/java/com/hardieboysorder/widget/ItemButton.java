package com.hardieboysorder.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hardieboysorder.R;
import com.hardieboysorder.model.Item;

public class ItemButton extends Button{
    private Item item;

    public ItemButton(Context context){
        super(context);
    }

    public ItemButton(Context context, int id){
        this(context);
        this.setId(id);
        this.setWidth(400);
        this.setHeight(360);
    }

    public ItemButton(Context context, Item item, int id){
        this(context, id);
        this.item = item;

        if(item.getIcon() != null){
            this.setBackground(new BitmapDrawable(getResources(), getImage(item.getIcon())));
        }else{
            this.setText(item.getCode());
        }
    }

    public Item getItem(){
        return this.item;
    }

    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
