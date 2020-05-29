package sections.libs.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.telegram.messenger.LocaleController;

import telegram.messenger.xtelex.R;

public class DialogHelper {

    public final static int PROXY_OFF = 200;
    public final static int PROXY_ANTI_FILTER = 201;
    public final static int PROXY_CUSTOM = 202;
    private static int selectedProxyitem;

    static {
        selectedProxyitem = 0;
    }

    public static void createChooseProxyDialog(Context context, OnProxyDialogItemClickListener onProxyDialogItemClickListener) {
        //String[] listItems = context.getResources().getStringArray(R.array.proxy_items);

        String proxyOff = LocaleController.getString("proxyOff", R.string.proxyOff);
        String antiFilterProxy = LocaleController.getString("antiFilterProxy", R.string.antiFilterProxy);
        String addCustomProxy = LocaleController.getString("addCustomProxy", R.string.addCustomProxy);

        String[] listItems = {proxyOff, antiFilterProxy, addCustomProxy};


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("select_proxy_type", R.string.select_proxy_type));
        builder.setPositiveButton(LocaleController.getString("Agree", R.string.Agree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (selectedProxyitem) {
                    case PROXY_OFF:
                        onProxyDialogItemClickListener.onProxyClick(PROXY_OFF);
                        dialog.dismiss();
                        break;
                    case PROXY_ANTI_FILTER:
                        //initProxy();
                        onProxyDialogItemClickListener.onProxyClick(PROXY_ANTI_FILTER);
                        break;
                    case PROXY_CUSTOM:
                        //presentFragment(new ProxyListActivity());
                        onProxyDialogItemClickListener.onProxyClick(PROXY_CUSTOM);
                        break;
                }

                dialog.dismiss();
            }
        });
        builder.setSingleChoiceItems(listItems, selectedProxyitem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        selectedProxyitem = PROXY_OFF;
                        break;
                    case 1:
                        selectedProxyitem = PROXY_ANTI_FILTER;
                        break;
                    case 2:
                        selectedProxyitem = PROXY_CUSTOM;
                        break;
                }


            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }


    public interface OnProxyDialogItemClickListener {
        void onProxyClick(int witchItem);


    }

}
