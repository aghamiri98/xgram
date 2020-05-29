package sections.libs;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.ui.ActionBar.ActionBarLayout;

import telegram.messenger.xtelex.ApplicationLoader;

public class LangHelper {

    public static void checkLang(int currentAccount, ActionBarLayout parentLayout, boolean saveLangIsChange) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean ischangeLangToFa = preferences.getBoolean("ischangeLangToFa", false);
        if (!ischangeLangToFa) {
            changeLanguageToFa(currentAccount, parentLayout);
            if (saveLangIsChange) {
                SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
                edit.putBoolean("ischangeLangToFa", true);
                edit.commit();


                SharedPreferences preferences2 = MessagesController.getGlobalMainSettings();
                SharedPreferences.Editor editor2 = preferences2.edit();
                editor2.putString("language", "fa");
                editor2.commit();


            }

        }
    }


    public static void changeLanguageToFa(int currentAccount, ActionBarLayout parentLayout) {
        LocaleController.LocaleInfo localeInfo = new LocaleController.LocaleInfo();
        localeInfo.name = "فارسی";
        localeInfo.nameEnglish = "Parsi";
        localeInfo.shortName = "fa";
        localeInfo.pathToFile = null;
        localeInfo.builtIn = true;
        if (localeInfo != null) {
            LocaleController.getInstance().applyLanguage(localeInfo, true, false, false, true, currentAccount);
            parentLayout.rebuildAllFragmentViews(false, false);
        }


    }


}
