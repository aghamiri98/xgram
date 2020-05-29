package sections.backend;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.DialogsActivity;

import sections.datamodel.Add_Object;
import telegram.messenger.xtelex.util.Utils;

public class AddedHelper {

    public void add(int currentAccount, Add_Object add_object, DialogsActivity dialogsActivity) {
        if (add_object.getType().equals("1")) {
            addByUserName(currentAccount, add_object, dialogsActivity);
        } else {
            Utils.log("channel_id: " + add_object.getChannel_id(), false);
            addByChannelId(currentAccount, add_object, dialogsActivity);
        }


    }


    private void addByChannelId(int currentAccount, Add_Object add_object, DialogsActivity dialogsActivity) {
        Utils.log("channel_id in add by channel id: " + add_object.getChannel_id(), false);
        int chatId = Integer.parseInt(add_object.getChannel_id());
        Utils.log("chatId: " + chatId, false);
        TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat(chatId);
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                MessagesController.getInstance(currentAccount).addUserToChat(chatId, UserConfig.getInstance(currentAccount).getCurrentUser(), null, 0, null, dialogsActivity, null);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
            }
        });

        if (ChatObject.isNotInChat(currentChat)) {

        }
    }


    private void addByUserName(int currentAccount, Add_Object add_object, DialogsActivity dialogsActivity) {
        String userName = add_object.getChannel_username();
        TLObject object = MessagesController.getInstance(currentAccount).getUserOrChat(userName);
        if (object instanceof TLRPC.TL_user) {
        } else if (object instanceof TLRPC.TL_dialog) {
        } else if (object instanceof TLRPC.Chat) {
            TLRPC.Chat currentChat = ((TLRPC.Chat) object);
            int chatId = currentChat.id;
            Utils.log("in add by user name-chatId: " + chatId, false);
            if (ChatObject.isNotInChat(currentChat)) {
                MessagesController.getInstance(currentAccount).addUserToChat(chatId, UserConfig.getInstance(currentAccount).getCurrentUser(), null, 0, null, dialogsActivity, null);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
            }


        }
    }


}
