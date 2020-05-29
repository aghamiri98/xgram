package sections.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import sections.materials.BaseFragmentAdapter;
import sections.ui.tabview.PlusManageTabsActivity;


public class TabSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListView listView;
    private ListAdapter listAdapter;

    private int overscrollRow;
    private int emptyRow;

    private int rowCount;
    private int dialogsSectionRow;
    private int dialogsSectionRow2;
    private int dialogsHideTabsCheckRow;
    private int dialogsTabsHeightRow;
    private int dialogsTabsRow;
    private int dialogsDisableTabsAnimationCheckRow;
    private int ShowTabsInBottomRow;
    private int dialogsInfiniteTabsSwipe;
    private int dialogsHideTabsCounters;
    private int dialogsTabsCountersCountChats;
    private int dialogsTabsCountersCountNotMuted;

    private int currentAccount = UserConfig.selectedAccount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        overscrollRow = -1;
        emptyRow = -1;


        dialogsSectionRow = rowCount++;
        dialogsSectionRow2 = rowCount++;

        dialogsHideTabsCheckRow = rowCount++;
        dialogsTabsRow = rowCount++;
        dialogsTabsHeightRow = rowCount++;
        dialogsDisableTabsAnimationCheckRow = rowCount++;
        ShowTabsInBottomRow = rowCount++;
        dialogsInfiniteTabsSwipe = rowCount++;
        dialogsHideTabsCounters = rowCount++;
        dialogsTabsCountersCountNotMuted = rowCount++;
        dialogsTabsCountersCountChats = rowCount++;


        MessagesController.getInstance(currentAccount).loadFullUser(UserConfig.getInstance(currentAccount).getCurrentUser(), classGuid, true);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.refreshTabs);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setTitle(LocaleController.getString("TabSettings", R.string.TabSettings));

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
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));


        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);

        listView.setAdapter(listAdapter);

        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                if (i == dialogsHideTabsCheckRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusHideTabs = !Theme.plusHideTabs;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabs", Theme.plusHideTabs);
                    editor.apply();

                    if (Theme.plusHideUsersTab && Theme.plusHideGroupsTab && Theme.plusHideSuperGroupsTab && Theme.plusHideChannelsTab && Theme.plusHideBotsTab && Theme.plusHideFavsTab) {

                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabs, 10);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideTabs);
                    }
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    //boolean disable = preferences.getBoolean("disableTabsAnimation", false);
                    Theme.plusDisableTabsAnimation = !Theme.plusDisableTabsAnimation;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableTabsAnimation", Theme.plusDisableTabsAnimation);
                    editor.apply();
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabs, 11);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusDisableTabsAnimation);
                    }
                } else if (i == ShowTabsInBottomRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusTabsToBottom = Theme.chatsTabsToBottom = !Theme.plusTabsToBottom;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsToBottom", Theme.plusTabsToBottom);
                    editor.apply();
                    SharedPreferences.Editor editorTheme = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE).edit();
                    editorTheme.putBoolean("chatsTabsToBottom", Theme.plusTabsToBottom);
                    editorTheme.apply();

                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabs, 14);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsToBottom);
                    }
                } else if (i == dialogsInfiniteTabsSwipe) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusInfiniteTabsSwipe = !Theme.plusInfiniteTabsSwipe;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("infiniteTabsSwipe", Theme.plusInfiniteTabsSwipe);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusInfiniteTabsSwipe);
                    }
                } else if (i == dialogsHideTabsCounters) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusHideTabsCounters = !Theme.plusHideTabsCounters;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabsCounters", Theme.plusHideTabsCounters);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideTabsCounters);
                    }
                } else if (i == dialogsTabsCountersCountChats) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusTabsCountersCountChats = !Theme.plusTabsCountersCountChats;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountChats", Theme.plusTabsCountersCountChats);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsCountersCountChats);
                    }
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    Theme.plusTabsCountersCountNotMuted = !Theme.plusTabsCountersCountNotMuted;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountNotMuted", Theme.plusTabsCountersCountNotMuted);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsCountersCountNotMuted);
                    }
                } else if (i == dialogsTabsHeightRow) {

                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TabsHeight", R.string.TabsHeight));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(30);
                    numberPicker.setMaxValue(48);
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    numberPicker.setValue(Theme.plusTabsHeight /*preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40)*/);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            Theme.plusTabsHeight = numberPicker.getValue();
                            editor.putInt("tabsHeight", Theme.plusTabsHeight);
                            editor.apply();
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabs, 12);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());

                } else if (i == dialogsTabsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    presentFragment(new PlusManageTabsActivity());
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//                    createTabsDialog(builder);
//                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
//                            if (listView != null) {
//                                listView.invalidateViews();
//                            }
//                        }
//                    });
//                    showDialog(builder.create());
                }
            }
        });

        frameLayout.addView(actionBar);

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        fixLayout();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, int currentAccount, Object... args) {

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
            return i == dialogsTabsHeightRow || i == dialogsTabsRow ||
                    i == dialogsHideTabsCheckRow || i == dialogsDisableTabsAnimationCheckRow || i == ShowTabsInBottomRow ||
                    i == dialogsInfiniteTabsSwipe || i == dialogsHideTabsCounters || i == dialogsTabsCountersCountChats || i == dialogsTabsCountersCountNotMuted;
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
            } else if (type == 1) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == dialogsTabsHeightRow) {
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
//                    int size = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40);
//                    textCell.setTextAndValue(LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", size), true);
                    textCell.setTag("tabsHeight");
                    //int size = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40);
                    textCell.setTextAndValue(LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", Theme.plusTabsHeight), true);

                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == dialogsHideTabsCheckRow) {
                    textCell.setTag("hideTabs");
                    textCell.setTextAndCheck(LocaleController.getString("HideTabs", R.string.HideTabs), Theme.plusHideTabs, true);
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    textCell.setTag("disableTabsAnimation");
                    textCell.setTextAndCheck(LocaleController.getString("DisableTabsAnimation", R.string.DisableTabsAnimation), Theme.plusDisableTabsAnimation, true);
                } else if (i == ShowTabsInBottomRow) {
                    textCell.setTag("tabsToBottom");
                    textCell.setTextAndCheck(LocaleController.getString("TabsToBottom", R.string.TabsToBottom), preferences.getBoolean("tabsToBottom", false), true);
                } else if (i == dialogsInfiniteTabsSwipe) {
                    textCell.setTag("infiniteTabsSwipe");
                    textCell.setTextAndCheck(LocaleController.getString("InfiniteSwipe", R.string.InfiniteSwipe), Theme.plusInfiniteTabsSwipe, true);
                } else if (i == dialogsHideTabsCounters) {
                    textCell.setTag("hideTabsCounters");
                    textCell.setTextAndCheck(LocaleController.getString("HideTabsCounters", R.string.HideTabsCounters), Theme.plusHideTabsCounters, true);
                } else if (i == dialogsTabsCountersCountChats) {
                    textCell.setTag("tabsCountersCountChats");
                    textCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountChats", R.string.HeaderTabCounterCountChats), Theme.plusTabsCountersCountChats, true);
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    textCell.setTag("tabsCountersCountNotMuted");
                    textCell.setTextAndCheck(LocaleController.getString("HeaderTabCounterCountNotMuted", R.string.HeaderTabCounterCountNotMuted), Theme.plusTabsCountersCountNotMuted, true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == dialogsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("DialogsSettings", R.string.DialogsSettings));
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if (i == dialogsTabsRow) {
                    String value;
//                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
//
//                    boolean hideUsers = preferences.getBoolean("hideUsers", false);
//                    boolean hideGroups = preferences.getBoolean("hideGroups", false);
//                    boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
//                    boolean hideChannels = preferences.getBoolean("hideChannels", false);
//                    boolean hideBots = preferences.getBoolean("hideBots", false);
//                    boolean hideFavs = preferences.getBoolean("hideFavs", false);

                    value = LocaleController.getString("SortTabs", R.string.SortTabs);
//
//                    String text = "";
//                    if (!hideUsers) {
//                        text += LocaleController.getString("Users", R.string.Users);
//                    }
//                    if (!hideGroups) {
//                        if (text.length() != 0) {
//                            text += ", ";
//                        }
//                        text += LocaleController.getString("Groups", R.string.Groups);
//                    }
//                    if (!hideSGroups) {
//                        if (text.length() != 0) {
//                            text += ", ";
//                        }
//                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
//                    }
//                    if (!hideChannels) {
//                        if (text.length() != 0) {
//                            text += ", ";
//                        }
//                        text += LocaleController.getString("Channels", R.string.Channels);
//                    }
//                    if (!hideBots) {
//                        if (text.length() != 0) {
//                            text += ", ";
//                        }
//                        text += LocaleController.getString("Bots", R.string.Bots);
//                    }
//                    if (!hideFavs) {
//                        if (text.length() != 0) {
//                            text += ", ";
//                        }
//                        text += LocaleController.getString("Favorites", R.string.Favorites);
//                    }
//                    if (text.length() == 0) {
//                        text = "";
//                    }
                    textCell.setTextAndValue(value, "", true);
                }
            } else if (type == 7) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }

            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (i == dialogsSectionRow) {
                return 1;
            } else if (i == dialogsHideTabsCheckRow || i == dialogsDisableTabsAnimationCheckRow || i == ShowTabsInBottomRow || i == dialogsInfiniteTabsSwipe ||
                    i == dialogsHideTabsCounters || i == dialogsTabsCountersCountChats || i == dialogsTabsCountersCountNotMuted) {
                return 3;
            } else if (i == dialogsTabsHeightRow) {
                return 2;
            } else if (i == dialogsTabsRow) {
                return 6;
            } else if (i == dialogsSectionRow2) {
                return 4;
            } else {
                return 2;
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


    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextInfoCell.class, TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
               // new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumb),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
              //  new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, 0, new Class[]{TextInfoCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText5),

        };
    }

}

