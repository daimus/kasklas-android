package id.web.daimus.kasklas;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.util.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFormActivity extends AppCompatActivity {

    private final String TAG = "debuglog";
    private ApiInterface apiInterface;
    private DialogUtils dialogUtils;

    // Layout component vars
    TextInputEditText name_ET, amount_ET;
    MaterialRippleLayout save_MRL;

    // Form field value
    Integer id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_form);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tambah Item Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        dialogUtils = new DialogUtils(this);

        // Layout components
        name_ET = (TextInputEditText) findViewById(R.id.name_ET);
        amount_ET = (TextInputEditText) findViewById(R.id.amount_ET);
        save_MRL = (MaterialRippleLayout) findViewById(R.id.save_MRL);
        save_MRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id <= 0){
                    try {
                        dialogUtils.showProgressDialog("Menyimpan...");
                        addProduct();
                    } catch (Exception e){
                        dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                    }
                } else {
                    try {
                        dialogUtils.showProgressDialog("Menyimpan...");
                        editProduct();
                    } catch (Exception e){
                        dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                    }
                }
            }
        });

        // Get intent params
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        if (id > 0){
            name_ET.setText(intent.getStringExtra("name"));
            amount_ET.setText(intent.getStringExtra("amount"));
        }
    }

    private void addProduct(){
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.addProduct(name_ET.getText().toString(), "once", Double.valueOf(amount_ET.getText().toString()));
        defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
            @Override
            public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh!" + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    Intent intent = new Intent();
                    intent.putExtra("RELOAD", true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    dialogUtils.showNotificationDialog(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<DefaultResponseModel> call, Throwable t) {
                dialogUtils.showNoConnectionDialog();
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        try {
                            dialogUtils.showProgressDialog("Menyimpan...");
                            addProduct();
                        } catch (Exception e){
                            dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                        }
                    }
                });
            }
        });
        dialogUtils.hideprogressDialog();
    }

    private void editProduct(){
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.editProduct(id, name_ET.getText().toString(), "once", Double.valueOf(amount_ET.getText().toString()));
        defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
            @Override
            public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh!" + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    Intent intent = new Intent();
                    intent.putExtra("RELOAD", true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    dialogUtils.showNotificationDialog(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<DefaultResponseModel> call, Throwable t) {
                dialogUtils.showNoConnectionDialog();
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        try {
                            dialogUtils.showProgressDialog("Menyimpan...");
                            addProduct();
                        } catch (Exception e){
                            dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                        }
                    }
                });
            }
        });
        dialogUtils.hideprogressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
