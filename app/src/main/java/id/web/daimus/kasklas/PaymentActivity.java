package id.web.daimus.kasklas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import id.web.daimus.kasklas.adapter.PaymentAdapter;
import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.PaymentListModel;
import id.web.daimus.kasklas.model.PaymentModel;
import id.web.daimus.kasklas.util.DialogUtils;
import id.web.daimus.kasklas.util.SessionUtils;
import id.web.daimus.kasklas.util.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PaymentActivity extends AppCompatActivity {

    private static String TAG = "debuglog";
    private ActionBar actionBar;
    private Toolbar toolbar;

    // Components vars
    private ViewStub viewStub;
    private View view;
    private ViewGroup viewGroup;
    private RecyclerView content_RV;
    private PaymentAdapter paymentAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout content_SRL;
    private FloatingActionButton add_FAB;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private DialogUtils dialogUtils;
    private ProgressBar init_PB;

    // Filter vars
    private Integer start = 0;
    private Integer length = 10;
    private Integer filterStudentId = 0;
    private Integer totalRows = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewStub = (ViewStub) findViewById(R.id.layout_stub);
        viewStub.setLayoutResource(R.layout.content_recycler);
        view = viewStub.inflate();

        initToolbar();
        initComponent();
        initNavigationMenu();
        loadPayment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            SessionUtils sessionUtils = new SessionUtils(this);
            sessionUtils.signout();
            return true;
        } else if (id == R.id.action_setting){
            Intent intent = new Intent(PaymentActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initComponent(){
        dialogUtils = new DialogUtils(this);
        init_PB = (ProgressBar) findViewById(R.id.init_PB);
        add_FAB = (FloatingActionButton) findViewById(R.id.add_FAB);
        add_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PaymentFormActivity.class);
                startActivityForResult(intent, 101);
            }
        });
        content_SRL = (SwipeRefreshLayout) findViewById(R.id.content_SRL);
        content_RV = (RecyclerView) findViewById(R.id.content_RV);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        content_RV.setItemAnimator(new DefaultItemAnimator());
        content_RV.setLayoutManager(linearLayoutManager);
        paymentAdapter = new PaymentAdapter(this, 5);
        content_RV.setAdapter(paymentAdapter);

        paymentAdapter.setOnLoadMoreListener(new PaymentAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (paymentAdapter.getItemCount() < totalRows){
                    paymentAdapter.setLoading();
                    loadPayment();
                }
            }
        });
        paymentAdapter.setOnClickListener(new PaymentAdapter.OnClickListener() {
            @Override
            public void onItemClick(View view, PaymentListModel obj, int pos) {
                if (paymentAdapter.getSelectedItemCount() > 0){
                    enableActionMode(pos);
                } else {
                    Intent intent = new Intent(getApplicationContext(), PaymentFormActivity.class);
                    intent.putExtra("id", obj.getPaymentId());
                    intent.putExtra("studentId", obj.getStudentId());
                    intent.putExtra("name", obj.getName());
                    startActivityForResult(intent, 101);
                }
            }

            @Override
            public void onItemLongClick(View view, PaymentListModel obj, int pos) {
                enableActionMode(pos);
            }
        });

        content_SRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetPagination();
                paymentAdapter.setLoading();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadPayment();
                        swipeProgress(false);
                    }
                }, 1500);
            }
        });

        actionModeCallback = new ActionModeCallback();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Pembayaran");
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
                    Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_payment){

                } else if (item.getItemId() == R.id.nav_product){
                    Intent intent = new Intent(PaymentActivity.this, ProductActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_bill){
                    Intent intent = new Intent(PaymentActivity.this, BillActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_expense){
                    Intent intent = new Intent(PaymentActivity.this, ExpenseActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_student){
                    Intent intent = new Intent(PaymentActivity.this, StudentActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_setting){
                    Intent intent = new Intent(PaymentActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_about){
                    Intent intent = new Intent(PaymentActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
            drawer.closeDrawers();
            return true;
            }
        });
    }

    public void loadPayment(){
        try {
            ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
            Call<PaymentModel> paymentModelCall= apiInterface.fetchPayment(start, length, filterStudentId);
            paymentModelCall.enqueue(new Callback<PaymentModel>() {
                @Override
                public void onResponse(Call<PaymentModel> call, Response<PaymentModel> response) {
                    if (init_PB.getVisibility() == View.VISIBLE){
                        init_PB.setVisibility(View.GONE);
                    }
                    if (response.code() != 200){
                        Log.d(TAG, "onResponse: " + response.code() + " - " + response.message());
                        return;
                    }
                    if (response.body().getSuccess()){
                        totalRows = response.body().getTotalRows();
                        // Case: total rows <= 0, inflate view with content_no_item
                        if (response.body().getTotalRows() <= 0){
                            if (view.getId() != R.id.contentNoResult){
                                replaceView(R.layout.content_no_item);
                                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_FAB);
                                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getApplicationContext(), PaymentFormActivity.class);
                                        startActivityForResult(intent, 101);
                                    }
                                });
                            }
                            return;
                        }
                        // Case: num rows > 0 add items to adapter
                        if (response.body().getNumRows() > 0){
                            if (view.getId() != R.id.contentRecycler){
                                replaceView(R.layout.content_recycler);
                            }
                            paymentAdapter.insertData(response.body().getData());
                            start = start + length;
                        }
                    }
                }

                @Override
                public void onFailure(Call<PaymentModel> call, Throwable t) {
                    if (init_PB.getVisibility() == View.VISIBLE){
                        init_PB.setVisibility(View.GONE);
                    }
                    if (view.getId() != R.id.contentNoConnection){
                        replaceView(R.layout.content_no_connection);
                    }
                    dialogUtils.showNoConnectionDialog();
                    dialogUtils.setOnClickListener(new DialogUtils.OnClickListener() {
                        @Override
                        public void onClick() {
                            resetPagination();
                            loadPayment();
                        }
                    });
                    return;
                }
            });
        } catch (Exception e){
            if (init_PB.getVisibility() == View.VISIBLE){
                init_PB.setVisibility(View.GONE);
            }
            dialogUtils.showNotificationDialog(e.getMessage());
        }
    }

    private void enableActionMode(int position){
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }
    private void toggleSelection(int position) {
        paymentAdapter.toggleSelection(position);
        int count = paymentAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Tools.setSystemBarColor(PaymentActivity.this, R.color.blue_grey_700);
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                actionMode = mode;
                dialogUtils.showConfirmDialog(getString(R.string.DELETE_CONFIRMATION_HEAD), getString(R.string.DELETE_CONFIRMATION_BODY));
                dialogUtils.setOnPositiveButtonClickListener(new DialogUtils.OnPositiveButtonClickListener() {
                    @Override
                    public void onClick() {
                        dialogUtils.showProgressDialog("Menghapus...");
                        deletePayment();
                        dialogUtils.hideprogressDialog();
                        actionMode.finish();
                    }
                });
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            paymentAdapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(PaymentActivity.this, R.color.colorPrimary);
        }
    }

    private void deletePayment(){
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        SparseBooleanArray s = paymentAdapter.getSelectedItem();
        for (int i = 0; i < s.size(); i++){
            Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.deletePayment(paymentAdapter.getItem(s.keyAt(i)).getPaymentId());
            defaultResponseModelCall.enqueue(new Callback<DefaultResponseModel>() {
                @Override
                public void onResponse(Call<DefaultResponseModel> call,
                                       Response<DefaultResponseModel> response) {
                    if (response.code() != 200){
                        dialogUtils.showNotificationDialog(response.message());
                        return;
                    }
                    if (response.body().getSuccess()){
                        resetPagination();
                        paymentAdapter.setLoading();
                        loadPayment();
                    }
                }

                @Override
                public void onFailure(Call<DefaultResponseModel> call, Throwable t) {
                    dialogUtils.showNotificationDialog(t.getMessage());
                }
            });
        }
    }

    private void resetPagination(){
        if (init_PB.getVisibility() == View.GONE) {
            init_PB.setVisibility(View.VISIBLE);
        }
        start = 0;
        length = 10;
        filterStudentId = 0;
        totalRows = 0;
        paymentAdapter.resetListData();
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            content_SRL.setRefreshing(show);
            return;
        }
        content_SRL.post(new Runnable() {
            @Override
            public void run() {
                content_SRL.setRefreshing(show);
            }
        });
    }

    private void replaceView(int layout){
        viewGroup = (ViewGroup) view.getParent();
        int index = viewGroup.indexOfChild(view);
        viewGroup.removeView(view);
        view = getLayoutInflater().inflate(layout, viewGroup, false);
        viewGroup.addView(view, index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 101 && resultCode == RESULT_OK){
            if (data.getBooleanExtra("RELOAD", false)){
                if (view.getId() != R.id.contentRecycler){
                    replaceView(R.layout.content_recycler);
                    initComponent();
                }
                resetPagination();
                loadPayment();
            }
        }
    }

}
