/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.GroupCreateCheckBox;
import org.telegram.ui.Components.LayoutHelper;

public class InviteUserCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private SimpleTextView nameTextView;
    private SimpleTextView statusTextView;
    private GroupCreateCheckBox checkBox;
    private AvatarDrawable avatarDrawable;
    private ContactsController.Contact currentContact;
    private CharSequence currentName;

    public InviteUserCell(Context context, boolean needCheck) {
        super(context);
        avatarDrawable = new AvatarDrawable();

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
        addView(avatarImageView, LayoutHelper.createFrame(50, 50, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 11, 11, LocaleController.isRTL ? 11 : 0, 0));

        nameTextView = new SimpleTextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTypeface(AndroidUtilities.getTypeface(null));
        nameTextView.setTextSize(17);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : 72, 14, LocaleController.isRTL ? 72 : 28, 0));

        statusTextView = new SimpleTextView(context);
        statusTextView.setTextSize(16);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : 72, 39, LocaleController.isRTL ? 72 : 28, 0));

        if (needCheck) {
            checkBox = new GroupCreateCheckBox(context);
            checkBox.setVisibility(VISIBLE);
            addView(checkBox, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 41, 41, LocaleController.isRTL ? 41 : 0, 0));
        }
    }

    public void setUser(ContactsController.Contact contact, CharSequence name) {
        currentContact = contact;
        currentName = name;
        update(0);
    }

    public void setChecked(boolean checked, boolean animated) {
        checkBox.setChecked(checked, animated);
    }

    public ContactsController.Contact getContact() {
        return currentContact;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(72), MeasureSpec.EXACTLY));
    }

    public void recycle() {
        avatarImageView.getImageReceiver().cancelLoadImage();
    }

    public void update(int mask) {
        if (currentContact == null) {
            return;
        }
        String newName = null;

        avatarDrawable.setInfo(currentContact.contact_id, currentContact.first_name, currentContact.last_name, false);

        if (currentName != null) {
            nameTextView.setText(currentName, true);
        } else {
            nameTextView.setText(ContactsController.formatName(currentContact.first_name, currentContact.last_name));
        }

        statusTextView.setTag(Theme.key_groupcreate_offlineText);
        statusTextView.setTextColor(Theme.getColor(Theme.key_groupcreate_offlineText));
        if (currentContact.imported > 0) {
            statusTextView.setText(LocaleController.formatPluralString("TelegramContacts", currentContact.imported));
        } else {
            statusTextView.setText(currentContact.phones.get(0));
        }

        avatarImageView.setImageDrawable(avatarDrawable);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
