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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import id.web.daimus.kasklas.adapter.StudentAdapter;
import id.web.daimus.kasklas.api.ApiClient;
import id.web.daimus.kasklas.api.ApiInterface;
import id.web.daimus.kasklas.model.DefaultResponseModel;
import id.web.daimus.kasklas.model.StudentListModel;
import id.web.daimus.kasklas.model.StudentModel;
import id.web.daimus.kasklas.util.DialogUtils;
import id.web.daimus.kasklas.util.SessionUtils;
import id.web.daimus.kasklas.util.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StudentActivity extends AppCompatActivity {

    private static String TAG = "debuglog";
    private ActionBar actionBar;
    private Toolbar toolbar;

    // Components vars
    private ViewStub viewStub;
    private View view;
    private ViewGroup viewGroup;
    private RecyclerView content_RV;
    private StudentAdapter studentAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar init_PB;
    private SwipeRefreshLayout content_SRL;
    private FloatingActionButton add_FAB;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private DialogUtils dialogUtils;

    // Filter vars
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
        loadStudent();
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
                studentAdapter.setLoading();
                loadStudent();
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
            Intent intent = new Intent(StudentActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initComponent(){
        dialogUtils = new DialogUtils(this);

        add_FAB = (FloatingActionButton) findViewById(R.id.add_FAB);
        add_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StudentFormActivity.class);
                startActivityForResult(intent, 101);
            }
        });
        init_PB = (ProgressBar) findViewById(R.id.init_PB);
        content_SRL = (SwipeRefreshLayout) findViewById(R.id.content_SRL);
        content_RV = (RecyclerView) findViewById(R.id.content_RV);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        content_RV.setItemAnimator(new DefaultItemAnimator());
        content_RV.setLayoutManager(linearLayoutManager);
        studentAdapter = new StudentAdapter(this, 5);
        content_RV.setAdapter(studentAdapter);
        studentAdapter.setOnLoadMoreListener(new StudentAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (studentAdapter.getItemCount() < totalRows){
                    studentAdapter.setLoading();
                    loadStudent();
                }
            }
        });
        studentAdapter.setOnClickListener(new StudentAdapter.OnClickListener() {
            @Override
            public void onItemClick(View view, StudentListModel obj, int pos) {
                if (studentAdapter.getSelectedItemCount() > 0){
                    enableActionMode(pos);
                } else {
                    Intent intent = new Intent(getApplicationContext(), StudentFormActivity.class);
                    intent.putExtra("id", obj.getId());
                    intent.putExtra("studentId", obj.getStudentId());
                    intent.putExtra("name", obj.getName());
                    intent.putExtra("gender", obj.getGender());
                    intent.putExtra("address", obj.getAddress());
                    intent.putExtra("phone", obj.getPhone());
                    startActivityForResult(intent, 101);
                }
            }

            @Override
            public void onItemLongClick(View view, StudentListModel obj, int pos) {
                enableActionMode(pos);
            }
        });

        content_SRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetPagination();
                studentAdapter.setLoading();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadStudent();
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
        actionBar.setTitle("Data Siswa");
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
                    Intent intent = new Intent(StudentActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_payment){
                    Intent intent = new Intent(StudentActivity.this, PaymentActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_product){
                    Intent intent = new Intent(StudentActivity.this, ProductActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_bill){
                    Intent intent = new Intent(StudentActivity.this, BillActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_expense){
                    Intent intent = new Intent(StudentActivity.this, ExpenseActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_student){

                } else if (item.getItemId() == R.id.nav_setting){
                    Intent intent = new Intent(StudentActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.nav_about){
                    Intent intent = new Intent(StudentActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
            drawer.closeDrawers();
            return true;
            }
        });
    }

    public void loadStudent(){
        try {
            ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
            Call<StudentModel> studentModelCall = apiInterface.fetchStudent(start, length, filterSearchQuery);
            studentModelCall.enqueue(new Callback<StudentModel>() {
                @Override
                public void onResponse(Call<StudentModel> call, Response<StudentModel> response) {
                    if (init_PB.getVisibility() == View.VISIBLE){
                        init_PB.setVisibility(View.GONE);
                    }
                    if (response.code() != 200){
                        dialogUtils.showNotificationDialog("Duh! " + response.message());
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
                                        Intent intent = new Intent(getApplicationContext(), StudentFormActivity.class);
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
                            studentAdapter.insertData(response.body().getData());
                            start = start + length;
                        }
                    }
                }

                @Override
                public void onFailure(Call<StudentModel> call, Throwable t) {
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
                            loadStudent();
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
        studentAdapter.toggleSelection(position);
        int count = studentAdapter.getSelectedItemCount();

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
            Tools.setSystemBarColor(StudentActivity.this, R.color.blue_grey_700);
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
                        deleteStudent();
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
            studentAdapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(StudentActivity.this, R.color.colorPrimary);
        }
    }

    private void deleteStudent(){
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        SparseBooleanArray s = studentAdapter.getSelectedItem();
        for (int i = 0; i < s.size(); i++){
            Call<DefaultResponseModel> defaultResponseModelCall = apiInterface.deleteStudent(studentAdapter.getItem(s.keyAt(i)).getId());
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
                        studentAdapter.setLoading();
                        loadStudent();
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
        filterSearchQuery = null;
        totalRows = 0;
        studentAdapter.resetListData();
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
                loadStudent();
            }
        }
    }

}
