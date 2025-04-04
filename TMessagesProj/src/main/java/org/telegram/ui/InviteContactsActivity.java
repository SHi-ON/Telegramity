/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.GroupCreateSectionCell;
import org.telegram.ui.Cells.InviteTextCell;
import org.telegram.ui.Cells.InviteUserCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.GroupCreateDividerItemDecoration;
import org.telegram.ui.Components.GroupCreateSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class InviteContactsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, View.OnClickListener {

    private ScrollView scrollView;
    private SpansContainer spansContainer;
    private EditTextBoldCursor editText;
    private RecyclerListView listView;
    private EmptyTextProgressView emptyView;
    private InviteAdapter adapter;
    private TextView infoTextView;
    private FrameLayout counterView;
    private TextView counterTextView;
    private TextView textView;
    private GroupCreateDividerItemDecoration decoration;
    private boolean ignoreScrollEvent;
    private ArrayList<ContactsController.Contact> phoneBookContacts;

    private int containerHeight;

    private boolean searchWas;
    private boolean searching;
    private HashMap<String, GroupCreateSpan> selectedContacts = new HashMap<>();
    private ArrayList<GroupCreateSpan> allSpans = new ArrayList<>();
    private GroupCreateSpan currentDeletingSpan;

    private int fieldY;

    private class SpansContainer extends ViewGroup {

        private AnimatorSet currentAnimation;
        private boolean animationStarted;
        private ArrayList<Animator> animators = new ArrayList<>();
        private View addingSpan;
        private View removingSpan;

        public SpansContainer(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int count = getChildCount();
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int maxWidth = width - AndroidUtilities.dp(32);
            int currentLineWidth = 0;
            int y = AndroidUtilities.dp(12);
            int allCurrentLineWidth = 0;
            int allY = AndroidUtilities.dp(12);
            int x;
            for (int a = 0; a < count; a++) {
                View child = getChildAt(a);
                if (!(child instanceof GroupCreateSpan)) {
                    continue;
                }
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                if (child != removingSpan && currentLineWidth + child.getMeasuredWidth() > maxWidth) {
                    y += child.getMeasuredHeight() + AndroidUtilities.dp(12);
                    currentLineWidth = 0;
                }
                if (allCurrentLineWidth + child.getMeasuredWidth() > maxWidth) {
                    allY += child.getMeasuredHeight() + AndroidUtilities.dp(12);
                    allCurrentLineWidth = 0;
                }
                x = AndroidUtilities.dp(16) + currentLineWidth;
                if (!animationStarted) {
                    if (child == removingSpan) {
                        child.setTranslationX(AndroidUtilities.dp(16) + allCurrentLineWidth);
                        child.setTranslationY(allY);
                    } else if (removingSpan != null) {
                        if (child.getTranslationX() != x) {
                            animators.add(ObjectAnimator.ofFloat(child, "translationX", x));
                        }
                        if (child.getTranslationY() != y) {
                            animators.add(ObjectAnimator.ofFloat(child, "translationY", y));
                        }
                    } else {
                        child.setTranslationX(x);
                        child.setTranslationY(y);
                    }
                }
                if (child != removingSpan) {
                    currentLineWidth += child.getMeasuredWidth() + AndroidUtilities.dp(9);
                }
                allCurrentLineWidth += child.getMeasuredWidth() + AndroidUtilities.dp(9);
            }
            int minWidth;
            if (AndroidUtilities.isTablet()) {
                minWidth = AndroidUtilities.dp(530 - 32 - 18 - 57 * 2) / 3;
            } else {
                minWidth = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(32 + 18 + 57 * 2)) / 3;
            }
            if (maxWidth - currentLineWidth < minWidth) {
                currentLineWidth = 0;
                y += AndroidUtilities.dp(32 + 12);
            }
            if (maxWidth - allCurrentLineWidth < minWidth) {
                allY += AndroidUtilities.dp(32 + 12);
            }
            editText.measure(MeasureSpec.makeMeasureSpec(maxWidth - currentLineWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
            if (!animationStarted) {
                int currentHeight = allY + AndroidUtilities.dp(32 + 12);
                int fieldX = currentLineWidth + AndroidUtilities.dp(16);
                fieldY = y;
                if (currentAnimation != null) {
                    int resultHeight = y + AndroidUtilities.dp(32 + 12);
                    if (containerHeight != resultHeight) {
                        animators.add(ObjectAnimator.ofInt(InviteContactsActivity.this, "containerHeight", resultHeight));
                    }
                    if (editText.getTranslationX() != fieldX) {
                        animators.add(ObjectAnimator.ofFloat(editText, "translationX", fieldX));
                    }
                    if (editText.getTranslationY() != fieldY) {
                        animators.add(ObjectAnimator.ofFloat(editText, "translationY", fieldY));
                    }
                    editText.setAllowDrawCursor(false);
                    currentAnimation.playTogether(animators);
                    currentAnimation.start();
                    animationStarted = true;
                } else {
                    containerHeight = currentHeight;
                    editText.setTranslationX(fieldX);
                    editText.setTranslationY(fieldY);
                }
            } else if (currentAnimation != null) {
                if (!ignoreScrollEvent && removingSpan == null) {
                    editText.bringPointIntoView(editText.getSelectionStart());
                }
            }
            setMeasuredDimension(width, containerHeight);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int count = getChildCount();
            for (int a = 0; a < count; a++) {
                View child = getChildAt(a);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }

        public void addSpan(final GroupCreateSpan span) {
            allSpans.add(span);
            selectedContacts.put(span.getKey(), span);

            editText.setHintVisible(false);
            if (currentAnimation != null) {
                currentAnimation.setupEndValues();
                currentAnimation.cancel();
            }
            animationStarted = false;
            currentAnimation = new AnimatorSet();
            currentAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    addingSpan = null;
                    currentAnimation = null;
                    animationStarted = false;
                    editText.setAllowDrawCursor(true);
                }
            });
            currentAnimation.setDuration(150);
            addingSpan = span;
            animators.clear();
            animators.add(ObjectAnimator.ofFloat(addingSpan, "scaleX", 0.01f, 1.0f));
            animators.add(ObjectAnimator.ofFloat(addingSpan, "scaleY", 0.01f, 1.0f));
            animators.add(ObjectAnimator.ofFloat(addingSpan, "alpha", 0.0f, 1.0f));
            addView(span);
        }

        public void removeSpan(final GroupCreateSpan span) {
            ignoreScrollEvent = true;
            selectedContacts.remove(span.getKey());
            allSpans.remove(span);
            span.setOnClickListener(null);

            if (currentAnimation != null) {
                currentAnimation.setupEndValues();
                currentAnimation.cancel();
            }
            animationStarted = false;
            currentAnimation = new AnimatorSet();
            currentAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    removeView(span);
                    removingSpan = null;
                    currentAnimation = null;
                    animationStarted = false;
                    editText.setAllowDrawCursor(true);
                    if (allSpans.isEmpty()) {
                        editText.setHintVisible(true);
                    }
                }
            });
            currentAnimation.setDuration(150);
            removingSpan = span;
            animators.clear();
            animators.add(ObjectAnimator.ofFloat(removingSpan, "scaleX", 1.0f, 0.01f));
            animators.add(ObjectAnimator.ofFloat(removingSpan, "scaleY", 1.0f, 0.01f));
            animators.add(ObjectAnimator.ofFloat(removingSpan, "alpha", 1.0f, 0.0f));
            requestLayout();
        }
    }

    public InviteContactsActivity() {
        super();
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsImported);
        fetchContacts();
        if (!UserConfig.contactsReimported) {
            ContactsController.getInstance().forceImportContacts();
            UserConfig.contactsReimported = true;
            UserConfig.saveConfig(false);
        }
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsImported);
    }

    @Override
    public void onClick(View v) {
        GroupCreateSpan span = (GroupCreateSpan) v;
        if (span.isDeleting()) {
            currentDeletingSpan = null;
            spansContainer.removeSpan(span);
            updateHint();
            checkVisibleRows();
        } else {
            if (currentDeletingSpan != null) {
                currentDeletingSpan.cancelDeleteAnimation();
            }
            currentDeletingSpan = span;
            span.startDeleteAnimation();
        }
    }

    @Override
    public View createView(Context context) {
        searching = false;
        searchWas = false;
        allSpans.clear();
        selectedContacts.clear();
        currentDeletingSpan = null;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("InviteFriends", R.string.InviteFriends));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        fragmentView = new ViewGroup(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(width, height);
                int maxSize;
                if (AndroidUtilities.isTablet() || height > width) {
                    maxSize = AndroidUtilities.dp(144);
                } else {
                    maxSize = AndroidUtilities.dp(56);
                }

                int h;
                infoTextView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.AT_MOST));
                counterView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
                if (infoTextView.getVisibility() == VISIBLE) {
                    h = infoTextView.getMeasuredHeight();
                } else {
                    h = counterView.getMeasuredHeight();
                }
                scrollView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.AT_MOST));
                listView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - scrollView.getMeasuredHeight() - h, MeasureSpec.EXACTLY));
                emptyView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - scrollView.getMeasuredHeight() - AndroidUtilities.dp(72), MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                scrollView.layout(0, 0, scrollView.getMeasuredWidth(), scrollView.getMeasuredHeight());
                listView.layout(0, scrollView.getMeasuredHeight(), listView.getMeasuredWidth(), scrollView.getMeasuredHeight() + listView.getMeasuredHeight());
                emptyView.layout(0, scrollView.getMeasuredHeight() + AndroidUtilities.dp(72), emptyView.getMeasuredWidth(), scrollView.getMeasuredHeight() + emptyView.getMeasuredHeight());
                int y = bottom - top - infoTextView.getMeasuredHeight();
                infoTextView.layout(0, y, infoTextView.getMeasuredWidth(), y + infoTextView.getMeasuredHeight());
                y = bottom - top - counterView.getMeasuredHeight();
                counterView.layout(0, y, counterView.getMeasuredWidth(), y + counterView.getMeasuredHeight());
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                boolean result = super.drawChild(canvas, child, drawingTime);
                if (child == listView || child == emptyView) {
                    parentLayout.drawHeaderShadow(canvas, scrollView.getMeasuredHeight());
                }
                return result;
            }
        };
        ViewGroup frameLayout = (ViewGroup) fragmentView;

        scrollView = new ScrollView(context) {
            @Override
            public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
                if (ignoreScrollEvent) {
                    ignoreScrollEvent = false;
                    return false;
                }
                rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
                rectangle.top += fieldY + AndroidUtilities.dp(20);
                rectangle.bottom += fieldY + AndroidUtilities.dp(50);
                return super.requestChildRectangleOnScreen(child, rectangle, immediate);
            }
        };
        scrollView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(scrollView);

        spansContainer = new SpansContainer(context);
        scrollView.addView(spansContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editText = new EditTextBoldCursor(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (currentDeletingSpan != null) {
                    currentDeletingSpan.cancelDeleteAnimation();
                    currentDeletingSpan = null;
                }
                return super.onTouchEvent(event);
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setHintColor(Theme.getColor(Theme.key_groupcreate_hintText));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setCursorColor(Theme.getColor(Theme.key_groupcreate_cursor));
        editText.setCursorWidth(1.5f);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setSingleLine(true);
        editText.setBackgroundDrawable(null);
        editText.setVerticalScrollBarEnabled(false);
        editText.setHorizontalScrollBarEnabled(false);
        editText.setTextIsSelectable(false);
        editText.setPadding(0, 0, 0, 0);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        spansContainer.addView(editText);
        editText.setHintText(LocaleController.getString("SearchFriends", R.string.SearchFriends));
        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        /*editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE && onDonePressed();
            }
        });*/
        editText.setOnKeyListener(new View.OnKeyListener() {

            private boolean wasEmpty;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    wasEmpty = editText.length() == 0;
                } else if (event.getAction() == KeyEvent.ACTION_UP && wasEmpty && !allSpans.isEmpty()){
                    spansContainer.removeSpan(allSpans.get(allSpans.size() - 1));
                    updateHint();
                    checkVisibleRows();
                    return true;
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editText.length() != 0) {
                    searching = true;
                    searchWas = true;
                    adapter.setSearching(true);
                    adapter.searchDialogs(editText.getText().toString());
                    listView.setFastScrollVisible(false);
                    listView.setVerticalScrollBarEnabled(true);
                    emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                } else {
                    closeSearch();
                }
            }
        });

        emptyView = new EmptyTextProgressView(context);
        if (ContactsController.getInstance().isLoadingContacts()) {
            emptyView.showProgress();
        } else {
            emptyView.showTextView();
        }
        emptyView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
        frameLayout.addView(emptyView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter = new InviteAdapter(context));
        listView.setLayoutManager(linearLayoutManager);
        listView.setVerticalScrollBarEnabled(true);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? View.SCROLLBAR_POSITION_LEFT : View.SCROLLBAR_POSITION_RIGHT);
        listView.addItemDecoration(decoration = new GroupCreateDividerItemDecoration());
        frameLayout.addView(listView);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == 0 && !searching) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        String text = ContactsController.getInstance().getInviteText(0);
                        intent.putExtra(Intent.EXTRA_TEXT, text);
                        getParentActivity().startActivityForResult(Intent.createChooser(intent, text), 500);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    return;
                }
                if (!(view instanceof InviteUserCell)) {
                    return;
                }
                InviteUserCell cell = (InviteUserCell) view;
                ContactsController.Contact contact = cell.getContact();
                if (contact == null) {
                    return;
                }
                boolean exists;
                if (exists = selectedContacts.containsKey(contact.key)) {
                    GroupCreateSpan span = selectedContacts.get(contact.key);
                    spansContainer.removeSpan(span);
                } else {
                    GroupCreateSpan span = new GroupCreateSpan(editText.getContext(), contact);
                    spansContainer.addSpan(span);
                    span.setOnClickListener(InviteContactsActivity.this);
                }
                updateHint();
                if (searching || searchWas) {
                    AndroidUtilities.showKeyboard(editText);
                } else {
                    cell.setChecked(!exists, true);
                }
                if (editText.length() > 0) {
                    editText.setText(null);
                }
            }
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(editText);
                }
            }
        });

        infoTextView = new TextView(context);
        infoTextView.setBackgroundColor(Theme.getColor(Theme.key_contacts_inviteBackground));
        infoTextView.setTextColor(Theme.getColor(Theme.key_contacts_inviteText));
        infoTextView.setGravity(Gravity.CENTER);
        infoTextView.setText(LocaleController.getString("InviteFriendsHelp", R.string.InviteFriendsHelp));
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        infoTextView.setTypeface(AndroidUtilities.getTypeface(null));
        infoTextView.setPadding(AndroidUtilities.dp(17), AndroidUtilities.dp(9), AndroidUtilities.dp(17), AndroidUtilities.dp(9));
        frameLayout.addView(infoTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));

        counterView = new FrameLayout(context);
        counterView.setBackgroundColor(Theme.getColor(Theme.key_contacts_inviteBackground));
        counterView.setVisibility(View.INVISIBLE);
        frameLayout.addView(counterView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.BOTTOM));
        counterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    StringBuilder builder = new StringBuilder();
                    int num = 0;
                    for (int a = 0; a < allSpans.size(); a++) {
                        ContactsController.Contact contact = allSpans.get(a).getContact();
                        if (builder.length() != 0) {
                            builder.append(';');
                        }
                        builder.append(contact.phones.get(0));
                        if (a == 0 && allSpans.size() == 1) {
                            num = contact.imported;
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + builder.toString()));
                    intent.putExtra("sms_body", ContactsController.getInstance().getInviteText(num));
                    getParentActivity().startActivityForResult(intent, 500);
                    MediaController.getInstance().startSmsObserver();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                finishFragment();
            }
        });

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        counterView.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        counterTextView = new TextView(context);
        counterTextView.setTypeface(AndroidUtilities.getTypeface(null));
        counterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        counterTextView.setTextColor(Theme.getColor(Theme.key_contacts_inviteBackground));
        counterTextView.setGravity(Gravity.CENTER);
        counterTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(10), 0xffffffff));
        counterTextView.setMinWidth(AndroidUtilities.dp(20));
        counterTextView.setPadding(AndroidUtilities.dp(6), 0, AndroidUtilities.dp(6), AndroidUtilities.dp(1));
        linearLayout.addView(counterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 20, Gravity.CENTER_VERTICAL, 0, 0, 10, 0));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTextColor(Theme.getColor(Theme.key_contacts_inviteText));
        textView.setGravity(Gravity.CENTER);
        textView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        textView.setText(LocaleController.getString("InviteToTelegram", R.string.InviteToTelegram).toUpperCase());
        textView.setTypeface(AndroidUtilities.getTypeface(null));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        updateHint();
        adapter.notifyDataSetChanged();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (editText != null) {
            editText.requestFocus();
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.contactsImported) {
            fetchContacts();
        }
    }

    public void setContainerHeight(int value) {
        containerHeight = value;
        if (spansContainer != null) {
            spansContainer.requestLayout();
        }
    }

    public int getContainerHeight() {
        return containerHeight;
    }

    private void checkVisibleRows() {
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof InviteUserCell) {
                InviteUserCell cell = (InviteUserCell) child;
                ContactsController.Contact contact = cell.getContact();
                if (contact != null) {
                    cell.setChecked(selectedContacts.containsKey(contact.key), true);
                }
            }
        }
    }

    private void updateHint() {
        if (selectedContacts.isEmpty()) {
            infoTextView.setVisibility(View.VISIBLE);
            counterView.setVisibility(View.INVISIBLE);
        } else {
            infoTextView.setVisibility(View.INVISIBLE);
            counterView.setVisibility(View.VISIBLE);
            counterTextView.setText(String.format("%d", selectedContacts.size()));
        }
    }

    private void closeSearch() {
        searching = false;
        searchWas = false;
        adapter.setSearching(false);
        adapter.searchDialogs(null);
        listView.setFastScrollVisible(true);
        listView.setVerticalScrollBarEnabled(false);
        emptyView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
    }

    private void fetchContacts() {
        phoneBookContacts = new ArrayList<>(ContactsController.getInstance().phoneBookContacts);
        Collections.sort(phoneBookContacts, new Comparator<ContactsController.Contact>() {
            @Override
            public int compare(ContactsController.Contact o1, ContactsController.Contact o2) {
                if (o1.imported > o2.imported) {
                    return -1;
                } else if (o1.imported < o2.imported) {
                    return 1;
                }
                return 0;
            }
        });
        if (emptyView != null) {
            emptyView.showTextView();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public class InviteAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;
        private ArrayList<ContactsController.Contact> searchResult = new ArrayList<>();
        private ArrayList<CharSequence> searchResultNames = new ArrayList<>();
        private Timer searchTimer;
        private boolean searching;

        public InviteAdapter(Context ctx) {
            context = ctx;
        }

        public void setSearching(boolean value) {
            if (searching == value) {
                return;
            }
            searching = value;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (searching) {
                return searchResult.size();
            }
            return phoneBookContacts.size() + 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 1:
                    view = new InviteTextCell(context);
                    ((InviteTextCell) view).setTextAndIcon(LocaleController.getString("ShareTelegram", R.string.ShareTelegram), R.drawable.share);
                    break;
                default:
                    view = new InviteUserCell(context, true);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    InviteUserCell cell = (InviteUserCell) holder.itemView;
                    ContactsController.Contact contact;
                    CharSequence name;
                    if (searching) {
                        contact = searchResult.get(position);
                        name = searchResultNames.get(position);
                    } else {
                        contact = phoneBookContacts.get(position - 1);
                        name = null;
                    }
                    cell.setUser(contact, name);
                    cell.setChecked(selectedContacts.containsKey(contact.key), false);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (!searching) {
                if (position == 0) {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            if (holder.itemView instanceof InviteUserCell) {
                ((InviteUserCell) holder.itemView).recycle();
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        public void searchDialogs(final String query) {
            try {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (query == null) {
                searchResult.clear();
                searchResultNames.clear();
                notifyDataSetChanged();
            } else {
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            searchTimer.cancel();
                            searchTimer = null;
                        } catch (Exception e) {
                            FileLog.e(e);
                        }

                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.searchQueue.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        String search1 = query.trim().toLowerCase();
                                        if (search1.length() == 0) {
                                            updateSearchResults(new ArrayList<ContactsController.Contact>(), new ArrayList<CharSequence>());
                                            return;
                                        }
                                        String search2 = LocaleController.getInstance().getTranslitString(search1);
                                        if (search1.equals(search2) || search2.length() == 0) {
                                            search2 = null;
                                        }
                                        String search[] = new String[1 + (search2 != null ? 1 : 0)];
                                        search[0] = search1;
                                        if (search2 != null) {
                                            search[1] = search2;
                                        }

                                        ArrayList<ContactsController.Contact> resultArray = new ArrayList<>();
                                        ArrayList<CharSequence> resultArrayNames = new ArrayList<>();

                                        for (int a = 0; a < phoneBookContacts.size(); a++) {
                                            ContactsController.Contact contact = phoneBookContacts.get(a);

                                            String name = ContactsController.formatName(contact.first_name, contact.last_name).toLowerCase();
                                            String tName = LocaleController.getInstance().getTranslitString(name);
                                            if (name.equals(tName)) {
                                                tName = null;
                                            }

                                            int found = 0;
                                            for (String q : search) {
                                                if (name.startsWith(q) || name.contains(" " + q) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                                                    found = 1;
                                                }

                                                if (found != 0) {
                                                    resultArrayNames.add(AndroidUtilities.generateSearchName(contact.first_name, contact.last_name, q));
                                                    resultArray.add(contact);
                                                    break;
                                                }
                                            }
                                        }
                                        updateSearchResults(resultArray, resultArrayNames);
                                    }
                                });
                            }
                        });

                    }
                }, 200, 300);
            }
        }

        private void updateSearchResults(final ArrayList<ContactsController.Contact> users, final ArrayList<CharSequence> names) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    searchResult = users;
                    searchResultNames = names;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            int count = getItemCount();
            emptyView.setVisibility(count == 1 ? View.VISIBLE : View.INVISIBLE);
            decoration.setSingle(count == 1);
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof InviteUserCell) {
                        ((InviteUserCell) child).update(0);
                    }
                }
            }
        };

        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(scrollView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollActive),
                new ThemeDescription(listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollInactive),
                new ThemeDescription(listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, Theme.key_fastScrollText),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_groupcreate_hintText),
                new ThemeDescription(editText, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_groupcreate_cursor),

                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GroupCreateSectionCell.class}, null, null, null, Theme.key_graySection),
                new ThemeDescription(listView, 0, new Class[]{GroupCreateSectionCell.class}, new String[]{"drawable"}, null, null, null, Theme.key_groupcreate_sectionShadow),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{GroupCreateSectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{InviteUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_groupcreate_sectionText),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{InviteUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_groupcreate_checkbox),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{InviteUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_groupcreate_checkboxCheck),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{InviteUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_groupcreate_onlineText),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{InviteUserCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_groupcreate_offlineText),
                new ThemeDescription(listView, 0, new Class[]{InviteUserCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink),

                new ThemeDescription(listView, 0, new Class[]{InviteTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{InviteTextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),

                new ThemeDescription(spansContainer, 0, new Class[]{GroupCreateSpan.class}, null, null, null, Theme.key_avatar_backgroundGroupCreateSpanBlue),
                new ThemeDescription(spansContainer, 0, new Class[]{GroupCreateSpan.class}, null, null, null, Theme.key_groupcreate_spanBackground),
                new ThemeDescription(spansContainer, 0, new Class[]{GroupCreateSpan.class}, null, null, null, Theme.key_groupcreate_spanText),
                new ThemeDescription(spansContainer, 0, new Class[]{GroupCreateSpan.class}, null, null, null, Theme.key_avatar_backgroundBlue),

                new ThemeDescription(infoTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_contacts_inviteText),
                new ThemeDescription(infoTextView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_contacts_inviteBackground),
                new ThemeDescription(counterView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_contacts_inviteBackground),
                new ThemeDescription(counterTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_contacts_inviteBackground),
                new ThemeDescription(textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_contacts_inviteText),
        };
    }
}
