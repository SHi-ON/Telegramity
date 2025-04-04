/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class FeaturedStickerSetInfoCell extends FrameLayout {

    private TextView nameTextView;
    private TextView infoTextView;
    private TextView addButton;
    private TLRPC.StickerSetCovered set;
    private Drawable addDrawable;
    private Drawable delDrawable;

    private boolean drawProgress;
    private float progressAlpha;
    private RectF rect = new RectF();
    private long lastUpdateTime;
    private Paint botProgressPaint;
    private int angle;
    private boolean isInstalled;
    private boolean hasOnClick;

    Drawable drawable = new Drawable() {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        public void draw(Canvas canvas) {
            paint.setColor(Theme.getColor(Theme.key_featuredStickers_unread));
            canvas.drawCircle(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(4), paint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public int getIntrinsicWidth() {
            return AndroidUtilities.dp(12);
        }

        @Override
        public int getIntrinsicHeight() {
            return AndroidUtilities.dp(26);
        }
    };

    public FeaturedStickerSetInfoCell(Context context, int left) {
        super(context);

        delDrawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_delButton), Theme.getColor(Theme.key_featuredStickers_delButtonPressed));
        addDrawable = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed));

        botProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        botProgressPaint.setColor(Theme.getColor(Theme.key_featuredStickers_buttonProgress));
        botProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        botProgressPaint.setStyle(Paint.Style.STROKE);
        botProgressPaint.setStrokeWidth(AndroidUtilities.dp(2));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelTrendingTitle));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        nameTextView.setTypeface(AndroidUtilities.getTypeface(null));
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setSingleLine(true);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, left, 8, 100, 0));

        infoTextView = new TextView(context);
        infoTextView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelTrendingDescription));
        infoTextView.setTypeface(AndroidUtilities.getTypeface(null));
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        infoTextView.setEllipsize(TextUtils.TruncateAt.END);
        infoTextView.setSingleLine(true);
        addView(infoTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, left, 30, 100, 0));

        addButton = new TextView(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (drawProgress || !drawProgress && progressAlpha != 0) {
                    botProgressPaint.setAlpha(Math.min(255, (int) (progressAlpha * 255)));
                    int x = getMeasuredWidth() - AndroidUtilities.dp(11);
                    rect.set(x, AndroidUtilities.dp(3), x + AndroidUtilities.dp(8), AndroidUtilities.dp(8 + 3));
                    canvas.drawArc(rect, angle, 220, false, botProgressPaint);
                    invalidate((int) rect.left - AndroidUtilities.dp(2), (int) rect.top - AndroidUtilities.dp(2), (int) rect.right + AndroidUtilities.dp(2), (int) rect.bottom + AndroidUtilities.dp(2));
                    long newTime = System.currentTimeMillis();
                    if (Math.abs(lastUpdateTime - System.currentTimeMillis()) < 1000) {
                        long delta = (newTime - lastUpdateTime);
                        float dt = 360 * delta / 2000.0f;
                        angle += dt;
                        angle -= 360 * (angle / 360);
                        if (drawProgress) {
                            if (progressAlpha < 1.0f) {
                                progressAlpha += delta / 200.0f;
                                if (progressAlpha > 1.0f) {
                                    progressAlpha = 1.0f;
                                }
                            }
                        } else {
                            if (progressAlpha > 0.0f) {
                                progressAlpha -= delta / 200.0f;
                                if (progressAlpha < 0.0f) {
                                    progressAlpha = 0.0f;
                                }
                            }
                        }
                    }
                    lastUpdateTime = newTime;
                    invalidate();
                }
            }
        };
        addButton.setGravity(Gravity.CENTER);
        addButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        addButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        addButton.setTypeface(AndroidUtilities.getTypeface(null));
        addView(addButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 28, Gravity.TOP | Gravity.RIGHT, 0, 16, 14, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
    }

    public void setAddOnClickListener(OnClickListener onClickListener) {
        hasOnClick = true;
        addButton.setOnClickListener(onClickListener);
    }

    public void setStickerSet(TLRPC.StickerSetCovered stickerSet, boolean unread) {
        lastUpdateTime = System.currentTimeMillis();
        nameTextView.setText(stickerSet.set.title);
        infoTextView.setText(LocaleController.formatPluralString("Stickers", stickerSet.set.count));
        if (unread) {
            nameTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            nameTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        if (hasOnClick) {
            addButton.setVisibility(VISIBLE);
            if (isInstalled = StickersQuery.isStickerPackInstalled(stickerSet.set.id)) {
                addButton.setBackgroundDrawable(delDrawable);
                addButton.setText(LocaleController.getString("StickersRemove", R.string.StickersRemove).toUpperCase());
            } else {
                addButton.setBackgroundDrawable(addDrawable);
                addButton.setText(LocaleController.getString("Add", R.string.Add).toUpperCase());
            }
            addButton.setPadding(AndroidUtilities.dp(17), 0, AndroidUtilities.dp(17), 0);
        } else {
            addButton.setVisibility(GONE);
        }

        set = stickerSet;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setDrawProgress(boolean value) {
        drawProgress = value;
        lastUpdateTime = System.currentTimeMillis();
        addButton.invalidate();
    }

    public TLRPC.StickerSetCovered getStickerSet() {
        return set;
    }
}
