/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Components;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class EmptyTextProgressView extends FrameLayout {

    private TextView textView;
    private RadialProgressView progressBar;
    private boolean inLayout;
    private boolean showAtCenter;

    public EmptyTextProgressView(Context context) {
        super(context);

        progressBar = new RadialProgressView(context);
        progressBar.setVisibility(INVISIBLE);
        addView(progressBar, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setTypeface(AndroidUtilities.getTypeface(null));
        textView.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(INVISIBLE);
        textView.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), 0);
        textView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void showProgress() {
        textView.setVisibility(INVISIBLE);
        progressBar.setVisibility(VISIBLE);
    }

    public void showTextView() {
        textView.setVisibility(VISIBLE);
        progressBar.setVisibility(INVISIBLE);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setProgressBarColor(int color) {
        progressBar.setProgressColor(color);
    }

    public void setTextSize(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public void setShowAtCenter(boolean value) {
        showAtCenter = value;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        inLayout = true;
        int width = r - l;
        int height = b - t;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            int x = (width - child.getMeasuredWidth()) / 2;
            int y;
            if (showAtCenter) {
                y = (height / 2 - child.getMeasuredHeight()) / 2;
            } else {
                y = (height - child.getMeasuredHeight()) / 2;
            }
            child.layout(x, y, x + child.getMeasuredWidth(), y + child.getMeasuredHeight());
        }
        inLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!inLayout) {
            super.requestLayout();
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
