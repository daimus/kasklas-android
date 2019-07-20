package id.web.daimus.kasklas;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.web.daimus.kasklas.adapter.CartAdapter;
import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.CartListModel;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.OptionModel;
import id.web.daimus.kasklas.model.PaymentItemModel;
import id.web.daimus.kasklas.util.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.FieldMap;

public class PaymentFormActivity extends AppCompatActivity {

    private String TAG = "debuglog";
    private RecyclerView cart_RV;
    private CartAdapter cartAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Button studentPicker_BTN;
    private TextView grandTotalAmount_TV;
    private MaterialRippleLayout save_MRL;
    private ApiInterface apiInterface;
    private DialogUtils dialogUtils;

    private String studentName = null;
    private Integer studentId = 0;
    private Integer paymentId = 0;
    private String invoiceMessage = null;

    private DecimalFormat indonesianCurrency;
    private DecimalFormatSymbols indonesianFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_form);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Buat Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        indonesianFormat = new DecimalFormatSymbols();

        indonesianFormat.setCurrencySymbol("Rp");
        indonesianFormat.setMonetaryDecimalSeparator(',');
        indonesianFormat.setGroupingSeparator('.');
        indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);

        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        dialogUtils = new DialogUtils(this);
        cart_RV = (RecyclerView) findViewById(R.id.cart_RV);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cart_RV.setItemAnimator(new DefaultItemAnimator());
        cart_RV.setLayoutManager(linearLayoutManager);
        cartAdapter = new CartAdapter();
        cart_RV.setAdapter(cartAdapter);
        cartAdapter.setOnAddItemListener(new CartAdapter.OnAddItemListener() {
            @Override
            public void onClick(CartListModel obj, int position) {
                obj.setQty(obj.getQty() + 1);
                cartAdapter.updateItem(obj, position);
                countGrandTotal();

            }
        });
        cartAdapter.setOnRemoveItemListener(new CartAdapter.OnRemoveItemListener() {
            @Override
            public void onClick(CartListModel obj, int position) {
                if (obj.getQty() > 0){
                    obj.setQty(obj.getQty() - 1);
                    cartAdapter.updateItem(obj, position);
                    countGrandTotal();
                } else {
                    cartAdapter.remove(obj);
                }
            }
        });
        grandTotalAmount_TV = (TextView) findViewById(R.id.grandTotalAmount_TV);
        studentPicker_BTN = (Button) findViewById(R.id.studentPicker_BTN);
        studentPicker_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentFormActivity.this, StudentPickerActivity.class);
                startActivityForResult(intent, 102);
            }
        });
        save_MRL = (MaterialRippleLayout) findViewById(R.id.save_MRL);
        save_MRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentId == 0){
                    Toast.makeText(getApplicationContext(), "Pilih siswa", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PaymentFormActivity.this, StudentPickerActivity.class);
                    startActivityForResult(intent, 102);
                    return;
                }
                if (paymentId <= 0){
                    try {
                        addPayment();
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: ", e);
                        dialogUtils.showNotificationDialog("Pembayaran tidak berhasil :(");
                    }
                } else {
                    try {
                        editPayment();
                    } catch (Exception e){
                        Log.e(TAG, "onClick: ", e);
                    }
                }

            }
        });

        // Get intent params
        Intent intent = getIntent();
        paymentId = intent.getIntExtra("id", 0);
        if (paymentId > 0){
            studentId = intent.getIntExtra("studentId", 0);
            studentPicker_BTN.setText(intent.getStringExtra("name"));
            loadPaymentItem();
        } else {
            getOption();
        }
    }

    private void countGrandTotal(){
        Double grandTotal = 0.0;
        for (CartListModel cartListModel : cartAdapter.getItems()){
            grandTotal += cartListModel.getQty() * cartListModel.getAmount();
        }
        grandTotalAmount_TV.setText(String.valueOf(indonesianCurrency.format(grandTotal)));
    }

    private void getOption(){
        Call<OptionModel> optionModelCall = apiInterface.getOption();
        optionModelCall.enqueue(new Callback<OptionModel>() {
            @Override
            public void onResponse(Call<OptionModel> call, Response<OptionModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh! " + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    for (int i = 0; i < response.body().getData().size(); i++){
                        if (response.body().getData().get(i).getCode() == 201){
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().getData().get(i).getValue());
                                Log.d(TAG, "onResponse: " + jsonObject.get("id"));
                                cartAdapter.add(new CartListModel(Integer.parseInt(String.valueOf(jsonObject.get("id"))),String.valueOf(jsonObject.get("name")), Double.parseDouble(String.valueOf(jsonObject.get("amount"))), 1));
                                countGrandTotal();
                            } catch (JSONException je) {
                                Log.e(TAG, "onResponse: ", je);
                            }
                        } else if (response.body().getData().get(i).getCode() == 203){
                            invoiceMessage = response.body().getData().get(i).getValue();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<OptionModel> call, Throwable t) {
                dialogUtils.showNotificationDialog(t.getMessage());
            }
        });
    }

    private void loadPaymentItem(){
        Call<PaymentItemModel> paymentItemModelCall = apiInterface.loadPaymentItem(paymentId);
        paymentItemModelCall.enqueue(new Callback<PaymentItemModel>() {
            @Override
            public void onResponse(Call<PaymentItemModel> call, Response<PaymentItemModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh!" + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    cartAdapter.addAll(response.body().getData());
                    countGrandTotal();
                } else {
                    dialogUtils.showNotificationDialog(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<PaymentItemModel> call, Throwable t) {
                dialogUtils.showNotificationDialog(t.getMessage());
            }
        });
    }

    private void addPayment(){
        ArrayList<Integer> productId = new ArrayList<>();
        ArrayList<Integer> qty = new ArrayList<>();
        for (CartListModel cartListModel : cartAdapter.getItems()){
            productId.add(cartListModel.getId());
            qty.add(cartListModel.getQty());
        }
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.addPayment(studentId, productId, qty);
        defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
            @Override
            public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh! " + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    Toast.makeText(getApplicationContext(), "Pembayaran berhasil :)", Toast.LENGTH_SHORT).show();
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
                dialogUtils.showNotificationDialog(t.getMessage());
            }
        });
    }

    private void editPayment(){
        ArrayList<Integer> productId = new ArrayList<>();
        ArrayList<Integer> qty = new ArrayList<>();
        for (CartListModel cartListModel : cartAdapter.getItems()){
            productId.add(cartListModel.getId());
            qty.add(cartListModel.getQty());
        }
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.editPayment(studentId, paymentId, productId, qty);
        defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
            @Override
            public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                if (response.code() != 200){
                    dialogUtils.showNotificationDialog("Duh! " + response.code() + " : " + response.message());
                    return;
                }
                if (response.body().getSuccess()){
                    Toast.makeText(getApplicationContext(), "Pembayaran berhasil :)", Toast.LENGTH_SHORT).show();
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
                dialogUtils.showNotificationDialog(t.getMessage());
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_add){
            Intent intent = new Intent(PaymentFormActivity.this, ProductPickerActivity.class);
            startActivityForResult(intent, 103);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 102 && resultCode == RESULT_OK){
            studentPicker_BTN.setText(data.getStringExtra("STUDENT_NAME"));
            studentId = data.getIntExtra("STUDENT_ID", 0);
        }
        if(requestCode == 103 && resultCode == RESULT_OK){
            Integer id = data.getIntExtra("ID", 0);
            List<CartListModel> cartListModelsList = cartAdapter.getItems();
            for (CartListModel cartListModel : cartListModelsList){
                if (id == cartListModel.getId()){
                    cartListModel.setQty(cartListModel.getQty() + 1);
                    cartAdapter.updateItem(cartListModel, cartListModelsList.indexOf(cartListModel));
                    countGrandTotal();
                    return;
                }
            }
            cartAdapter.add(new CartListModel(id, data.getStringExtra("NAME"), data.getDoubleExtra("AMOUNT", 0), 1));
            countGrandTotal();
        }
    }
}
