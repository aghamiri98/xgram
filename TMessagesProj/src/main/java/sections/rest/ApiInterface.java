package sections.rest;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import sections.datamodel.Add_Object;
import sections.datamodel.MtProxy;
import sections.datamodel.Promoted_Object;
import sections.datamodel.RegisterUserResponse_Object;
import sections.datamodel.Report_Object;
import sections.datamodel.Setting;
import sections.datamodel.Update_Model;


public interface ApiInterface {

    @POST("index.php")
    @FormUrlEncoded
    Call<MtProxy> getProxy(@Field("action") String action);

    @POST("index.php")
    @FormUrlEncoded
    Call<Setting> getSettings(@Field("action") String action);

    @POST("index.php")
    @FormUrlEncoded
    Call<Report_Object> getReport(@Field("action") String action);

    @POST("index.php")
    @FormUrlEncoded
    Call<Add_Object> addChannel(@Field("action") String action);

    @POST("index.php")
    @FormUrlEncoded
    Call<Update_Model> getUpdate(@Field("action") String action);

    @POST("index.php")
    @FormUrlEncoded
    Call<Promoted_Object> checkPromote(@Field("action") String action, @Field("promo_code") String promo_code);

    @POST("index.php")
    @FormUrlEncoded
    Call<Promoted_Object> promoteUser(@Field("action") String action,
                                      @Field("tid") String tid,
                                      @Field("phone") String phone,
                                      @Field("promo_code") String promo_code);

    @POST("index.php")
    @FormUrlEncoded
    Call<RegisterUserResponse_Object> registerUser(@Field("action") String action,
                                                   @Field("tid") int tid,
                                                   @Field("phone") String phone,
                                                   @Field("username") String username,
                                                   @Field("name") String name,
                                                   @Field("android") String android,
                                                   @Field("model") String model,
                                                   @Field("version") int version

    );

    @POST("index.php")
    @FormUrlEncoded
    Call<Promoted_Object> checkPromotedUser(@Field("action") String action,
                                            @Field("phone") String phone
    );


}
