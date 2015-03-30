package com.hardieboysorder.widget;

import android.content.Context;
import android.widget.Button;

public class NumberButton extends Button {

    private int number;

    public NumberButton(Context context){
        super(context);
        this.setWidth(180);
        this.setHeight(300);
        this.setTextSize(30);
    }

    public NumberButton(Context context, int number){
        this(context);
        this.number = number;
        this.setText(number + "");
    }

    public NumberButton(Context context, String text) {
        this(context);
        this.setText(text);
    }

}
