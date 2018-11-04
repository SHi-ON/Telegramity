package org.gramity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class FontSelectActivity extends BaseFragment {

    public static final String DEFAULT_FONT_PATH = "fonts/IRANSansMobileRegular.ttf";
    public static final int FONTS_ROW_COUNT = 13;


    private ListAdapter listAdapter;
    private List<String> nameList;
    private List<String> pathList;

    private String[] fontNamesArray = ApplicationLoader.applicationContext.getResources().getStringArray(R.array.FontNameArr);
    private String[] fontPathsArray = {
            "fonts/IRANSansDNLight.ttf",
            "fonts/IRANSansDNRegular.ttf",
            "fonts/IRANSansDNBold.ttf",
            "fonts/IRANYekanMobileLight.ttf",
            "fonts/IRANYekanMobileRegular.ttf",
            "fonts/IRANYekanMobileBold.ttf",
            "fonts/IRANSansMobileUltraLight.ttf",
            "fonts/IRANSansMobileLight.ttf",
            "fonts/IRANSansMobileRegular.ttf",
            "fonts/IRANSansMobileMedium.ttf",
            "fonts/IRANSansMobileBold.ttf",
            "fonts/rmedium.ttf",
            "device"};

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("FontActivityTitle", R.string.FontActivityTitle));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        EmptyTextProgressView emptyView = new EmptyTextProgressView(context);
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.showTextView();
        emptyView.setShowAtCenter(true);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        RecyclerListView listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        nameList = new ArrayList<>(FONTS_ROW_COUNT);
        pathList = new ArrayList<>(FONTS_ROW_COUNT);
        for (int a = 0; a <= (FONTS_ROW_COUNT - 1); a++) {
            nameList.add(fontNamesArray[a]);
            pathList.add(fontPathsArray[a]);
        }

        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(GramityConstants.ADVANCED_PREFERENCES, Context.MODE_PRIVATE);

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setMessage(LocaleController.getString("ChangeFontAlertMessage", R.string.ChangeFontAlertMessage));
                TextView titleTextView = new TextView(getParentActivity());
                titleTextView.setText(LocaleController.getString("AppNameTgy", R.string.AppNameTgy));
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                titleTextView.setTextColor(Theme.getColor(Theme.key_actionBarDefault));
                titleTextView.setTypeface(AndroidUtilities.getTypeface(null));
                titleTextView.setPadding(24, 18, 24, 0);
                builder.setCustomTitle(titleTextView);
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(GramityConstants.PREF_CUSTOM_FONT_PATH, pathList.get(position));
                        editor.putString(GramityConstants.PREF_CUSTOM_FONT_NAME, nameList.get(position));
                        editor.commit();
                        GramityUtilities.restartTelegramity();
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        return fragmentView;
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
            if (nameList == null) {
                return 0;
            }
            return nameList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new TextSettingsCell(mContext));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
            textSettingsCell.setText(nameList.get(position), position != nameList.size() - 1);
            textSettingsCell.setTextFont(pathList.get(position));
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }
}
