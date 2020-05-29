package sections.promote;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Utils;

public class PromoteHelper {

    public static void promoteUser() {
        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("isPromotedUser", true);
        edit.commit();


    }

    public static void UnPromoteUser() {
        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("isPromotedUser", false);
        edit.commit();


    }

    public static void activeSpecialFeatures() {

    }

    /*public static boolean isPromotedUser() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        return preferences.getBoolean("isPromotedUser", false);
    }
*/

}
