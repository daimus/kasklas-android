package id.web.daimus.kasklas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.LoginModel;
import id.web.daimus.kasklas.util.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText username_ET, password_ET;
    Button signin_BTN;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DialogUtils dialogUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponent();
    }

    public void initComponent(){
        dialogUtils = new DialogUtils(this);
        sharedPreferences = getSharedPreferences(getString(R.string.sharedprefname), MODE_PRIVATE);
        username_ET = (TextInputEditText) findViewById(R.id.username_ET);
        password_ET = (TextInputEditText) findViewById(R.id.password_ET);
        signin_BTN = (Button) findViewById(R.id.signin_BTN);
        signin_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showProgressDialog("Sedang login...");
                login();
            }
        });
    }

    public void login(){
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        try {
            Call<LoginModel> loginModelCall = apiInterface.signin(username_ET.getText().toString(), password_ET.getText().toString());
            loginModelCall.enqueue(new Callback<LoginModel>() {
                @Override
                public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                    Log.d("debuglog", "onResponse: " + response.body().getMessage());
                    dialogUtils.hideprogressDialog();
                    if (response.body().getSuccess()){
                        editor = sharedPreferences.edit();
                        editor.putString("hash", response.body().getHash());
                        editor.putString("user", response.body().getUser());
                        editor.commit();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    } else {
                        dialogUtils.showNotificationDialog(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<LoginModel> call, Throwable t) {
                    dialogUtils.hideprogressDialog();
                    dialogUtils.showNoConnectionDialog();
                    dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                        @Override
                        public void onClick() {
                            login();
                        }
                    });
                }
            });
        } catch (Exception e){
            dialogUtils.hideprogressDialog();
            dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
        }
    }
}
