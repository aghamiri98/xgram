package sections.libs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;

import sections.taptargetview.TapTarget;
import sections.taptargetview.TapTargetView;
import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.R;

public class TapTargetViewHelper {

    public static final int outerCircleColor = 0xff008c7f;
    public static final int targetCircleColor = 0xffffffff;
    public static final int descriptionTextColor = 0xffffffff;
    public static final int textColor = 0xffffffff;
    public static final int dimColor = 0xff4CAF50;



    private static void chackShowTapTarget(Activity context, View view) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean isShowTapTargetProxyButton = preferences.getBoolean("isShowTapTargetProxyButton", false);
        if (!isShowTapTargetProxyButton) {
            showTapTargetForProxyButton(context, view);
        }


    }


    public static void showTapTargetForProxyButton(Activity context, View view) {
        TapTargetView.showFor(context,
                TapTarget.forView(view,
                        LocaleController.getString("RefreshProxy", R.string.RefreshProxy),
                        LocaleController.getString("RefreshProxyMessage", R.string.RefreshProxyMessage))
                        .outerCircleColor(R.color.primary_dark)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(22)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(14)
                        .descriptionTextColor(R.color.white)
                        .textColor(R.color.white)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.alerter_default_success_background)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        //.icon(Drawable)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        //doSomething();
                    }
                });

        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("isShowTapTargetProxyButton", true);
        edit.commit();



    }

    public static void showTapTargetForChangeLangButton(Activity context, View view) {
        TapTargetView.showFor(context,
                TapTarget.forView(view,
                        LocaleController.getString("changeLanguage", R.string.changeLanguage),
                        "")
                        .outerCircleColor(R.color.primary_dark)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(22)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(14)
                        .descriptionTextColor(R.color.white)
                        .textColor(R.color.white)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.alerter_default_success_background)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        //.icon(Drawable)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        //doSomething();
                    }
                });

        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putBoolean("isShowTapTargetProxyButton", true);
        edit.commit();



    }


}
