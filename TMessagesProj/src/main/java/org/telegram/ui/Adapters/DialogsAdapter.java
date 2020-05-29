/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogMeUrlCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Const;
import telegram.messenger.xtelex.util.Utils;

public class DialogsAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private int dialogsType;
    private long openedDialogId;
    private int currentCount;
    private boolean isOnlySelect;
    private ArrayList<Long> selectedDialogs;
    private boolean hasHints;
    private int currentAccount = UserConfig.selectedAccount;
    private boolean showContacts;

    //Devgram
    private ArrayList<TLRPC.TL_dialog> dialogsArray = new ArrayList<>();
    //


    public DialogsAdapter(Context context, int type, boolean onlySelect) {
        mContext = context;
        dialogsType = type;
        isOnlySelect = onlySelect;
        hasHints = type == 0 && !onlySelect;
        if (onlySelect) {
            selectedDialogs = new ArrayList<>();
        }
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean hasSelectedDialogs() {
        return selectedDialogs != null && !selectedDialogs.isEmpty();
    }

    public void addOrRemoveSelectedDialog(long did, View cell) {
        if (selectedDialogs.contains(did)) {
            selectedDialogs.remove(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(false, true);
            }
        } else {
            selectedDialogs.add(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(true, true);
            }
        }
    }

    public ArrayList<Long> getSelectedDialogs() {
        return selectedDialogs;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getItemCount() || current == 1;
    }

    @Override
    public int getItemCount() {
        showContacts = false;
        ArrayList<TLRPC.TL_dialog> array = getDialogsArray();
        int dialogsCount = array.size();
        if (dialogsCount == 0 && MessagesController.getInstance(currentAccount).loadingDialogs) {
            return 0;
        }
        int count = dialogsCount;
        if (!MessagesController.getInstance(currentAccount).dialogsEndReached || dialogsCount == 0) {
            count++;
        }
        if (hasHints) {
            count += 2 + MessagesController.getInstance(currentAccount).hintDialogs.size();
        } else if (dialogsType == 0 && dialogsCount == 0) {
            if (ContactsController.getInstance(currentAccount).contacts.isEmpty() && ContactsController.getInstance(currentAccount).isLoadingContacts()) {
                return 0;
            }
            if (!ContactsController.getInstance(currentAccount).contacts.isEmpty()) {
                count += ContactsController.getInstance(currentAccount).contacts.size() + 2;
                showContacts = true;
            }
        }
        currentCount = count;
        return count;
    }

    public TLObject getItem(int i) {
        if (showContacts) {
            i -= 3;
            if (i < 0 || i >= ContactsController.getInstance(currentAccount).contacts.size()) {
                return null;
            }
            return MessagesController.getInstance(currentAccount).getUser(ContactsController.getInstance(currentAccount).contacts.get(i).user_id);
        }
        ArrayList<TLRPC.TL_dialog> arrayList = getDialogsArray();
        if (hasHints) {
            int count = MessagesController.getInstance(currentAccount).hintDialogs.size();
            if (i < 2 + count) {
                return MessagesController.getInstance(currentAccount).hintDialogs.get(i - 1);
            } else {
                i -= count + 2;
            }
        }
        if (i < 0 || i >= arrayList.size()) {
            return null;
        }
        return arrayList.get(i);
    }

    @Override
    public void notifyDataSetChanged() {
        hasHints = dialogsType == 0 && !isOnlySelect && !MessagesController.getInstance(currentAccount).hintDialogs.isEmpty();
        super.notifyDataSetChanged();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView instanceof DialogCell) {
            ((DialogCell) holder.itemView).checkCurrentDialogIndex();
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        return viewType != 1 && viewType != 5 && viewType != 3 && viewType != 8 && viewType != 7;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new DialogCell(mContext, isOnlySelect);
                break;
            case 1:
                view = new LoadingCell(mContext);
                break;
            case 2: {
                HeaderCell headerCell = new HeaderCell(mContext);
                headerCell.setText(LocaleController.getString("RecentlyViewed", R.string.RecentlyViewed));

                TextView textView = new TextView(mContext);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
                textView.setText(LocaleController.getString("RecentlyViewedHide", R.string.RecentlyViewedHide));
                textView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
                headerCell.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 17, 15, 17, 0));
                textView.setOnClickListener(view1 -> {
                    MessagesController.getInstance(currentAccount).hintDialogs.clear();
                    SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                    preferences.edit().remove("installReferer").commit();
                    notifyDataSetChanged();
                });

                view = headerCell;
                break;
            }
            case 3:
                FrameLayout frameLayout = new FrameLayout(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(12), MeasureSpec.EXACTLY));
                    }
                };
                frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                View v = new View(mContext);
                v.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                frameLayout.addView(v, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                view = frameLayout;
                break;
            case 4:
                view = new DialogMeUrlCell(mContext);
                break;
            case 5:
                view = new DialogsEmptyCell(mContext);
                break;
            case 6:
                view = new UserCell(mContext, 8, 0, false);
                break;
            case 7:
                HeaderCell headerCell = new HeaderCell(mContext);
                headerCell.setText(LocaleController.getString("YourContacts", R.string.YourContacts));
                view = headerCell;
                break;
            case 8:
            default:
                view = new ShadowSectionCell(mContext);
                Drawable drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow);
                CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                combinedDrawable.setFullsize(true);
                view.setBackgroundDrawable(combinedDrawable);
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, viewType == 5 ? RecyclerView.LayoutParams.MATCH_PARENT : RecyclerView.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        switch (holder.getItemViewType()) {
            case 0: {
                DialogCell cell = (DialogCell) holder.itemView;
                TLRPC.TL_dialog dialog = (TLRPC.TL_dialog) getItem(i);
                TLRPC.TL_dialog nextDialog = (TLRPC.TL_dialog) getItem(i + 1);

                if (hasHints) {
                    i -= 2 + MessagesController.getInstance(currentAccount).hintDialogs.size();
                }
                cell.useSeparator = (i != getItemCount() - 1);
                cell.fullSeparator = dialog.pinned && nextDialog != null && !nextDialog.pinned;
                if (dialogsType == 0) {
                    if (AndroidUtilities.isTablet()) {
                        cell.setDialogSelected(dialog.id == openedDialogId);
                    }
                }
                if (selectedDialogs != null) {
                    cell.setChecked(selectedDialogs.contains(dialog.id), false);
                }



                cell.setDialog(dialog, i, dialogsType);
                break;
            }
            case 5: {
                DialogsEmptyCell cell = (DialogsEmptyCell) holder.itemView;
                cell.setType(showContacts ? 1 : 0);
                break;
            }
            case 4: {
                DialogMeUrlCell cell = (DialogMeUrlCell) holder.itemView;
                cell.setRecentMeUrl((TLRPC.RecentMeUrl) getItem(i));
                break;
            }
            case 6: {
                UserCell cell = (UserCell) holder.itemView;
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(ContactsController.getInstance(currentAccount).contacts.get(i - 3).user_id);
                cell.setData(user, null, null, 0);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (showContacts) {
            if (i == 0) {
                return 5;
            } else if (i == 1) {
                return 8;
            } else if (i == 2) {
                return 7;
            } else {
                return 6;
            }
        } else if (hasHints) {
            int count = MessagesController.getInstance(currentAccount).hintDialogs.size();
            if (i < 2 + count) {
                if (i == 0) {
                    return 2;
                } else if (i == 1 + count) {
                    return 3;
                }
                return 4;
            } else {
                i -= 2 + count;
            }
        }
        if (i == getDialogsArray().size()) {
            if (!MessagesController.getInstance(currentAccount).dialogsEndReached) {
                return 1;
            } else {
                return 5;
            }
        }
        return 0;
    }

    /*Devgram*/

    //Devgram
    public ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        dialogsArray = MessagesController.getInstance(currentAccount).dialogs;
    /*if (dialogsArray != null) {
      return dialogsArray;
    } else {*/
        dialogsArray = sortDialogs();
        return dialogsArray;
        //}
    }

    //Devgram
    public ArrayList<TLRPC.TL_dialog> sortDialogs() {
        //Log.e("DialogsAdapter", "sortDialogs dialogsType " + dialogsType);
        switch (dialogsType) {
            case 0:
                if (Theme.plusSortAll == 0 || Theme.plusHideTabs) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogs);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogs);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogs);
                }
                return MessagesController.getInstance(currentAccount).dialogs;
            case 1:
                return MessagesController.getInstance(currentAccount).dialogsServerOnly;
            case 2:
                return MessagesController.getInstance(currentAccount).dialogsGroupsOnly;
            case 3:
                if (Theme.plusSortUsers == 0) {
                    sortUsersDefault();
                } else {
                    //sortUsersByStatus();
                    sortUsersByStatusPinnedOnTop();
                }
                return MessagesController.getInstance(currentAccount).dialogsUsers;
            case 4:
                if (Theme.plusSortGroups == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsGroups);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsGroups);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsGroups);
                }
                return MessagesController.getInstance(currentAccount).dialogsGroups;
            case 5:
                if (Theme.plusSortChannels == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsChannels);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsChannels);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsChannels);
                }
                return MessagesController.getInstance(currentAccount).dialogsChannels;
            case 6:
                if (Theme.plusSortBots == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsBots);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsBots);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsBots);
                }
                return MessagesController.getInstance(currentAccount).dialogsBots;
            case 7:
                if (Theme.plusSortSuperGroups == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsMegaGroups);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsMegaGroups);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsMegaGroups);
                }
                return MessagesController.getInstance(currentAccount).dialogsMegaGroups;
            case 8:
                if (Theme.plusSortFavs == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsFavs);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsFavs);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsFavs);
                }
                return MessagesController.getInstance(currentAccount).dialogsFavs;
            case 9:
                if (Theme.plusSortGroups == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsGroupsAll);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogsGroupsAll);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsGroupsAll);
                }
                return MessagesController.getInstance(currentAccount).dialogsGroupsAll;
            case 10:
                if (Theme.plusSortAdmin == 0) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsadmins);
                } else {
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsadmins);
                }
                return MessagesController.getInstance(currentAccount).dialogsadmins;
            case 11:
                if (Theme.plusSortUnread == 0) {
                    //Log.e("DialogsAdapter", "sortDialogs Unread  " + Theme.plusSortUnread);
                    sortDefault(MessagesController.getInstance(currentAccount).dialogsUnread);
                } else {
                    if (Theme.plusSortUnread == 2) {
                        sortUnreadPinedOnTopUnmutedFirst(MessagesController.getInstance(currentAccount).dialogsUnread);
                    } else {
                        sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogsUnread);
                    }
                }
                return MessagesController.getInstance(currentAccount).dialogsUnread;

            case 12:
                return Const.dialogsCats;


            default:
                if (Theme.plusSortAll == 0 || Theme.plusHideTabs) {
                    sortDefault(MessagesController.getInstance(currentAccount).dialogs);
                } else {
                    //sortUnread(MessagesController.getInstance().dialogs);
                    sortUnreadPinedOnTop(MessagesController.getInstance(currentAccount).dialogs);
                }
                return MessagesController.getInstance(currentAccount).dialogs;
        }
    }
    //

    //Devgram
    public void setDialogsType(int type) {
        this.dialogsType = type;
    }
    //


    //Devgram
    private void sortUnreadPinedOnTop(ArrayList<TLRPC.TL_dialog> dialogs) {
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (dialog1.unread_count == dialog2.unread_count) {
                    return 0;
                } else if (dialog1.unread_count < dialog2.unread_count) {
                    return 1;
                } else {
                    return -1;
                }

            }
        });
    }
    //


    //Devgram
    private void sortUnreadPinedOnTopUnmutedFirst(ArrayList<TLRPC.TL_dialog> dialogs) {
        Collections.sort(dialogs, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                /*if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if(!MessagesController.getInstance().isDialogMuted(dialog1.id) && MessagesController.getInstance().isDialogMuted(dialog2.id)){
                    return 1;
                } else if(MessagesController.getInstance().isDialogMuted(dialog1.id) && !MessagesController.getInstance().isDialogMuted(dialog2.id)){
                    return -1;
                } else if(MessagesController.getInstance().isDialogMuted(dialog1.id) && MessagesController.getInstance().isDialogMuted(dialog2.id)){

                }*/
                if (!MessagesController.getInstance(currentAccount).isDialogMuted(dialog2.id)) {
                    return 1;
                } else if (!MessagesController.getInstance(currentAccount).isDialogMuted(dialog1.id)) {
                    return -1;
                } else {
                    //if (dialog1.unread_count == dialog2.unread_count) {
                    return 0;
                    /*} else if (dialog1.unread_count < dialog2.unread_count) {
                        return 1;
                    } else {
                        return -1;
                    }*/
                }
            }
        });
    }
    //


    //Devgram
    private void sortUsersDefault() {
        Collections.sort(MessagesController.getInstance(currentAccount).dialogsUsers, dialogComparator);
    }
    //


    //Devgram
    private final Comparator<TLRPC.TL_dialog> dialogComparator = new Comparator<TLRPC.TL_dialog>() {
        @Override
        public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {

            if (!dialog1.pinned && dialog2.pinned) {
                return 1;
            } else if (dialog1.pinned && !dialog2.pinned) {
                return -1;
            } else if (dialog1.pinned && dialog2.pinned) {
                if (dialog1.pinnedNum < dialog2.pinnedNum) {
                    return 1;
                } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                    return -1;
                } else {
                    return 0;
                }
            }
            TLRPC.DraftMessage draftMessage = DataQuery.getInstance(currentAccount).getDraft(dialog1.id);
            int date1 = draftMessage != null && draftMessage.date >= dialog1.last_message_date ? draftMessage.date : dialog1.last_message_date;
            draftMessage = DataQuery.getInstance(currentAccount).getDraft(dialog2.id);
            int date2 = draftMessage != null && draftMessage.date >= dialog2.last_message_date ? draftMessage.date : dialog2.last_message_date;
            if (date1 < date2) {
                return 1;
            } else if (date1 > date2) {
                return -1;
            }
            return 0;
        }
    };
    //

    //Devgram
    private void sortUsersByStatusPinnedOnTop() {
        Collections.sort(MessagesController.getInstance(currentAccount).dialogsUsers, new Comparator<TLRPC.TL_dialog>() {
            @Override
            public int compare(TLRPC.TL_dialog dialog1, TLRPC.TL_dialog dialog2) {
                if (!dialog1.pinned && dialog2.pinned) {
                    return 1;
                } else if (dialog1.pinned && !dialog2.pinned) {
                    return -1;
                } else if (dialog1.pinned && dialog2.pinned) {
                    if (dialog1.pinnedNum < dialog2.pinnedNum) {
                        return 1;
                    } else if (dialog1.pinnedNum > dialog2.pinnedNum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                TLRPC.User user1 = MessagesController.getInstance(currentAccount).getUser((int) dialog2.id);
                TLRPC.User user2 = MessagesController.getInstance(currentAccount).getUser((int) dialog1.id);
                int status1 = 0;
                int status2 = 0;
                if (user1 != null && user1.status != null) {
                    if (user1.id == UserConfig.getInstance(currentAccount).getClientUserId()) {
                        status1 = ConnectionsManager.getInstance(currentAccount).getCurrentTime() + 50000;
                    } else {
                        status1 = user1.status.expires;
                    }
                }
                if (user2 != null && user2.status != null) {
                    if (user2.id == UserConfig.getInstance(currentAccount).getClientUserId()) {
                        status2 = ConnectionsManager.getInstance(currentAccount).getCurrentTime() + 50000;
                    } else {
                        status2 = user2.status.expires;
                    }
                }
                if (status1 > 0 && status2 > 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 < 0) {
                    if (status1 > status2) {
                        return 1;
                    } else if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                } else if (status1 < 0 && status2 > 0 || status1 == 0 && status2 != 0) {
                    return -1;
                } else if (status2 < 0 && status1 > 0 || status2 == 0 && status1 != 0) {
                    return 1;
                }
                return 0;
            }
        });
    }
    //

    //Devgram
    private void sortDefault(ArrayList<TLRPC.TL_dialog> dialogs) {


        //Log.e("DialogsAdapter", "sortDefault dialogsType " + dialogsType);
        Collections.sort(dialogs, dialogComparator);
    }
    //


}
