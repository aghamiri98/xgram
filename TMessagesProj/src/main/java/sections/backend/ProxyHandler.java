package sections.backend;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;

import sections.datamodel.MtProxy;
import sections.rest.ApiHelper;
import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.util.Const;


public class ProxyHandler {
    private static final String TAG = "ProxyHandler";
    private int currentAccount = UserConfig.selectedAccount;
    private static SharedConfig.ProxyInfo currentInfo = SharedConfig.currentProxy;
    private static int currentConnectionState;
    private static MtProxy lastMtProxy = new MtProxy();

    static {
        new ProxyHandler();
    }

    private ProxyHandler() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdateConnectionState);

    }

    public static void getLastProxy() {
        long currentTime = System.currentTimeMillis();
        long last_smart_proxy_change_time = MessagesController.getGlobalMainSettings().getLong(Const.SMART_PROXY_CHANGE_TIME, 0);

        long interval = 10800000;//3hour
        //long interval=30000;//30 sec

        if (last_smart_proxy_change_time + interval < currentTime && MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED, false)) {

            ApiHelper.getProxy(new ApiHelper.CallBackProxy() {
                @Override
                public void proxy(MtProxy mtProxy) {
                    if (mtProxy != null) {
                        changeProxy(mtProxy, true);
                    }
                }
            });


        } else if (MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED, true)) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            String proxyAddress = preferences.getString("smart_proxy_ip", "");
            String proxyUsername = preferences.getString("smart_proxy_user", "");
            String proxyPassword = preferences.getString("smart_proxy_pass", "");
            String proxySecret = preferences.getString("smart_proxy_secret", "");
            int proxyPort = preferences.getInt("smart_proxy_port", 1080);

            lastMtProxy.setHostName(proxyAddress);
            lastMtProxy.setPort(String.valueOf(proxyPort));
            lastMtProxy.setSecret(proxySecret);


            ConnectionsManager.setProxySettings(true, proxyAddress, proxyPort, proxyUsername, proxyPassword, proxySecret);

        }
    }

    public static void changeProxy(MtProxy proxy, boolean forceChange) {

        if (forceChange || MessagesController.getGlobalMainSettings().getBoolean(Const.PROXY_ANTY_FILTER_ENABLED, true)) {
            SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
            edit.putString("smart_proxy_ip", proxy.getHostName());
            edit.putString("smart_proxy_pass", "");
            edit.putString("smart_proxy_user", "");
            edit.putInt("smart_proxy_port", Integer.valueOf(proxy.getPort()));
            edit.putString("smart_proxy_secret", proxy.getSecret());

            edit.putLong("smart_proxy_change_time", System.currentTimeMillis());


            edit.putBoolean(Const.PROXY_ANTY_FILTER_ENABLED, true);
            edit.apply();

            ConnectionsManager.setProxySettings(true, proxy.getHostName(), Integer.valueOf(proxy.getPort()), "", "", proxy.getSecret());
        }

    }


}
