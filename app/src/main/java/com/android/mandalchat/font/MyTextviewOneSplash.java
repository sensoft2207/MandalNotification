package com.android.mandalchat.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mxicoders on 11/9/17.
 */

public class MyTextviewOneSplash extends TextView {

    public MyTextviewOneSplash(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextviewOneSplash(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextviewOneSplash(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/FREDOKAONE-REGULAR.TTF");
        setTypeface(tf, 1);

    }

}
