package sections.backend;

import android.content.SharedPreferences;
import android.os.Build;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import sections.datamodel.RegisterUserResponse_Object;
import sections.rest.ApiHelper;
import telegram.messenger.xtelex.util.Utils;

public class UserHelper implements ApiHelper.CallbackRegisterUser {
    private String userPhone;

    public void register(TLRPC.User user) {
        userPhone = user.phone;
        if (!MessagesController.getGlobalMainSettings().getBoolean("user_is_registerd" + userPhone, false)) {
            int tid = user.id;
            String phone = user.phone;
            String username = user.username;
            String name = user.first_name + " " + (user.last_name != null ? user.last_name : "");
            String android =  Build.VERSION.RELEASE;
            String model = Build.MODEL.replace(" ","-");
            int version= BuildVars.BUILD_VERSION;
            if(model.length()>39){
                model=model.substring(0,39);
            }


            ApiHelper.registerUser(this, tid, phone, username, name,android,model,version);
        }
    }

    @Override
    public void registerUser(RegisterUserResponse_Object registerUserResponse_object) {
        if (registerUserResponse_object != null) {
            if (registerUserResponse_object.getStatus() == 201) {
                SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
                edit.putBoolean("user_is_registerd"+ userPhone, true);
                edit.apply();
            } else if (registerUserResponse_object.getStatus() == 202) {
                SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
                edit.putBoolean("user_is_registerd"+ userPhone, true);
                edit.apply();
            }
        }
    }
}
