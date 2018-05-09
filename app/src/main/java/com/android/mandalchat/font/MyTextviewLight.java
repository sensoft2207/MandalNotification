package com.android.mandalchat.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mxi on 31/10/17.
 */

public class MyTextviewLight extends TextView {

    public MyTextviewLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextviewLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextviewLight(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/MONTSERRAT-LIGHT.OTF");
        setTypeface(tf, 1);

    }

}
