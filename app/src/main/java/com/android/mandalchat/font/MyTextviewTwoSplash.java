package com.android.mandalchat.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mxi on 31/10/17.
 */

public class MyTextviewTwoSplash extends TextView {

    public MyTextviewTwoSplash(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextviewTwoSplash(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextviewTwoSplash(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/FredokaOne-Regular.ttf");
        setTypeface(tf, 1);

    }

}
