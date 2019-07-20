package id.web.daimus.kasklas;

import android.app.Activity;
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
import id.web.daimus.kasklas.util.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StudentPickerActivity extends AppCompatActivity {

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
    private SwipeRefreshLayout content_SRL;
    private FloatingActionButton add_FAB;
    private DialogUtils dialogUtils;
    private ProgressBar init_PB;

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
        loadStudent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_search, menu);
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
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initComponent(){
        dialogUtils = new DialogUtils(this);
        init_PB = (ProgressBar) findViewById(R.id.init_PB);
        add_FAB = (FloatingActionButton) findViewById(R.id.add_FAB);
        add_FAB.hide();
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
                Intent intent = new Intent();
                intent.putExtra("STUDENT_ID", obj.getId());
                intent.putExtra("STUDENT_NAME", obj.getName());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, StudentListModel obj, int pos) {

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
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Pilih Siswa");
        Tools.setSystemBarColor(this);
    }

    public void loadStudent(){
        if (init_PB.getVisibility() == View.VISIBLE){
            init_PB.setVisibility(View.GONE);
        }
        try {
            ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
            Call<StudentModel> studentModelCall = apiInterface.fetchStudent(start, length, filterSearchQuery);
            studentModelCall.enqueue(new Callback<StudentModel>() {
                @Override
                public void onResponse(Call<StudentModel> call, Response<StudentModel> response) {
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
                            studentAdapter.insertData(response.body().getData());
                            start = start + length;
                        }
                    }
                }

                @Override
                public void onFailure(Call<StudentModel> call, Throwable t) {
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
            dialogUtils.showNotificationDialog(e.getMessage());
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

}
