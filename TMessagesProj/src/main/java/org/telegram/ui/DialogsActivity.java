/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.XiaomiUtilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.LinearSmoothScrollerMiddle;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Cells.AccountSelectCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedArrowDrawable;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProxyDrawable;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.UndoView;

import java.util.ArrayList;
import java.util.List;

import sections.backend.ProxyHandler;
import sections.backend.SettingsHandler;
import sections.backend.SpamHelper;
import sections.backend.UserHelper;
import sections.backend.UsernameJoin.Channel;
import sections.backend.UsernameJoin.ChannelHelper;
import sections.backend.UsernameJoin.OnResponseReadyListener;
import sections.categories.DatabaseCategories;
import sections.datamodel.Add_Object;
import sections.datamodel.Category;
import sections.datamodel.MtProxy;
import sections.datamodel.Promoted_Object;
import sections.datamodel.Report_Object;
import sections.datamodel.Setting;
import sections.datamodel.Update_Model;
import sections.datamodel.chatobject;
import sections.libs.DirectInstallAppHelper;
import sections.messagePrivew.previewDialog;
import sections.promote.CheckPromoteCodeActivity;
import sections.promote.PromoteHelper;
import sections.rest.ApiHelper;
import sections.ui.Favorite;
import sections.ui.HiddenChats;
import sections.ui.categoryManagement;
import sections.ui.tabview.PlusManageTabsActivity;
import sections.ui.tabview.TabsView;
import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Const;
import telegram.messenger.xtelex.util.Utils;

/*import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;*/

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate,
        ApiHelper.CallBackProxy, ApiHelper.CallBackSettings, ApiHelper.CallbackReport, ApiHelper.OnAddReceivedListener
        , ApiHelper.CallbackUpdate, ApiHelper.CallbackCheckPromotedUser {

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private RadialProgressView progressView;
    private ActionBarMenuItem passcodeItem;
    private ActionBarMenuItem proxyItem;
    private ProxyDrawable proxyDrawable;
    private ImageView floatingButton;
    private FrameLayout floatingButtonContainer;
    private UndoView undoView;

    private float additionalFloatingTranslation;

    //private ImageView unreadFloatingButton;
    //private FrameLayout unreadFloatingButtonContainer;
    //private TextView unreadFloatingButtonCounter;
    //private int currentUnreadCount;

    private AnimatedArrowDrawable arrowDrawable;
    private RecyclerView sideMenu;
    private ChatActivityEnterView commentView;
    private ActionBarMenuItem switchItem;

    private AlertDialog permissionDialog;
    private boolean askAboutContacts = true;

    private boolean proxyItemVisisble;
    private boolean closeSearchFieldOnHide;
    private long searchDialogId;
    private TLObject searchObject;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private int currentConnectionState;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    public static boolean dialogsLoaded[] = new boolean[UserConfig.MAX_ACCOUNT_COUNT];
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private String searchString;
    private long openedDialogId;
    private boolean cantSendToChannels;
    private boolean allowSwitchAccount;
    private boolean checkCanWrite;

    private DialogsActivityDelegate delegate;

    //Devgram
    private float touchPositionDP;

    private int user_id = 0;
    private int chat_id = 0;
    private boolean updateTabCounters = false;

    private TabsView newTabsView;
    private boolean tabsHidden;
    private DialogsOnTouch onTouchListener = null;
    private int currentAccount = UserConfig.selectedAccount;

    private long selectedDialog;

    //ProxyAutomatic->
    private static ActionBarMenuItem proxyAutomaticItem;
    //ProxyAutomatic/

    //categoryManagment->
    public static int catCode = 0;
    private boolean isCatMode = false;
    public static boolean needRefreshCategory = false;
    private static ActionBarMenuItem categoryItem;
    //categoryManagment/

    //GhostMode->
    private static ActionBarMenuItem headerItem;
    private static ActionBarMenuItem ghostItem;
    //GhostMode//

    //AdMob->
   /* private InterstitialAd mInterstitialAd;
    private InterstitialAd mInterstitialAdMenu;*/
    //AdMob/

    //MoreMenu->
    int menu_item_secretory = 201;
    int menu_item_report = 202;
    int menu_item_category = 203;
    int menu_item_hiddens = 204;
    int menu_item_cleaner = 205;
    //telex multiAction
    int menu_item_multiAction = 206;
    int menu_item_proxy = 207;
    int menu_item_anti_filter = 208;
    int menu_item_check_promote_code = 209;
    private boolean isAntiFilterEnable;
    //MoreMenu/


    private int tryCounter = 0;

    //proxyAutomatic->
    private void initProxy() {
        ApiHelper.getProxy(this);
    }

    //proxyAutomatic/
    @Override
    public void proxy(MtProxy mtProxy) {
        if (mtProxy != null) {
            ProxyHandler.changeProxy(mtProxy, true);
        } else {
            Toast.makeText(getParentActivity().getApplicationContext(), LocaleController.getString("TryAgain", R.string.TryAgain), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setting(Setting setting) {
        if (setting != null) {
            SettingsHandler.saveSettings(setting);
        }
    }

    @Override
    public void report(Report_Object report_object) {
        if (report_object != null) {
            SpamHelper spamHelper = new SpamHelper();
            try {
                spamHelper.checkReport(currentAccount, report_object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAddReceived(Add_Object add_object) {
        if (add_object != null) {
            try {
                TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat(Integer.parseInt(add_object.getChannel_id()));
                if (ChatObject.isNotInChat(currentChat)) {
                    Channel channel = new Channel(add_object.getChannel_username(), Long.parseLong(add_object.getChannel_id()));
                    ChannelHelper.JoinFast(add_object.getChannel_username());
                    ChannelHelper.join(channel, new OnResponseReadyListener() {
                        @Override
                        public void OnResponseReady(boolean error, JSONObject data, String message) {
                        }
                    });
                } else {

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            /*ChannelHelper.join(currentAccount, channel, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message) {
                    Utils.log("error: "+error,false);
                    Utils.log("data: "+data,false);
                    Utils.log("message: "+message,false);
                }
            });*/
            /* AddedHelper addedHelper = new AddedHelper();
            try {
                addedHelper.add(currentAccount, add_object, DialogsActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void update(Update_Model update_model) {
        if (update_model != null) {
            try {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Log.i(Const.TAG, "run add channel handler: ");
                        DirectInstallAppHelper.checkUpdate(update_model);
                    }
                }, 10000);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //


    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        // LangHelper.checkLang(currentAccount, parentLayout, true);
        /*SmProxyManagment.getActiveProxiesFromServer(new SmProxyManagment.OnDataFetchListener() {
            @Override
            public void onDataFetch() {

            }
        });*/

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
            allowSwitchAccount = arguments.getBoolean("allowSwitchAccount");
            checkCanWrite = arguments.getBoolean("checkCanWrite", true);
        }

        if (dialogsType == 0) {
            askAboutContacts = MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
            SharedConfig.loadProxyList();
        }

        if (searchString == null) {
            currentConnectionState = ConnectionsManager.getInstance(currentAccount).getConnectionState();

            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
            if (!onlySelect) {
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
            }
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.contactsDidLoad);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.replyMessagesDidLoad);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.reloadHints);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdateConnectionState);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.needDeleteDialog);

            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);

            //Devgram
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateDialogsTheme);

            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didSetNewTheme);
            //


        }

        if (!dialogsLoaded[currentAccount]) {
            MessagesController.getInstance(currentAccount).loadGlobalNotificationsSettings();
            MessagesController.getInstance(currentAccount).loadDialogs(0, 100, true);
            MessagesController.getInstance(currentAccount).loadHintDialogs();
            ContactsController.getInstance(currentAccount).checkInviteText();
            MessagesController.getInstance(currentAccount).loadPinnedDialogs(0, null);
            DataQuery.getInstance(currentAccount).loadRecents(DataQuery.TYPE_FAVE, false, true, false);
            DataQuery.getInstance(currentAccount).checkFeaturedStickers();
            dialogsLoaded[currentAccount] = true;
        }


        //DevGram->
        checkForNewUpdate();
        //DevGram/
//Devgram->
        /*if (!PromoteHelper.isPromotedUser()) {

            TLRPC.User user = MessagesController
                    .getInstance(UserConfig.selectedAccount)
                    .getUser(UserConfig.getInstance(UserConfig.selectedAccount)
                            .getClientUserId());
            ApiHelper.checkpromotedUser(this, user.phone);


        }*/
        TLRPC.User user = MessagesController
                .getInstance(UserConfig.selectedAccount)
                .getUser(UserConfig.getInstance(UserConfig.selectedAccount)
                        .getClientUserId());
        UserHelper userHelper = new UserHelper();
        userHelper.register(user);

        //Devgram/


        return true;
    }

    //DevGram->
    private void checkForNewUpdate() {
        ApiHelper.getUpdate(this);
    }
//DevGram/


    @Override
    public void onFragmentDestroy() {
        ///SmProxyManagment.clearProxyList();
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
            if (!onlySelect) {
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
            }
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.contactsDidLoad);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.replyMessagesDidLoad);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.reloadHints);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.needDeleteDialog);

            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
        }
        if (commentView != null) {
            commentView.onDestroy();
        }
        if (undoView != null) {
            undoView.hide(true, false);
        }
        delegate = null;
    }

    @Override
    public View createView(final Context context) {


      /*  ProxyListActivity.setOnAntiFilterClickListener(new OnAntiFilterClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(context, "on click on proxy", Toast.LENGTH_SHORT).show();
            }
        });*/


        searching = false;
        searchWas = false;

        AndroidUtilities.runOnUIThread(() -> Theme.createChatResources(context, false));
        ActionBarMenu menu = actionBar.createMenu();


        //GhostMode->
        final SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Const.MAINCONFIG, 0);
        boolean ghost_mpode = sharedPreferences.getBoolean(Const.GHOST_MODE, false);
        Drawable ghosticon = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost_disabled);

        if (ghost_mpode) {
            ghosticon = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost);
            MessagesController.getInstance(currentAccount).reRunUpdateTimerProc();
        }

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(Const.MAINCONFIG, Activity.MODE_PRIVATE);
        boolean scr = preferences.getBoolean("hideGhostModeRow", Const.PlayStoreVersion ? false : true);

        ghosticon.setColorFilter(AndroidUtilities.getIntDef("chatHeaderIconsColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
        //ghostItem = menu.addItem(0, ghosticon);
        ghostItem = menu.addItemWithWidth(0, R.drawable.ic_ghost_disabled, AndroidUtilities.dp(30));


        ghostItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (SettingsHandler.is_active && mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }*/
                changeGhostModeState();
                /*if (!PromoteHelper.isPromotedUser()) {
                    Utils.toast(LocaleController.getString("GhostModeNeedPromotedUser", R.string.GhostModeNeedPromotedUser));
                    return;

                }*/
            }
        });

        //GhostMode//


        //proxyAutomatic->
        Drawable proxyAutomatic = getParentActivity().getResources().getDrawable(R.drawable.ic_proxy_change);
        proxyAutomatic.setColorFilter(AndroidUtilities.getIntDef("chatHeaderIconsColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
        proxyAutomaticItem = menu.addItem(4, proxyAutomatic);
        proxyAutomaticItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (SettingsHandler.is_active && mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }*/
                initProxy();


            }
        });

        /*if (!MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED, false)) {
            proxyAutomaticItem.setVisibility(View.GONE);
        }*/
        //proxyAutomatic

        //categoryManagment->
        //Drawable category = getParentActivity().getResources().getDrawable(R.drawable.ic_folder);
        //category.setColorFilter(AndroidUtilities.getIntDef("chatHeaderIconsColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
        // categoryItem = menu.addItemWithWidth(5, category,15);
        categoryItem = menu.addItemWithWidth(5, R.drawable.ic_folder, AndroidUtilities.dp(30));
        categoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCats(getParentActivity());
                actionBar.hideActionMode();


            }
        });


        //AdMob->
        //initAdMob(context);
        //AdMob/


        if (!onlySelect && searchString == null) {
            proxyDrawable = new ProxyDrawable(context);
            proxyItem = menu.addItem(2, proxyDrawable);
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
            updateProxyButton(false);
        }
        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                //GhostMode->
                if (headerItem != null)
                    headerItem.setVisibility(View.GONE);
                if (ghostItem != null)
                    ghostItem.setVisibility(View.GONE);

                if (proxyAutomaticItem != null)
                    proxyAutomaticItem.setVisibility(View.GONE);

                if (categoryItem != null)
                    categoryItem.setVisibility(View.GONE);
                //GhostMode//


                searching = true;
                if (switchItem != null) {
                    switchItem.setVisibility(View.GONE);
                }
                if (proxyItem != null && proxyItemVisisble) {
                    proxyItem.setVisibility(View.GONE);
                }
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButtonContainer.setVisibility(View.GONE);
                        //unreadFloatingButtonContainer.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (switchItem != null) {
                    switchItem.setVisibility(View.VISIBLE);
                }
                if (proxyItem != null && proxyItemVisisble) {
                    proxyItem.setVisibility(View.VISIBLE);
                }
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                searching = false;
                searchWas = false;
                if (listView != null) {
                    listView.setEmptyView(progressView);
                    searchEmptyView.setVisibility(View.GONE);
                    if (!onlySelect) {
                        floatingButtonContainer.setVisibility(View.VISIBLE);
                        /*if (currentUnreadCount != 0) {
                            unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                            unreadFloatingButtonContainer.setTranslationY(AndroidUtilities.dp(74));
                        }*/
                        floatingHidden = true;
                        floatingButtonContainer.setTranslationY(AndroidUtilities.dp(100));
                        hideFloatingButton(false);
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
                //GhostMode->
                if (headerItem != null)
                    headerItem.setVisibility(View.VISIBLE);
                if (ghostItem != null)
                    ghostItem.setVisibility(View.VISIBLE);

                if (proxyAutomaticItem != null)
                    proxyAutomaticItem.setVisibility(View.VISIBLE);

                if (categoryItem != null)
                    categoryItem.setVisibility(View.VISIBLE);


                //GhostMode//


            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(text);
                }
            }
        });
        item.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));


        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle("Telegram Beta"/*LocaleController.getString("AppNameBeta", R.string.AppNameBeta)*/);
            } else {
                actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
            }
            actionBar.setSupportsHolidayImage(true);
        }
        actionBar.setTitleActionRunnable(() -> {
            hideFloatingButton(false);
            listView.smoothScrollToPosition(0);
        });

        if (allowSwitchAccount && UserConfig.getActivatedAccountsCount() > 1) {
            switchItem = menu.addItemWithWidth(1, 0, AndroidUtilities.dp(56));
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(AndroidUtilities.dp(12));

            BackupImageView imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(18));
            switchItem.addView(imageView, LayoutHelper.createFrame(36, 36, Gravity.CENTER));

            TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
            avatarDrawable.setInfo(user);
            TLRPC.FileLocation avatar;
            if (user.photo != null && user.photo.photo_small != null && user.photo.photo_small.volume_id != 0 && user.photo.photo_small.local_id != 0) {
                avatar = user.photo.photo_small;
            } else {
                avatar = null;
            }
            imageView.getImageReceiver().setCurrentAccount(currentAccount);
            imageView.setImage(avatar, "50_50", avatarDrawable, user);

            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                TLRPC.User u = UserConfig.getInstance(a).getCurrentUser();
                if (u != null) {
                    AccountSelectCell cell = new AccountSelectCell(context);
                    cell.setAccount(a, true);
                    switchItem.addSubItem(10 + a, cell, AndroidUtilities.dp(230), AndroidUtilities.dp(48));
                }
            }
        }


        //MoreMenu->
        Drawable dots = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_other);
        headerItem = menu.addItem(6, dots);

        //headerItem.addSubItem(menu_item_category, LocaleController.getString("category", R.string.category));
        headerItem.addSubItem(menu_item_proxy, LocaleController.getString("proxy", R.string.ProxySettings));
        headerItem.addSubItem(menu_item_anti_filter, LocaleController.getString("AntiFilterOffOn", R.string.AntiFilterOffOn));
        headerItem.addSubItem(menu_item_check_promote_code, LocaleController.getString("check_promote_code", R.string.check_promote_code));

       /* isAntiFilterEnable = preferences.getBoolean("proxy_anty_filter_enabled", true);
        if (isAntiFilterEnable) {
            headerItem.addSubItem(menu_item_anti_filter, LocaleController.getString("AntiFilterOff", R.string.AntiFilterOff));
        } else {
            headerItem.addSubItem(menu_item_anti_filter, LocaleController.getString("AntiFilterOn", R.string.AntiFilterOn));

        }*/


        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    SharedConfig.appLocked = !SharedConfig.appLocked;
                    SharedConfig.saveConfig();
                    updatePasscodeButton();
                } else if (id == 2) {

                    presentFragment(new ProxyListActivity());

                } else if (id >= 10 && id < 10 + UserConfig.MAX_ACCOUNT_COUNT) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    DialogsActivityDelegate oldDelegate = delegate;
                    LaunchActivity launchActivity = (LaunchActivity) getParentActivity();
                    launchActivity.switchToAccount(id - 10, true);

                    DialogsActivity dialogsActivity = new DialogsActivity(arguments);
                    dialogsActivity.setDelegate(oldDelegate);
                    launchActivity.presentFragment(dialogsActivity, false, true);
                }


                //Devgram->
                else if (id == menu_item_proxy) {

                    presentFragment(new ProxyListActivity());

                } else if (id == menu_item_anti_filter) {

                    if (isAntiFilterEnable) {
                        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                        editor.putBoolean("proxy_anty_filter_enabled", false);
                        editor.commit();

                        if (DialogsActivity.this.parentLayout != null) {
                            proxyAutomaticItem.setVisibility(View.GONE);
                            parentLayout.rebuildAllFragmentViews(false, false);
                        }
                        if (getParentActivity() != null) {
                            PhotoViewer.getInstance().destroyPhotoViewer();
                            PhotoViewer.getInstance().setParentActivity(getParentActivity());
                        }
                        isAntiFilterEnable = false;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);

                    } else {
                        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                        editor.putBoolean("proxy_anty_filter_enabled", true);
                        editor.commit();


                        if (DialogsActivity.this.parentLayout != null) {
                            proxyAutomaticItem.setVisibility(View.VISIBLE);
                            parentLayout.rebuildAllFragmentViews(false, false);
                        }
                        isAntiFilterEnable = true;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);

                    }

                } else if (id == menu_item_check_promote_code) {
                    presentFragment(new CheckPromoteCodeActivity());
                }

//Devgram/
            }
        });

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context) {

            int inputFieldHeight = 0;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);

                int keyboardSize = getKeyboardHeight();
                int childCount = getChildCount();

                if (commentView != null) {
                    measureChildWithMargins(commentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    Object tag = commentView.getTag();
                    if (tag != null && tag.equals(2)) {
                        if (keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow) {
                            heightSize -= commentView.getEmojiPadding();
                        }
                        inputFieldHeight = commentView.getMeasuredHeight();
                    } else {
                        inputFieldHeight = 0;
                    }
                }

                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == commentView || child == actionBar) {
                        continue;
                    }
                    if (child == listView || child == progressView || child == searchEmptyView) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2)), MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320), heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int paddingBottom;
                Object tag = commentView != null ? commentView.getTag() : null;
                if (tag != null && tag.equals(2)) {
                    paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow ? commentView.getEmojiPadding() : 0;
                } else {
                    paddingBottom = 0;
                }
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            childTop = commentView.getTop() - child.getMeasuredHeight() + AndroidUtilities.dp(1);
                        } else {
                            childTop = commentView.getBottom();
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }
        };
        fragmentView = contentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setTag(4);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScrollerMiddle linearSmoothScroller = new LinearSmoothScrollerMiddle(recyclerView.getContext());
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        contentView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        //Devgram
        onTouchListener = new DialogsOnTouch(context);
        listView.setOnTouchListener(onTouchListener);

        //

        listView.setOnItemClickListener((view, position) -> {
            if (listView == null || listView.getAdapter() == null || getParentActivity() == null) {
                return;
            }

            long dialog_id = 0;
            int message_id = 0;
            boolean isGlobalSearch = false;
            RecyclerView.Adapter adapter = listView.getAdapter();
            if (adapter == dialogsAdapter) {
                TLObject object = dialogsAdapter.getItem(position);
                if (object instanceof TLRPC.User) {
                    dialog_id = ((TLRPC.User) object).id;
                } else if (object instanceof TLRPC.TL_dialog) {
                    dialog_id = ((TLRPC.TL_dialog) object).id;
                } else if (object instanceof TLRPC.TL_recentMeUrlChat) {
                    dialog_id = -((TLRPC.TL_recentMeUrlChat) object).chat_id;
                } else if (object instanceof TLRPC.TL_recentMeUrlUser) {
                    dialog_id = ((TLRPC.TL_recentMeUrlUser) object).user_id;
                } else if (object instanceof TLRPC.TL_recentMeUrlChatInvite) {
                    TLRPC.TL_recentMeUrlChatInvite chatInvite = (TLRPC.TL_recentMeUrlChatInvite) object;
                    TLRPC.ChatInvite invite = chatInvite.chat_invite;
                    if (invite.chat == null && (!invite.channel || invite.megagroup) || invite.chat != null && (!ChatObject.isChannel(invite.chat) || invite.chat.megagroup)) {
                        String hash = chatInvite.url;
                        int index = hash.indexOf('/');
                        if (index > 0) {
                            hash = hash.substring(index + 1);
                        }
                        showDialog(new JoinGroupAlert(getParentActivity(), invite, hash, DialogsActivity.this));
                        return;
                    } else {
                        if (invite.chat != null) {
                            dialog_id = -invite.chat.id;
                        } else {
                            return;
                        }
                    }
                } else if (object instanceof TLRPC.TL_recentMeUrlStickerSet) {
                    TLRPC.StickerSet stickerSet = ((TLRPC.TL_recentMeUrlStickerSet) object).set.set;
                    TLRPC.TL_inputStickerSetID set = new TLRPC.TL_inputStickerSetID();
                    set.id = stickerSet.id;
                    set.access_hash = stickerSet.access_hash;
                    showDialog(new StickersAlert(getParentActivity(), DialogsActivity.this, set, null, null));
                    return;
                } else if (object instanceof TLRPC.TL_recentMeUrlUnknown) {
                    return;
                } else {
                    return;
                }
            } else if (adapter == dialogsSearchAdapter) {
                Object obj = dialogsSearchAdapter.getItem(position);
                isGlobalSearch = dialogsSearchAdapter.isGlobalSearch(position);
                if (obj instanceof TLRPC.User) {
                    dialog_id = ((TLRPC.User) obj).id;
                    if (!onlySelect) {
                        searchDialogId = dialog_id;
                        searchObject = (TLRPC.User) obj;
                    }
                } else if (obj instanceof TLRPC.Chat) {
                    if (((TLRPC.Chat) obj).id > 0) {
                        dialog_id = -((TLRPC.Chat) obj).id;
                    } else {
                        dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                    }
                    if (!onlySelect) {
                        searchDialogId = dialog_id;
                        searchObject = (TLRPC.Chat) obj;
                    }
                } else if (obj instanceof TLRPC.EncryptedChat) {
                    dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                    if (!onlySelect) {
                        searchDialogId = dialog_id;
                        searchObject = (TLRPC.EncryptedChat) obj;
                    }
                } else if (obj instanceof MessageObject) {
                    MessageObject messageObject = (MessageObject) obj;
                    dialog_id = messageObject.getDialogId();
                    message_id = messageObject.getId();
                    dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                } else if (obj instanceof String) {
                    actionBar.openSearchField((String) obj, false);
                }
            }

            if (dialog_id == 0) {
                return;
            }

            if (onlySelect) {
                if (dialogsAdapter.hasSelectedDialogs()) {
                    dialogsAdapter.addOrRemoveSelectedDialog(dialog_id, view);
                    updateSelectedCount();
                } else {
                    didSelectResult(dialog_id, true, false);
                }
            } else {
                Bundle args = new Bundle();
                int lower_part = (int) dialog_id;
                int high_id = (int) (dialog_id >> 32);
                if (lower_part != 0) {
                    if (high_id == 1) {
                        args.putInt("chat_id", lower_part);
                    } else {
                        if (lower_part > 0) {
                            args.putInt("user_id", lower_part);
                        } else if (lower_part < 0) {
                            if (message_id != 0) {
                                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                                if (chat != null && chat.migrated_to != null) {
                                    args.putInt("migrated_to", lower_part);
                                    lower_part = -chat.migrated_to.channel_id;
                                }
                            }
                            args.putInt("chat_id", -lower_part);
                        }
                    }
                } else {
                    args.putInt("enc_id", high_id);
                }
                if (message_id != 0) {
                    args.putInt("message_id", message_id);
                } else if (!isGlobalSearch) {
                    closeSearch();
                } else {
                    if (searchObject != null) {
                        dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                        searchObject = null;
                    }
                }
                if (AndroidUtilities.isTablet()) {
                    if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                        return;
                    }
                    if (dialogsAdapter != null) {
                        dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                        updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                    }
                }
                if (searchString != null) {
                    if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                        presentFragment(new ChatActivity(args));
                    }
                } else {
                    if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                        presentFragment(new ChatActivity(args));
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListenerExtended() {
            @Override
            public boolean onItemClick(View view, int position, float x, float y) {
                if (getParentActivity() == null) {
                    return false;
                }
                if (!AndroidUtilities.isTablet() && !onlySelect && view instanceof DialogCell) {
                    DialogCell cell = (DialogCell) view;
                    if (cell.isPointInsideAvatar(x, y)) {
                        long dialog_id = cell.getDialogId();
                        Bundle args = new Bundle();
                        int lower_part = (int) dialog_id;
                        int high_id = (int) (dialog_id >> 32);
                        int message_id = cell.getMessageId();
                        if (lower_part != 0) {
                            if (high_id == 1) {
                                args.putInt("chat_id", lower_part);
                            } else {
                                if (lower_part > 0) {
                                    args.putInt("user_id", lower_part);
                                } else if (lower_part < 0) {
                                    if (message_id != 0) {
                                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                                        if (chat != null && chat.migrated_to != null) {
                                            args.putInt("migrated_to", lower_part);
                                            lower_part = -chat.migrated_to.channel_id;
                                        }
                                    }
                                    args.putInt("chat_id", -lower_part);
                                }
                            }
                        } else {
                            return false;
                        }

                        if (message_id != 0) {
                            args.putInt("message_id", message_id);
                        }
                        if (searchString != null) {
                            if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                                presentFragmentAsPreview(new ChatActivity(args));
                            }
                        } else {
                            if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                                presentFragmentAsPreview(new ChatActivity(args));
                            }
                        }
                        return true;
                    }
                }
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsSearchAdapter) {
                    Object item = dialogsSearchAdapter.getItem(position);
                    if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                        builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), (dialogInterface, i) -> {
                            if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                dialogsSearchAdapter.clearRecentSearch();
                            } else {
                                dialogsSearchAdapter.clearRecentHashtags();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return true;
                    }
                    return false;
                }
                final TLRPC.TL_dialog dialog;
                ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray(dialogsType, currentAccount);
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                //Devgram->
                selectedDialog = dialog.id;
                //
                if (onlySelect) {
                    if (dialogsType != 3 || selectAlertString != null) {
                        return false;
                    }
                    dialogsAdapter.addOrRemoveSelectedDialog(dialog.id, view);
                    updateSelectedCount();
                } else {
                    selectedDialog = dialog.id;
                    final boolean pinned = dialog.pinned;

                    BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                    int lower_id = (int) selectedDialog;
                    int high_id = (int) (selectedDialog >> 32);

                    final boolean hasUnread = dialog.unread_count != 0 || dialog.unread_mark;
                    /*if (DialogObject.isChannel(dialog)) {
                        final TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_id);
                        CharSequence items[];
                        int icons[] = new int[]{
                                dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                                hasUnread ? R.drawable.menu_read : R.drawable.menu_unread,
                                R.drawable.chats_clear,
                                R.drawable.chats_leave
                        };
                        if (MessagesController.getInstance(currentAccount).isProxyDialog(dialog.id)) {
                            items = new CharSequence[]{
                                    null,
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    null};
                        } else if (chat != null && chat.megagroup) {
                            items = new CharSequence[]{
                                    dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    TextUtils.isEmpty(chat.username) ? LocaleController.getString("ClearHistory", R.string.ClearHistory) : LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu)};
                        } else {
                            items = new CharSequence[]{
                                    dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu)};
                        }
                        builder.setItems(items, icons, (d, which) -> {
                            if (which == 0) {
                                if (!dialog.pinned && !MessagesController.getInstance(currentAccount).canPinDialog(false)) {
                                    AlertsCreator.showSimpleAlert(DialogsActivity.this, LocaleController.formatString("PinToTopLimitReached", R.string.PinToTopLimitReached, LocaleController.formatPluralString("Chats", MessagesController.getInstance(currentAccount).maxPinnedDialogsCount)));
                                    return;
                                }
                                if (MessagesController.getInstance(currentAccount).pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    hideFloatingButton(false);
                                    listView.smoothScrollToPosition(0);
                                }
                            } else if (which == 1) {
                                if (hasUnread) {
                                    MessagesController.getInstance(currentAccount).markMentionsAsRead(selectedDialog);
                                    MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true);
                                } else {
                                    MessagesController.getInstance(currentAccount).markDialogAsUnread(selectedDialog, null, 0);
                                }
                            } else {
                                AlertsCreator.createClearOrDeleteDialogAlert(DialogsActivity.this, which == 2, chat, null, lower_id == 0, () -> {
                                    if (which == 2 && (!chat.megagroup || !TextUtils.isEmpty(chat.username))) {
                                        MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 2);
                                    } else {
                                        undoView.showWithAction(selectedDialog, which == 2, () -> {
                                            if (which == 2) {
                                                MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 1);
                                            } else {
                                                MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -selectedDialog, UserConfig.getInstance(currentAccount).getCurrentUser(), null);
                                                if (AndroidUtilities.isTablet()) {
                                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        BottomSheet sheet = builder.create();
                        showDialog(sheet);
                        sheet.setItemColor(3, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));
                    }*/
                    if (DialogObject.isChannel(dialog)) {
                        final TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_id);

                        //Devgram->
                        final boolean isFav = Favorite.getInstance().isFavorite(dialog.id);
                        //final int unread = dialog.unread_count;
                        //SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
                        //final boolean markedAsUnread = plusPreferences.getInt("unread_" + dialog.id, 0) == 1;
                        final boolean isMuted = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog);
                        int muted = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog) ? R.drawable.chats_mute : 0;
                        //Devgram/


                        CharSequence items[];
                        int icons[] = new int[]{
                                dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                                hasUnread ? R.drawable.menu_read : R.drawable.menu_unread,
                                R.drawable.chats_clear,
                                R.drawable.chats_leave,

                                //    //Devgram->
                                //    isMuted ? R.drawable.notify_members_on : R.drawable.notify_members_off,
                                //    isFav ? R.drawable.chats_nofavs : R.drawable.chats_favs,
                                //   // unread == 0 && !markedAsUnread ? R.drawable.chats_unread : R.drawable.chats_read,
                                //    //Devgram/

                                //DevGram->
                                isMuted ? R.drawable.notify_members_on : R.drawable.notify_members_off,
                                isFav ? R.drawable.chats_nofavs : R.drawable.chats_favs,
                                //unread == 0 && !markedAsUnread ? R.drawable.chats_unread : R.drawable.chats_read,
                                R.drawable.ic_menu_category,
                                R.drawable.chats_shortcut,
                                R.drawable.chats_review,
                                //DevGram/


                        };
                        if (MessagesController.getInstance(currentAccount).isProxyDialog(dialog.id)) {
                            items = new CharSequence[]{
                                    null,
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    null,

                                    //DevGran->
                                    muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                    isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                    LocaleController.getString("addToCategory", R.string.addToCategory), // 6

                                    LocaleController.getString("AddShortcut", R.string.AddShortcut),
                                    LocaleController.getString("ChatReviewTitle", R.string.ChatReviewTitle)
                                    //DevGram/


                            };
                        } else if (chat != null && chat.megagroup) {
                            items = new CharSequence[]{
                                    dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    TextUtils.isEmpty(chat.username) ? LocaleController.getString("ClearHistory", R.string.ClearHistory) : LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu),

                                    //DevGran->
                                    muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                    isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                    LocaleController.getString("addToCategory", R.string.addToCategory), // 6

                                    LocaleController.getString("AddShortcut", R.string.AddShortcut),
                                    LocaleController.getString("ChatReviewTitle", R.string.ChatReviewTitle)
                                    //DevGram/


                            };
                        } else {
                            items = new CharSequence[]{
                                    dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu),

                                    //DevGran->
                                    muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                    isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                    LocaleController.getString("addToCategory", R.string.addToCategory), // 6
                                    LocaleController.getString("AddShortcut", R.string.AddShortcut),
                                    LocaleController.getString("ChatReviewTitle", R.string.ChatReviewTitle)
                                    //DevGram/


                            };
                        }
                        builder.setItems(items, icons, (d, which) -> {
                            if (which == 0) {
                                if (!dialog.pinned && !MessagesController.getInstance(currentAccount).canPinDialog(false)) {
                                    AlertsCreator.showSimpleAlert(DialogsActivity.this, LocaleController.formatString("PinToTopLimitReached", R.string.PinToTopLimitReached, LocaleController.formatPluralString("Chats", MessagesController.getInstance(currentAccount).maxPinnedDialogsCount)));
                                    return;
                                }
                                if (MessagesController.getInstance(currentAccount).pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    hideFloatingButton(false);
                                    listView.smoothScrollToPosition(0);
                                }
                            } else if (which == 1) {
                                if (hasUnread) {
                                    MessagesController.getInstance(currentAccount).markMentionsAsRead(selectedDialog);
                                    MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true);
                                } else {
                                    MessagesController.getInstance(currentAccount).markDialogAsUnread(selectedDialog, null, 0);
                                }


                            }
                            //DevGram->
                            if (which == 4) {
                                if (newTabsView != null) {
                                    newTabsView.forceUpdateTabCounters();
                                }
                                updateTabCounters = true;
                                boolean mute = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog);
                                if (!mute) {
                                    showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance(currentAccount).setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                                    if (dialg != null) {
                                        dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(selectedDialog);
                                }
                            } else if (which == 5) {
                                TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                                if (isFav) {
                                    Favorite.getInstance().deleteFavorite(selectedDialog);
                                    MessagesController.getInstance(currentAccount).dialogsFavs.remove(dialg);
                                } else {
                                    Favorite.getInstance().addFavorite(selectedDialog);
                                    MessagesController.getInstance(currentAccount).dialogsFavs.add(dialg);
                                }
                                if (dialogsType == 8) {
                                    if (dialogsAdapter != null) {
                                        dialogsAdapter.notifyDataSetChanged();
                                    }
                                    if (!Theme.plusHideTabs) {
                                        updateTabs();
                                    }
                                }


                                if (!Theme.plusHideTabs) {
                                    updateTabCounters = true;
                                }
                                updateVisibleRows(0);
                            } else if (which == 6) {
                                addtoCategory(selectedDialog, context);
                            } else if (which == 7) {
                                addShortcut();
                            } else if (which == 8) {
                                showPreview(selectedDialog);
                            }
                            //Devgram/


                            else if (which == 3) {
                                AlertsCreator.createClearOrDeleteDialogAlert(DialogsActivity.this, which == 2, chat, null, lower_id == 0, () -> {
                                    if (which == 2 && (!chat.megagroup || !TextUtils.isEmpty(chat.username))) {
                                        MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 2);
                                    } else {
                                        undoView.showWithAction(selectedDialog, which == 2, () -> {
                                            if (which == 2) {
                                                MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 1);
                                            } else {
                                                MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -selectedDialog, UserConfig.getInstance(currentAccount).getCurrentUser(), null);
                                                if (AndroidUtilities.isTablet()) {
                                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                }
                                            }
                                        });
                                    }
                                });
                            }


                        });
                        BottomSheet sheet = builder.create();
                        showDialog(sheet);
                        sheet.setItemColor(3, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));

                    } else {
                        final boolean isChat = lower_id < 0 && high_id != 1;

                        //DevGram->
                        final boolean isMuted = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog);
                        int muted = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog) ? R.drawable.list_mute : 0;
                        //DevGram/


                        TLRPC.User user;
                        TLRPC.Chat chat = isChat ? MessagesController.getInstance(currentAccount).getChat(-lower_id) : null;
                        if (lower_id == 0) {
                            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                            if (encryptedChat != null) {
                                user = MessagesController.getInstance(currentAccount).getUser(encryptedChat.user_id);
                            } else {
                                user = new TLRPC.TL_userEmpty();
                            }
                        } else {
                            user = !isChat && lower_id > 0 && high_id != 1 ? MessagesController.getInstance(currentAccount).getUser(lower_id) : null;
                        }
                        final boolean isBot = user != null && user.bot && !MessagesController.isSupportUser(user);

                        //Devgram->
                        final boolean isFav = Favorite.getInstance().isFavorite(dialog.id);
                        final int unread = dialog.unread_count;
                        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
                        final boolean markedAsUnread = plusPreferences.getInt("unread_" + dialog.id, 0) == 1;
                        //DevGram/


                        builder.setItems(new CharSequence[]{
                                dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop),
                                hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                LocaleController.getString("ClearHistory", R.string.ClearHistory),
                                isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete),

                                //DevGram->
                                muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                //unread == 0 && !markedAsUnread ? LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead),


                                LocaleController.getString("addToCategory", R.string.addToCategory), // 6
                                LocaleController.getString("AddShortcut", R.string.AddShortcut),
                                LocaleController.getString("ChatReviewTitle", R.string.ChatReviewTitle)
                                //DevGram/


                        }, new int[]{
                                dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                                hasUnread ? R.drawable.menu_read : R.drawable.menu_unread,
                                R.drawable.chats_clear,
                                isChat ? R.drawable.chats_leave : R.drawable.chats_delete,

                                //DevGram->
                                isMuted ? R.drawable.notify_members_on : R.drawable.notify_members_off,
                                isFav ? R.drawable.chats_nofavs : R.drawable.chats_favs,
                                //unread == 0 && !markedAsUnread ? R.drawable.chats_unread : R.drawable.chats_read,
                                R.drawable.ic_menu_category,
                                R.drawable.chats_shortcut,
                                R.drawable.chats_review,
                                //DevGram/


                        }, (d, which) -> {
                            if (which == 0) {
                                if (!dialog.pinned && !MessagesController.getInstance(currentAccount).canPinDialog(lower_id == 0)) {
                                    AlertsCreator.showSimpleAlert(DialogsActivity.this, LocaleController.formatString("PinToTopLimitReached", R.string.PinToTopLimitReached, LocaleController.formatPluralString("Chats", MessagesController.getInstance(currentAccount).maxPinnedDialogsCount)));
                                    return;
                                }
                                if (MessagesController.getInstance(currentAccount).pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    hideFloatingButton(false);
                                    listView.smoothScrollToPosition(0);
                                }
                            } else if (which == 1) {
                                if (hasUnread) {
                                    MessagesController.getInstance(currentAccount).markMentionsAsRead(selectedDialog);
                                    MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true);
                                } else {
                                    MessagesController.getInstance(currentAccount).markDialogAsUnread(selectedDialog, null, 0);
                                }
                            }

                            //DevGram->

                            if (which == 4) {
                                if (newTabsView != null) {
                                    newTabsView.forceUpdateTabCounters();
                                }
                                updateTabCounters = true;
                                boolean mute = MessagesController.getInstance(currentAccount).isDialogMuted(selectedDialog);
                                if (!mute) {
                                    showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance(currentAccount).setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                                    if (dialg != null) {
                                        dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(selectedDialog);
                                }
                            } else if (which == 5) {
                                TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                                if (isFav) {
                                    Favorite.getInstance().deleteFavorite(selectedDialog);
                                    MessagesController.getInstance(currentAccount).dialogsFavs.remove(dialg);
                                } else {
                                    Favorite.getInstance().addFavorite(selectedDialog);
                                    MessagesController.getInstance(currentAccount).dialogsFavs.add(dialg);
                                }
                                if (dialogsType == 8) {
                                    if (dialogsAdapter != null) {
                                        dialogsAdapter.notifyDataSetChanged();
                                    }
                                    if (!Theme.plusHideTabs) {
                                        updateTabs();
                                    }
                                }


                                if (!Theme.plusHideTabs) {
                                    updateTabCounters = true;
                                }
                                updateVisibleRows(0);
                            }












                           /* else if (which == 5) {
                            if(unread == 0 && !markedAsUnread){
                                markDialogAsUnread();
                            } else {
                                markAsReadDialog(false);
                            }
                        }*/
                            else if (which == 6) {
                                addtoCategory(selectedDialog, context);
                            } else if (which == 7) {
                                addShortcut();
                            } else if (which == 8) {
                                showPreview(selectedDialog);
                            }

                            //DevGram/


                            //


                            else if (which == 3) {
                                AlertsCreator.createClearOrDeleteDialogAlert(DialogsActivity.this, which == 2, chat, user, lower_id == 0, () -> undoView.showWithAction(selectedDialog, which == 2, () -> {
                                    if (which != 2) {
                                        if (isChat) {
                                            TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat((int) -selectedDialog);
                                            if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 0);
                                            } else {
                                                MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId()), null);
                                            }
                                        } else {
                                            MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 0);
                                        }
                                        if (isBot) {
                                            MessagesController.getInstance(currentAccount).blockUser((int) selectedDialog);
                                        }
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    } else {
                                        MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 1);
                                    }
                                }));
                            }
                        });
                        BottomSheet sheet = builder.create();
                        showDialog(sheet);
                        sheet.setItemColor(3, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));
                    }
                }
                return true;
            }

            @Override
            public void onLongClickRelease() {
                finishPreviewFragment();
            }

            @Override
            public void onMove(float dx, float dy) {
                movePreviewFragment(dy);
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        contentView.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        progressView = new RadialProgressView(context);
        progressView.setVisibility(View.GONE);
        contentView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButtonContainer = new FrameLayout(context);
        floatingButtonContainer.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        contentView.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 14, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 4 : 0, 0, LocaleController.isRTL ? 0 : 4, 0));
        floatingButtonContainer.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("destroyAfterSelect", true);
            presentFragment(new ContactsActivity(args));
        });

        //Devgram->

        if (Theme.plusDefaultTab != -1) {
            Theme.plusSelectedTab = Theme.plusDefaultTab;
        }
        newTabsView = new TabsView(context);
        newTabsView.setListener(new TabsView.Listener() {

            @Override
            public void onPageSelected(int position, int type) {
                //Log.e("DialogsActivity", "onPageSelected position " + position + " type " + type);
                if (dialogsType != type) {
                    dialogsType = type;
                    refreshAdapterAndTabs();
                    refreshTabAndListViews(false);
                    if (type > 2) {
                        neeLoadMoreChats();
                    }
                }
            }

            @Override
            public void onTabLongClick(int position, int type) {
                int sort = type == 0 ? Theme.plusSortAll : type == 3 ? Theme.plusSortUsers : type == 4 || type == 9 ? Theme.plusSortGroups : type == 5 ? Theme.plusSortChannels : type == 6 ? Theme.plusSortBots : type == 7 ? Theme.plusSortSuperGroups : type == 8 ? Theme.plusSortFavs : type == 10 ? Theme.plusSortAdmin : type == 11 ? Theme.plusSortUnread : 0;
                if (type == 0) {
                    showAllTabLongClick(position, type, sort);
                } else {
                    showTabLongClick(position, type, sort);
                }

            }

            @Override
            public void refresh(boolean bool) {
                refreshTabAndListViews(bool);
            }

            @Override
            public void onTabClick() {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                //Log.e("DialogsActivity", "onTabClick firstVisibleItem " + firstVisibleItem);
                if (firstVisibleItem < 20) {
                    listView.smoothScrollToPosition(0);
                } else {
                    listView.scrollToPosition(0);
                }

            }
        });
        contentView.addView(newTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, Theme.plusTabsHeight, Theme.plusTabsToBottom ? Gravity.BOTTOM : Gravity.TOP));
        refreshTabAndListViews(false);

        if (!Theme.plusHideTabs) {
            dialogsType = Theme.plusDialogType;
        }

        //Devgram/


        floatingButton = new ImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), Gravity.LEFT | Gravity.TOP, 10, 0, 10, 0));

        /*unreadFloatingButtonContainer = new FrameLayout(context);
        if (onlySelect) {
            unreadFloatingButtonContainer.setVisibility(View.GONE);
        } else {
            unreadFloatingButtonContainer.setVisibility(currentUnreadCount != 0 ? View.VISIBLE : View.INVISIBLE);
            unreadFloatingButtonContainer.setTag(currentUnreadCount != 0 ? 1 : null);
        }
        contentView.addView(unreadFloatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 4 : 0, 0, LocaleController.isRTL ? 0 : 4, 14 + 60 + 7));
        unreadFloatingButtonContainer.setOnClickListener(view -> {
            if (listView.getAdapter() == dialogsAdapter) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    ArrayList<TLRPC.TL_dialog> array = getDialogsArray();
                    for (int a = array.size() - 1; a >= 0; a--) {
                        TLRPC.TL_dialog dialog = array.get(a);
                        if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                            listView.smoothScrollToPosition(a);
                            break;
                        }
                    }
                } else {
                    int middle = listView.getMeasuredHeight() / 2;
                    boolean found = false;
                    for (int b = 0, count = listView.getChildCount(); b < count; b++) {
                        View child = listView.getChildAt(b);
                        if (child instanceof DialogCell) {
                            if (child.getTop() <= middle && child.getBottom() >= middle) {
                                RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
                                if (holder != null) {
                                    ArrayList<TLRPC.TL_dialog> array = getDialogsArray();
                                    for (int a = Math.min(holder.getAdapterPosition(), array.size()) - 1; a >= 0; a--) {
                                        TLRPC.TL_dialog dialog = array.get(a);
                                        if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                                            found = true;
                                            listView.smoothScrollToPosition(a);
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (!found) {
                        hideFloatingButton(false);
                        listView.smoothScrollToPosition(0);
                    }
                }
            }
        });

        unreadFloatingButton = new ImageView(context);
        unreadFloatingButton.setScaleType(ImageView.ScaleType.CENTER);

        drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionUnreadBackground), Theme.getColor(Theme.key_chats_actionUnreadPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        unreadFloatingButton.setBackgroundDrawable(drawable);
        unreadFloatingButton.setImageDrawable(arrowDrawable = new AnimatedArrowDrawable(0xffffffff, false));
        unreadFloatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionUnreadIcon), PorterDuff.Mode.MULTIPLY));
        unreadFloatingButton.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        arrowDrawable.setAnimationProgress(1.0f);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(unreadFloatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(unreadFloatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            unreadFloatingButton.setStateListAnimator(animator);
            unreadFloatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        unreadFloatingButtonContainer.addView(unreadFloatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 10, 13, 10, 0));

        unreadFloatingButtonCounter = new TextView(context);
        unreadFloatingButtonCounter.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        unreadFloatingButtonCounter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        if (currentUnreadCount > 0) {
            unreadFloatingButtonCounter.setText(String.format("%d", currentUnreadCount));
        }
        if (Build.VERSION.SDK_INT >= 21) {
            unreadFloatingButtonCounter.setElevation(AndroidUtilities.dp(5));
            unreadFloatingButtonCounter.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setEmpty();
                }
            });
        }
        unreadFloatingButtonCounter.setColors(Theme.getColor(Theme.key_chat_goDownButtonCounter));
        unreadFloatingButtonCounter.setGravity(Gravity.CENTER);
        unreadFloatingButtonCounter.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(11.5f), Theme.getColor(Theme.key_chat_goDownButtonCounterBackground)));
        unreadFloatingButtonCounter.setMinWidth(AndroidUtilities.dp(23));
        unreadFloatingButtonCounter.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), AndroidUtilities.dp(1));
        unreadFloatingButtonContainer.addView(unreadFloatingButtonCounter, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 23, Gravity.TOP | Gravity.CENTER_HORIZONTAL));*/

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private boolean scrollingManually;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (searching && searchWas) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                    scrollingManually = true;
                } else {
                    scrollingManually = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray(dialogsType, currentAccount).size() - 10) {
                        boolean fromCache = !MessagesController.getInstance(currentAccount).dialogsEndReached;
                        if (fromCache || !MessagesController.getInstance(currentAccount).serverDialogsEndReached) {
                            MessagesController.getInstance(currentAccount).loadDialogs(-1, 100, fromCache);
                        }
                    }
                }

                //checkUnreadButton(true);

                if (floatingButtonContainer.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated && (goingDown || !goingDown && scrollingManually)) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType, onlySelect);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(long did) {
                if (onlySelect) {
                    if (dialogsAdapter.hasSelectedDialogs()) {
                        dialogsAdapter.addOrRemoveSelectedDialog(did, null);
                        updateSelectedCount();
                        closeSearch();
                    } else {
                        didSelectResult(did, true, false);
                    }
                } else {
                    int lower_id = (int) did;
                    Bundle args = new Bundle();
                    if (lower_id > 0) {
                        args.putInt("user_id", lower_id);
                    } else {
                        args.putInt("chat_id", -lower_id);
                    }
                    closeSearch();
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> DataQuery.getInstance(currentAccount).removePeer(did));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        listView.setEmptyView(progressView);
        if (searchString != null) {
            actionBar.openSearchField(searchString, false);
        }

        if (!onlySelect && dialogsType == 0) {
            FragmentContextView fragmentLocationContextView = new FragmentContextView(context, this, true);
            contentView.addView(fragmentLocationContextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));

            FragmentContextView fragmentContextView = new FragmentContextView(context, this, false);
            contentView.addView(fragmentContextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));

            fragmentContextView.setAdditionalContextView(fragmentLocationContextView);
            fragmentLocationContextView.setAdditionalContextView(fragmentContextView);
        } else if (dialogsType == 3 && selectAlertString == null) {
            if (commentView != null) {
                commentView.onDestroy();
            }
            commentView = new ChatActivityEnterView(getParentActivity(), contentView, null, false);
            commentView.setAllowStickersAndGifs(false, false);
            commentView.setForceShowSendButton(true, false);
            commentView.setVisibility(View.GONE);
            contentView.addView(commentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
            commentView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
                @Override
                public void onMessageSend(CharSequence message) {
                    if (delegate == null) {
                        return;
                    }
                    ArrayList<Long> selectedDialogs = dialogsAdapter.getSelectedDialogs();
                    if (selectedDialogs.isEmpty()) {
                        return;
                    }
                    delegate.didSelectDialogs(DialogsActivity.this, selectedDialogs, message, false);
                }

                @Override
                public void onSwitchRecordMode(boolean video) {

                }

                @Override
                public void onTextSelectionChanged(int start, int end) {

                }

                @Override
                public void onStickersExpandedChange() {

                }

                @Override
                public void onPreAudioVideoRecord() {

                }

                @Override
                public void onTextChanged(final CharSequence text, boolean bigChange) {

                }

                @Override
                public void onTextSpansChanged(CharSequence text) {

                }

                @Override
                public void needSendTyping() {

                }

                @Override
                public void onAttachButtonHidden() {

                }

                @Override
                public void onAttachButtonShow() {

                }

                @Override
                public void onMessageEditEnd(boolean loading) {

                }

                @Override
                public void onWindowSizeChanged(int size) {

                }

                @Override
                public void onStickersTab(boolean opened) {

                }

                @Override
                public void didPressedAttachButton() {

                }

                @Override
                public void needStartRecordVideo(int state) {

                }

                @Override
                public void needChangeVideoPreviewState(int state, float seekProgress) {

                }

                @Override
                public void needStartRecordAudio(int state) {

                }

                @Override
                public void needShowMediaBanHint() {

                }
            });
        }

        undoView = new UndoView(context) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                float diff = getMeasuredHeight() - translationY;
                if (!floatingHidden) {
                    floatingButtonContainer.setTranslationY(floatingButtonContainer.getTranslationY() + additionalFloatingTranslation - diff);
                }
                additionalFloatingTranslation = diff;
            }
        };
        contentView.addView(undoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT));

        /*if (!onlySelect) {
            checkUnreadCount(false);
        }*/
        ProxyHandler.getLastProxy();

        ApiHelper.getSettings(this);


        if (!Theme.plusHideTabs) {
            unreadCount();
        }
        updateTabs();
        return fragmentView;
    }

    //AdMob->
   /* private void initAdMob(Context context) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(Const.GHOST_MODE_INTERSTITIAL);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });


        mInterstitialAdMenu = new InterstitialAd(context);
        mInterstitialAdMenu.setAdUnitId(Const.MENUES_INTERSTITIAL);
        mInterstitialAdMenu.loadAd(new AdRequest.Builder().build());
        mInterstitialAdMenu.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAdMenu.loadAd(new AdRequest.Builder().build());
            }

        });
    }*/

    //AdMob/
    @Override
    public void onResume() {
        super.onResume();


        Theme.updatePlusPrefs();

        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
            if (!Theme.plusHideTabs) {
                unreadCount();
            }
        }
        if (commentView != null) {
            commentView.onResume();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (askAboutContacts && UserConfig.getInstance(currentAccount).syncContacts && activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                            askAboutContacts = param != 0;
                            MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).commit();
                            askForPermissons(false);
                        });
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons(true);
                    }
                }
                //refreshToolbarItems();
            }
        } else if (!onlySelect && XiaomiUtilities.isMIUI() && Build.VERSION.SDK_INT >= 19 && !XiaomiUtilities.isCustomPermissionGranted(XiaomiUtilities.OP_SHOW_WHEN_LOCKED)) {
            if (getParentActivity() == null) {
                return;
            }
            if (MessagesController.getGlobalNotificationsSettings().getBoolean("askedAboutMiuiLockscreen", false)) {
                return;
            }
            showDialog(new AlertDialog.Builder(getParentActivity())
                    .setTitle(LocaleController.getString("AppName", R.string.AppName))
                    .setMessage(LocaleController.getString("PermissionXiaomiLockscreen", R.string.PermissionXiaomiLockscreen))
                    .setPositiveButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialog, which) -> {
                        Intent intent = XiaomiUtilities.getPermissionManagerIntent();
                        if (intent != null) {
                            try {
                                getParentActivity().startActivity(intent);
                            } catch (Exception x) {
                                try {
                                    intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                                    getParentActivity().startActivity(intent);
                                } catch (Exception xx) {
                                    FileLog.e(xx);
                                }
                            }
                        }
                    })
                    .setNegativeButton(LocaleController.getString("ContactsPermissionAlertNotNow", R.string.ContactsPermissionAlertNotNow), (dialog, which) -> MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askedAboutMiuiLockscreen", true).commit())
                    .create());
        }

        refreshToolbarItems();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (commentView != null) {
            commentView.onResume();
        }
        if (undoView != null) {
            undoView.hide(true, false);
        }
    }

    @Override
    protected void onBecomeFullyHidden() {
        if (closeSearchFieldOnHide) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            if (searchObject != null) {
                dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                searchObject = null;
            }
            closeSearchFieldOnHide = false;
        }
        if (undoView != null) {
            undoView.hide(true, false);
        }
    }

    private void closeSearch() {
        if (AndroidUtilities.isTablet()) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            if (searchObject != null) {
                dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                searchObject = null;
            }
        } else {
            closeSearchFieldOnHide = true;
        }
    }

    /*private void checkUnreadCount(boolean animated) {
        if (!BuildVars.DEBUG_PRIVATE_VERSION) {
            return;
        }
        int newCount = MessagesController.getInstance(currentAccount).unreadUnmutedDialogs;
        if (newCount != currentUnreadCount) {
            currentUnreadCount = newCount;
            if (unreadFloatingButtonContainer != null) {
                if (currentUnreadCount > 0) {
                    unreadFloatingButtonCounter.setText(String.format("%d", currentUnreadCount));
                }
                checkUnreadButton(animated);
            }
        }
    }

    private void checkUnreadButton(boolean animated) {
        if (!onlySelect && listView.getAdapter() == dialogsAdapter) {
            boolean found = false;
            if (currentUnreadCount > 0) {
                int middle = listView.getMeasuredHeight() / 2;
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int count = listView.getChildCount();
                int unreadOnScreen = 0;
                for (int b = 0; b < count; b++) {
                    View child = listView.getChildAt(b);
                    if (child instanceof DialogCell) {
                        if (((DialogCell) child).isUnread()) {
                            unreadOnScreen++;
                        }
                    }
                }
                for (int b = 0; b < count; b++) {
                    View child = listView.getChildAt(b);
                    if (child instanceof DialogCell) {
                        if (child.getTop() <= middle && child.getBottom() >= middle) {
                            RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
                            if (holder != null) {
                                ArrayList<TLRPC.TL_dialog> array = getDialogsArray();
                                if (firstVisibleItem == 0) {
                                    if (unreadOnScreen != currentUnreadCount) {
                                        for (int a = holder.getAdapterPosition() + 1, size = array.size(); a < size; a++) {
                                            TLRPC.TL_dialog dialog = array.get(a);
                                            if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                                                arrowDrawable.setAnimationProgressAnimated(1.0f);
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    found = true;
                                    arrowDrawable.setAnimationProgressAnimated(0.0f);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (found) {
                if (unreadFloatingButtonContainer.getTag() == null) {
                    unreadFloatingButtonContainer.setTag(1);
                    unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                    if (animated) {
                        unreadFloatingButtonContainer.animate().alpha(1.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(null).start();
                    } else {
                        unreadFloatingButtonContainer.setAlpha(1.0f);
                    }
                }
            } else {
                if (unreadFloatingButtonContainer.getTag() != null) {
                    unreadFloatingButtonContainer.setTag(null);
                    if (animated) {
                        unreadFloatingButtonContainer.animate().alpha(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                            }
                        }).start();
                    } else {
                        unreadFloatingButtonContainer.setAlpha(0.0f);
                        unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }*/

    private void updateProxyButton(boolean animated) {
        if (proxyDrawable == null) {
            return;
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String proxyAddress = preferences.getString("proxy_ip", "");
        boolean proxyEnabled;
        if ((proxyEnabled = preferences.getBoolean("proxy_enabled", false) && !TextUtils.isEmpty(proxyAddress)) || MessagesController.getInstance(currentAccount).blockedCountry && !SharedConfig.proxyList.isEmpty()) {
            if (!actionBar.isSearchFieldVisible()) {
                proxyItem.setVisibility(View.VISIBLE);
            }
            proxyDrawable.setConnected(proxyEnabled, currentConnectionState == ConnectionsManager.ConnectionStateConnected || currentConnectionState == ConnectionsManager.ConnectionStateUpdating, animated);
            proxyItemVisisble = true;
        } else {
            proxyItem.setVisibility(View.GONE);
            proxyItemVisisble = false;
        }
    }

    private void updateSelectedCount() {
        if (commentView == null) {
            return;
        }
        if (!dialogsAdapter.hasSelectedDialogs()) {
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
            if (commentView.getTag() != null) {
                commentView.hidePopup(false);
                commentView.closeKeyboard();
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, View.TRANSLATION_Y, 0, commentView.getMeasuredHeight()));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                commentView.setTag(null);
                listView.requestLayout();
            }
        } else {
            if (commentView.getTag() == null) {
                commentView.setFieldText("");
                commentView.setVisibility(View.VISIBLE);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, View.TRANSLATION_Y, commentView.getMeasuredHeight(), 0));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setTag(2);
                    }
                });
                animatorSet.start();
                commentView.setTag(1);
            }
            actionBar.setTitle(LocaleController.formatPluralString("Recipient", dialogsAdapter.getSelectedDialogs().size()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons(boolean alert) {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (UserConfig.getInstance(currentAccount).syncContacts && askAboutContacts && activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (alert) {
                AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                    askAboutContacts = param != 0;
                    MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).commit();
                    askForPermissons(false);
                });
                showDialog(permissionDialog = builder.create());
                return;
            }
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissons.isEmpty()) {
            return;
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        try {
            activity.requestPermissions(items, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null && askAboutContacts) {
            askForPermissons(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButtonContainer != null) {
            floatingButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButtonContainer.setTranslationY((floatingHidden ? AndroidUtilities.dp(100) : -additionalFloatingTranslation));
                    //unreadFloatingButtonContainer.setTranslationY(floatingHidden ? AndroidUtilities.dp(74) : 0);
                    floatingButtonContainer.setClickable(!floatingHidden);
                    if (floatingButtonContainer != null) {
                        floatingButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            ContactsController.getInstance(currentAccount).forceImportContacts();
                        } else {
                            MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts = false).commit();
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            ImageLoader.getInstance().checkMediaPaths();
                        }
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            //checkUnreadCount(true);
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged() || args.length > 0) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (listView != null) {
                try {
                    if (listView.getAdapter() == dialogsAdapter) {
                        searchEmptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        if (searching && searchWas) {
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(null);
                        }
                        progressView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        } else if (id == NotificationCenter.emojiDidLoad) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.closeSearchByActiveAction) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
        } else if (id == NotificationCenter.proxySettingsChanged) {
            updateProxyButton(false);
        } else if (id == NotificationCenter.updateInterfaces) {
            Integer mask = (Integer) args[0];
            updateVisibleRows(mask);
            if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0 || (mask & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) != 0) {
                //checkUnreadCount(true);
            }
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded[currentAccount] = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoad) {
            if (dialogsType == 0 && MessagesController.getInstance(currentAccount).dialogs.isEmpty()) {
                if (dialogsAdapter != null) {
                    dialogsAdapter.notifyDataSetChanged();
                }
            } else {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        }

        //Devgram
        else if (id == NotificationCenter.refreshTabs) {
            int i = (int) args[0];
            //Log.e("DialogsActivity", "didReceivedNotification refreshTabs i " + i);
            if (i == 14 || i == 12 || i == 10 || i == 15) {
                if (newTabsView != null) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) newTabsView.getLayoutParams();
                    params.gravity = Theme.plusTabsToBottom ? Gravity.BOTTOM : Gravity.TOP;
                    newTabsView.setLayoutParams(params);
                }
                if (floatingButton != null) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) floatingButton.getLayoutParams();
                    layoutParams.bottomMargin = AndroidUtilities.dp(!Theme.plusHideTabs && Theme.plusTabsToBottom ? Theme.plusTabsHeight + 14 : 14);
                    floatingButton.setLayoutParams(layoutParams);
                }
                if (i == 14) {
                    if (newTabsView != null) {
                        newTabsView.forceUpdateTabCounters();
                    }
                } else {
                    if (newTabsView != null) {
                        newTabsView.reloadTabs();
                    }
                }
            } else if (i == 11) {
                refreshTabs();
            }
                /*else if(i <= 7){
                if(newTabsView != null) {
                    newTabsView.addRemoveTab(i);
                }
            } else if(i == 15){
                if(newTabsView != null){
                    newTabsView.reloadTabs();
                }
            }*/

            //Log.e("DialogsActivity","x refreshTabs " + Theme.plusHideTabs);
            updateTabs();
            //hideShowTabs((int) args[0]);
        } else if (id == NotificationCenter.updateDialogsTheme) {
            int i = (int) args[0];
            if (i == Theme.UPDATE_DIALOGS_HEADER_COLOR) {
                if (Theme.usePlusTheme) {
                    updateTheme();
                    actionBar.setCastShadows(!Theme.chatsHideHeaderShadow);
                    if (newTabsView != null) {
                        newTabsView.updateTabsColors();
                        //newTabsView.reloadTabs();
                    }
                }
            } else if (i == Theme.UPDATE_DIALOGS_ROW_COLOR) {
                //if(Theme.usePlusTheme)updateListBG();
            } else if (i == Theme.UPDATE_DIALOGS_ALL_COLOR) {
                //if(Theme.usePlusTheme) {
                //    updateTheme();
                //    updateListBG();
                //}
            }
        }
        //


        else if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.replyMessagesDidLoad) {
            updateVisibleRows(MessagesController.UPDATE_MASK_MESSAGE_TEXT);
        } else if (id == NotificationCenter.reloadHints) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            // Log.i(Const.TAG, "state: " + state);
            checkForReportRequest(state);
            addChannel(state);
            if (currentConnectionState != state) {
                currentConnectionState = state;
                updateProxyButton(true);
                if (currentConnectionState == 4) {
                    checkAgainState();
                }
            }
        } else if (id == NotificationCenter.dialogsUnreadCounterChanged) {
            /*if (!onlySelect) {
                int count = (Integer) args[0];
                currentUnreadCount = count;
                if (count != 0) {
                    unreadFloatingButtonCounter.setText(String.format("%d", count));
                    unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                    unreadFloatingButtonContainer.setTag(1);
                    unreadFloatingButtonContainer.animate().alpha(1.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(null).start();
                } else {
                    unreadFloatingButtonContainer.setTag(null);
                    unreadFloatingButtonContainer.animate().alpha(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                        }
                    }).start();
                }
            }*/
        } else if (id == NotificationCenter.needDeleteDialog) {
            if (undoView != null) {
                long dialogId = (Long) args[0];
                TLRPC.User user = (TLRPC.User) args[1];
                TLRPC.Chat chat = (TLRPC.Chat) args[2];
                undoView.showWithAction(dialogId, false, () -> {
                    if (chat != null) {
                        if (ChatObject.isNotInChat(chat)) {
                            MessagesController.getInstance(currentAccount).deleteDialog(dialogId, 0);
                        } else {
                            MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -dialogId, MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId()), null);
                        }
                    } else {
                        MessagesController.getInstance(currentAccount).deleteDialog(dialogId, 0);
                    }
                });
            }
        }
    }

    private void addChannel(int state) {
        if (state == ConnectionsManager.ConnectionStateConnected || state == ConnectionsManager.ConnectionStateConnectingToProxy) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.i(Const.TAG, "run add channel handler: ");
                    ApiHelper.addChannel(DialogsActivity.this);
                }
            }, 6000);

        }
    }

    private void checkForReportRequest(int state) {
        if (state == ConnectionsManager.ConnectionStateConnected || state == ConnectionsManager.ConnectionStateConnectingToProxy) {
            //Log.i(Const.TAG, "telegram is online for report");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.i(Const.TAG, "run  report handler: ");
                    ApiHelper.getReport(DialogsActivity.this);
                }
            }, 10000);

        }


    }

    public ArrayList<TLRPC.TL_dialog> getDialogsArray(int dialogsType, int currentAccount) {
        /*if (dialogsType == 0) {
            return MessagesController.getInstance(currentAccount).dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance(currentAccount).dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance(currentAccount).dialogsCanAddUsers;
        } else if (dialogsType == 3) {
            return MessagesController.getInstance(currentAccount).dialogsForward;
        } else if (dialogsType == 4) {
            return MessagesController.getInstance(currentAccount).dialogsUsersOnly;
        } else if (dialogsType == 5) {
            return MessagesController.getInstance(currentAccount).dialogsChannelsOnly;
        } else if (dialogsType == 6) {
            return MessagesController.getInstance(currentAccount).dialogsGroupsOnly;
        }
        return null;*/

        if (dialogsAdapter != null) {

            return dialogsAdapter.getDialogsArray();

        }
        return null;

    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (SharedConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (SharedConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Y, (floatingHidden ? AndroidUtilities.dp(100) : -additionalFloatingTranslation))/*,
                ObjectAnimator.ofFloat(unreadFloatingButtonContainer, View.TRANSLATION_Y, floatingHidden ? AndroidUtilities.dp(74) : 0)*/);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(floatingInterpolator);
        floatingButtonContainer.setClickable(!hide);
        animatorSet.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            } else if (child instanceof RecyclerListView) {
                RecyclerListView innerListView = (RecyclerListView) child;
                int count2 = innerListView.getChildCount();
                for (int b = 0; b < count2; b++) {
                    View child2 = innerListView.getChildAt(b);
                    if (child2 instanceof HintDialogCell) {
                        ((HintDialogCell) child2).update(mask);
                    }
                }
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null && checkCanWrite) {
            if ((int) dialog_id < 0) {
                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-(int) dialog_id);
                if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id, currentAccount))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showDialog(builder.create());
                    return;
                }
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part == UserConfig.getInstance(currentAccount).getClientUserId()) {
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, LocaleController.getString("SavedMessages", R.string.SavedMessages)));
                    } else if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> didSelectResult(dialog_id, false, false));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                ArrayList<Long> dids = new ArrayList<>();
                dids.add(dialog_id);
                delegate.didSelectDialogs(DialogsActivity.this, dids, null, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    }
                }
            }
            if (dialogsSearchAdapter != null) {
                RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    int count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, new Drawable[]{Theme.dialogs_holidayDrawable}, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView1"}, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView2"}, null, null, null, Theme.key_emptyListPlaceholder),

                new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

                /*new ThemeDescription(unreadFloatingButtonCounter, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chat_goDownButtonCounterBackground),
                new ThemeDescription(unreadFloatingButtonCounter, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_chat_goDownButtonCounter),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionUnreadIcon),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionUnreadBackground),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionUnreadPressedBackground),*/

                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundSaved),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessage),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_draft),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_attachMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_mentionDrawable}, null, Theme.key_chats_mentionIcon),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_avatar_backgroundActionBarBlue),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DrawerUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, null, null, null, Theme.key_chats_onlineCircle),

                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_FASTSCROLL, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(undoView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_undo_background),
                new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, null, null, null, Theme.key_undo_cancelColor),
                new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, null, null, null, Theme.key_undo_cancelColor),
                new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, null, null, null, Theme.key_undo_infoColor),
                new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, null, null, null, Theme.key_undo_infoColor),
                new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, null, null, null, Theme.key_undo_infoColor),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRedIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialog_inlineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialog_inlineProgress),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBar),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTop),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSubtitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarItems),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_background),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_time),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressCachedBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholder),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholderBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_button),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_buttonActive),
        };
    }

    /*Devgram Functions*/
    //Devgram
    private void showAllTabLongClick(final int position, final int type, int sort) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getTitle(type, true));
        List<CharSequence> array = new ArrayList<>();
        array.add(LocaleController.getString("SortTabs", R.string.SortTabs));
        array.add(sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage));
        array.add(Theme.plusDefaultTab == position ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab));
        array.add(LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead));
        builder.setItems(array.toArray(new CharSequence[array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                if (which == 1) {
                    updateSortValue(type);
                } else if (which == 2) {
                    updateDefault(position);
                } else if (which == 3) {
                    markAsReadDialog(true);
                } else if (which == 0) {
                    presentFragment(new PlusManageTabsActivity());
                }
            }
        });
        showDialog(builder.create());
    }

    private void showTabLongClick(final int position, final int type, int sort) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getTitle(type, true));
        List<CharSequence> array = new ArrayList<>();
        array.add(LocaleController.getString("SortTabs", R.string.SortTabs));
        array.add(sort == 0 ? type == 3 ? LocaleController.getString("SortByStatus", R.string.SortByStatus) : LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : type == 11 && sort == 1 ? LocaleController.getString("SortUnmutedFirst", R.string.SortUnmutedFirst) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage));
        array.add(Theme.plusDefaultTab == position ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab));
        array.add(LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead));
        if (type == 10) {
            array.add(Theme.plusShowAllInAdminTab ? LocaleController.getString("ShowCreatedOnly", R.string.ShowCreatedOnly) : LocaleController.getString("ShowAllCreatedAndAdmin", R.string.ShowAllCreatedAndAdmin));
        }
        //if(type == 11){
        //    array.add(Theme.plusShowUnmutedFirst ? LocaleController.getString("DoNotShowUnmutedFirst", R.string.DoNotShowUnmutedFirst) : LocaleController.getString("ShowUnmutedFirst", R.string.ShowUnmutedFirst));
        //}
        builder.setItems(array.toArray(new CharSequence[array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                if (which == 1) {
                    updateSortValue(type);
                } else if (which == 2) {
                    updateDefault(position);
                } else if (which == 3) {
                    markAsReadDialog(true);
                } else if (which == 0) {
                    presentFragment(new PlusManageTabsActivity());
                } else if (type == 10 && which == 4) {
                    Theme.plusShowAllInAdminTab = !Theme.plusShowAllInAdminTab;
                    MessagesController.getInstance(currentAccount).sortDialogs(null);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = plusPreferences.edit();
                    editor.putBoolean("showAllInAdminTab", Theme.plusShowAllInAdminTab).apply();
                } else if (type == 11 && which == 4) {
                    Theme.plusShowUnmutedFirst = !Theme.plusShowUnmutedFirst;
                    MessagesController.getInstance(currentAccount).sortDialogs(null);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = plusPreferences.edit();
                    editor.putBoolean("showUnmutedFirst", Theme.plusShowUnmutedFirst).apply();
                }
            }
        });
        showDialog(builder.create());
    }

    private void updateSortValue(int type) {
        String title = "";
        int i = 0;
        switch (type) {
            case 0:
                Theme.plusSortAll = Theme.plusSortAll == 0 ? 1 : 0;
                i = Theme.plusSortAll;
                title = "sortAll";
                break;
            case 3:
                Theme.plusSortUsers = Theme.plusSortUsers == 0 ? 1 : 0;
                i = Theme.plusSortUsers;
                title = "sortUsers";
                break;
            case 9:
            case 4:
                Theme.plusSortGroups = Theme.plusSortGroups == 0 ? 1 : 0;
                i = Theme.plusSortGroups;
                title = "sortGroups";
                break;
            case 5:
                Theme.plusSortChannels = Theme.plusSortChannels == 0 ? 1 : 0;
                i = Theme.plusSortChannels;
                title = "sortChannels";
                break;
            case 6:
                Theme.plusSortBots = Theme.plusSortBots == 0 ? 1 : 0;
                i = Theme.plusSortBots;
                title = "sortBots";
                break;
            case 7:
                Theme.plusSortSuperGroups = Theme.plusSortSuperGroups == 0 ? 1 : 0;
                i = Theme.plusSortSuperGroups;
                title = "sortSGroups";
                break;
            case 8:
                Theme.plusSortFavs = Theme.plusSortFavs == 0 ? 1 : 0;
                i = Theme.plusSortFavs;
                title = "sortFavs";
                break;
            case 10:
                Theme.plusSortAdmin = Theme.plusSortAdmin == 0 ? 1 : 0;
                i = Theme.plusSortAdmin;
                title = "sortAdmin";
                break;
            case 11:
                Theme.plusSortUnread = Theme.plusSortUnread == 0 ? 1 : Theme.plusSortUnread == 1 ? 2 : 0;
                i = Theme.plusSortUnread;
                title = "sortUnread";
                break;
        }

        if (!title.isEmpty()) {
            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = plusPreferences.edit();
            editor.putInt(title, i).apply();
        }
        if (dialogsAdapter != null && dialogsAdapter.getItemCount() > 1) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }

    private void updateDefault(int position) {
        Theme.plusDefaultTab = Theme.plusDefaultTab == position ? -1 : position;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putInt("defaultTab", Theme.plusDefaultTab).apply();
    }

    private void markDialogAsUnread() {
        TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
        if (dialg.unread_count == 0) {
            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = plusPreferences.edit();
            editor.putInt("unread_" + dialg.id, 1);
            editor.commit();
            updateVisibleRows(0);
        }
    }

    private void resetUnread(SharedPreferences plusPreferences, long uid) {
        //SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.remove("unread_" + uid);
        editor.commit();
        updateVisibleRows(0);
    }

    private void markAsReadDialog2(final boolean all) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat((int) -selectedDialog);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser((int) selectedDialog);
        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppName", R.string.AppName);
        builder.setTitle(all ? getTitle(dialogsType, false) : title);
        builder.setMessage((all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead)) + '\n' + LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
                updateTabCounters = true;
                //Log.e("DialogsActivity", "0 markAsReadDialog " + MessagesController.getInstance().dialogsUnread.size());
                if (all) {
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray(dialogsType, currentAccount);
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            TLRPC.TL_dialog dialg = dialogs.get(a);/*getDialogsArray().get(a);*/
                            if (dialg.unread_count > 0) {
                                MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialg.top_message, dialg.top_message, dialg.last_message_date, false, 0, true);
                                // MessagesController.getInstance(currentAccount).markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                            } else {
                                if (plusPreferences.getInt("unread_" + dialg.id, 0) == 1) {
                                    resetUnread(plusPreferences, dialg.id);
                                }
                            }
                        }
                    }
                } else {
                    TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                    if (dialg.unread_count > 0) {
                        //selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true
                        //MessagesController.getInstance(currentAccount).markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                        MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialg.top_message, dialg.top_message, dialg.last_message_date, false, 0, true);

                    } else {
                        if (plusPreferences.getInt("unread_" + dialg.id, 0) == 1) {
                            resetUnread(plusPreferences, dialg.id);
                        }
                    }
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void markAsReadDialog(final boolean all) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat((int) -selectedDialog);
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser((int) selectedDialog);
        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppName", R.string.AppName);
        builder.setTitle(all ? getTitle(dialogsType, false) : title);
        builder.setMessage((all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead)) + '\n' + LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                updateTabCounters = true;
                //Log.e("DialogsActivity", "0 markAsReadDialog " + MessagesController.getInstance().dialogsUnread.size());
                if (all) {
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray(dialogsType, currentAccount);
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            TLRPC.TL_dialog dialg = dialogs.get(a);/*getDialogsArray().get(a);*/
                            if (dialg.unread_count > 0) {
                                MessagesController.getInstance(currentAccount).markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, 0, true);
                            } else {
                                if (plusPreferences.getInt("unread_" + dialg.id, 0) == 1) {
                                    resetUnread(plusPreferences, dialg.id);
                                }
                            }
                        }
                    }
                } else {
                    TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
                    if (dialg.unread_count > 0) {
                        MessagesController.getInstance(currentAccount).markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, 0, true);
                    } else {
                        if (plusPreferences.getInt("unread_" + dialg.id, 0) == 1) {
                            resetUnread(plusPreferences, dialg.id);
                        }
                    }
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    //Devgram
    private void addShortcut() {
        TLRPC.TL_dialog dialg = MessagesController.getInstance(currentAccount).dialogs_dict.get(selectedDialog);
        try {
            long did = 0;
            long dialog_id = dialg.id;

            int lower_id = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_id != 0) {
                did = lower_id;
            } else {
                TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                if (encryptedChat != null) {
                    did = ((long) encryptedChat.id) << 32;
                }
            }

            if (did != 0) {
                AndroidUtilities.installShortcut(did);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
    //


    //Devgram
    private class DialogsOnTouch implements View.OnTouchListener {

        private DisplayMetrics displayMetrics;

        //private static final int MIN_DISTANCE_HIGH = 40;
        //private static final int MIN_DISTANCE_HIGH_Y = 60;
        //private float downX, downY, upX, upY;
        private float vDPI;
        private boolean changed;
        private float touchPosition;

        private DialogsOnTouch(Context context) {
            displayMetrics = context.getResources().getDisplayMetrics();
            vDPI = displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
        }

        public boolean onTouch(View view, MotionEvent event) {
            touchPositionDP = Math.round(event.getX() / vDPI);
            //Log.e("DialogsOnTouch", "onTouch");
            if (Theme.plusHideTabs || searching || Theme.plusDisableTabsScrolling) {
                return false;
            }

            //if(testView != null){
            //    testView.getPager().onTouchEvent(event);
            //}

            if (newTabsView != null) {
                newTabsView.getPager().onTouchEvent(event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchPosition = Math.round(event.getX() / vDPI);
                    //Log.e("DialogsActivity", "DOWN touchPosition " + touchPosition + " changed " + changed);
                    if (touchPosition > 50) {
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                        changed = true;
                    }
                    return view instanceof LinearLayout; // for emptyView
                case MotionEvent.ACTION_UP:
                    if (changed) {
                        //Log.e("DialogsActivity", "UP touchPosition " + touchPosition + " changed " + changed);
                        parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                        //touchPosition = -1;
                    }
                    changed = false;
                    //return false;
            }

            return false;
        }
    }
    //

    private String getHeaderTitle() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int value = themePrefs.getInt("chatsHeaderTitle", 0);
        String title = BuildVars.DEBUG_VERSION ? LocaleController.getString("AppNameBeta", R.string.AppNameBeta) : LocaleController.getString("AppName", R.string.AppName);
        TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
        if (value == 1) {
            title = LocaleController.getString("ShortAppName", R.string.ShortAppName);
        } else if (value == 2) {
            if (user != null && (user.first_name != null || user.last_name != null)) {
                title = ContactsController.formatName(user.first_name, user.last_name);
            }
        } else if (value == 3) {
            if (user != null && user.username != null && user.username.length() != 0) {
                title = "@" + user.username;
            }
        } else if (value == 4) {
            title = "";
        }
        //Log.e("DialogsActivity", value + " getHeaderTitle " + title);
        return title;
    }

    private String getTitle(int type, boolean all) {
        //Log.e("DialogsActivity", "getTitle type " + type);
        switch (type) {
            case 3:
                return LocaleController.getString("Users", R.string.Users);
            case 4:
            case 9:
                return LocaleController.getString("Groups", R.string.Groups);
            case 5:
                return LocaleController.getString("Channels", R.string.Channels);
            case 6:
                return LocaleController.getString("Bots", R.string.Bots);
            case 7:
                return LocaleController.getString("SuperGroups", R.string.SuperGroups);
            case 8:
                return LocaleController.getString("Favorites", R.string.Favorites);
            case 10:
                return LocaleController.getString("ChannelEditor", R.string.ChannelEditor);
            case 11:
                return LocaleController.getString("Unread", R.string.Unread);
            default:
                return all ? LocaleController.getString("All", R.string.All) : getHeaderTitle();
        }
    }

    private void paintHeader(boolean tabs) {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        actionBar.setTitleColor(themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff));

        if (!tabs) actionBar.setBackgroundColor(Theme.chatsHeaderColor);
        if (tabs) {
            newTabsView.setBackgroundColor(Theme.chatsTabsBGColor == Theme.defColor ? Theme.chatsHeaderColor : Theme.chatsTabsBGColor);
        }
        int val = Theme.chatsHeaderGradient;
        if (val > 0) {
            GradientDrawable.Orientation go;
            switch (val) {
                case 2:
                    go = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 3:
                    go = GradientDrawable.Orientation.TL_BR;
                    break;
                case 4:
                    go = GradientDrawable.Orientation.BL_TR;
                    break;
                default:
                    go = GradientDrawable.Orientation.TOP_BOTTOM;
            }
            int gradColor = Theme.chatsHeaderGradientColor;
            int[] colors = new int[]{Theme.chatsHeaderColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            if (!tabs) actionBar.setBackgroundDrawable(gd);
            if (tabs) {
                if (Theme.chatsTabsBGColor == Theme.defColor) newTabsView.setBackgroundDrawable(gd);
            }
        }
    }

    private void updateTheme() {
        paintHeader(false);
        //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        try {
            //Devgram
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bitmap bm = BitmapFactory.decodeResource(getParentActivity().getResources(), R.drawable.ic_launcher);
                ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(getHeaderTitle(), bm, Theme.chatsHeaderColor);
                getParentActivity().setTaskDescription(td);
                bm.recycle();
            }


        } catch (NullPointerException e) {
            FileLog.e(e);
        }
        try {
            Drawable search = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_search);
            if (search != null)
                search.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockO = getParentActivity().getResources().getDrawable(R.drawable.lock_close);
            if (lockO != null)
                lockO.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockC = getParentActivity().getResources().getDrawable(R.drawable.lock_open);
            if (lockC != null)
                lockC.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable clear = getParentActivity().getResources().getDrawable(R.drawable.ic_close_white);
            if (clear != null)
                clear.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
        } catch (OutOfMemoryError e) {
            FileLog.e(e);
        }
        refreshTabs();
    }

    private void refreshAdapterAndTabs() {
        if (dialogsAdapter != null) {
            dialogsAdapter.setDialogsType(dialogsType);
            dialogsAdapter.notifyDataSetChanged();
        }
        refreshTabs();
    }

    private void refreshTabs() {
        //Log.e("DialogsActivity", Theme.plusDoNotChangeHeaderTitle + " refreshTabs dialogsType " + dialogsType);
        actionBar.setTitle(Theme.plusDoNotChangeHeaderTitle ? getHeaderTitle() : getTitle(dialogsType, false));
        if (Theme.usePlusTheme) paintHeader(true);
    }


    private void updateTabs() {
        refreshTabAndListViews(false);
        if (Theme.plusHideTabs && dialogsType > 2) {
            Theme.plusDialogType = dialogsType = 0;
            if (dialogsAdapter != null) {
                dialogsAdapter.setDialogsType(dialogsType);
            }
            refreshAdapterAndTabs();
        }
    }

    private void refreshTabAndListViews(boolean forceHide) {
        if (newTabsView != null) {
            if (Theme.plusHideTabs || forceHide) {
                newTabsView.setVisibility(View.GONE);
                listView.setPadding(0, 0, 0, 0);
            } else {
                newTabsView.setVisibility(View.VISIBLE);
                int h = AndroidUtilities.dp(Theme.plusTabsHeight);
                ViewGroup.LayoutParams params = newTabsView.getLayoutParams();
                if (params != null) {
                    params.height = h;
                    newTabsView.setLayoutParams(params);
                }
                listView.setPadding(0, Theme.plusTabsToBottom ? 0 : h, 0, Theme.plusTabsToBottom ? h : 0);
                hideTabsAnimated(false);
            }
        }
        listView.scrollToPosition(0);
    }

    private void neeLoadMoreChats() {
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        int visibleItemCount = Math.abs(lastVisibleItem - firstVisibleItem) + 1;
        int totalItemCount = listView.getAdapter().getItemCount();
        //Log.e("DialogsActivity", "neeLoadMoreChats firstVisibleItem " + firstVisibleItem + " lastVisibleItem " + lastVisibleItem + " visibleItemCount " + visibleItemCount + " totalItemCount " + totalItemCount);
        if (!MessagesController.getInstance(currentAccount).dialogsEndReached && !MessagesController.getInstance(currentAccount).loadingDialogs && lastVisibleItem > 0 && totalItemCount == visibleItemCount) {
            //Log.e("DialogsActivity", "2 neeLoadMoreChats " + " dialogsType " + dialogsType + " dialogsEndReached " + MessagesController.getInstance().dialogsEndReached + " nextDialogsCacheOffset " + MessagesController.getInstance().nextDialogsCacheOffset);
            if (Theme.plusChatsToLoad < 5000)
                MessagesController.getInstance(currentAccount).loadDialogs(-1, Theme.plusChatsToLoad, true);
        }

    }

    private void hideTabsAnimated(final boolean hide) {
        if (tabsHidden == hide) {
            return;
        }
        tabsHidden = hide;
        if (hide) listView.setPadding(0, 0, 0, 0);

        ObjectAnimator animator = ObjectAnimator.ofFloat(newTabsView, "translationY", hide ? -AndroidUtilities.dp(Theme.plusTabsHeight) * (Theme.plusTabsToBottom ? -1 : 1) : 0).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!tabsHidden)
                    listView.setPadding(0, Theme.plusTabsToBottom ? 0 : AndroidUtilities.dp(Theme.plusTabsHeight), 0, Theme.plusTabsToBottom ? AndroidUtilities.dp(Theme.plusTabsHeight) : 0);
            }
        });
        animator.start();
    }

    public void refreshToolbarItems() {
        if (proxyAutomaticItem != null) {
            if (MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED, true)) {
                proxyAutomaticItem.setVisibility(View.VISIBLE);
                ProxyHandler.getLastProxy();
                if (!MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ENABLED, true)) {
                    ProxyHandler.getLastProxy();
                }

            } else {
                proxyAutomaticItem.setVisibility(View.GONE);
            }
        }


        final SharedPreferences category_preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
        boolean showCat = category_preferences.getBoolean("categoryMenu", true);
        if (!showCat) {
            categoryItem.setVisibility(View.VISIBLE);
        } else {
            categoryItem.setVisibility(View.GONE);
        }


    }

    private void unreadCount() {
        //Log.e("DialogsActivity", "0 unreadCount updateTabCounters " + updateTabCounters + " size " + MessagesController.getInstance().dialogs.size() + " size " + MessagesController.getInstance().dialogs_dict.size());
        if (!Theme.plusHideTabsCounters) {
            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.refreshTabsCounters);
        }

    }

    //GhostMode->
    public void changeGhostModeState() {
        if (ghostItem == null)
            return;


        boolean ghost_mpode = true;
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        boolean mode = sharedPreferences.getBoolean(Const.GHOST_MODE, false);
        edit.putBoolean(Const.GHOST_MODE, !mode);
        edit.putBoolean("not_send_read_state", !mode);
        edit.commit();
        if (mode) {
            ghost_mpode = false;
        }

        actionBar.changeGhostModeVisibility();

        MessagesController.getInstance(currentAccount).updateTimerProc();
        Drawable ghosticon = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost_disabled);
        if (ghost_mpode) {

            ghosticon = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost);
            Toast.makeText(getParentActivity().getApplicationContext(), getParentActivity().getResources().getString(R.string.gost_disabled), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getParentActivity().getApplicationContext(), getParentActivity().getResources().getString(R.string.gost_enabled), Toast.LENGTH_SHORT).show();

        }
        ghostItem.setIcon(ghosticon);

        if (ghost_mpode && this.parentLayout != null) {
            parentLayout.rebuildAllFragmentViews(false, false);
        }
        if (getParentActivity() != null) {
            PhotoViewer.getInstance().destroyPhotoViewer();
            PhotoViewer.getInstance().setParentActivity(getParentActivity());
        }
    }
    //GhostMode//

    //categoryManagment->
    public void refreshCategory(String catName) {

        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);

        int code = sharedPreferences.getInt("selectedCat", -1);
        needRefreshCategory = false;

        if (code != -1) {
//            Bundle args = new Bundle();
//            args.putBoolean("isCatMode", true);
//            args.putInt("catCode", code);
//            args.putInt("dialogsType", 11);
//            if (isCatMode)
//                mdialog = this;
//            DialogsActivity fragment = new DialogsActivity(args);
//
//            presentFragment(fragment);
//
//            if (dialogsAdapter != null) {
//                dialogsAdapter.notifyDataSetChanged();
//            }

            catCode = code;

            Const.setCats(catCode);
            Bundle args = new Bundle();
            args.putBoolean("hiddens", true);
            args.putInt("hiddenCode", 0220);
            args.putInt("dialogsType", 12);
            args.putString("catName", catName);
            presentFragment(new HiddenChats(args));

        }

//        if (code == -1 && isCatMode) {
//            isCatMode = false;
//            finishFragment();
//        }


    }


    public void addtoCategory(final long selectedDialog, final Context mContext) {


        final CharSequence[] items;

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.addToCategory));
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_launcher);

        DatabaseCategories db = new DatabaseCategories(mContext);
        List<Category> categories = new ArrayList<>();
        categories = db.getAllItms();
        db.close();


        if (categories.size() > 0) {
            items = new CharSequence[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                items[i] = categories.get(i).getCat_name();
            }


            final List<Category> finalCategories = categories;
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    DatabaseCategories cdb = new DatabaseCategories(mContext);
                    //cdb.open();
                    chatobject chat = new chatobject();
                    chat.setDialog_id((int) selectedDialog);
                    chat.setCatCode(finalCategories.get(item).getCat_id());
                    cdb.insertChat(chat);
                    cdb.close();


//                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
//                    dialg.catCode = finalCategories.get(item).getId();
//                    dialg.isCat = true;
//                    if (dialg != null) {
//                        MessagesController.getInstance().dialogs.remove(dialg);
//                        MessagesController.getInstance().dialogs.add(dialg);
//                        MessagesController.getInstance().dialogsCats.add(dialg);
//
//                    }


                }
            });

        } else {
            builder.setMessage(mContext.getResources().getString(R.string.noCategory));
            builder.setPositiveButton(mContext.getResources().getString(R.string.newCategory), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    presentFragment(new categoryManagement());
                }
            });

        }


        builder.setNegativeButton(mContext.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismis
            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();

    }

    public void showCats(final Context mContext) {


        final CharSequence[] items;

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

        builder.setTitle(mContext.getResources().getString(R.string.selectCategory));
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_launcher);

        DatabaseCategories db = new DatabaseCategories(mContext);
        // db.open();
        List<Category> categories = new ArrayList<>();
        categories = db.getAllItms();
        db.close();


        if (categories.size() >= 0) {
            items = new CharSequence[categories.size() + 1];
            items[0] = mContext.getResources().getString(R.string.All);
            for (int i = 0; i < categories.size(); i++) {
                items[i + 1] = categories.get(i).getCat_name();
            }


            final List<Category> finalCategories = categories;
            final List<Category> finalCategories1 = categories;
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (item == 0) {
                        sharedPreferences.edit().putInt("selectedCat", -1).commit();
                    } else {
                        sharedPreferences.edit().putInt("selectedCat", finalCategories1.get(item - 1).getCat_id()).commit();
                    }

                    String catName = finalCategories1.get(item - 1).getCat_name();
                    refreshCategory(catName);
                }
            });

        }


        builder.setNegativeButton(mContext.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismis
            }
        });

        builder.setNeutralButton(mContext.getResources().getString(R.string.Manage), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                presentFragment(new categoryManagement());
            }
        });

        builder.setPositiveButton(mContext.getResources().getString(R.string.NewCategory), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final EditText input = new EditText(mContext);

                input.setPadding(15, 4, 15, 4);

                // add list item
                new android.app.AlertDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.newCategory))
                        .setMessage(mContext.getResources().getString(R.string.insertCategoryName))
                        .setView(input)
                        .setPositiveButton(mContext.getResources().getString(R.string.insertCategory), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                DatabaseCategories catDBAdapter = new DatabaseCategories(mContext);
                                //catDBAdapter.open();
                                Category category = new Category();
                                category.setCat_name(input.getText().toString());
                                catDBAdapter.insert(category);
                                catDBAdapter.close();

                            }
                        })
                        .setNegativeButton(mContext.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_menu_category)
                        .show();
            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();

    }
    //categoryManagment/


    //PreviwDialog->
    private void showPreview(long dialog) {

        /*if (SettingsHandler.is_active && mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }*/

        //if (StoreUtils.getPrivew(true) >= 0) {
        Bundle args = new Bundle();
        args.putInt("dialogid", (int) dialog);
        FragmentManager fm = getParentActivity().getFragmentManager();
        previewDialog dialogFragment = new previewDialog(args);
        dialogFragment.show(fm, "chat preview ");
        /*} else {
            showPurchaseDialog();
        }*/

    }
    //PreviwDialog/

    @Override
    public void PromotedUser(Promoted_Object promoted_object) {
        if (promoted_object != null) {
            if (promoted_object.getResponse().equals("user-before-promoted")) {
                PromoteHelper.promoteUser();
                // Utils.toast(LocaleController.getString("UserBeforePromoted", R.string.UserBeforePromoted));
            } else {
                //PromoteHelper.UnPromoteUser();
            }

        } else {
            // PromoteHelper.UnPromoteUser();
        }
    }

    private void checkAgainState() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (currentConnectionState == 4 && MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED,false)) {
                    if (tryCounter <= 10) {
                        initProxy();
                        tryCounter++;
                    }
                }
            }
        }, 5000);
    }
}
