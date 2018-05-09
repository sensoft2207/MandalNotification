package com.android.mandalchat.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by mxicoders on 11/9/17.
 */

public class MyButtonBlack extends Button {

    public MyButtonBlack(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyButtonBlack(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyButtonBlack(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/MONTSERRAT-LIGHT.OTF");
        setTypeface(tf, 1);

    }

}
