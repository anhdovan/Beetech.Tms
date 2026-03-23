package beetech.tms.android.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import beetech.tms.android.utils.SettingsManager;
import beetech.tms.android.TmsApp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = null;

    public static TmsApi getApi() {
        String baseUrl = SettingsManager.getInstance().getServerUrl();

        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        SharedPreferences prefs = TmsApp.getInstance().getSharedPreferences("beetech_tms_prefs", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", "");
                        
                        Request.Builder builder = chain.request().newBuilder();
                        if (!token.isEmpty()) {
                            builder.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit.create(TmsApi.class);
    }
}
