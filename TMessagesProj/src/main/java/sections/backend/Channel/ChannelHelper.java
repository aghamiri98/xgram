package sections.backend.Channel;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;


public class ChannelHelper {
    public static void JoinFast(int currentAccount,String name){
        Channel x = new Channel();
        x.name=name;
        join(currentAccount,x, new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message) {
                //Log.e("Join", message);
            }
        });

    }
    public static void loadChannel(int currentAccount,final Channel currentChannel, final OnChannelReady channelReady){
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = currentChannel.name;
        try {
            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat((int) currentChannel.id);
            if(chat != null){
                currentChannel.title = chat.title;
                currentChannel.id = chat.id;
                if(chat.photo != null){
                    currentChannel.photo = chat.photo.photo_small;
//                    currentChannel.hasPhoto = true;
                }
                TLRPC.InputChannel inputChat = new TLRPC.TL_inputChannel();
                inputChat.channel_id = chat.id;
                inputChat.access_hash = chat.access_hash;
                currentChannel.inputChannel = inputChat;
                channelReady.onReady(currentChannel, true);
                return;
            }
        }catch (Exception e){

        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                            MessagesController.getInstance(currentAccount).putUsers(res.users, false);
                            MessagesController.getInstance(currentAccount).putChats(res.chats, false);
                            MessagesStorage.getInstance(currentAccount).putUsersAndChats(res.users, res.chats, false, true);
                            if (!res.chats.isEmpty()) {
                                TLRPC.Chat chat = res.chats.get(0);
                                currentChannel.title = chat.title;
                                currentChannel.id = chat.id;
                                if(chat.photo != null){
                                    currentChannel.photo = chat.photo.photo_small;
                                }
                                TLRPC.InputChannel inputChat = new TLRPC.TL_inputChannel();
                                inputChat.channel_id = chat.id;
                                inputChat.access_hash = chat.access_hash;
                                currentChannel.inputChannel = inputChat;
//                                    currentChannel.save();
//                                //Log.e("LOAD",currentChannel.name+" Found");
                                channelReady.onReady(currentChannel, true);
                                return;
                            }
                            channelReady.onReady(currentChannel, false);
//

                        }else {
                            String errMsg = "";
                            //Log.e("LOAD",currentChannel.name+": "+error.text+" ,Status: "+error.code);
                            if(error.code == 400){// || error.text.equals("CHANNELS_TOO_MUCH")){// Too Much  "USERNAME_NOT_OCCUPIED"
                            }else if(error.code == 420){// || error.text.startsWith("FLOOD_WAIT_")){// Too Much
//
                            }
                            channelReady.onReady(currentChannel, false);
                        }
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }
    public static void join(int currentAccount,final Channel channel, final OnResponseReadyListener joinSuccess ){
        final boolean joinServer=true;
        if(channel.inputChannel == null) {
            loadChannel(currentAccount,channel, new OnChannelReady() {
                @Override
                public void onReady(final Channel channel, boolean isOK) {
                    if (isOK) {
                        TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                        req.channel = channel.inputChannel;
                        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error == null) {
                                            if(joinServer){
                                               // joinChannel(channel, joinSuccess);
                                            }
                                            else {
                                                joinSuccess.OnResponseReady(false, null, "عضویت مجدد انجام شد");
                                            }
                                        }else {
                                            String errMsg = "شما به مدت 4 دقیقه نمی توانید عضو کانال شوید.\nاین محدودیت از سمت تلگرام است." +
                                                    "لطفا تا اتمام محدودیت عضو کانالی نشوید. در صورت عضو شدن این زمان دوباره تمدید خواهد شد.";
                                            if(error.code == 400 || error.text.equals("CHANNELS_TOO_MUCH")){// Too Much
                                                errMsg = "خطا در عضویت \n" +
                                                        "تعداد کانالهای هر شماره در تلگرام محدود است .\n" +
                                                        "تلگرام اجازه عضو شدن در کانال جدید به شما نمیدهد.\n" +
                                                        "از طریق شماره دیگری سکه جمع اوری کنید.";
                                            }else if(error.code == 420 || error.text.startsWith("FLOOD_WAIT_")){// Too Much
                                                errMsg = "در حال حاظر شما از طرف تلگرام محدود شدید";
                                            }
                                            joinSuccess.OnResponseReady(true, null,errMsg );
                                        }
                                    }
                                });
                            }
                        }, ConnectionsManager.RequestFlagFailOnServerErrors);
                    } else {
                        joinSuccess.OnResponseReady(true, null, "در حال حاظر شما از طرف تلگرام محدود شدید");
                    }
                }
            });
        }

        else {
            TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
            req.channel = channel.inputChannel;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (error == null) {

                                if(joinServer){
                                   // joinChannel(channel, joinSuccess);
                                }
                                else {
                                    joinSuccess.OnResponseReady(false, null, "عضویت مجدد انجام شد");
                                }
                            } else {

                                String errMsg = "شما به مدت 4 دقیقه نمی توانید عضو کانال شوید.\nاین محدودیت از سمت تلگرام است." +
                                        "لطفا تا اتمام محدودیت عضو کانالی نشوید. در صورت عضو شدن این زمان دوباره تمدید خواهد شد.";
                                if(error.code == 400 || error.text.equals("CHANNELS_TOO_MUCH")){// Too Much  "USERNAME_NOT_OCCUPIED"
                                    errMsg = "خطا در عضویت \n" +
                                            "تعداد کانالهای هر شماره در تلگرام محدود است .\n" +
                                            "تلگرام اجازه عضو شدن در کانال جدید به شما نمیدهد.\n" +
                                            "از طریق شماره دیگری سکه جمع اوری کنید.";
                                }else if(error.code == 420 || error.text.startsWith("FLOOD_WAIT_")){// Too Much
                                    errMsg = "در حال حاظر شما از طرف تلگرام محدود شدید";
                                }
                                joinSuccess.OnResponseReady(true, null,errMsg );
                            }
                        }
                    });
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);
        }


    }

}
