package com.hardieboysorder.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hardieboysorder.R;
import com.hardieboysorder.model.Item;

public class ItemButton extends ImageButton {
    private Item item;

    public ItemButton(Context context){
        super(context);
    }

    public ItemButton(Context context, Item item){
        super(context);

        this.item = item;
        if(item.getIcon() != null){
            this.setImageBitmap(getImage(item.getIcon()));
        }
    }

    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
