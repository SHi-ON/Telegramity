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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

public class BotKeyboardView extends LinearLayout {

    private LinearLayout container;
    private TLRPC.TL_replyKeyboardMarkup botButtons;
    private BotKeyboardViewDelegate delegate;
    private int panelHeight;
    private boolean isFullSize;
    private int buttonHeight;
    private ArrayList<TextView> buttonViews = new ArrayList<>();
    private ScrollView scrollView;

    public interface BotKeyboardViewDelegate {
        void didPressedButton(TLRPC.KeyboardButton button);
    }

    public BotKeyboardView(Context context) {
        super(context);

        setOrientation(VERTICAL);

        scrollView = new ScrollView(context);
        addView(scrollView);
        container = new LinearLayout(context);
        container.setOrientation(VERTICAL);
        scrollView.addView(container);
        AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_chat_emojiPanelBackground));

        setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
    }

    public void setDelegate(BotKeyboardViewDelegate botKeyboardViewDelegate) {
        delegate = botKeyboardViewDelegate;
    }

    public void setPanelHeight(int height) {
        panelHeight = height;
        if (isFullSize && botButtons != null && botButtons.rows.size() != 0) {
            buttonHeight = !isFullSize ? 42 : (int) Math.max(42, (panelHeight - AndroidUtilities.dp(30) - (botButtons.rows.size() - 1) * AndroidUtilities.dp(10)) / botButtons.rows.size() / AndroidUtilities.density);
            int count = container.getChildCount();
            int newHeight = AndroidUtilities.dp(buttonHeight);
            for (int a = 0; a < count; a++) {
                View v = container.getChildAt(a);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                if (layoutParams.height != newHeight) {
                    layoutParams.height = newHeight;
                    v.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public void invalidateViews() {
        for (int a = 0; a < buttonViews.size(); a++) {
            buttonViews.get(a).invalidate();
        }
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public void setButtons(TLRPC.TL_replyKeyboardMarkup buttons) {
        botButtons = buttons;
        container.removeAllViews();
        buttonViews.clear();
        scrollView.scrollTo(0, 0);

        if (buttons != null && botButtons.rows.size() != 0) {
            isFullSize = !buttons.resize;
            buttonHeight = !isFullSize ? 42 : (int) Math.max(42, (panelHeight - AndroidUtilities.dp(30) - (botButtons.rows.size() - 1) * AndroidUtilities.dp(10)) / botButtons.rows.size() / AndroidUtilities.density);
            for (int a = 0; a < buttons.rows.size(); a++) {
                TLRPC.TL_keyboardButtonRow row = buttons.rows.get(a);

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, buttonHeight, 15, a == 0 ? 15 : 10, 15, a == buttons.rows.size() - 1 ? 15 : 0));

                float weight = 1.0f / row.buttons.size();
                for (int b = 0; b < row.buttons.size(); b++) {
                    TLRPC.KeyboardButton button = row.buttons.get(b);
                    TextView textView = new TextView(getContext());
                    textView.setTag(button);
                    textView.setTextColor(Theme.getColor(Theme.key_chat_botKeyboardButtonText));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_chat_botKeyboardButtonBackground), Theme.getColor(Theme.key_chat_botKeyboardButtonBackgroundPressed)));
                    textView.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                    textView.setText(Emoji.replaceEmoji(button.text, textView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16), false));
                    textView.setTypeface(AndroidUtilities.getTypeface(null));
                    layout.addView(textView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, weight, 0, 0, b != row.buttons.size() - 1 ? 10 : 0, 0));
                    textView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delegate.didPressedButton((TLRPC.KeyboardButton) v.getTag());
                        }
                    });
                    buttonViews.add(textView);
                }
            }
        }
    }

    public int getKeyboardHeight() {
        return isFullSize ? panelHeight : botButtons.rows.size() * AndroidUtilities.dp(buttonHeight) + AndroidUtilities.dp(30) + (botButtons.rows.size() - 1) * AndroidUtilities.dp(10);
    }
}
