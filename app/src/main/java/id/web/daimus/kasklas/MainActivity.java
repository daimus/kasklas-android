package id.web.daimus.kasklas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;

import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DashboardModel;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.util.DialogUtils;
import id.web.daimus.kasklas.util.SessionUtils;
import id.web.daimus.kasklas.util.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "debuglog";
    private ActionBar actionBar;
    private Toolbar toolbar;
    private DialogUtils dialogUtils;

    // Components vars
    TextView balance_TV, income_TV, expense_TV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.content_main);
        stub.inflate();

        initToolbar();
        initComponent();
        initNavigationMenu();
        loadDashboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            SessionUtils sessionUtils = new SessionUtils(this);
            sessionUtils.signout();
            return true;
        } else if (id == R.id.action_setting){
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initComponent(){
        dialogUtils = new DialogUtils(this);
        balance_TV = (TextView) findViewById(R.id.balance_TV);
        income_TV = (TextView) findViewById(R.id.income_TV);
        expense_TV = (TextView) findViewById(R.id.expense_TV);
        ImageButton refresh_IB = (ImageButton) findViewById(R.id.refresh_IB);
        refresh_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDashboard();
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("KasKlas");
        Tools.setSystemBarColor(this);
    }

    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
            if (item.getItemId() == R.id.nav_home){

            } else if (item.getItemId() == R.id.nav_payment){
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_product){
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_bill){
                Intent intent = new Intent(MainActivity.this, BillActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_expense){
                Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_student){
                Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_setting){
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.nav_about){
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
            drawer.closeDrawers();
            return true;
            }
        });
    }

    public void loadDashboard(){
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        try {
            Call<DashboardModel> dashboardModelCall = apiInterface.dashboard();
            dashboardModelCall.enqueue(new Callback<DashboardModel>() {
                @Override
                public void onResponse(Call<DashboardModel> call, Response<DashboardModel> response) {
                    if (response.code() != 200){
                        dialogUtils.showNotificationDialog("Duh! Error: " + response.code() + " - " + response.message());
                        return;
                    }
                    Log.d(TAG, "onResponse: " + response.body().getMessage());
                    if (response.body().getSuccess()){
                        DecimalFormat indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                        DecimalFormatSymbols indonesianFormat = new DecimalFormatSymbols();

                        indonesianFormat.setCurrencySymbol("Rp");
                        indonesianFormat.setMonetaryDecimalSeparator(',');
                        indonesianFormat.setGroupingSeparator('.');
                        indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);

                        Double income = (response.body().getIncome() == null) ? 0 : response.body().getIncome();
                        Double expense = (response.body().getExpense() == null) ? 0 : response.body().getExpense();

                        balance_TV.setText(String.valueOf(indonesianCurrency.format(income - expense)));
                        income_TV.setText(String.valueOf(indonesianCurrency.format(income)));
                        expense_TV.setText(String.valueOf(indonesianCurrency.format(expense)));
                    }
                }

                @Override
                public void onFailure(Call<DashboardModel> call, Throwable t) {
                    dialogUtils.showNoConnectionDialog();
                    dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                        @Override
                        public void onClick() {
                            loadDashboard();
                        }
                    });
                }
            });
        } catch (Exception e){
            Log.e(TAG, "loadDashboard: ", e);
            dialogUtils.showNotificationDialog(getString(R.string.APP_CONNECTION_EXCEPTION));
        }
    }
}
