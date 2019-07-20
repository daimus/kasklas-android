package id.web.daimus.kasklas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.util.DialogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentFormActivity extends AppCompatActivity {

    private final String TAG = "debuglog";
    private ApiInterface apiInterface;
    private DialogUtils dialogUtils;

    // Layout component vars
    TextInputEditText studentId_ET, name_ET, address_ET, phone_ET;
    RadioGroup gender_RG;
    ImageButton contactPicker_IB;
    MaterialRippleLayout save_MRL;

    // Form field value
    Integer id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tambah Data Siswa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        dialogUtils = new DialogUtils(this);

        // Layout components
        studentId_ET = (TextInputEditText) findViewById(R.id.studentId_ET);
        name_ET = (TextInputEditText) findViewById(R.id.name_ET);
        address_ET = (TextInputEditText) findViewById(R.id.address_ET);
        phone_ET = (TextInputEditText) findViewById(R.id.phone_ET);
        gender_RG = (RadioGroup) findViewById(R.id.gender_RG);
        contactPicker_IB = (ImageButton) findViewById(R.id.contactPicker_IB);
        contactPicker_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 201);
            }
        });
        save_MRL = (MaterialRippleLayout) findViewById(R.id.save_MRL);
        save_MRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id <= 0){
                    try {
                        dialogUtils.showProgressDialog("Menyimpan...");
                        saveStudent();
                    } catch (Exception e){
                        dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                    }
                } else {
                    try {
                        dialogUtils.showProgressDialog("Menyimpan...");
                        editStudent();
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
            getSupportActionBar().setTitle("Edit Data Siswa");
            studentId_ET.setText(intent.getStringExtra("studentId"));
            name_ET.setText(intent.getStringExtra("name"));
            address_ET.setText(intent.getStringExtra("address"));
            phone_ET.setText(intent.getStringExtra("phone"));
            if (intent.getStringExtra("gender").equals("MALE")){
                RadioButton radioButton = (RadioButton) findViewById(R.id.male_RB);
                radioButton.setChecked(true);
            } else {
                RadioButton radioButton = (RadioButton) findViewById(R.id.female_RB);
                radioButton.setChecked(true);
            }
        }
    }

    private void saveStudent(){
        int sr = gender_RG.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(sr);
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.addStudent(studentId_ET.getText().toString(), name_ET.getText().toString(), radioButton.getText().toString(), address_ET.getText().toString(), phone_ET.getText().toString());
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
                            saveStudent();
                        } catch (Exception e){
                            dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
                        }
                    }
                });
            }
        });
        dialogUtils.hideprogressDialog();
    }

    private void editStudent(){
        int sr = gender_RG.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(sr);
        Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.editStudent(id, studentId_ET.getText().toString(), name_ET.getText().toString(), radioButton.getText().toString(), address_ET.getText().toString(), phone_ET.getText().toString());
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
                            saveStudent();
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
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 201){
            if (resultCode == Activity.RESULT_OK){
                try {
                    Uri uri = data.getData();
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    if (Integer.valueOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) == 1){
                        Cursor cursorPhones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                        while (cursorPhones.moveToNext()){
                            String phone = cursorPhones.getString(cursorPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phone_ET.setText(phone);
                        }
                    }
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Tidak bisa membaca kontak :(", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
