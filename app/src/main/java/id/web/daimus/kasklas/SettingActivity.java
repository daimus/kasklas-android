package id.web.daimus.kasklas;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.OptionModel;
import id.web.daimus.kasklas.util.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity {

    private String TAG = "debuglog";
    private LinearLayout institution_LL, class_LL, treasurer_LL, nominal_LL, period_LL, message_LL, datePayment_LL;
    private TextView institution_TV, class_TV, treasurer_TV, nominal_TV, period_TV, datePayment_TV;
    private DialogUtils dialogUtils;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private String bill_message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initToolbar();
        initComponent();

        getOptions();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        dialogUtils = new DialogUtils(this);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        institution_TV = (TextView) findViewById(R.id.institution_TV);
        class_TV = (TextView) findViewById(R.id.class_TV);
        treasurer_TV = (TextView) findViewById(R.id.treasurer_TV);
        nominal_TV = (TextView) findViewById(R.id.nominal_TV);
        period_TV = (TextView) findViewById(R.id.period_TV);
        datePayment_TV = (TextView) findViewById(R.id.datePayment_TV);

        institution_LL = (LinearLayout) findViewById(R.id.institution_LL);
        institution_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showTextInputDialog("Institusi", String.valueOf(institution_TV.getText()));
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menyimpan...");
                        saveOption(101, dialogUtils.getResultText());
                    }
                });
            }
        });
        class_LL = (LinearLayout) findViewById(R.id.class_LL);
        class_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showTextInputDialog("Kelas", String.valueOf(class_TV.getText()));
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menyimpan");
                        saveOption(102, dialogUtils.getResultText());
                    }
                });
            }
        });
        treasurer_LL = (LinearLayout) findViewById(R.id.treasurer_LL);
        treasurer_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showTextInputDialog("Bendahara", String.valueOf(treasurer_TV.getText()));
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menyimpan");
                        saveOption(103, dialogUtils.getResultText());
                    }
                });
            }
        });
        nominal_LL = (LinearLayout) findViewById(R.id.nominal_LL);
        nominal_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showTextInputDialog("Nominal Pembayaran", String.valueOf(nominal_TV.getText()));
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menyimpan");
                        saveOption(201, dialogUtils.getResultText());
                    }
                });
            }
        });
        period_LL = (LinearLayout) findViewById(R.id.period_LL);
        period_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Tidak dapat mengedit periode pembayaran", Toast.LENGTH_SHORT).show();
            }
        });
        message_LL = (LinearLayout) findViewById(R.id.message_LL);
        message_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showTextInputDialog("Pesan Tagihan", bill_message);
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menyimpan");
                        saveOption(204, dialogUtils.getResultText());
                    }
                });
            }
        });
        datePayment_LL = (LinearLayout) findViewById(R.id.datePayment_LL);
        datePayment_LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
    }

    private void showDateDialog(){
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dialogUtils.showProgressDialog("Menyimpan...");
                saveOption(202, String.valueOf(dateFormatter.format(newDate.getTime())));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void getOptions(){
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<OptionModel> optionModelCall = apiInterface.getOption();
        optionModelCall.enqueue(new Callback<OptionModel>() {
            @Override
            public void onResponse(Call<OptionModel> call, Response<OptionModel> response) {
                if (response.code() != 200){
                    return;
                }
                if (response.body().getSuccess()){
                    for (int i = 0; i < response.body().getData().size(); i++){
                        if (response.body().getData().get(i).getCode() == 101){
                            institution_TV.setText(response.body().getData().get(i).getValue());
                        } else if (response.body().getData().get(i).getCode() == 102){
                            class_TV.setText(response.body().getData().get(i).getValue());
                        } else if (response.body().getData().get(i).getCode() == 103){
                            treasurer_TV.setText(response.body().getData().get(i).getValue());
                        } else if (response.body().getData().get(i).getCode() == 201){
                            try {
                                JSONObject nominalObj = new JSONObject(String.valueOf(response.body().getData().get(i).getValue()));
                                nominal_TV.setText(nominalObj.getString("amount"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.body().getData().get(i).getCode() == 202){
                            datePayment_TV.setText(response.body().getData().get(i).getValue());
                        } else if (response.body().getData().get(i).getCode() == 204){
                            bill_message = response.body().getData().get(i).getValue();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<OptionModel> call, Throwable t) {
                dialogUtils.showNoConnectionDialog();
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        getOptions();
                    }
                });
            }
        });
    }

    private void saveOption(Integer code, String value){
        final Integer code_ = code;
        final String value_ = value;
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.saveOption(code, value);
        defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
            @Override
            public void onResponse(Call<DefaultResponseModel> call, Response<DefaultResponseModel> response) {
                dialogUtils.hideprogressDialog();
                if (response.code() != 200){
                    return;
                }
                if (response.body().getSuccess()){
                    getOptions();
                } else {
                    dialogUtils.showNotificationDialog(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<DefaultResponseModel> call, Throwable t) {
                dialogUtils.hideprogressDialog();
                dialogUtils.showNoConnectionDialog();
                dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                    @Override
                    public void onClick() {
                        saveOption(code_, value_);
                    }
                });
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
