package org.gramity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.IntroActivity;
import org.telegram.ui.LaunchActivity;

public class ChooseLanguageActivity extends Activity {

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_TMessages);
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(rootLayout);

        Drawable titleDrawable = new IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_globe_alt).color(Theme.getColor(Theme.key_actionBarDefaultTitle)).sizeDp(36).paddingDp(5);

        TextView titleTextView = new TextView(this);
        titleTextView.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        titleTextView.setPadding(20, 17, 20, 17);
        titleTextView.setText(LocaleController.getString("Language", R.string.Language));
        titleTextView.setCompoundDrawables(LocaleController.isRTL ? null : titleDrawable, null, LocaleController.isRTL ? titleDrawable : null, null);
        titleTextView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        titleTextView.setTextSize(20);
        titleTextView.setTypeface(AndroidUtilities.getTypeface(null), Typeface.BOLD);
        rootLayout.addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        RecyclerListView listView = new RecyclerListView(this);
        listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        listView.setListSelectorColor(Color.GREEN);
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView.setVerticalScrollBarEnabled(false);
        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);
        rootLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LocaleController.LocaleInfo localeInfo = null;
                if (position >= 0 && position < LocaleController.getInstance().languages.size()) {
                    localeInfo = LocaleController.getInstance().languages.get(position);
                }
                if (localeInfo != null) {
                    LocaleController.getInstance().applyLanguage(localeInfo, true, false, false, true);
                }
                Intent intent2 = new Intent(ChooseLanguageActivity.this, LaunchActivity.class);
                intent2.putExtra("fromIntro", true);
                startActivity(intent2);
                finish();
//                Toast.makeText(ChooseLanguageActivity.this, LocaleController.getString("Loading", R.string.Loading),Toast.LENGTH_LONG).show();
//                GramityUtilities.restartTelegramity();
            }
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    AndroidUtilities.hideKeyboard(ChooseLanguageActivity.this.getCurrentFocus());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            if (LocaleController.getInstance().languages == null) {
                return 0;
            }
            return LocaleController.getInstance().languages.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new TextSettingsCell(mContext));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
            LocaleController.LocaleInfo localeInfo;
            localeInfo = LocaleController.getInstance().languages.get(position);
//            boolean last = position == LocaleController.getInstance().sortedLanguages.size() - 1;
            textSettingsCell.setText(localeInfo.name, false);
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }
}
