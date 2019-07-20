package id.web.daimus.kasklas;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.util.DialogUtils;
import id.web.daimus.kasklas.util.Tools;
import id.web.daimus.kasklas.util.ViewAnimation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private final static int LOADING_DURATION = 2000;
    private String hash = null;
    private String user = null;
    private Boolean show_stepper = true;
    private Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DialogUtils dialogUtils;
    LinearLayout lyt_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Tools.setSystemBarTransparent(this);

        dialogUtils = new DialogUtils(this);
        sharedPreferences = getSharedPreferences(getString(R.string.sharedprefname), MODE_PRIVATE);
        hash = sharedPreferences.getString("hash", null);
        user = sharedPreferences.getString("user", null);
        show_stepper = sharedPreferences.getBoolean("show_stepper", true);


        lyt_progress = (LinearLayout) findViewById(R.id.lyt_progress);
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);

        if (hash == null || user == null){
            if (show_stepper){
                intent = new Intent(SplashActivity.this, StepperActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
        } else {
            try {
                validateSession();
            } catch (Exception e){
                dialogUtils.showNotificationDialog(e.getMessage());
            }
        }
    }

    private void validateSession(){

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        try {
            Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.validateSession(hash, 1);
            defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
                @Override
                public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                    if (response.code() != 200){
                        dialogUtils.showNotificationDialog("Duh! " + response.code() + " : " + response.message());
                        ViewAnimation.fadeOut(lyt_progress);
                        return;
                    }
                    if (response.body().getSuccess()){
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    } else {
                        dialogUtils.showNotificationDialog("Duh! " + response.body().getMessage());
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    }
                    ViewAnimation.fadeOut(lyt_progress);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<DefaultResponseModel> call, Throwable t) {
                    dialogUtils.showNoConnectionDialog();
                    dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                        @Override
                        public void onClick() {
                            validateSession();
                        }
                    });
                    ViewAnimation.fadeOut(lyt_progress);
                    return;
                }
            });
        } catch (Exception e){
            dialogUtils.showNotificationDialog("Terjadi kesalahan saat menghubungi server");
            ViewAnimation.fadeOut(lyt_progress);
        }
    }
}
