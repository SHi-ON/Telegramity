/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Components;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import org.telegram.messenger.AndroidUtilities;

public class TypefaceSpan extends MetricAffectingSpan {

    private Typeface typeface;
    private int textSize;
    private int color;

    public TypefaceSpan(Typeface tf) {
        typeface = tf;
    }

    public TypefaceSpan(Typeface tf, int size) {
        typeface = tf;
        textSize = size;
    }

    public TypefaceSpan(Typeface tf, int size, int textColor) {
        typeface = tf;
        textSize = size;
        color = textColor;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public boolean isBold() {
        return typeface == AndroidUtilities.getTypeface("fonts/IRANSansMobileBold.ttf");
    }

    public boolean isItalic() {
        return typeface == AndroidUtilities.getTypeface("fonts/ritalic.ttf");
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        if (typeface != null) {
            p.setTypeface(typeface);
        }
        if (textSize != 0) {
            p.setTextSize(textSize);
        }
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (typeface != null) {
            tp.setTypeface(typeface);
        }
        if (textSize != 0) {
            tp.setTextSize(textSize);
        }
        if (color != 0) {
            tp.setColor(color);
        }
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
