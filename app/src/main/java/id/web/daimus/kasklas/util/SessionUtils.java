package id.web.daimus.kasklas.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import id.web.daimus.kasklas.LoginActivity;
import id.web.daimus.kasklas.model.DefaultResponseModel;

public class SessionUtils {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionUtils(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("kasklasconf", Context.MODE_PRIVATE);
    }

    public void signout(){
        editor = sharedPreferences.edit();
        editor.putString("hash", null);
        editor.commit();
        ((Activity) context).finish();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void preprocessResponse(DefaultResponseModel response){

    }
}
