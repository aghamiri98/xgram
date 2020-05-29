package sections.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import telegram.messenger.xtelex.util.Const;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {


  //TODO Change the url

  private static Retrofit retrofit = null;


  public static Retrofit getClient() {
    if (retrofit == null) {
      Gson gson = new GsonBuilder()
        .setLenient()
        .create();
      retrofit = new Retrofit.Builder()
        .baseUrl(Const.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
    }
    return retrofit;
  }

}
