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
import org.telegram.ui.Components.NumberPicker;
import sections.materials.BaseFragmentAdapter;


public class chatSettingsActivity  extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListView listView;
    private ListAdapter listAdapter;

    private int overscrollRow;
    private int emptyRow;


    private int mediaDownloadSection;
    private int mediaDownloadSection2;


    private int messagesSectionRow;
    private int messagesSectionRow2;

    private int profileSectionRow;
    private int profileSectionRow2;
    private int profileSharedOptionsRow;




    private int rowCount;
    private int disableMessageClickRow;
    private int keepOriginalFilenameRow;
    private int keepOriginalFilenameDetailRow;
    private int emojiPopupSize;
    private int maxPinnedDialogsCount;
    private int disableAudioStopRow;


    private int chatShowDirectShareBtn;
    private int chatDirectShareToMenu;
    private int chatDirectShareReplies;
    private int chatDirectShareFavsFirst;
    private int chatHideLeftGroupRow;
    private int chatHideJoinedGroupRow;
    private int chatHideBotKeyboardRow;
    private int chatSearchUserOnTwitterRow;
    private int chatShowPaintRow;
    private int chatFloatDateRow;
    private int chatStickerRow;
    private int chatvoiceRow;
    private int showAdminTextView;


    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        overscrollRow = -1;
        emptyRow = -1;



        messagesSectionRow = rowCount++;
        messagesSectionRow2 = rowCount++;

        emojiPopupSize = rowCount++;
        maxPinnedDialogsCount = rowCount++;

        disableAudioStopRow = rowCount++;
        disableMessageClickRow = rowCount++;
        chatShowDirectShareBtn = rowCount++;
        chatDirectShareReplies = rowCount++;
        chatDirectShareToMenu = rowCount++;
        chatDirectShareFavsFirst = rowCount++;
        chatHideLeftGroupRow = rowCount++;
        chatHideJoinedGroupRow = -1;
        chatHideBotKeyboardRow = rowCount++;
        chatSearchUserOnTwitterRow = rowCount++;
        chatShowPaintRow = rowCount++;
        chatFloatDateRow = rowCount++;
        chatStickerRow = rowCount++;
        chatvoiceRow = rowCount++;
        showAdminTextView = rowCount++;


        profileSectionRow = rowCount++;
        profileSectionRow2 = rowCount++;

        profileSharedOptionsRow = rowCount++;



        mediaDownloadSection = rowCount++;
        mediaDownloadSection2 = rowCount++;
        keepOriginalFilenameRow = rowCount++;
        keepOriginalFilenameDetailRow = rowCount++;


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
        actionBar.setTitle(LocaleController.getString("ChatsSettings", R.string.ChatsSettings));

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


//        AndroidUtilities.setListViewEdgeEffectColor(listView, hColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                if (i == emojiPopupSize) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("EmojiPopupSize", R.string.EmojiPopupSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(60);
                    numberPicker.setMaxValue(100);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    numberPicker.setValue(preferences.getInt("emojiPopupSize", AndroidUtilities.isTablet() ? 65 : 60));
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("emojiPopupSize", numberPicker.getValue());
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == maxPinnedDialogsCount) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("maxPinnedDialogsCount", R.string.maxPinnedDialogsCount));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(5);
                    numberPicker.setMaxValue(20);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                    numberPicker.setValue(preferences.getInt("maxPinnedDialogsCount", 5 ));
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("maxPinnedDialogsCount", numberPicker.getValue());
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                }  else if (i == disableAudioStopRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("disableAudioStop", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableAudioStop", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == disableMessageClickRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("disableMessageClick", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableMessageClick", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareReplies) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareReplies", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareReplies", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareToMenu) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareToMenu", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareToMenu", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareFavsFirst) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareFavsFirst", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareFavsFirst", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                }  else if (i == chatHideLeftGroupRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideLeftGroup", false);
                    MessagesController.getInstance(currentAccount).hideLeftGroup = !hide;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideLeftGroup", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == chatHideJoinedGroupRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideJoinedGroup", false);
                    MessagesController.getInstance(currentAccount).hideJoinedGroup = !hide;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideJoinedGroup", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == chatHideBotKeyboardRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideBotKeyboard", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideBotKeyboard", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }  else if (i == keepOriginalFilenameRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean keep = preferences.getBoolean("keepOriginalFilename", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("keepOriginalFilename", !keep);
                    editor.apply();
                    ApplicationLoader.KEEP_ORIGINAL_FILENAME = !keep;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!keep);
                    }
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
                } else if (i == chatShowDirectShareBtn) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createDialog(builder, chatShowDirectShareBtn);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == chatSearchUserOnTwitterRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("searchOnTwitter", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("searchOnTwitter", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }else if (i == chatShowPaintRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("chatShowPaintRow2", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatShowPaintRow2", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }else if (i == chatFloatDateRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("chatFloatDateRow", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatFloatDateRow", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }else if (i == chatStickerRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("chatStickerRow", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatStickerRow", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }else if (i == chatvoiceRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("chatvoiceRow", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("chatvoiceRow", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                }else if (i == showAdminTextView) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("showAdminTextView", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showAdminTextView", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
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

    private AlertDialog.Builder createDialog(AlertDialog.Builder builder, int i){
        if (i == chatShowDirectShareBtn) {
            builder.setTitle(LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton));

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
            //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", false);
            boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
            boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
            boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
            boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

            builder.setMultiChoiceItems(
                    new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots)},
                    new boolean[]{showDSBtnUsers, showDSBtnGroups, showDSBtnSGroups, showDSBtnChannels, showDSBtnBots},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (which == 0) {
                                editor.putBoolean("showDSBtnUsers", isChecked);
                                Theme.plusShowDSBtnUsers = isChecked;
                            } else if (which == 1) {
                                editor.putBoolean("showDSBtnGroups", isChecked);
                                Theme.plusShowDSBtnGroups = isChecked;
                            } else if (which == 2) {
                                editor.putBoolean("showDSBtnSGroups", isChecked);
                                Theme.plusShowDSBtnSGroups = isChecked;
                            } else if (which == 3) {
                                editor.putBoolean("showDSBtnChannels", isChecked);
                                Theme.plusShowDSBtnChannels = isChecked;
                            } else if (which == 4) {
                                editor.putBoolean("showDSBtnBots", isChecked);
                                Theme.plusShowDSBtnBots = isChecked;
                            }
                            editor.apply();

                        }
                    });
        }

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
            return  i == emojiPopupSize ||i == maxPinnedDialogsCount ||  i == chatShowDirectShareBtn || i == profileSharedOptionsRow ||
                    i == disableAudioStopRow || i == disableMessageClickRow || i == chatDirectShareToMenu || i == chatDirectShareReplies || i == chatDirectShareFavsFirst  ||
                    i == chatHideLeftGroupRow || i == chatHideJoinedGroupRow || i == chatHideBotKeyboardRow || i == chatSearchUserOnTwitterRow || i == chatShowPaintRow  || i == chatFloatDateRow ||i == chatStickerRow ||i == chatvoiceRow ||i == showAdminTextView ||
                    i == keepOriginalFilenameRow ;
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
                if (i == emojiPopupSize) {
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int size = preferences.getInt("emojiPopupSize", AndroidUtilities.isTablet() ? 65 : 60);
                    textCell.setTextAndValue(LocaleController.getString("EmojiPopupSize", R.string.EmojiPopupSize), String.format("%d", size), true);
                } else if (i == maxPinnedDialogsCount) {
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int size = mainPreferences.getInt("maxPinnedDialogsCount",5);
                    textCell.setTextAndValue(LocaleController.getString("maxPinnedDialogsCount", R.string.maxPinnedDialogsCount), String.format("%d", size), true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == disableAudioStopRow) {
                    textCell.setTextAndCheck(LocaleController.getString("DisableAudioStop", R.string.DisableAudioStop), preferences.getBoolean("disableAudioStop", false), true);
                } else if (i == disableMessageClickRow) {
                    textCell.setTextAndCheck(LocaleController.getString("DisableMessageClick", R.string.DisableMessageClick), preferences.getBoolean("disableMessageClick", false), true);
                } else if (i == chatDirectShareReplies) {
                    textCell.setTextAndCheck(LocaleController.getString("DirectShareReplies", R.string.DirectShareReplies), preferences.getBoolean("directShareReplies", false), true);
                } else if (i == chatDirectShareToMenu) {
                    textCell.setTextAndCheck(LocaleController.getString("DirectShareToMenu", R.string.DirectShareToMenu), preferences.getBoolean("directShareToMenu", true), true);
                } else if (i == chatDirectShareFavsFirst) {
                    textCell.setTextAndCheck(LocaleController.getString("DirectShareShowFavsFirst", R.string.DirectShareShowFavsFirst), preferences.getBoolean("directShareFavsFirst", false), true);
                }  else if (i == chatHideLeftGroupRow) {
                    textCell.setTextAndCheck(LocaleController.getString("HideLeftGroup", R.string.HideLeftGroup), preferences.getBoolean("hideLeftGroup", false), true);
                } else if (i == chatHideJoinedGroupRow) {
                    textCell.setTextAndCheck(LocaleController.getString("HideJoinedGroup", R.string.HideJoinedGroup), preferences.getBoolean("hideJoinedGroup", false), true);
                } else if (i == chatHideBotKeyboardRow) {
                    textCell.setTextAndCheck(LocaleController.getString("HideBotKeyboard", R.string.HideBotKeyboard), preferences.getBoolean("hideBotKeyboard", false), true);
                } else if (i == keepOriginalFilenameRow) {
                    textCell.setTextAndCheck(LocaleController.getString("KeepOriginalFilename", R.string.KeepOriginalFilename), preferences.getBoolean("keepOriginalFilename", false), false);
                } else if (i == chatSearchUserOnTwitterRow) {
                    textCell.setTextAndCheck(LocaleController.getString("SearchUserOnTwitter", R.string.SearchUserOnTwitter), preferences.getBoolean("searchOnTwitter", true), true);
                }else if (i == chatShowPaintRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ShowPaint", R.string.ShowPaint), preferences.getBoolean("chatShowPaintRow2", true), true);
                }else if (i == chatFloatDateRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ShowChatFloatDate", R.string.ShowChatFloatDate), preferences.getBoolean("chatFloatDateRow", true), true);
                }else if (i == chatStickerRow) {
                    textCell.setTextAndCheck(LocaleController.getString("chatStickerRow", R.string.chatStickerRow), preferences.getBoolean("chatStickerRow", true), true);
                }else if (i == chatvoiceRow) {
                    textCell.setTextAndCheck(LocaleController.getString("chatvoiceRow", R.string.chatvoiceRow), preferences.getBoolean("chatvoiceRow", true), true);
                }else if (i == showAdminTextView) {
                    textCell.setTextAndCheck(LocaleController.getString("showAdminTextView", R.string.showAdminTextView), preferences.getBoolean("showAdminTextView", false), true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
               if (i == messagesSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == profileSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("ProfileScreen", R.string.ProfileScreen));
                } else if (i == mediaDownloadSection2) {
                    ((HeaderCell) view).setText(LocaleController.getString("SharedMedia", R.string.SharedMedia));
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if (i == chatShowDirectShareBtn) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", false);
                    boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
                    boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
                    boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
                    boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

                    value = LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton);

                    String text = "";
                    if (showDSBtnUsers) {
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (showDSBtnGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (showDSBtnSGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (showDSBtnChannels) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (showDSBtnBots) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }

                    if (text.length() == 0) {
                        text = LocaleController.getString("Channels", R.string.UsernameEmpty);
                    }
                    textCell.setTextAndValue(value, text, true);
                }  else if (i == profileSharedOptionsRow) {
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
                if (i == keepOriginalFilenameDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("KeepOriginalFilenameHelp", R.string.KeepOriginalFilenameHelp));
                    view.setBackgroundResource(R.drawable.greydivider);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (i == messagesSectionRow || i == profileSectionRow ||
                    i == mediaDownloadSection) {
                return 1;
            } else if (i == disableAudioStopRow || i == disableMessageClickRow ||
                    i == keepOriginalFilenameRow|| i == chatDirectShareToMenu || i == chatDirectShareReplies || i == chatDirectShareFavsFirst ||
                    i == chatHideLeftGroupRow || i == chatHideJoinedGroupRow || i == chatHideBotKeyboardRow|| i == chatShowPaintRow  || i == chatFloatDateRow || i == chatStickerRow || i == chatvoiceRow || i == showAdminTextView || i == chatSearchUserOnTwitterRow) {
                return 3;
            } else if (i == emojiPopupSize  || i == maxPinnedDialogsCount) {
                return 2;
            } else if (i == chatShowDirectShareBtn || i == profileSharedOptionsRow) {
                return 6;
            } else if (i == keepOriginalFilenameDetailRow) {
                return 7;
            } else if ( i == messagesSectionRow2 || i == profileSectionRow2  ||
                    i == mediaDownloadSection2 ) {
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

