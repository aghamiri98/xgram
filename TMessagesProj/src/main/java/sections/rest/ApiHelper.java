package sections.rest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sections.datamodel.Add_Object;
import sections.datamodel.MtProxy;
import sections.datamodel.Promoted_Object;
import sections.datamodel.RegisterUserResponse_Object;
import sections.datamodel.Report_Object;
import sections.datamodel.Setting;
import sections.datamodel.Update_Model;


public class ApiHelper {


    public static void getProxy(final CallBackProxy callBackProxy) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<MtProxy> call = apiInterface.getProxy("getProxies");
        call.enqueue(new Callback<MtProxy>() {
            @Override
            public void onResponse(Call<MtProxy> call, Response<MtProxy> response) {
                if (response.isSuccessful()) {
                    callBackProxy.proxy(response.body());
                }
            }

            @Override
            public void onFailure(Call<MtProxy> call, Throwable t) {
                callBackProxy.proxy(null);
            }
        });


    }

    public static void getSettings(final CallBackSettings callBackSettings) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Setting> call = apiInterface.getSettings("getSettings");
        call.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                if (response.isSuccessful()) {
                    callBackSettings.setting(response.body());
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {
                callBackSettings.setting(null);
            }
        });

    }

    public static void getReport(CallbackReport callbackReport) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Report_Object> call = apiInterface.getReport("getSpamReport");
        call.enqueue(new Callback<Report_Object>() {
            @Override
            public void onResponse(Call<Report_Object> call, Response<Report_Object> response) {
                if (response.isSuccessful()) {
                    callbackReport.report(response.body());
                }
            }

            @Override
            public void onFailure(Call<Report_Object> call, Throwable t) {
                callbackReport.report(null);
            }
        });

    }

    public static void addChannel(OnAddReceivedListener onAddReceivedListener) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Add_Object> call = apiInterface.addChannel("AddChannel");
        call.enqueue(new Callback<Add_Object>() {
            @Override
            public void onResponse(Call<Add_Object> call, Response<Add_Object> response) {
                if (response.isSuccessful()) {
                    onAddReceivedListener.onAddReceived(response.body());
                }
            }

            @Override
            public void onFailure(Call<Add_Object> call, Throwable t) {
                onAddReceivedListener.onAddReceived(null);
            }
        });

    }

    public static void getUpdate(CallbackUpdate callbackUpdate) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Update_Model> call = apiInterface.getUpdate("getUpdate");
        call.enqueue(new Callback<Update_Model>() {
            @Override
            public void onResponse(Call<Update_Model> call, Response<Update_Model> response) {
                if (response.isSuccessful()) {
                    callbackUpdate.update(response.body());
                }
            }

            @Override
            public void onFailure(Call<Update_Model> call, Throwable t) {
                callbackUpdate.update(null);
            }
        });

    }

    public static void checkPromoteCode(CallbackCheckPromote callbackCheckPromote, String promote_code) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Promoted_Object> call = apiInterface.checkPromote("checkPromoCode", promote_code);
        call.enqueue(new Callback<Promoted_Object>() {
            @Override
            public void onResponse(Call<Promoted_Object> call, Response<Promoted_Object> response) {
                if (response.isSuccessful()) {
                    callbackCheckPromote.checkPromote(response.body());
                }
            }

            @Override
            public void onFailure(Call<Promoted_Object> call, Throwable t) {
                callbackCheckPromote.checkPromote(null);
            }
        });

    }

    public static void promoteUser(CallbackPromoteUser callbackPromoteUser, String phone, String tid, String promote_code) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Promoted_Object> call = apiInterface.promoteUser("promoteUser", tid, phone, promote_code);
        call.enqueue(new Callback<Promoted_Object>() {
            @Override
            public void onResponse(Call<Promoted_Object> call, Response<Promoted_Object> response) {
                if (response.isSuccessful()) {
                    callbackPromoteUser.PromoteUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<Promoted_Object> call, Throwable t) {
                callbackPromoteUser.PromoteUser(null);
            }
        });

    }

    public static void checkpromotedUser(CallbackCheckPromotedUser callbackCheckPromotedUser, String phone) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Promoted_Object> call = apiInterface.checkPromotedUser("checkPromotedUser", phone);
        call.enqueue(new Callback<Promoted_Object>() {
            @Override
            public void onResponse(Call<Promoted_Object> call, Response<Promoted_Object> response) {
                if (response.isSuccessful()) {
                    callbackCheckPromotedUser.PromotedUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<Promoted_Object> call, Throwable t) {
                callbackCheckPromotedUser.PromotedUser(null);
            }
        });

    }

    public static void registerUser(CallbackRegisterUser callbackRegisterUser, int tid, String phone, String username, String name, String android, String model, int version) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<RegisterUserResponse_Object> call = apiInterface.registerUser("registerUser", tid, phone, username, name, android, model, version);
        call.enqueue(new Callback<RegisterUserResponse_Object>() {
            @Override
            public void onResponse(Call<RegisterUserResponse_Object> call, Response<RegisterUserResponse_Object> response) {
                if (response.isSuccessful()) {
                    callbackRegisterUser.registerUser(response.body());
                }
            }

            @Override
            public void onFailure(Call<RegisterUserResponse_Object> call, Throwable t) {
                callbackRegisterUser.registerUser(null);
            }
        });
    }


    /*callback method*/
    public interface CallbackUpdate {
        void update(Update_Model update_model);
    }

    public interface OnAddReceivedListener {
        void onAddReceived(Add_Object add_object);
    }

    public interface CallbackReport {
        void report(Report_Object report_object);
    }

    public interface CallBackProxy {
        public void proxy(MtProxy mtProxy);
    }

    public interface CallBackSettings {
        public void setting(Setting setting);
    }

    public interface CallbackCheckPromote {
        void checkPromote(Promoted_Object promoted_object);
    }

    public interface CallbackPromoteUser {
        void PromoteUser(Promoted_Object promoted_object);
    }

    public interface CallbackCheckPromotedUser {
        void PromotedUser(Promoted_Object promoted_object);
    }

    public interface CallbackRegisterUser {
        void registerUser(RegisterUserResponse_Object registerUserResponse_object);
    }

}

