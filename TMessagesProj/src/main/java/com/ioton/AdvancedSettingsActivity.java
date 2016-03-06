/*
 * This is the source code of Telegramity for Android v. 3.x.x.
 *
 * Copyright Shayan Amani, 2015.
 */

package com.ioton;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ChangeNameActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.PasscodeActivity;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;
import java.util.Locale;

public class AdvancedSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AnimatorSetProxy writeButtonAnimation;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private View extraHeightView;
    private View shadowView;

    private int extraHeight;
    private boolean needRestart;

    private int overscrollRow;
    private int emptyRow;
    /*private int shareAndForwardSectionRow;
    private int quickShareRow;
    private int forwardNoCaptionRow;
    private int shareAndForwardSectionBottomRow;*/
    private int slySectionRow;
    private int specterModeRow;
    private int hiddenTypingRow;
    private int noNumberRow;
    private int slyDetailRow;
    private int slySectionBottomRow;
    private int customizationsSectionRow;
    private int actionBarBackgroundColorRow;
    private int drawerHeaderColorRow;
    private int profileBackgroundColorRow;
    private int resetDefaultRow;
    private int customizationsDetailRow;
    private int customizationsSectionBottomRow;
    private int premiumSecuritySectionRow;
    private int passcodeRow;
    private int premiumSecurityDetailRow;
    private int premiumSecuritySectionBottomRow;
    private int textDescriptionRow;
    private int rowCount;

    private final static int edit_name = 1;
    private final static int logout = 2;

    @Override
    public boolean onFragmentCreate() {

        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TLRPC.TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo) response;
                            ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                            TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 1000);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photo.photo.id;
                            if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            if (bigSize != null) {
                                user.photo.photo_big = bigSize.location;
                            } else if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            MessagesStorage.getInstance().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    UserConfig.saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);

        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;
        /*shareAndForwardSectionRow = rowCount++;
        quickShareRow = rowCount++;
        forwardNoCaptionRow = rowCount++;
        shareAndForwardSectionBottomRow = rowCount++;*/
        slySectionRow = rowCount++;
        specterModeRow = rowCount++;
        hiddenTypingRow = rowCount++;
        noNumberRow = rowCount++;
        slyDetailRow = rowCount++;
        slySectionBottomRow = rowCount++;
        customizationsSectionRow = rowCount++;
        actionBarBackgroundColorRow = rowCount++;
        drawerHeaderColorRow = rowCount++;
        profileBackgroundColorRow = rowCount++;
        resetDefaultRow = rowCount++;
        customizationsDetailRow = rowCount++;
        customizationsSectionBottomRow = rowCount++;
        premiumSecuritySectionRow = rowCount++;
        passcodeRow = rowCount++;
        premiumSecurityDetailRow = rowCount++;
        premiumSecuritySectionBottomRow = rowCount++;
        textDescriptionRow = rowCount++;

        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        avatarUpdater.clear();

        if (needRestart) {
            TelegramityUtilities.restartTelegramity();
        }
    }

    @Override
    public View createView(final Context context) {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
        final int instanceOfProfileBackgroundColor = preferences.getInt("profileBackgroundColor", ApplicationLoader.PBG_COLOR);
        needRestart = false;
        actionBar.setBackgroundColor(instanceOfProfileBackgroundColor);
        actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (id == edit_name) {
                    presentFragment(new ChangeNameActivity());
                } else if (id == logout) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSureLogout", R.string.AreYouSureLogout));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MessagesController.getInstance().performLogout(true);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_other);
        item.addSubItem(edit_name, LocaleController.getString("EditName", R.string.EditName), 0);
        item.addSubItem(logout, LocaleController.getString("LogOut", R.string.LogOut), 0);

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) {
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child == listView) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (parentLayout != null) {
                        int actionBarHeight = 0;
                        int childCount = getChildCount();
                        for (int a = 0; a < childCount; a++) {
                            View view = getChildAt(a);
                            if (view == child) {
                                continue;
                            }
                            if (view instanceof ActionBar && view.getVisibility() == VISIBLE) {
                                if (((ActionBar) view).getCastShadows()) {
                                    actionBarHeight = view.getMeasuredHeight();
                                }
                                break;
                            }
                        }
                        parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                    }
                    return result;
                } else {
                    return super.drawChild(canvas, child, drawingTime);
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(true);
        AndroidUtilities.setListViewEdgeEffectColor(listView, instanceOfProfileBackgroundColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                if (i == specterModeRow) {
                    boolean specter = preferences.getBoolean("specterMode", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("specterMode", !specter);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!specter);
                        if (!specter) {
                            onlineTextView.setText(LocaleController.getString("SpecterOnline", R.string.SpecterOnline));
                        } else {
                            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));
                        }
                    }
                    MessagesController.getInstance().reRunUpdateTimerProc();
                } else if (i == hiddenTypingRow) {
                    boolean typing = preferences.getBoolean("hiddenTyping", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hiddenTyping", !typing);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!typing);
                    }
                } else if (i == noNumberRow) {
                    boolean number = preferences.getBoolean("noNumber", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("noNumber", !number);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!number);
                    }
                } else if (i == actionBarBackgroundColorRow || i == profileBackgroundColorRow || i == drawerHeaderColorRow) {
                    if (getParentActivity() == null) {
                        return;
                    }

                    int initialActionBarBackgroundColor = preferences.getInt("actionBarBackgroundColor", ApplicationLoader.ABBG_COLOR);
                    int initialDrawerHeaderColor = preferences.getInt("drawerHeaderColor", ApplicationLoader.DH_COLOR);

                    LinearLayout linearLayout = new LinearLayout(getParentActivity());
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    final ColorPicker colorPicker = new ColorPicker(getParentActivity());
                    SVBar svBar = new SVBar(getParentActivity());
                    /*OpacityBar opacityBar = new OpacityBar(getParentActivity());
                    SaturationBar saturationBar = new SaturationBar(getParentActivity());
                    ValueBar valueBar = new ValueBar(getParentActivity());*/
                    linearLayout.addView(colorPicker, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(svBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
/*                    linearLayout.addView(opacityBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(saturationBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                    linearLayout.addView(valueBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));*/
                    colorPicker.addSVBar(svBar);
/*                    colorPicker.addOpacityBar(opacityBar);
                    colorPicker.addSaturationBar(saturationBar);
                    colorPicker.addValueBar(valueBar);*/
//                    ColorPickerDialogBuilder cPDB = ColorPickerDialogBuilder.with(context);

                    if (i == actionBarBackgroundColorRow) {
                        colorPicker.setOldCenterColor(initialActionBarBackgroundColor);
//                        cPDB.initialColor(initialActionBarBackgroundColor);
                    } else if (i == profileBackgroundColorRow) {
                        colorPicker.setOldCenterColor(instanceOfProfileBackgroundColor);
//                        cPDB.initialColor(instanceOfProfileBackgroundColor);
                    } else if (i == drawerHeaderColorRow) {
                        colorPicker.setOldCenterColor(initialDrawerHeaderColor);
//                        cPDB.initialColor(initialDrawerHeaderColor);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder
                            .setTitle(LocaleController.getString("ChooseColor", R.string.ChooseColor))
                            .setView(linearLayout)
                            .setPositiveButton(LocaleController.getString("ThemeDialogSetButton", R.string.ThemeDialogSetButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    if (i == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), colorPicker.getColor(), true);
                                        needRestart = true;
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == profileBackgroundColorRow) {
                                        editor.putInt("profileBackgroundColor", colorPicker.getColor());
                                        textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), colorPicker.getColor(), true);
                                    }
                                    editor.apply();
                                }
                            })
                            .setNeutralButton(LocaleController.getString("ThemeDialogCancelButton", R.string.ThemeDialogCancelButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    showDialog(builder.create());

                    /*cPDB
                            .setTitle(LocaleController.getString("ChooseColor", R.string.ChooseColor))
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .lightnessSliderOnly()
                            .density(14)
                            .setPositiveButton(LocaleController.getString("ThemeDialogSetButton", R.string.ThemeDialogSetButton), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    TextColorCell textCell = (TextColorCell) view;
                                    if (i == actionBarBackgroundColorRow) {
                                        editor.putInt("actionBarBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == drawerHeaderColorRow) {
                                        editor.putInt("drawerHeaderColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), selectedColor, true);
                                        toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                                    } else if (i == profileBackgroundColorRow) {
                                        editor.putInt("profileBackgroundColor", selectedColor);
                                        textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), selectedColor, true);
                                    }
                                    editor.apply();
//                                    listView.invalidateViews();
                                }
                            })
                            .setNegativeButton(LocaleController.getString("ThemeDialogCancelButton", R.string.ThemeDialogCancelButton), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();*/
                } else if (i == resetDefaultRow) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("resetDefaultMessage", R.string.resetDefaultMessage));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("actionBarBackgroundColor", ApplicationLoader.ABBG_COLOR);
                            editor.putInt("drawerHeaderColor", ApplicationLoader.DH_COLOR);
                            editor.putInt("profileBackgroundColor", ApplicationLoader.PBG_COLOR);
                            editor.apply();
                            needRestart = true;
                            toast(context, LocaleController.getString("ThemeChangeToastMessage", R.string.ThemeChangeToastMessage));
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == passcodeRow) {
                    if (UserConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                }
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        ViewProxy.setPivotY(extraHeightView, 0);
        extraHeightView.setBackgroundColor(instanceOfProfileBackgroundColor);
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        ViewProxy.setPivotX(avatarImage, 0);
        ViewProxy.setPivotY(avatarImage, 0);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, AdvancedSettingsActivity.this);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        ViewProxy.setPivotX(nameTextView, 0);
        ViewProxy.setPivotY(nameTextView, 0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(5));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        writeButton = new ImageView(context);
        writeButton.setBackgroundResource(R.drawable.floating_user_states);
        writeButton.setImageDrawable(new IconicsDrawable(context, FontAwesome.Icon.faw_camera).sizePx(40).color(0xff8e6455)); //Brown +2
        writeButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            writeButton.setStateListAnimator(animator);
            writeButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(writeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                CharSequence[] items;

                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                boolean fullMenu = false;
                if (user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty)) {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                    fullMenu = true;
                } else {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                final boolean full = fullMenu;
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            MessagesController.getInstance().deleteUserPhoto(null);
                        }
                    }
                });
                showDialog(builder.create());
            }
        });              // Camera icon on the top - End

        needLayout();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                object.user_id = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                object.scale = ViewProxy.getScaleX(avatarImage);
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
    }

    @Override
    public void willHidePhotoViewer() {
        avatarImage.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index) {
    }

    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void sendButtonPressed(int index) {
    }

    @Override
    public int getSelectedCount() {
        return 0;
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                ViewProxy.setTranslationY(extraHeightView, newTop);
            }
        }

        if (avatarImage != null) {
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            ViewProxy.setScaleY(extraHeightView, diff);
            ViewProxy.setTranslationY(shadowView, newTop + extraHeight);

            if (Build.VERSION.SDK_INT < 11) {
                layoutParams = (FrameLayout.LayoutParams) writeButton.getLayoutParams();
                layoutParams.topMargin = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f);
                writeButton.setLayoutParams(layoutParams);
            } else {
                ViewProxy.setTranslationY(writeButton, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f));
            }

            final boolean setVisible = diff > 0.2f;
            boolean currentVisible = writeButton.getTag() == null;
            if (setVisible != currentVisible) {
                if (setVisible) {
                    writeButton.setTag(null);
                    writeButton.setVisibility(View.VISIBLE);
                } else {
                    writeButton.setTag(0);
                }
                if (writeButtonAnimation != null) {
                    AnimatorSetProxy old = writeButtonAnimation;
                    writeButtonAnimation = null;
                    old.cancel();
                }
                writeButtonAnimation = new AnimatorSetProxy();
                if (setVisible) {
                    writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 1.0f)
                    );
                } else {
                    writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 0.0f)
                    );
                }
                writeButtonAnimation.setDuration(150);
                writeButtonAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                            writeButton.clearAnimation();
                            writeButton.setVisibility(setVisible ? View.VISIBLE : View.GONE);
                            writeButtonAnimation = null;
                        }
                    }
                });
                writeButtonAnimation.start();
            }

            ViewProxy.setScaleX(avatarImage, (42 + 18 * diff) / 42.0f);
            ViewProxy.setScaleY(avatarImage, (42 + 18 * diff) / 42.0f);
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            ViewProxy.setTranslationX(avatarImage, -AndroidUtilities.dp(47) * diff);
            ViewProxy.setTranslationY(avatarImage, (float) Math.ceil(avatarY));
            ViewProxy.setTranslationX(nameTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(nameTextView, (float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            ViewProxy.setTranslationX(onlineTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(onlineTextView, (float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);
            ViewProxy.setScaleX(nameTextView, 1.0f + 0.12f * diff);
            ViewProxy.setScaleY(nameTextView, 1.0f + 0.12f * diff);
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(0xff5c98cd);
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
            boolean specter = preferences.getBoolean("specterMode", false);
            if (specter) {
                onlineTextView.setText(LocaleController.getString("SpecterOnline", R.string.SpecterOnline));
            } else {
                onlineTextView.setText(LocaleController.getString("Online", R.string.Online));
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }

    private void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return /*i == quickShareRow || i == forwardNoCaptionRow ||*/ i == specterModeRow || i == hiddenTypingRow || i == noNumberRow || i == actionBarBackgroundColorRow || i == drawerHeaderColorRow
                    || i == profileBackgroundColorRow || i == resetDefaultRow || i == passcodeRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new EmptyCell(mContext);
                }
                if (i == overscrollRow) {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(88));
                } else {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(16));
                }
            } else if (type == 1) {             // Ending section header cell (gray shadow)
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {             // Color and text item (LED Color)
                if (view == null) {
                    view = new TextColorCell(mContext);
                }
                TextColorCell textCell = (TextColorCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                int initialActionBarBackgroundColor = preferences.getInt("actionBarBackgroundColor", ApplicationLoader.ABBG_COLOR);
                int initialDrawerHeaderColor = preferences.getInt("drawerHeaderColor", ApplicationLoader.DH_COLOR);
                final int initialProfileBackgroundColor = preferences.getInt("profileBackgroundColor", ApplicationLoader.PBG_COLOR);

                if (i == actionBarBackgroundColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemActionBarBackgroundColor", R.string.itemActionBarBackgroundColor), initialActionBarBackgroundColor, true);
                } else if (i == drawerHeaderColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemDrawerHeaderColor", R.string.itemDrawerHeaderColor), initialDrawerHeaderColor, true);
                } else if (i == profileBackgroundColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("itemProfileBackgroundColor", R.string.itemProfileBackgroundColor), initialProfileBackgroundColor, true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == slyDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SlyHelp", R.string.SlyHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                } else if (i == customizationsDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomizationsHelp", R.string.CustomizationsHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                } else if (i == premiumSecurityDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("PremiumSecurityHelp", R.string.AdvancedSecurityHelp));
                    view.setBackgroundResource(R.drawable.greydivider_bottom);
                }
            } else if (type == 4) {             // Header type cell, categorie's header
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                /*if (i == shareAndForwardSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("ShareHeader", R.string.ShareHeader));
                } else*/
                if (i == slySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SlyHeader", R.string.SlyHeader));
                } else if (i == customizationsSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("CustomizationsHeader", R.string.CustomizationsHeader));
                } else if (i == premiumSecuritySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("PremiumSecurityHeader", R.string.AdvancedSecurityHeader));
                }
            } else if (type == 5) {             // Text information cell (e.g. ending version gray colored text)
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        ((TextInfoCell) view).setText(String.format(Locale.US, "Telegramity for Android v%s (%d)", pInfo.versionName, pInfo.versionCode));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;

                if (i == resetDefaultRow) {
                    textCell.setText(LocaleController.getString("CustomizationsResetDefault", R.string.CustomizationsResetDefault), true);
                } else if (i == passcodeRow) {
                    textCell.setText(LocaleController.getString("Passcode", R.string.Passcode), true);
                }
            } else if (type == 7) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCheckCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("AdvancedPreferences", Activity.MODE_PRIVATE);
                /*if (i == quickShareRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemQuickShare", R.string.itemQuickShare), preferences.getBoolean("quickShare", true), true);
                } else if (i == forwardNoCaptionRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemForwardNoCaption", R.string.itemForwardNoCaption), preferences.getBoolean("forwardNoCaption", false), true);
                } else*/
                if (i == specterModeRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemSpecterMode", R.string.itemSpecterMode), preferences.getBoolean("specterMode", false), true);
                } else if (i == hiddenTypingRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemHiddenTyping", R.string.itemHiddenTyping), preferences.getBoolean("hiddenTyping", false), true);
                } else if (i == noNumberRow) {
                    textCheckCell.setTextAndCheck(LocaleController.getString("itemNoNumber", R.string.itemNoNumber), preferences.getBoolean("noNumber", false), true);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (/*i == shareAndForwardSectionBottomRow ||*/ i == customizationsSectionBottomRow || i == premiumSecuritySectionBottomRow || i == slySectionBottomRow) {
                return 1;
            } else if (i == actionBarBackgroundColorRow || i == drawerHeaderColorRow || i == profileBackgroundColorRow) {
                return 2;
            } else if (i == customizationsDetailRow || i == premiumSecurityDetailRow || i == slyDetailRow) {
                return 3;
            } else if (/*i == shareAndForwardSectionRow || */i == customizationsSectionRow || i == premiumSecuritySectionRow || i == slySectionRow) {
                return 4;
            } else if (i == textDescriptionRow) {
                return 5;
            } else if (i == resetDefaultRow || i == passcodeRow) {
                return 6;
            } else {
                return 7;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
