package id.web.daimus.kasklas;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
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
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import id.web.daimus.kasklas.adapter.BillAdapter;
import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.BillListModel;
import id.web.daimus.kasklas.model.BillModel;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.DetailBillModel;
import id.web.daimus.kasklas.model.OptionModel;
import id.web.daimus.kasklas.model.StudentListModel;
import id.web.daimus.kasklas.model.StudentModel;
import id.web.daimus.kasklas.util.DialogUtils;
import id.web.daimus.kasklas.util.SessionUtils;
import id.web.daimus.kasklas.util.Tools;
import id.web.daimus.kasklas.widget.LineItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BillActivity extends AppCompatActivity {

    private static String TAG = "debuglog";
    private ActionBar actionBar;
    private Toolbar toolbar;

    // Components vars
    private ViewStub viewStub;
    private View view;
    private ViewGroup viewGroup;
    private RecyclerView content_RV;
    private BillAdapter billAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout content_SRL;
    private FloatingActionButton add_FAB;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private DialogUtils dialogUtils;
    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View bottom_sheet;
    private ProgressBar init_PB;

    private ApiInterface apiInterface;

    private String billMessage = null;
    private String treasurerName = null;

    // Filter vars
    private String hash = null;
    private Integer start = 0;
    private Integer length = 10;
    private String filterSearchQuery = null;
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
        getOption();
        loadBill();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_search_menu, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Cari sesuatu");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (view.getId() != R.id.contentRecycler){
                    replaceView(R.layout.content_recycler);
                    initComponent();
                }
                resetPagination();
                filterSearchQuery = newText;
                billAdapter.setLoading();
                loadBill();
                return true;
            }
        });

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
            Intent intent = new Intent(BillActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initComponent(){
        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        dialogUtils = new DialogUtils(this);
        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);
        init_PB = (ProgressBar) findViewById(R.id.init_PB);
        add_FAB = (FloatingActionButton) findViewById(R.id.add_FAB);
        add_FAB.hide();
        content_SRL = (SwipeRefreshLayout) findViewById(R.id.content_SRL);
        content_RV = (RecyclerView) findViewById(R.id.content_RV);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        content_RV.setItemAnimator(new DefaultItemAnimator());
        content_RV.setLayoutManager(linearLayoutManager);
        content_RV.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        billAdapter = new BillAdapter(this, 5);
        content_RV.setAdapter(billAdapter);
        billAdapter.setOnLoadMoreListener(new BillAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (billAdapter.getItemCount() < totalRows){
                    billAdapter.setLoading();
                    loadBill();
                }
            }
        });

        billAdapter.setOnClickListener(new BillAdapter.OnClickListener() {
            @Override
            public void onItemClick(View view, BillListModel obj, int pos) {
                if (obj.getTotalBill() <= 0){
                    Toast.makeText(getApplicationContext(), obj.getName() + " tidak memiliki tagihan kas", Toast.LENGTH_SHORT).show();
                } else {
                    showBottomSheetDialog(obj);
                }
            }

            @Override
            public void onItemLongClick(View view, BillListModel obj, int pos) {

            }
        });

        content_SRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetPagination();
                billAdapter.setLoading();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadBill();
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
        actionBar.setTitle("Tagihan");
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
                    Intent intent = new Intent(BillActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_payment){
                    Intent intent = new Intent(BillActivity.this, PaymentActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_product){
                    Intent intent = new Intent(BillActivity.this, ProductActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_bill){

                } else if (item.getItemId() == R.id.nav_expense){
                    Intent intent = new Intent(BillActivity.this, ExpenseActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_student){
                    Intent intent = new Intent(BillActivity.this, StudentActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_setting){
                    Intent intent = new Intent(BillActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_about){
                    Intent intent = new Intent(BillActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
            drawer.closeDrawers();
            return true;
            }
        });
    }

    public void loadBill(){
        try {
            Call<BillModel> billModelCall = apiInterface.fetchBill(start, length, filterSearchQuery);
            billModelCall.enqueue(new Callback<BillModel>() {
                @Override
                public void onResponse(Call<BillModel> call, Response<BillModel> response) {
                    if (init_PB.getVisibility() == View.VISIBLE){
                        init_PB.setVisibility(View.GONE);
                    }
                    if (response.code() != 200){
                        dialogUtils.showNotificationDialog("Duh! " + response.code() + " : " + response.message());
                        return;
                    }
                    if (response.body().getSuccess()){
                        totalRows = response.body().getTotalRows();
                        // Case: total rows <= 0, inflate view with content_no_item
                        if (response.body().getTotalRows() <= 0){
                            if (view.getId() != R.id.contentNoResult){
                                replaceView(R.layout.content_no_item);
                                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_FAB);
                                floatingActionButton.hide();
                            }
                            return;
                        }
                        // Case: num rows > 0 add items to adapter
                        if (response.body().getNumRows() > 0){
                            if (view.getId() != R.id.contentRecycler){
                                replaceView(R.layout.content_recycler);
                            }
                            billAdapter.insertData(response.body().getData());
                            start = start + length;
                        }
                    }
                }

                @Override
                public void onFailure(Call<BillModel> call, Throwable t) {
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
                            loadBill();
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

    private void getOption(){
        Log.d(TAG, "getOption: ");
        Call<OptionModel> optionModelCall = apiInterface.getOption();
        optionModelCall.enqueue(new Callback<OptionModel>() {
            @Override
            public void onResponse(Call<OptionModel> call, Response<OptionModel> response) {
                if (response.code() != 200){
                    Log.d(TAG, "onResponse: " + response.message());
                    return;
                }
                if (response.body().getSuccess()){

                    for (int i = 0; i < response.body().getData().size(); i++){
                        if (response.body().getData().get(i).getCode() == 204){
                            billMessage = response.body().getData().get(i).getValue();
                        } else if (response.body().getData().get(i).getCode() == 103){
                            treasurerName = response.body().getData().get(i).getValue();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<OptionModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void enableActionMode(int position){
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }
    private void toggleSelection(int position) {
        billAdapter.toggleSelection(position);
        int count = billAdapter.getSelectedItemCount();

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
            Tools.setSystemBarColor(BillActivity.this, R.color.blue_grey_700);
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
            billAdapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(BillActivity.this, R.color.colorPrimary);
        }
    }

    private void resetPagination(){
        if (init_PB.getVisibility() == View.GONE) {
            init_PB.setVisibility(View.VISIBLE);
        }
        start = 0;
        length = 10;
        filterSearchQuery = null;
        totalRows = 0;
        billAdapter.resetListData();
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

    private String generateBillMessage(BillListModel billListModel){
        DecimalFormat indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols indonesianFormat = new DecimalFormatSymbols();

        indonesianFormat.setCurrencySymbol("Rp");
        indonesianFormat.setMonetaryDecimalSeparator(',');
        indonesianFormat.setGroupingSeparator('.');
        indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);

        Double totalBill = (billListModel.getTotalBill() == null) ? 0 : billListModel.getTotalBill();
        String message = billMessage;
        message = message.replace(":name", billListModel.getName());
        message = message.replace(":bill-amount", String.valueOf(indonesianCurrency.format(totalBill)));
        String billDetails = "";
        for (int i = 0; i < billListModel.getBills().size(); i++){
            int no = i + 1;
            billDetails +=  no + ". " + billListModel.getBills().get(i).getName() + " = " + String.valueOf(indonesianCurrency.format(billListModel.getBills().get(i).getAmount())) + "\n";
        }
        message = message.replace(":bill-detail", billDetails);
        message = message.replace(":treasurer-name", treasurerName);
        return message;
    }

    private void showBottomSheetDialog(final BillListModel billListModel) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        final String procesedBillMessage = generateBillMessage(billListModel);

        ((View) view.findViewById(R.id.whatsapp_LL)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PackageManager pm = getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    String url = "https://wa.me/"+ billListModel.getPhone();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = Uri.parse(url)
                            .buildUpon()
                            .appendQueryParameter("text", procesedBillMessage)
                            .build().toString();
                    intent.setData(Uri.parse(uri));
                    intent.setPackage("com.whatsapp");
                    startActivity(intent);
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Aplikasi WhatsApp mungkin tidak terinstall atau berhenti berjalan :(", Toast.LENGTH_SHORT).show();
                }
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.sms_LL)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(billListModel.getPhone(), null, procesedBillMessage, null, null);
                Toast.makeText(getApplicationContext(), "Tagihan dikirim ke " + billListModel.getName(), Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.share_LL)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, procesedBillMessage);
                startActivity(Intent.createChooser(intent, "Bagikan dengan"));
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.copy_LL)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(null, procesedBillMessage);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Tersalin ke clipboard", Toast.LENGTH_SHORT).show();
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.detail_LL)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DecimalFormat indonesianCurrency = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                DecimalFormatSymbols indonesianFormat = new DecimalFormatSymbols();

                indonesianFormat.setCurrencySymbol("Rp");
                indonesianFormat.setMonetaryDecimalSeparator(',');
                indonesianFormat.setGroupingSeparator('.');
                indonesianCurrency.setDecimalFormatSymbols(indonesianFormat);
                String billDetails = "";
                for (int i = 0; i < billListModel.getBills().size(); i++){
                    int no = i + 1;
                    billDetails +=  no + ". " + billListModel.getBills().get(i).getName() + " = " + String.valueOf(indonesianCurrency.format(billListModel.getBills().get(i).getAmount())) + "\n";
                }
                dialogUtils.showMessageDialog("Bill Details", billDetails);
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
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
                loadBill();
            }
        }
    }

}
