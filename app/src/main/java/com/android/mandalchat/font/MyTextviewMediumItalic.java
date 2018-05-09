package com.android.mandalchat.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mxi on 31/10/17.
 */

public class MyTextviewMediumItalic extends TextView {

    public MyTextviewMediumItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextviewMediumItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextviewMediumItalic(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font/MONTSERRAT-MEDIUMITALIC.TTF");
        setTypeface(tf, 1);

    }

}
