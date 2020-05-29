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
import android.widget.Toast;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
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
import sections.materials.BaseFragmentAdapter;


public class viewSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListView listView;
    private ListAdapter listAdapter;

    private int overscrollRow;
    private int emptyRow;

    private int settingsSectionRow;
    private int settingsSectionRow2;


    private int drawerSectionRow;
    private int drawerSectionRow2;
    private int showUsernameRow;

    private int messagesSectionRow;
    private int messagesSectionRow2;

    private int profileSectionRow;
    private int profileSectionRow2;
    private int profileSharedOptionsRow;

    private int notificationSectionRow;
    private int notificationSection2Row;
    private int notificationInvertMessagesOrderRow;

    private int privacySectionRow;
    private int privacySectionRow2;
    private int hideMobileNumberRow;


    private int rowCount;
    private int disableMessageClickRow;
    private int showAndroidEmojiRow;
    private int useDeviceFontRow;
    //    private int setspecificFont;
    private int dialogsPicClickRow;
    private int dialogsGroupPicClickRow;

    private int currentAccount = UserConfig.selectedAccount;
    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        overscrollRow = -1;
        emptyRow = -1;

        settingsSectionRow = rowCount++;
        settingsSectionRow2 = rowCount++;

        if (android.os.Build.VERSION.SDK_INT >= 19) { // Only enable this option for Kitkat and newer android versions
            showAndroidEmojiRow = rowCount++;
        } else {
            showAndroidEmojiRow = -1;
        }
        useDeviceFontRow = rowCount++;
//        setspecificFont = rowCount++;
        dialogsPicClickRow = rowCount++;
        dialogsGroupPicClickRow = rowCount++;


        messagesSectionRow = rowCount++;
        messagesSectionRow2 = rowCount++;

        disableMessageClickRow = rowCount++;


        profileSectionRow = rowCount++;
        profileSectionRow2 = rowCount++;

        profileSharedOptionsRow = rowCount++;

        drawerSectionRow = rowCount++;
        drawerSectionRow2 = rowCount++;
        showUsernameRow = rowCount++;

        notificationSectionRow = rowCount++;
        notificationSection2Row = rowCount++;
        notificationInvertMessagesOrderRow = rowCount++;

        privacySectionRow = rowCount++;
        privacySectionRow2 = rowCount++;
        hideMobileNumberRow = rowCount++;


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
        actionBar.setTitle(LocaleController.getString("ViewSetting", R.string.ViewSetting));

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

                if (i == showAndroidEmojiRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    boolean enabled = preferences.getBoolean("showAndroidEmoji", false);
                    editor.putBoolean("showAndroidEmoji", !enabled);
                    editor.apply();
                    ApplicationLoader.SHOW_ANDROID_EMOJI = !enabled;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!enabled);
                    }
                } else if (i == useDeviceFontRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    boolean enabled = preferences.getBoolean("useDeviceFont", false);
                    editor.putBoolean("useDeviceFont", !enabled);
                    editor.apply();
                    ApplicationLoader.USE_DEVICE_FONT = !enabled;
                    AndroidUtilities.needRestart = true;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getParentActivity() != null) {
                                Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("AppWillRestart", R.string.AppWillRestart), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!enabled);
                    }
                }
//                else if (i == setspecificFont) {
//                    presentFragment(new FontsSelect());
//
//                }
                else if (i == disableMessageClickRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("disableMessageClick", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableMessageClick", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == showUsernameRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean scr = preferences.getBoolean("showUsername", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showUsername", !scr);
                    /*if(!scr){
                        editor.putBoolean("hideMobile", true);
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }*/
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!scr);
                    }
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == hideMobileNumberRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean scr = preferences.getBoolean("hideMobile", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideMobile", !scr);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!scr);
                    }
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == dialogsPicClickRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ClickOnContactPic", R.string.ClickOnContactPic));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled),
                            LocaleController.getString("ShowPics", R.string.ShowPics),
                            LocaleController.getString("ShowProfile", R.string.ShowProfile)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("dialogsClickOnPic", which);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.show();
                } else if (i == dialogsGroupPicClickRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ClickOnGroupPic", R.string.ClickOnGroupPic));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled),
                            LocaleController.getString("ShowPics", R.string.ShowPics),
                            LocaleController.getString("ShowProfile", R.string.ShowProfile)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("dialogsClickOnGroupPic", which);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.show();
                } else if (i == profileSharedOptionsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createSharedOptions(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == notificationInvertMessagesOrderRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean scr = preferences.getBoolean("invertMessagesOrder", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("invertMessagesOrder", !scr);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!scr);
                    }
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                }
            }
        });

        frameLayout.addView(actionBar);

        return fragmentView;
    }

    private AlertDialog.Builder createTabsDialog(AlertDialog.Builder builder) {
        builder.setTitle(LocaleController.getString("HideShowTabs", R.string.HideShowTabs));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
        boolean hideUsers = preferences.getBoolean("hideUsers", false);
        boolean hideGroups = preferences.getBoolean("hideGroups", false);
        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
        boolean hideChannels = preferences.getBoolean("hideChannels", false);
        boolean hideBots = preferences.getBoolean("hideBots", false);
        boolean hideFavs = preferences.getBoolean("hideFavs", true);

        builder.setMultiChoiceItems(
                new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots), LocaleController.getString("Favorites", R.string.Favorites)},
                new boolean[]{!hideUsers, !hideGroups, !hideSGroups, !hideChannels, !hideBots, !hideFavs},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if (which == 0) {
                            editor.putBoolean("hideUsers", !isChecked);
                        } else if (which == 1) {
                            editor.putBoolean("hideGroups", !isChecked);
                        } else if (which == 2) {
                            editor.putBoolean("hideSGroups", !isChecked);
                        } else if (which == 3) {
                            editor.putBoolean("hideChannels", !isChecked);
                        } else if (which == 4) {
                            editor.putBoolean("hideBots", !isChecked);
                        } else if (which == 5) {
                            editor.putBoolean("hideFavs", !isChecked);
                        }
                        editor.apply();

                        boolean hideUsers = preferences.getBoolean("hideUsers", false);
                        boolean hideGroups = preferences.getBoolean("hideGroups", false);
                        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                        boolean hideChannels = preferences.getBoolean("hideChannels", false);
                        boolean hideBots = preferences.getBoolean("hideBots", false);
                        boolean hideFavs = preferences.getBoolean("hideFavs", true);
                        if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs) {
                            editor.putBoolean("hideTabs", true);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabs, which);
                    }
                });
        return builder;
    }

    private AlertDialog.Builder createSharedOptions(AlertDialog.Builder builder) {
        builder.setTitle(LocaleController.getString("SharedMedia", R.string.SharedMedia));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
        boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
        boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
        boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
        boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);
        CharSequence[] cs = BuildVars.DEBUG_VERSION ? new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle), LocaleController.getString("LinksTitle", R.string.LinksTitle)} :
                new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle)};
        boolean[] b = BuildVars.DEBUG_VERSION ? new boolean[]{!hideMedia, !hideFiles, !hideMusic, !hideLinks} :
                new boolean[]{!hideMedia, !hideFiles, !hideMusic};
        builder.setMultiChoiceItems(cs, b,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if (which == 0) {
                            editor.putBoolean("hideSharedMedia", !isChecked);
                        } else if (which == 1) {
                            editor.putBoolean("hideSharedFiles", !isChecked);
                        } else if (which == 2) {
                            editor.putBoolean("hideSharedMusic", !isChecked);
                        } else if (which == 3) {
                            editor.putBoolean("hideSharedLinks", !isChecked);
                        }
                        editor.apply();
                    }
                });
        return builder;
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
    public void didReceivedNotification(int id,int account, Object... args) {

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
            return i == showAndroidEmojiRow || i == useDeviceFontRow ||
//                    i == setspecificFont ||
                    i == profileSharedOptionsRow ||
                    i == disableMessageClickRow ||
                    i == hideMobileNumberRow || i == showUsernameRow ||
                    i == notificationInvertMessagesOrderRow || i == dialogsPicClickRow || i == dialogsGroupPicClickRow;
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
                if (i == dialogsPicClickRow) {
                    String value;
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    int sort = preferences.getInt("dialogsClickOnPic", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled);
                    } else if (sort == 1) {
                        value = LocaleController.getString("ShowPics", R.string.ShowPics);
                    } else {
                        value = LocaleController.getString("ShowProfile", R.string.ShowProfile);
                    }
                    textCell.setTextAndValue(LocaleController.getString("ClickOnContactPic", R.string.ClickOnContactPic), value, true);
                } else if (i == dialogsGroupPicClickRow) {
                    String value;
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    int sort = preferences.getInt("dialogsClickOnGroupPic", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled);
                    } else if (sort == 1) {
                        value = LocaleController.getString("ShowPics", R.string.ShowPics);
                    } else {
                        value = LocaleController.getString("ShowProfile", R.string.ShowProfile);
                    }
                    textCell.setTextAndValue(LocaleController.getString("ClickOnGroupPic", R.string.ClickOnGroupPic), value, true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == disableMessageClickRow) {
                    textCell.setTextAndCheck(LocaleController.getString("DisableMessageClick", R.string.DisableMessageClick), preferences.getBoolean("disableMessageClick", false), true);
                }
                if (i == showAndroidEmojiRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ShowAndroidEmoji", R.string.ShowAndroidEmoji), preferences.getBoolean("showAndroidEmoji", false), true);
                } else if (i == useDeviceFontRow) {
                    textCell.setTextAndCheck(LocaleController.getString("UseDeviceFont", R.string.UseDeviceFont), preferences.getBoolean("useDeviceFont", false), true);
                } else if (i == hideMobileNumberRow) {
                    textCell.setTextAndCheck(LocaleController.getString("HideMobile", R.string.HideMobile), preferences.getBoolean("hideMobile", false), true);
                } else if (i == showUsernameRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ShowUsernameInMenu", R.string.ShowUsernameInMenu), preferences.getBoolean("showUsername", false), true);
                } else if (i == notificationInvertMessagesOrderRow) {
                    textCell.setTextAndCheck(LocaleController.getString("InvertMessageOrder", R.string.InvertMessageOrder), preferences.getBoolean("invertMessagesOrder", false), true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == settingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("General", R.string.General));
                } else if (i == messagesSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == profileSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("ProfileScreen", R.string.ProfileScreen));
                } else if (i == drawerSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("NavigationDrawer", R.string.NavigationDrawer));
                } else if (i == privacySectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("PrivacySettings", R.string.PrivacySettings));
                } else if (i == notificationSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("Notifications", R.string.Notifications));
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

//                if (i == setspecificFont) {
//                    textCell.setMultilineDetail(true);
//                    textCell.setTextAndValue(LocaleController.getString("FontChange", R.string.FontChange), "", true);
//
//                } else

                if (i == profileSharedOptionsRow) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);

                    boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
                    boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
                    boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
                    boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);

                    value = LocaleController.getString("SharedMedia", R.string.SharedMedia);

                    String text = "";
                    if (!hideMedia) {
                        text += LocaleController.getString("Users", R.string.SharedMediaTitle);
                    }
                    if (!hideFiles) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle);
                    }
                    if (!hideMusic) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AudioTitle", R.string.AudioTitle);
                    }
                    if (!hideLinks && BuildVars.DEBUG_VERSION) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("LinksTitle", R.string.LinksTitle);
                    }

                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
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
            if (i == settingsSectionRow || i == messagesSectionRow || i == profileSectionRow || i == drawerSectionRow || i == privacySectionRow ||
                    i == notificationSectionRow) {
                return 1;
            } else if (i == disableMessageClickRow ||
                    i == showAndroidEmojiRow || i == useDeviceFontRow ||
                    i == hideMobileNumberRow || i == showUsernameRow ||
                    i == notificationInvertMessagesOrderRow) {
                return 3;
            } else if (i == dialogsPicClickRow || i == dialogsGroupPicClickRow) {
                return 2;
            } else if (i == profileSharedOptionsRow /*|| i == setspecificFont*/) {
                return 6;
            } else if (i == settingsSectionRow2 || i == messagesSectionRow2 || i == profileSectionRow2 || i == drawerSectionRow2 ||
                    i == privacySectionRow2 || i == notificationSection2Row) {
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
                //new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumb),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                //new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, 0, new Class[]{TextInfoCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText5),

        };
    }

}

