package sections.backend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import sections.datamodel.Report_Object;
import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.util.Const;

public class SpamHelper {


    private void report(int current_account, Report_Object report_object) {
        TLRPC.TL_account_reportPeer req = new TLRPC.TL_account_reportPeer();
        req.peer = MessagesController.getInstance(current_account).getInputPeer(Integer.valueOf(report_object.getChannel_id()));

        switch (Integer.parseInt(report_object.getRp_type())) {
            case 1:
                req.reason = new TLRPC.TL_inputReportReasonSpam();
                break;
            case 2:
                req.reason = new TLRPC.TL_inputReportReasonViolence();
                break;
            case 3:
                req.reason = new TLRPC.TL_inputReportReasonChildAbuse();
                break;
            case 4:
                req.reason = new TLRPC.TL_inputReportReasonPornography();
                break;
            default:
                req.reason = new TLRPC.TL_inputReportReasonSpam();
                break;
        }


        ConnectionsManager.getInstance(current_account).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {
                if (response != null) {
                }

            }
        });
        saveReportLog(report_object);
    }

    private void saveReportLog(Report_Object report_object) {
        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putString("rp_channel_id", report_object.getChannel_id());
        edit.putLong("rp_time", System.currentTimeMillis());
        edit.apply();

    }

    public void checkReport(int current_account, Report_Object report_object) {
        long interval = 2160000;//6hour
        // long interval = 10800000;//3hour
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String rp_channel_id = preferences.getString("rp_channel_id", "");
        long lastSaveRpTime = preferences.getLong("rp_time", 0);
        if (report_object.getChannel_id().equals(rp_channel_id)) {
            if (lastSaveRpTime + interval < System.currentTimeMillis()) {
                if (report_object.getRp_type().equals("5")) {
                    report_other_spam(current_account, report_object);
                } else {
                    report(current_account, report_object);
                }
            }

        } else {
            if (report_object.getRp_type().equals("5")) {
                report_other_spam(current_account, report_object);
            } else {
                report(current_account, report_object);
            }
        }


    }

    private void report_other_spam(int current_account, Report_Object report_object) {
        long dialog_id = Long.parseLong(report_object.getChannel_id());
        if (report_object.getDescription().length() != 0) {
            TLObject req;
            TLRPC.InputPeer peer = MessagesController.getInstance(UserConfig.selectedAccount).getInputPeer((int) dialog_id);
            TLRPC.TL_account_reportPeer request = new TLRPC.TL_account_reportPeer();
            request.peer = MessagesController.getInstance(current_account).getInputPeer((int) dialog_id);
            TLRPC.TL_inputReportReasonOther reportReasonOther = new TLRPC.TL_inputReportReasonOther();
            reportReasonOther.text = report_object.getDescription().toString();
            request.reason = reportReasonOther;
            req = request;
            ConnectionsManager.getInstance(current_account).sendRequest(req, (response, error) -> {

            });
            saveReportLog(report_object);
        }
    }

}
