package sections.backend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.util.Const;

import org.telegram.ui.ActionBar.Theme;

import sections.datamodel.Setting;

public class SettingsHandler {

    public static boolean is_active;

    static {
        ad_is_actived();
    }


    public static void saveSettings(Setting setting) {
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putString(Const.ACTIVE_AD, setting.getActive_ad()).apply();
        editor.putString(Const.TELEX_CHANNEL_URL, setting.getChannel_address()).apply();

        //Toast.makeText(ApplicationLoader.applicationContext, "active_ad :" + setting.getActive_ad(), Toast.LENGTH_SHORT).show();
    }


    private static boolean ad_is_actived() {
       // boolean is_active;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences(Theme.CONFIG_PREF_NAME, Activity.MODE_PRIVATE);
        String active = plusPreferences.getString(Const.ACTIVE_AD, "0");

        if (active.equals("1")) {
            is_active = true;
        } else {
            is_active = false;
        }
        //Toast.makeText(ApplicationLoader.applicationContext, "is_active :" + is_active, Toast.LENGTH_SHORT).show();

        return is_active;
    }


}



