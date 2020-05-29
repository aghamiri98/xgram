package sections.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
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
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.DocumentSelectActivity;
import org.telegram.ui.PasscodeActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.SessionsActivity;
import org.telegram.ui.TwoStepVerificationActivity;
import sections.backend.AnimationCompat.ViewProxy;
import sections.materials.BaseFragmentAdapter;
import sections.ui.components.AvatarUpdater;


public class BaseSettings extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
   private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private View extraHeightView;
    private View shadowView;

    private int extraHeight;

    private int overscrollRow;
    private int emptyRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;

    //
    private int tabsRow;
    private int viewSettingRow;
    private int chatSettingsRow;
    private int forwardSettingsRow;
    private int categorySettingsRow;
    private int maxPinnedDialogsCount;

//    private int notificationBarSettingsRow;


    private int PrivacySection;
    private int PrivacySection2;
    //    private int lockRow;
    private int reportHelp;
    private int sessionsRow;
    private int passwordRow;
    private int passcodeRow;
    private int fileLocationRow;

    private int plusSettingsSectionRow;
    private int plusSettingsSectionRow2;
    private int savePlusSettingsRow;
    private int restorePlusSettingsRow;
    private int resetPlusSettingsRow;


    private boolean reseting = false;
    private boolean saving = false;


    private int rowCount;


    //Tele X

    private int linkSearchRequestId;
    private TLRPC.WebPage foundWebPage;
    private int pass;


    private int currentAccount = UserConfig.selectedAccount;


    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer, @NonNull MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e(e);
            }
            return false;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.file = file;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                            if (user == null) {
                                user = UserConfig.getInstance(currentAccount).getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance(currentAccount).putUser(user, false);
                            } else {
                                UserConfig.getInstance(currentAccount).setCurrentUser(user);
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
                            MessagesStorage.getInstance(currentAccount).clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, false, true);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    UserConfig.getInstance(currentAccount).saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userInfoDidLoad);
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean hideMobile = plusPreferences.getBoolean("hideMobile", false);
        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;

        settingsSectionRow = rowCount++;
        settingsSectionRow2 = rowCount++;
        tabsRow = rowCount++;
        viewSettingRow = rowCount++;
        chatSettingsRow = rowCount++;
        forwardSettingsRow = rowCount++;
        categorySettingsRow = rowCount++;
        maxPinnedDialogsCount = rowCount++;
//        notificationBarSettingsRow = rowCount++;


        PrivacySection = rowCount++;
        PrivacySection2 = rowCount++;
//
//        lockRow = rowCount++;
        reportHelp = rowCount++;

        sessionsRow = rowCount++;
        passwordRow = rowCount++;
        passcodeRow = rowCount++;
        fileLocationRow = rowCount++;

        plusSettingsSectionRow = rowCount++;
        plusSettingsSectionRow2 = rowCount++;
//        ghostModeRow = rowCount++;
        savePlusSettingsRow = rowCount++;
        restorePlusSettingsRow = rowCount++;
        resetPlusSettingsRow = rowCount++;


        MessagesController.getInstance(currentAccount).loadFullUser(UserConfig.getInstance(currentAccount).getCurrentUser(), classGuid, true);
        getUserAbout(UserConfig.getInstance(currentAccount).getClientUserId());
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance(currentAccount).cancelLoadFullUser(UserConfig.getInstance(currentAccount).getClientUserId());
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoad);
       avatarUpdater.clear();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
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
                }
            }
        });

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
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int i, long l) {
                /*if (i == textSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TextSize", R.string.TextSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(12);
                    numberPicker.setMaxValue(30);
                    numberPicker.setValue(MessagesController.getInstance().fontSize);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("fons_size", numberPicker.getValue());
                            MessagesController.getInstance().fontSize = numberPicker.getValue();
                            editor.commit();
                            //
                            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                            SharedPreferences.Editor edit = themePrefs.edit();
                            edit.putInt("chatTextSize", numberPicker.getValue());
                            edit.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == emojiPopupSize) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("EmojiPopupSize", R.string.EmojiPopupSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        numberPicker.setMinValue(60);
                        numberPicker.setMaxValue(100);
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("emoji", Activity.MODE_PRIVATE);
                        numberPicker.setValue(preferences.getInt("emojiPopupSize", AndroidUtilities.isTablet() ? 65 : 60));
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("emoji", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("emojiPopupSize", numberPicker.getValue());
                                editor.apply();;
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        showDialog(builder.create());
                    }else */
                if (i == tabsRow) {
                    presentFragment(new TabSettingsActivity());
                } else if (i == viewSettingRow) {
                    presentFragment(new viewSettingsActivity());
                } else if (i == chatSettingsRow) {
                    presentFragment(new chatSettingsActivity());
                } else if (i == forwardSettingsRow) {
                    presentFragment(new forwardSettingsActivity());
                } else if (i == categorySettingsRow) {
                    presentFragment(new categoryManagement());
                } else if (i == maxPinnedDialogsCount) {

                    if (getParentActivity() == null) {
                        return;
                    }
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("maxPinnedDialogsCount", R.string.maxPinnedDialogsCount));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(5);
                    numberPicker.setMaxValue(20);
                    numberPicker.setValue(preferences.getInt("maxPinnedDialogsCount", 5));
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
                } else if (i == reportHelp) {
                    presentFragment(new ReportHelpActivity());
                } else if (i == sessionsRow) {
                    presentFragment(new SessionsActivity(0));
                } else if (i == passwordRow) {
                    presentFragment(new TwoStepVerificationActivity(0));
                } else if (i == passcodeRow) {
                    if (SharedConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                } else if (i == fileLocationRow) {
                    // *****

                    final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    final boolean isInternal = preferences.getBoolean("isInternal", true);

                    if (isInternal) {
                        String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        if (absolutePath.endsWith("/")) {
                            absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
                        }
                        File file2 = new File(absolutePath + "/Android/data/" + getParentActivity().getPackageName() + "/files/");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.formatString("StorageToExtFilesDirAlert", R.string.StorageToExtFilesDirAlert, file2.getAbsolutePath()));
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().putBoolean("isInternal", !isInternal).commit();
                                ((TextDetailSettingsCell) view).setTextAndValue(LocaleController.getString("fileLocationRow", R.string.fileLocationRow), preferences.getBoolean("isInternal", true) ? LocaleController.getString("fileLocatiInternal", R.string.fileLocatiInternal) : LocaleController.getString("fileLocatiExternal", R.string.fileLocatiExternal), true);

                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else {

                        preferences.edit().putBoolean("isInternal", !isInternal).commit();
                        ((TextDetailSettingsCell) view).setTextAndValue(LocaleController.getString("fileLocationRow", R.string.fileLocationRow), preferences.getBoolean("isInternal", true) ? LocaleController.getString("fileLocatiInternal", R.string.fileLocatiInternal) : LocaleController.getString("fileLocatiExternal", R.string.fileLocatiExternal), true);
                    }
                }
                //Tele X
                else if (i == savePlusSettingsRow) {
                    LayoutInflater li = LayoutInflater.from(getParentActivity());
                    View promptsView = li.inflate(R.layout.editbox_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setView(promptsView);
                    final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                    userInput.setHint(LocaleController.getString("EnterName", R.string.EnterName));
                    userInput.setHintTextColor(0xff979797);
                    SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                    int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                    userInput.getBackground().setColorFilter(themePrefs.getInt("dialogColor", defColor), PorterDuff.Mode.SRC_IN);
                    AndroidUtilities.clearCursorDrawable(userInput);
                    //builder.setMessage(LocaleController.getString("EnterName", R.string.EnterName));
                    builder.setTitle(LocaleController.getString("SaveSettings", R.string.SaveSettings));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (saving) {
                                return;
                            }
                            saving = true;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    saving = false;
                                    if (getParentActivity() != null) {
                                        String pName = userInput.getText().toString();
                                        //AndroidUtilities.setStringPref(getParentActivity(), "themeName", pName);
                                        //try{
                                        //    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                                        //    AndroidUtilities.setStringPref(getParentActivity(),"version", pInfo.versionName);
                                        //} catch (Exception e) {
                                        //    FileLog.e( e);
                                        //}
                                        //AndroidUtilities.setStringPref(getParentActivity(),"model", android.os.Build.MODEL+"/"+android.os.Build.VERSION.RELEASE);
                                        Utilities.savePreferencesToSD(getParentActivity(), "/Telegram/Telegram Documents", "roboconfig.xml", pName + ".xml", true);
                                        //Utilities.copyWallpaperToSD(getParentActivity(), pName, true);
                                        //Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("SaveThemeToastText", R.string.SaveThemeToastText), Toast.LENGTH_SHORT);
                                        //toast.show();
                                    }
                                }
                            });
                        }
                    });

                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == restorePlusSettingsRow) {
                    DocumentSelectActivity fragment = new DocumentSelectActivity(false);
                    fragment.fileFilter = ".xml";
                    fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
                        @Override
                        public void didSelectFiles(final DocumentSelectActivity activity, ArrayList<String> files) {
                            final String xmlFile = files.get(0);
                            File file = new File(xmlFile);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("RestoreSettings", R.string.RestoreSettings));
                            builder.setMessage(file.getName());
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Utilities.loadPrefFromSD(getParentActivity(), xmlFile, "BaseConfig") == 4) {
                                                Utilities.restartApp();
                                                /*activity.finishFragment();
                                                if (listView != null) {
                                                    listView.invalidateViews();
                                                    fixLayout();
                                                }*/
                                            }
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }

                        @Override
                        public void startDocumentSelectActivity() {
                        }
                    });
                    presentFragment(fragment);
                } else if (i == resetPlusSettingsRow) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                    builder.setTitle(LocaleController.getString("ResetSettings", R.string.ResetSettings));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (reseting) {
                                return;
                            }
                            reseting = true;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    reseting = false;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear();
                                    editor.apply();
                                    if (listView != null) {
                                        listView.invalidateViews();
                                        fixLayout();
                                    }
                                }
                            });
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
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (getParentActivity() == null) {
                    return false;
                }

                return true;
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        ViewProxy.setPivotY(extraHeightView, 0);
        extraHeightView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        avatarImage = new BackupImageView(context);
        //avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        int radius = AndroidUtilities.getIntDef("prefAvatarRadius", 32);
        avatarImage.setRoundRadius(radius);
        ViewProxy.setPivotX(avatarImage, 0);
        ViewProxy.setPivotY(avatarImage, 0);
        int aSize = themePrefs.getInt("prefAvatarSize", 42);
        //frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(aSize, aSize, Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, BaseSettings.this);
                }
            }
        });

        SharedPreferences themesPrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
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
        onlineTextView.setTextColor(Theme.getColor(Theme.key_avatar_subtitleInProfileBlue));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        //statusTextView = new TextView(context);
        //statusTextView.setText("TEST");
        //statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        //statusTextView.setTextColor(preferences.getInt("prefHeaderStatusColor", AndroidUtilities.getIntDarkerColor("themeColor", -0x40)));
        //statusTextView.setLines(1);
        //statusTextView.setMaxLines(1);
        //statusTextView.setSingleLine(true);
        //statusTextView.setEllipsize(TextUtils.TruncateAt.END);
        //statusTextView.setGravity(Gravity.LEFT);
        //frameLayout.addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));


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
        //MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public boolean allowCaption() {
        return false;
    }

    @Override
    public boolean scaleToFill() {
        return false;
    }

    @Override
    public void toggleGroupPhotosEnabled() {

    }

    @Override
    public ArrayList<Object> getSelectedPhotosOrder() {
        return null;
    }

    @Override
    public HashMap<Object, Object> getSelectedPhotos() {
        return null;
    }

    @Override
    public boolean canScrollAway() {
        return false;
    }

    @Override
    public boolean allowGroupPhotos() {
        return false;
    }

    @Override
    public int getPhotoIndex(int index) {
        return 0;
    }

    @Override
    public void deleteImageAtIndex(int index) {

    }

    @Override
    public String getDeleteMessageString() {
        return null;
    }

    @Override
    public boolean canCaptureMorePhotos() {
        return false;
    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
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
                object.dialogId = UserConfig.getInstance(currentAccount).getClientUserId();
                //object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                object.scale = ViewProxy.getScaleX(avatarImage);
                return object;
            }
        }
        return null;
    }

    @Override
    public ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
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
    public int setPhotoChecked(int index, VideoEditedInfo videoEditedInfo) {
        return 0;
    }

    @Override
    public int setPhotoUnchecked(Object photoEntry) {
        return 0;
    }


    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void needAddMorePhotos() {

    }

    @Override
    public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo) {

    }


    @Override
    public int getSelectedCount() {
        return 0;
    }

    public void performAskAQuestion() {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        int uid = preferences.getInt("support_id", 0);
        TLRPC.User supportUser = null;
        if (uid != 0) {
            supportUser = MessagesController.getInstance(currentAccount).getUser(uid);
            if (supportUser == null) {
                String userString = preferences.getString("support_user", null);
                if (userString != null) {
                    try {
                        byte[] datacentersBytes = Base64.decode(userString, Base64.DEFAULT);
                        if (datacentersBytes != null) {
                            SerializedData data = new SerializedData(datacentersBytes);
                            supportUser = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                            if (supportUser != null && supportUser.id == 333000) {
                                supportUser = null;
                            }
                            data.cleanup();
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                        supportUser = null;
                    }
                }
            }
        }
        if (supportUser == null) {
            final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            TLRPC.TL_help_getSupport req = new TLRPC.TL_help_getSupport();
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {
                    if (error == null) {

                        final TLRPC.TL_help_support res = (TLRPC.TL_help_support) response;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("support_id", res.user.id);
                                SerializedData data = new SerializedData();
                                res.user.serializeToStream(data);
                                editor.putString("support_user", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                                editor.commit();
                                data.cleanup();
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                                ArrayList<TLRPC.User> users = new ArrayList<>();
                                users.add(res.user);
                                MessagesStorage.getInstance(currentAccount).putUsersAndChats(users, null, true, true);
                                MessagesController.getInstance(currentAccount).putUser(res.user, false);
                                Bundle args = new Bundle();
                                args.putInt("user_id", res.user.id);
                                presentFragment(new ChatActivity(args));
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            MessagesController.getInstance(currentAccount).putUser(supportUser, true);
            Bundle args = new Bundle();
            args.putInt("user_id", supportUser.id);
            presentFragment(new ChatActivity(args));
        }
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
    public void didReceivedNotification(int id,int account, Object... args) {
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
//        updateTheme();
        fixLayout();
    }



    public void getUserAbout(final int uid) {
        final TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(uid);
        if (user == null || user.username == null) {
            return;
        }
        String link = String.format("https://telegram.me/%s", user.username);
        //Log.e("SettingsActivity", "getUserAbout link "+link);
        final TLRPC.TL_messages_getWebPagePreview req = new TLRPC.TL_messages_getWebPagePreview();
        req.message = link;

        linkSearchRequestId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        linkSearchRequestId = 0;
                        if (error == null) {
                            if (response instanceof TLRPC.TL_messageMediaWebPage) {
                                foundWebPage = ((TLRPC.TL_messageMediaWebPage) response).webpage;
                                if (foundWebPage.description != null) {
                                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.userInfoDidLoad, uid);
                                } else {
                                    if (pass != 1) {
                                        pass = 1;
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                getUserAbout(uid);
                                            }
                                        }, 500);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(linkSearchRequestId, classGuid);
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


            final boolean setVisible = diff > 0.2f;
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            int aSize = themePrefs.getInt("prefAvatarSize", 42);
            //ViewProxy.setScaleX(avatarImage, (42 + 18 * diff) / 42.0f);
            //ViewProxy.setScaleY(avatarImage, (42 + 18 * diff) / 42.0f);
            ViewProxy.setScaleX(avatarImage, (aSize + 18 * diff) / (aSize * 1.0f));
            ViewProxy.setScaleY(avatarImage, (aSize + 18 * diff) / (aSize * 1.0f));
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            ViewProxy.setTranslationX(avatarImage, -AndroidUtilities.dp(47) * diff);
            ViewProxy.setTranslationY(avatarImage, (float) Math.ceil(avatarY));
            ViewProxy.setTranslationX(nameTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(nameTextView, (float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            ViewProxy.setTranslationX(onlineTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(onlineTextView, (float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);
            //ViewProxy.setTranslationX(statusTextView, -21 * AndroidUtilities.density * diff);
            //ViewProxy.setTranslationY(statusTextView, (float) Math.floor(avatarY) + AndroidUtilities.dp(32) + (float) Math.floor(22 * AndroidUtilities.density) * diff);
            ViewProxy.setScaleX(nameTextView, 1.0f + 0.12f * diff);
            ViewProxy.setScaleY(nameTextView, 1.0f + 0.12f * diff);
            //if (diff > 0.85) {
            //    statusTextView.setVisibility(View.VISIBLE);
            //} else {
            //    statusTextView.setVisibility(View.GONE);
            //}
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
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        int radius = AndroidUtilities.dp(AndroidUtilities.getIntDef("prefAvatarRadius", 32));
        avatarImage.getImageReceiver().setRoundRadius(radius);
//        avatarDrawable.setRadius(radius);
        if (avatarImage != null) {
            //avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));
            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));

            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
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
            return i == tabsRow || i == chatSettingsRow ||
                    i == forwardSettingsRow || i == categorySettingsRow || i == maxPinnedDialogsCount ||
                    i == viewSettingRow || i == reportHelp || i == sessionsRow || i == passwordRow || i == passcodeRow || i == fileLocationRow ||
                    i == savePlusSettingsRow || i == restorePlusSettingsRow || i == resetPlusSettingsRow;
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
                if (i == tabsRow) {
                    textCell.setText(LocaleController.getString("TabSettings", R.string.TabSettings), true);
                } else if (i == chatSettingsRow) {
                    textCell.setText(LocaleController.getString("ChatsSettings", R.string.ChatsSettings), true);
                } else if (i == forwardSettingsRow) {
                    textCell.setText(LocaleController.getString("ForwardSettings", R.string.ForwardSettings), true);
                } else if (i == categorySettingsRow) {
                    textCell.setText(LocaleController.getString("CategorySettings", R.string.CategorySettings), true);
                } else if (i == reportHelp) {
                    textCell.setText(LocaleController.getString("ReportProblemHelp", R.string.ReportProblemHelp), true);
                } else if (i == sessionsRow) {
                    textCell.setText(LocaleController.getString("SessionsTitle", R.string.SessionsTitle), true);
                } else if (i == passwordRow) {
                    textCell.setText(LocaleController.getString("TwoStepVerification", R.string.TwoStepVerification), true);
                } else if (i == passcodeRow) {
                    textCell.setText(LocaleController.getString("Passcode", R.string.Passcode), true);
                } else if (i == viewSettingRow) {
                    textCell.setText(LocaleController.getString("ViewSetting", R.string.ViewSetting), true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);

            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == settingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                } else if (i == PrivacySection2) {
                    ((HeaderCell) view).setText(LocaleController.getString("PrivacyTitle", R.string.PrivacyTitle));
                } else if (i == plusSettingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("PlusSettings", R.string.PlusSettings));
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 0:
                                abi = "arm";
                                break;
                            case 1:
                                abi = "arm-v7a";
                                break;
                            case 2:
                                abi = "x86";
                                break;
                            case 3:
                                abi = "universal";
                                break;
                        }
                        ((TextInfoCell) view).setText(String.format(Locale.US, "Telegram for Android v%s (%d) %s", pInfo.versionName, code, abi));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;


                if (i == savePlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("SaveSettings", R.string.SaveSettings), LocaleController.getString("SaveSettingsSum", R.string.SaveSettingsSum), true);
                } else if (i == fileLocationRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("fileLocationRow", R.string.fileLocationRow), preferences.getBoolean("isInternal", true) ? LocaleController.getString("fileLocatiInternal", R.string.fileLocatiInternal) : LocaleController.getString("fileLocatiExternal", R.string.fileLocatiExternal), true);
                } else if (i == restorePlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("RestoreSettings", R.string.RestoreSettings), LocaleController.getString("RestoreSettingsSum", R.string.RestoreSettingsSum), true);
                } else if (i == resetPlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("ResetSettings", R.string.ResetSettings), LocaleController.getString("ResetSettingsSum", R.string.ResetSettingsSum), false);
                }
            } else if (type == 7) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == maxPinnedDialogsCount) {
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("BaseConfig", Activity.MODE_PRIVATE);
                    SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                    int size = mainPreferences.getInt("maxPinnedDialogsCount", 5);
                    textCell.setTextAndValue(LocaleController.getString("maxPinnedDialogsCount", R.string.maxPinnedDialogsCount), String.format("%d", size), true);
                }
            } else if (type == 8) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }

            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (i == settingsSectionRow || i == PrivacySection || i == plusSettingsSectionRow) {
                return 1;
            } else if (i == tabsRow || i == chatSettingsRow ||
                    i == forwardSettingsRow || i == categorySettingsRow || i == viewSettingRow ||
                    i == reportHelp || i == sessionsRow || i == passwordRow || i == passcodeRow) {
                return 2;
            } else if (i == savePlusSettingsRow ||
                    i == restorePlusSettingsRow || i == resetPlusSettingsRow || i == fileLocationRow) {
                return 6;
            } else if (i == settingsSectionRow2 || i == PrivacySection2 || i == plusSettingsSectionRow2) {
                return 4;
            } else if (i == maxPinnedDialogsCount) {
                return 7;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 9;
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
                new ThemeDescription(extraHeightView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_title),
                new ThemeDescription(onlineTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileBlue),
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
                //new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, 0, new Class[]{TextInfoCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText5),

                new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{Theme.avatar_savedDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),

        };
    }

}

