package id.web.daimus.kasklas.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class ApiClient {

//    public static final String BASE_URL = "http://192.168.33.4/lecture/kasklas-api/";
    public static final String BASE_URL = "https://api.kasklas.daimus.web.id/";
    private static Retrofit retrofit = null;

    private static OkHttpClient client =new OkHttpClient();

    public static Retrofit getClient(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("kasklasconf", MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        String hash = sharedPreferences.getString("hash", null);

        Log.d("debuglog", "getClient: user = " + user);
        Log.d("debuglog", "getClient: hash = " + hash);

        try {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new BasicAuthInterceptor(user, hash))
                    .sslSocketFactory(new TLSSocketFactory())
                    .build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
