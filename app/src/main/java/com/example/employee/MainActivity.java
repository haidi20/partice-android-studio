package com.example.employee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.employee.adapter.ListItemEmployee;
import com.example.employee.adapter.ListItemPost;
import com.example.employee.model.Employee;
import com.example.employee.model.Post;
import com.example.employee.table.tbl_m_employee;
import com.example.employee.table.tbl_m_post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

public class MainActivity extends AppCompatActivity {
    PopupWindow popUp;
    String orderBy = "date";
    final String ACTIVITY = "ACTIVITY";
    String textSearch = "";
    EditText search;
    int countEndScroll = 0;
    int paged = 1;
    private Toolbar toolbar;
    FloatingActionButton btnAdd;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<Post> listPost = new ArrayList<Post>();
    RecyclerView lv_post;
    ListItemPost adapter;
    String typeCalling;
    boolean onTypeSearch = false;
    boolean waitingAsync = false;
    ProgressBar animateLoading;
    SharedPrefManager sharedPrefManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefManager = new SharedPrefManager(this);
        animateLoading = (ProgressBar) findViewById(R.id.animate_loading);

        lv_post = (RecyclerView) findViewById(R.id.lv_employee);

        adapter = new ListItemPost(listPost, MainActivity.this);
        lv_post.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        lv_post.setAdapter(adapter);

        typeCalling = "onCreate";
        getDataApi(lv_post);
//        showPostOnListView();
        handleSearchData();
        handleAddData();
        handleSwipeRefresh();
//        handleSwipeDeleteItem(lv_employee);
        handleDetectEndScrolling(lv_post);
//        insertData();

        sharedPrefManager.setSaveCache(sharedPrefManager.SP_SAVE_CACHE, false);
        sharedPrefManager.setModeEdit(sharedPrefManager.SP_MODE_EDIT, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch(id) {
            case R.id.orderBy_title : {
                orderBy = "TITLE";
                Log.d("ACITIVITY", "TERBAHARUI ORDER BY NRP");
                RecyclerView lv_employee = (RecyclerView) findViewById(R.id.lv_employee);

                listPost.clear();
                lv_employee.setAdapter(adapter);
                lv_employee.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                showPostOnListView();
                break;
            }
            case R.id.orderBy_date : {
                orderBy = "DATE";
                Log.d("ACITIVITY", "TERBAHARUI ORDER BY NAMA");
                RecyclerView lv_employee = (RecyclerView) findViewById(R.id.lv_employee);

                listPost.clear();
                lv_employee.setAdapter(adapter);
                lv_employee.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                showPostOnListView();
                break;
            }
        }

        return super.onOptionsItemSelected(item);

    }

    private List<tbl_m_post> queryShowData() {
//        List<tbl_m_post> iTblAll = Select.from(tbl_m_post.class).limit(setFrom+","+setTo).list();
        List<tbl_m_post> iTblAll = Select.from(tbl_m_post.class)
                .where(Condition.prop("title").like("%"+textSearch+"%"))
                .whereOr(Condition.prop("content").like("%"+textSearch+"%"))
                .orderBy(orderBy)
                .list();
//        List<tbl_m_employee> iTblAll = Select.from(tbl_m_employee.class).orderBy(orderBy).limit(setFrom+","+setTo).list();

        return iTblAll;
    }

    private List<tbl_m_post> queryShowDataScroll() {
        List<tbl_m_post> iTblAll = Select.from(tbl_m_post.class)
                .where(Condition.prop("type").notEq("local"))
                .where(Condition.prop("paged").eq(paged))
                .orderBy(orderBy)
                .list();
//        List<tbl_m_employee> iTblAll = Select.from(tbl_m_employee.class).orderBy(orderBy).limit(setFrom+","+setTo).list();

        return iTblAll;
    }

    private void showPostOnListView() {

//        List<tbl_m_employee> iTblAll = tbl_m_employee.listAll(tbl_m_employee.class);
//        List<tbl_m_employee> iTblAll = Select.from(tbl_m_employee.class).orderBy("id DESC").list();
        List<tbl_m_post> iTblAll = queryShowData();

        if(typeCalling == "onCreate" || typeCalling == "refresh"){
            iTblAll = queryShowData();
        }else if(typeCalling == "scrolling"){
            iTblAll = queryShowDataScroll();
            Log.d(ACTIVITY, "scroll");
        }else {
            iTblAll = queryShowData();
        }

        for (tbl_m_post itblItem : iTblAll) {
            listPost.add(new Post(
                    itblItem.getId(),
                    itblItem.getTitle(),
                    itblItem.getImage(),
                    itblItem.getContent(),
                    itblItem.getDate(),
                    itblItem.getType(),
                    itblItem.getCategory()
            ));
        }

//      ookup the recyclerview in activity layout
        RecyclerView lv_employee = (RecyclerView) findViewById(R.id.lv_employee);
//
        lv_employee.setAdapter(adapter);
        lv_employee.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    private void getDataApi(RecyclerView lv_post) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://youlead.id/wp-json/barav/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        PostInterface postInterface = retrofit.create(PostInterface.class);

        Call<String> call = postInterface.STRING_CALL(paged, 5);

        animateLoading.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body() != null) {
                    animateLoading.setVisibility(View.GONE);
                    waitingAsync = false;

                    if(typeCalling == "onCreate") {
                        Log.d(ACTIVITY, "delete tbl m post");
                        tbl_m_post iTbl = new tbl_m_post();
                        iTbl.deleteAll(tbl_m_post.class,"type=?", "online");
                    }

                    try{
                        JSONArray jsonArray = new JSONArray(response.body());

                        parseResult(jsonArray, lv_post);
//                        adapter.notifyDataSetChanged();
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Log.d(ACTIVITY, "koneksi gagal atau tidak ada isinya");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                animateLoading.setVisibility(View.GONE);

                Toast.makeText(MainActivity.this, "Koneksi bermasalah", Toast.LENGTH_LONG).show();

                Log.d(ACTIVITY, "error = "+ t.toString());

                showPostOnListView();
            }
        });
    }

    private void parseResult(JSONArray jsonArray, RecyclerView lv_post) {

        for(int itemJson = 0; itemJson < jsonArray.length(); itemJson++) {
            try {
                JSONObject object = jsonArray.getJSONObject(itemJson);
                JSONObject urlImage = object.getJSONObject("featured_image");

                String dateGmt = object.getString("date_gmt");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date insertDate = sdf.parse(dateGmt);
                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateParse = targetFormat.format(insertDate);
                Long idData = Long.parseLong(object.getString("id"));

                long iTblCheckData = Select.from(tbl_m_post.class)
                                                .where(Condition.prop("id").eq(idData))
                                                .count();

                if(iTblCheckData <= 0){
                    tbl_m_post iTbl = new tbl_m_post();
                    iTbl.setId(Long.parseLong(object.getString("id")));
                    iTbl.setTitle(object.getString("title"));
                    iTbl.setContent(object.getString("content"));
                    iTbl.setImage(urlImage.getString("large"));
                    iTbl.setDate(dateParse);
                    iTbl.setType("online");
                    iTbl.setPaged(paged);
                    iTbl.save();
                }else {
                    tbl_m_post iTbl = tbl_m_post.findById(tbl_m_post.class, idData);
                    iTbl.setId(Long.parseLong(object.getString("id")));
                    iTbl.setTitle(object.getString("title"));
                    iTbl.setContent(object.getString("content"));
                    iTbl.setImage(urlImage.getString("large"));
                    iTbl.setDate(dateParse);
                    iTbl.setType("online");
                    iTbl.setPaged(paged);
                    iTbl.save();
                }

//                Log.d("JSON TITLE", object.getString("content"));
            }catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        }

        if(typeCalling == "onCreate") {
            showPostOnListView();
        }else {
            Log.d(ACTIVITY, "scroll update data dan paged = " + paged);
//            lv_post.setAdapter(adapter);
            List<tbl_m_post> iTblAll = queryShowDataScroll();
            for (tbl_m_post itblItem : iTblAll) {
                Log.d(ACTIVITY, "data judul = " + itblItem.getTitle());
                listPost.add(new Post(
                        itblItem.getId(),
                        itblItem.getTitle(),
                        itblItem.getImage(),
                        itblItem.getContent(),
                        itblItem.getDate(),
                        itblItem.getType(),
                        itblItem.getCategory()
                ));
            }

            adapter.notifyDataSetChanged();

            View currentFocus = ((MainActivity)MainActivity.this).getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
            }
        }

    }

    private void handleAddData() {
        btnAdd = findViewById(R.id.floatingActionButton);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//               Log.d("ACTIVITY", "")
//               Toast.makeText(MainActivity.this, "tombol add aktif", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, FormPostActivity.class);
//               intent.putExtra("value", menuItem.toString() );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_left_up, R.anim.slide_right_up );

                MainActivity.this.finish();
            }
        });
    }

    private void handleSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        typeCalling = "refresh";
                        listPost.clear();
                        showPostOnListView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    private void handleSearchData() {
        search = findViewById(R.id.text_search);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                listEmployee.clear();
                onTypeSearch = true;
                listPost.clear();

                textSearch = charSequence.toString();

                List<tbl_m_post> iTblAll = queryShowData();

                for (tbl_m_post itblItem : iTblAll){
//                    listEmployee.add(new Employee(itblItem.getNrp(), itblItem.getNama(), itblItem.getDepartment()));
                    listPost.add(new Post(
                            itblItem.getId(),
                            itblItem.getTitle(),
                            itblItem.getImage(),
                            itblItem.getContent(),
                            itblItem.getDate(),
                            itblItem.getType(),
                            itblItem.getCategory()
                    ));
                }

                if(textSearch.length() <= 0) {
                    onTypeSearch = false;
                }

//                lookup the recyclerview in activity layout
                RecyclerView lv_employee = (RecyclerView) findViewById(R.id.lv_employee);
//
                adapter.notifyDataSetChanged();
                lv_employee.setAdapter(adapter);
                lv_employee.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

//    private void handleSwipeDeleteItem(RecyclerView lv_employee) {
//
//        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
//
//            boolean viewBeingCleared;
////            final List mEmployeeModels = listEmployee;
//            final List mPostModels = listPost;
//
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
////                Toast.makeText(, "on Move", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
////            @Override
////            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
//////                Toast.makeText(MainActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();
////                //Remove swiped item from list and notify the RecyclerView
////
////                if (direction == ItemTouchHelper.LEFT) {
////
////                    final int position = viewHolder.getAdapterPosition();
//////                    final Employee employeeModels = listEmployee.get(position);
////                    final Post postModels = listPost.get(position);
////
////                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
////                    builder1.setMessage("Apakah anda yakin ingin menghapus data "+ employeeModels.getName());
////                    builder1.setCancelable(true);
////                    builder1.setPositiveButton(
////                            "YA",
////                            new DialogInterface.OnClickListener() {
////                                public void onClick(DialogInterface dialog, int id) {
////
////                                    tbl_m_employee dataEmployee = Select.from(tbl_m_employee.class)
////                                            .where(Condition.prop("nrp").eq(employeeModels.getNrp()))
////                                            .first();
////
////                                    tbl_m_employee iTbl = new tbl_m_employee();
////                                    iTbl.setId(dataEmployee.getId());
////                                    iTbl.delete(iTbl);
////
////                                    mEmployeeModels.remove(position);
////                                    adapter.notifyItemRemoved(position);
////                                    adapter.notifyItemRangeChanged(position, listEmployee.size());
////
////                                    dialog.cancel();
////                                }
////                            });
////
////                    builder1.setNegativeButton(
////                            "TIDAK",
////                            new DialogInterface.OnClickListener() {
////                                public void onClick(DialogInterface dialog, int id) {
////
////                                    dialog.cancel();
////                                }
////                            });
////
////                    AlertDialog alert11 = builder1.create();
////                    alert11.show();
////                }
////
////            }
//
//            @Override
//            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                super.clearView(recyclerView, viewHolder);
//                ViewCompat.setElevation(viewHolder.itemView, 0);
//                viewBeingCleared = true;
//            }
//
//            @Override
//            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                super.onChildDraw(c, recyclerView, viewHolder, dX / 3, dY, actionState, isCurrentlyActive);
//                View itemView = viewHolder.itemView;
//
//                // ketika ada event swipe
//                if(ACTION_STATE_SWIPE == actionState) {
//
//                    // ketika item active di klik oleh user
//                    if ( isCurrentlyActive) {
//                        // agar swipe ke kiri tidak keterusan / hilang item tersebut.
//                        dX = dX / 3;
//                        Log.d("ACTIVITY", "di luar -900");
//                    }
//                    else {
//                        // agar item kembali ke tengah
//                        dX = 0;
//                        Log.d("ACTIVITY", "MASUK -900");
//                    }
//                }
//
//                Log.d("ACTIVITY", "dX = "+dX);
//                Log.d("ACTIVITY", "SAMA DENGAN 0 = "+(dX == (float) 1 ? 1 : 0));
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//
//            }
//        };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
//        itemTouchHelper.attachToRecyclerView(null);
//        itemTouchHelper.attachToRecyclerView(lv_employee);
//    }

    private void insertData() {
        List<tbl_m_employee> emp = new ArrayList<>();

        emp.add(new tbl_m_employee("190194","TRI HARI PRIYONO","SPLA"));
        emp.add(new tbl_m_employee("190279","DWI SETYONO","SPMG"));
        emp.add(new tbl_m_employee("190367","NGADIONO","SPRO"));
        emp.add(new tbl_m_employee("6103105","JOKO EDI PRAYITNO","SPRO"));
        emp.add(new tbl_m_employee("6103120","ATARIS MARIO GINTING","SPLA"));
        emp.add(new tbl_m_employee("6104026","TRI PAMUNGKAS WAHYU NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("6104026","TRI PAMUNGKAS WAHYU NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("6104121","ARIYADI","SENG"));
        emp.add(new tbl_m_employee("6104179","YUSRIZAL","SPRO"));
        emp.add(new tbl_m_employee("6104287","BUDI ANTORO","SPRO"));
        emp.add(new tbl_m_employee("6105022","SUPRIYONO","SPRO"));
        emp.add(new tbl_m_employee("6105252","SURYA","SPRO"));
        emp.add(new tbl_m_employee("6105273","ROHMAT AGUS BIANTORO","SPRO"));
        emp.add(new tbl_m_employee("6105292","TOBA S.CO","SPRO"));
        emp.add(new tbl_m_employee("6105296","YUS KURNIADI","SPRO"));
        emp.add(new tbl_m_employee("6105307","DARWIN SITOMPUL","SPRO"));
        emp.add(new tbl_m_employee("6105442","RIDWAN","SPRO"));
        emp.add(new tbl_m_employee("6105488","YANTO ARYANTO","SPRO"));
        emp.add(new tbl_m_employee("6105524","GATOT SAPUTRO","SPRO"));
        emp.add(new tbl_m_employee("6105534","SRI HARYANTO","SPRO"));
        emp.add(new tbl_m_employee("6105612","SIGIT PURWANTO","SPRO"));
        emp.add(new tbl_m_employee("6105613","SUHARYANTO","SPRO"));
        emp.add(new tbl_m_employee("6105620","HUDAN SYAIFULLOH","SPRO"));
        emp.add(new tbl_m_employee("6105671","TAUFAN MAHARDHIKA","SPRO"));
        emp.add(new tbl_m_employee("6105738","PRIYO PAMBUDI","SPLA"));
        emp.add(new tbl_m_employee("6105752","KRISWANTO","SPRO"));
        emp.add(new tbl_m_employee("6105769","DADANG PURNAMA","SPLA"));
        emp.add(new tbl_m_employee("6105794","DWI SUYAMTO","SPRO"));
        emp.add(new tbl_m_employee("6105810","ANTON SUPARJO","SPRO"));
        emp.add(new tbl_m_employee("6106029","DENI ARISMAN","SPLA"));
        emp.add(new tbl_m_employee("6106050","SAPTA MURDIONO","SPLA"));
        emp.add(new tbl_m_employee("6106089","AJI MURWANTO","SPRO"));
        emp.add(new tbl_m_employee("6106095","ANDAR SADIKIN","SPRO"));
        emp.add(new tbl_m_employee("6106101","ARI PERDANA","SPRO"));
        emp.add(new tbl_m_employee("6106119","DIYAN HARTANTO","SPRO"));
        emp.add(new tbl_m_employee("6106154","MOH. HARIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6106250","AGUS SAEPUDIN","SPRO"));
        emp.add(new tbl_m_employee("6106270","DIDIT TRI YULIANTO","SPRO"));
        emp.add(new tbl_m_employee("6106301","SUGIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6106383","PARJA","SPRO"));
        emp.add(new tbl_m_employee("6106392","KUSNAN","SPRO"));
        emp.add(new tbl_m_employee("6106421","AKHMAD JUMROH","SPRO"));
        emp.add(new tbl_m_employee("6106427","RICKY RIZKI MULIA ERINAS","SFIN"));
        emp.add(new tbl_m_employee("6106466","LILIS WAHYUDI","SPRO"));
        emp.add(new tbl_m_employee("6106466","LILIS WAHYUDI","SPRO"));
        emp.add(new tbl_m_employee("6106505","ANA SHOLIHIN","SPLA"));
        emp.add(new tbl_m_employee("6106565","SYUKIRMAN","SPRO"));
        emp.add(new tbl_m_employee("6106638","IMAM MUNANDAR","SPLA"));
        emp.add(new tbl_m_employee("6107059","MOCHAMMAD RIZAL FIRDAUS","SPRO"));
        emp.add(new tbl_m_employee("6107093","JOHAN WAHYUDI","SPRO"));
        emp.add(new tbl_m_employee("6107108","ARIFIN WAHYU BUDIANTO","SPRO"));
        emp.add(new tbl_m_employee("6107109","ARIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6107129","ABDUL SOLEHUDIN","SPRO"));
        emp.add(new tbl_m_employee("6107134","ARINDY SOPYAN SUJONO","SPRO"));
        emp.add(new tbl_m_employee("6107143","APRIL ARWANSYA","SPRO"));
        emp.add(new tbl_m_employee("6107304","PARJIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6107350","EKO HARIK RUSTIYANTO","SPLA"));
        emp.add(new tbl_m_employee("6107392","SRI WIDODO","SPRO"));
        emp.add(new tbl_m_employee("6107408","WIYONO","SPRO"));
        emp.add(new tbl_m_employee("6107419","GALES ELIS TAUFIK HIDAYAT","SPLA"));
        emp.add(new tbl_m_employee("6107585","YUDI ISMANTO","SPRO"));
        emp.add(new tbl_m_employee("6107617","SUPENO","SPRO"));
        emp.add(new tbl_m_employee("6107627","BUDI IRAWAN","SPRO"));
        emp.add(new tbl_m_employee("6107630","ZAINAL ARIFIN","SPRO"));
        emp.add(new tbl_m_employee("6107652","SUJARWO","SPRO"));
        emp.add(new tbl_m_employee("6107749","JATARA WASENA","SPLA"));
        emp.add(new tbl_m_employee("6108053","ANDHI WIYOKO","SPLA"));
        emp.add(new tbl_m_employee("6108057","AFIF BUDIYANTO","SPLA"));
        emp.add(new tbl_m_employee("6108138","HARYADI","SPRO"));
        emp.add(new tbl_m_employee("6108146","MIFTAH SHOLIHIN","SPRO"));
        emp.add(new tbl_m_employee("6108166","SUNARDI","SPRO"));
        emp.add(new tbl_m_employee("6108180","YANUAR ANSOR","SPRO"));
        emp.add(new tbl_m_employee("6108252","JOKO SRIDADI","SPRO"));
        emp.add(new tbl_m_employee("6108272","TAUFAN ARIF RAHMAN","SPRO"));
        emp.add(new tbl_m_employee("6108273","YUSKAMAMI","SPRO"));
        emp.add(new tbl_m_employee("6108279","ARIFIN KURNIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6108298","SUNARDI","SPRO"));
        emp.add(new tbl_m_employee("6108302","TEDDY RUSHAD MAJI","SPRO"));
        emp.add(new tbl_m_employee("6108304","YON NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("6108356","HERMANTO","SPRO"));
        emp.add(new tbl_m_employee("6108410","UJANG SOPIAN","SPRO"));
        emp.add(new tbl_m_employee("6108410","UJANG SOPIAN","SPRO"));
        emp.add(new tbl_m_employee("6108492","HENDRA PRAYITNO","SPRO"));
        emp.add(new tbl_m_employee("6108515","AGUS JOKO SANTOSO","SPRO"));
        emp.add(new tbl_m_employee("6108518","TULUS CAHYO ROHMADI","SPRO"));
        emp.add(new tbl_m_employee("6108630","FAUZAN MUTAQIN","SPLA"));
        emp.add(new tbl_m_employee("6108647","WIJIONO","SPRO"));
        emp.add(new tbl_m_employee("6108704","DIDIK RIANTO SETIAWAN","SPLA"));
        emp.add(new tbl_m_employee("6108724","DIAN AHMAD IRWANTO","SPLA"));
        emp.add(new tbl_m_employee("6108731","EKO MARDIYANTO","SPLA"));
        emp.add(new tbl_m_employee("6108749","KEN RUSTIAN","SPLA"));
        emp.add(new tbl_m_employee("6108775","DEDI WIDIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6108775","DEDI WIDIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6108791","ANANG DWI SEPTI AJI","SPRO"));
        emp.add(new tbl_m_employee("6108794","TRI JOKO","SPRO"));
        emp.add(new tbl_m_employee("6108940","WISNU SAPUTRO","SPRO"));
        emp.add(new tbl_m_employee("6109010","ANANG WAWAN DARMAWAN","SPLA"));
        emp.add(new tbl_m_employee("6109024","DANI NURMANTO","SENG"));
        emp.add(new tbl_m_employee("6109129","EGA EDO ARESA","SPRO"));
        emp.add(new tbl_m_employee("6109138","RAHMAD SETIADI","SPRO"));
        emp.add(new tbl_m_employee("6109209","WALOYO","SPRO"));
        emp.add(new tbl_m_employee("6109211","ARIS DWI MARTANTO","SPRO"));
        emp.add(new tbl_m_employee("6109214","DIAN AGUS PERMANA","SPRO"));
        emp.add(new tbl_m_employee("6109295","MUSAEDI","SPRO"));
        emp.add(new tbl_m_employee("6109308","IMAM SUTRISNO","SPRO"));
        emp.add(new tbl_m_employee("6109326","RUSMANTO","SPRO"));
        emp.add(new tbl_m_employee("6109334","AGUNG WAHYUONO","SPRO"));
        emp.add(new tbl_m_employee("6109341","FARDISAL","SPRO"));
        emp.add(new tbl_m_employee("6109393","DANANG PUJI SANTOSO","SPRO"));
        emp.add(new tbl_m_employee("6109490","TAUFIQ ANWARI","SPRO"));
        emp.add(new tbl_m_employee("6109531","ALI MARUF","SPRO"));
        emp.add(new tbl_m_employee("6109549","KHAMDAN MUBAROKATUL ANFAL","SPRO"));
        emp.add(new tbl_m_employee("6109557","AGUS HARTANTO","SPRO"));
        emp.add(new tbl_m_employee("6109575","NUGROHO SETIAWAN","SPLA"));
        emp.add(new tbl_m_employee("6109577","SULISTIONO","SPLA"));
        emp.add(new tbl_m_employee("6109586","SURANTO","SPRO"));
        emp.add(new tbl_m_employee("6109646","DWI PURWANTO","SPRO"));
        emp.add(new tbl_m_employee("6109717","EKO YULIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6109724","MARDIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6109744","GELAR SETIAWAN","SPLA"));
        emp.add(new tbl_m_employee("6109771","SANTOK","SPRO"));
        emp.add(new tbl_m_employee("6109825","ANJAR PURNOMO","SPRO"));
        emp.add(new tbl_m_employee("6109951","AHMAT NURTAIN","SPRO"));
        emp.add(new tbl_m_employee("6109954","HERMAN CATUR PRIMANTORO","SPRO"));
        emp.add(new tbl_m_employee("6110256","ADI HARIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6110273","MAHENDRA DARMAWAN SAPUTRO","SPRO"));
        emp.add(new tbl_m_employee("6110296","EKO RUDIANTO","SPRO"));
        emp.add(new tbl_m_employee("6110404","ANDIKA SUDARSONO","SPRO"));
        emp.add(new tbl_m_employee("6110411","IRVAN DENI RISMAWAN","SPRO"));
        emp.add(new tbl_m_employee("6110439","ARI AKBAR","SLOG"));
        emp.add(new tbl_m_employee("6110515","RUSDIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6110635","HANDOKO","SPRO"));
        emp.add(new tbl_m_employee("6110656","ARIS TRIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6110657","BUDI SETYARNO","SPRO"));
        emp.add(new tbl_m_employee("6110691","AHMAD KHOIRUL IHSAN","SPRO"));
        emp.add(new tbl_m_employee("6110743","MOKHLISIN","SPLA"));
        emp.add(new tbl_m_employee("6110763","CANDRA FAUZI ROHMAN","SPLA"));
        emp.add(new tbl_m_employee("6110834","SIGIT DWI PURNOMO","SPRO"));
        emp.add(new tbl_m_employee("6110846","ARIFIN HERMAWAN","SPRO"));
        emp.add(new tbl_m_employee("6110928","IMAM SYAFII","SPRO"));
        emp.add(new tbl_m_employee("6110996","RYAN BUDI SISWANTO","SPRO"));
        emp.add(new tbl_m_employee("6111013","DIMAS SETIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6111101","TUNJUNG SETIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6111129","ADI SETIADI","SPRO"));
        emp.add(new tbl_m_employee("6111133","DANI GINANJAR","SPRO"));
        emp.add(new tbl_m_employee("6111151","HENDY DWI HARTANTO","SPRO"));
        emp.add(new tbl_m_employee("6111237","NURUL KUSUMA","SPRO"));
        emp.add(new tbl_m_employee("6111263","SUGIYONO","SPLA"));
        emp.add(new tbl_m_employee("6111396","SUMARNO","SPRO"));
        emp.add(new tbl_m_employee("6111419","SIDIK MAULANA","SPRO"));
        emp.add(new tbl_m_employee("6111428","FIRMAN SAHRI","SPRO"));
        emp.add(new tbl_m_employee("6111484","PRATAMA SETIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6111490","MUHAMMAD WAHYU NUGROHO","SPLA"));
        emp.add(new tbl_m_employee("6111490","MUHAMMAD WAHYU NUGROHO","SPLA"));
        emp.add(new tbl_m_employee("6111508","HANAFI ARDY PRASETYO","SPLA"));
        emp.add(new tbl_m_employee("6111563","MOHAMMAD TAKWA SURYANTO","SPRO"));
        emp.add(new tbl_m_employee("6111644","PONIMIN","SPRO"));
        emp.add(new tbl_m_employee("6111663","DENA PERMANA","SPRO"));
        emp.add(new tbl_m_employee("6111672","YANTO","SPRO"));
        emp.add(new tbl_m_employee("6111719","NUR CHOLIS","SPRO"));
        emp.add(new tbl_m_employee("6111791","WIKO TITIS DARMAWAN","SPRO"));
        emp.add(new tbl_m_employee("6111886","OKI ADHENAN","SPRO"));
        emp.add(new tbl_m_employee("6111902","ANDHI KURNIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6112011","ANDIKA UTAMAWAN","SPRO"));
        emp.add(new tbl_m_employee("6112016","EKA MADE PRASETYA","SPRO"));
        emp.add(new tbl_m_employee("6112077","RIZAL YACUB","SPRO"));
        emp.add(new tbl_m_employee("6112087","EKO PRASTIYO","SPRO"));
        emp.add(new tbl_m_employee("6112096","INDRA PURWANTO","SPRO"));
        emp.add(new tbl_m_employee("6112122","SYAEFUL AKBAR","SPRO"));
        emp.add(new tbl_m_employee("6112128","SURATMAN","SPRO"));
        emp.add(new tbl_m_employee("6112138","TAUFIK QUROHMAN","SPRO"));
        emp.add(new tbl_m_employee("6112179","HERI ARFIANTO","SPLA"));
        emp.add(new tbl_m_employee("6112302","SAEFUL CAHYO WINARKO","SPLA"));
        emp.add(new tbl_m_employee("6112324","KUSWANTORO","SPLA"));
        emp.add(new tbl_m_employee("6112389","RUSTANTO","SPRO"));
        emp.add(new tbl_m_employee("6112452","ACHMAD ZUNAEDI","SPRO"));
        emp.add(new tbl_m_employee("6112593","MUHAMMAD SUGONDO","SPRO"));
        emp.add(new tbl_m_employee("6112599","MUHAMMAD IQBAL AROZI","SPRO"));
        emp.add(new tbl_m_employee("6112601","MUHAMMAD RIO ARIZAL","SPRO"));
        emp.add(new tbl_m_employee("6112604","IMAWAN BAGUS ANDRIANTO","SPRO"));
        emp.add(new tbl_m_employee("6112633","YUSIF EVENDI","SPRO"));
        emp.add(new tbl_m_employee("6112650","DANNY BAKHTIAR","SPRO"));
        emp.add(new tbl_m_employee("6112653","TRY ADI PURNOMO","SPRO"));
        emp.add(new tbl_m_employee("6112708","MARTIN INDRA GANDHI","SPRO"));
        emp.add(new tbl_m_employee("6112719","FIDA ACHMAD","SPRO"));
        emp.add(new tbl_m_employee("6112724","DEDE MARIANA","SPRO"));
        emp.add(new tbl_m_employee("6112772","MAGA DWI PUTRA","SPRO"));
        emp.add(new tbl_m_employee("6112813","SAGE DWI HARYANTO","SPRO"));
        emp.add(new tbl_m_employee("6112838","SUWANDI","SPRO"));
        emp.add(new tbl_m_employee("6112841","USMAN ISMAIL","SPRO"));
        emp.add(new tbl_m_employee("6112844","YATNO PURNOMO","SPRO"));
        emp.add(new tbl_m_employee("6112890","ANDHIKA ARI BOWO","SPRO"));
        emp.add(new tbl_m_employee("6112922","AFRI SETIA AGUNG","SPRO"));
        emp.add(new tbl_m_employee("6112957","MUHAMMAD BUDI SAGARA","SPRO"));
        emp.add(new tbl_m_employee("6112975","EDI SUPRAPTO","SPLA"));
        emp.add(new tbl_m_employee("6112975","EDI SUPRAPTO","SPLA"));
        emp.add(new tbl_m_employee("6112977","ROLLIS","SPLA"));
        emp.add(new tbl_m_employee("6112987","JODI SETYONO","SPLA"));
        emp.add(new tbl_m_employee("6112987","JODI SETYONO","SPLA"));
        emp.add(new tbl_m_employee("6113129","RIO WIDYA WIROTTAMA","SPRO"));
        emp.add(new tbl_m_employee("6113338","IQBALUDIN EMANIRUS SYAM","SENG"));
        emp.add(new tbl_m_employee("6113452","ANDIKA FERNANDES","SPRO"));
        emp.add(new tbl_m_employee("6113455","HENDRI FRASTIAN AFRIANTO","SPRO"));
        emp.add(new tbl_m_employee("6113505","SUSANTO","SPRO"));
        emp.add(new tbl_m_employee("6113519","FERRY","SPRO"));
        emp.add(new tbl_m_employee("6113521","HERIAWAN DESANUR AFIAN","SPRO"));
        emp.add(new tbl_m_employee("6113529","LODI WIBOWO","SPRO"));
        emp.add(new tbl_m_employee("6113569","ALIM MUNTAHA","SPLA"));
        emp.add(new tbl_m_employee("6113823","SEPTIAWAN SANGAJI","SENG"));
        emp.add(new tbl_m_employee("6113849","AHMAD ARI HERMAWAN","SPRO"));
        emp.add(new tbl_m_employee("6114155","AGUS SUJATMIKO","SPRO"));
        emp.add(new tbl_m_employee("6114188","MUHAMMAD ARIFIN","SPRO"));
        emp.add(new tbl_m_employee("6114228","YOHANES CHRISTIAN CHANDRA PURNAMA","SPRO"));
        emp.add(new tbl_m_employee("6114338","JOSUA DEWA GEDE CHRISTIAN HENDRA PUTRA","SHCG"));
        emp.add(new tbl_m_employee("6114390","IMAM ALKAUSAR","SSHE"));
        emp.add(new tbl_m_employee("6114398","YAN ANDRIANSYAH KABUL","SPRO"));
        emp.add(new tbl_m_employee("6114484","FAHMI ALIF","SHCG"));
        emp.add(new tbl_m_employee("6114511","HERNANDA DANAR DONO","SPRO"));
        emp.add(new tbl_m_employee("6115035","OKI FIDI SISWOYO","SPRO"));
        emp.add(new tbl_m_employee("6115118","IMAM SYAFI'I","SPRO"));
        emp.add(new tbl_m_employee("6115128","ARIF RIZKI ZELANI","SPRO"));
        emp.add(new tbl_m_employee("6115195","AMAROHIM","SPRO"));
        emp.add(new tbl_m_employee("6115264","ENRICO AGTHA NARESWARA","SPLA"));
        emp.add(new tbl_m_employee("6115312","MUHAMMAD AHYARI","SPRO"));
        emp.add(new tbl_m_employee("6115327","HENDRA WIDI SAPUTRA","SPRO"));
        emp.add(new tbl_m_employee("6115341","MARYONO","SPRO"));
        emp.add(new tbl_m_employee("6115379","SATRIYA MAHENDRA PUTRA","SPRO"));
        emp.add(new tbl_m_employee("6115426","CATUR YULIAKHIR YANTO","SLOG"));
        emp.add(new tbl_m_employee("6115599","ROLLES OKTA RESANDI","SPLA"));
        emp.add(new tbl_m_employee("6115641","AZHAR RIZZA FACHRUDIN","SPLA"));
        emp.add(new tbl_m_employee("6116008","SIGIT CHAERUDHIN FEBRUHANANTO","SPLA"));
        emp.add(new tbl_m_employee("6116013","MOCHAMMAD SYAIFUL ULUM","SPLA"));
        emp.add(new tbl_m_employee("6116014","AINUR ROFIQ","SPLA"));
        emp.add(new tbl_m_employee("6116023","ANGGUN SUSANTO","SPLA"));
        emp.add(new tbl_m_employee("6116027","LUTPIA ILHAMSYAH","SPLA"));
        emp.add(new tbl_m_employee("6116028","MOKHAMAD ZAINUL ARIFIN","SPLA"));
        emp.add(new tbl_m_employee("6116090","NURROKHMAN","SPRO"));
        emp.add(new tbl_m_employee("6116093","HARI ALFIANTO NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("6116098","MAULANA NAFI'I","SPRO"));
        emp.add(new tbl_m_employee("6116111","RIA WIJAYA","SPRO"));
        emp.add(new tbl_m_employee("6116112","DARMONO","SPRO"));
        emp.add(new tbl_m_employee("6116117","AGUS SOLIKIN","SPRO"));
        emp.add(new tbl_m_employee("6116121","HERI SULISTIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6116127","MUHAMMAD MUCHLIS","SPRO"));
        emp.add(new tbl_m_employee("6116129","RADHITIYA SATRIA WIBAWA","SPRO"));
        emp.add(new tbl_m_employee("6116134","DEFIT FEBRIYANTO","SPRO"));
        emp.add(new tbl_m_employee("6116195","MUHAMMAD ZULKARNAIN RAMADHAN","SPRO"));
        emp.add(new tbl_m_employee("6116198","NUR KHAFID SUSANTO","SPRO"));
        emp.add(new tbl_m_employee("6116217","AKBAR GUNAWAN","SPRO"));
        emp.add(new tbl_m_employee("6116223","ALI ASHAB","SPRO"));
        emp.add(new tbl_m_employee("6116253","MUKHLIS NUR ARIFIN","SPRO"));
        emp.add(new tbl_m_employee("6116384","SAEPUL ANWAR","SLOG"));
        emp.add(new tbl_m_employee("6117066","ARIF YURAHMAN","SENG"));
        emp.add(new tbl_m_employee("6117075","MUCH MASRUH BAIDHOWI","SENG"));
        emp.add(new tbl_m_employee("6117172","BAYU RAGIL SAPUTRO","SPRO"));
        emp.add(new tbl_m_employee("6117204","MUHAMMAD FATKHURROHMAN","SPRO"));
        emp.add(new tbl_m_employee("6117206","PUGUH SWASONO","SPRO"));
        emp.add(new tbl_m_employee("6117213","WIDI HARTONO","SPRO"));
        emp.add(new tbl_m_employee("6117249","WAHYU GINANJAR","SPRO"));
        emp.add(new tbl_m_employee("6117305","SUPARDI","SPRO"));
        emp.add(new tbl_m_employee("6117476","REYHAN WIYARTA SUNDAJI","SPRO"));
        emp.add(new tbl_m_employee("6117585","TIYO ABDULLAH","SPRO"));
        emp.add(new tbl_m_employee("6117592","AHMAD IRFAN","SPRO"));
        emp.add(new tbl_m_employee("6117630","WAHYU IRIANTO","SPRO"));
        emp.add(new tbl_m_employee("6117632","ULIL ALBAB","SPRO"));
        emp.add(new tbl_m_employee("6117702","AGUNG DWI KURNIAWAN","SPRO"));
        emp.add(new tbl_m_employee("6117704","JAROT AMANDA","SPRO"));
        emp.add(new tbl_m_employee("6117796","DJODI PERWIRA NAGARA","SPLA"));
        emp.add(new tbl_m_employee("6117844","DERI TRI LAKSONO","SPRO"));
        emp.add(new tbl_m_employee("6117845","WAHYU MUNTAHA","SPRO"));
        emp.add(new tbl_m_employee("6117856","AGUNG BASUKI","SSHE"));
        emp.add(new tbl_m_employee("6117917","DIMAS ASNANDAR PUTRA","SHCG"));
        emp.add(new tbl_m_employee("6117976","DWI SETIAWAN","SENG"));
        emp.add(new tbl_m_employee("6118481","MUHAMMAD ALPANI","SPRO"));
        emp.add(new tbl_m_employee("6118590","DIMAS INDRA SETYAWAN","SPLA"));
        emp.add(new tbl_m_employee("6118592","HENDRAWAN ARI SUDRAJAT","SENG"));
        emp.add(new tbl_m_employee("6118728","ARIZAL KURNIANTO","SPLA"));
        emp.add(new tbl_m_employee("6118729","DENI TRI HARYANTO","SPLA"));
        emp.add(new tbl_m_employee("6118735","EKA APRILIO GUSTOMY","SPRO"));
        emp.add(new tbl_m_employee("6118807","DEDI PUTRA","SPRO"));
        emp.add(new tbl_m_employee("6118911","NANDA MAULANA","SPRO"));
        emp.add(new tbl_m_employee("6118998","AGUNG WIBOWO","SPLA"));
        emp.add(new tbl_m_employee("6119046","DWI WARDANA","SPLA"));
        emp.add(new tbl_m_employee("6119156","MUKHLAS DWI PUTRA","SPMG"));
        emp.add(new tbl_m_employee("6119481","MA'RUF SABILAN","SPLA"));
        emp.add(new tbl_m_employee("6119630","DWI ARDIANTO","SPLA"));
        emp.add(new tbl_m_employee("6119631","DWI NURYANTO","SPLA"));
        emp.add(new tbl_m_employee("6119632","EKA SEPTYAN RANDITYA WIDIYANTO","SPLA"));
        emp.add(new tbl_m_employee("6119633","FEBRI PUTRA PAMUNGKAS","SPLA"));
        emp.add(new tbl_m_employee("6119634","HARIS SETYARTO","SPLA"));
        emp.add(new tbl_m_employee("6119635","MUHAMMAD EKO BAGUS SAPUTRO","SPLA"));
        emp.add(new tbl_m_employee("6119636","MUHAMMAD ANWAR","SPLA"));
        emp.add(new tbl_m_employee("6119637","MUHAMMAD FAHRURROZI","SPLA"));
        emp.add(new tbl_m_employee("6119638","ADITYA YULIANTO","SPLA"));
        emp.add(new tbl_m_employee("6119810","MUHAMMAD RIZQI FAUZI","SPLA"));
        emp.add(new tbl_m_employee("6119959","ANDI HERMAWAN","SPLA"));
        emp.add(new tbl_m_employee("6119960","TOMI ARI ANGGARA","SPLA"));
        emp.add(new tbl_m_employee("6119961","IKHSAN PRIYADI","SPLA"));
        emp.add(new tbl_m_employee("6119962","YAZID THOIFURI","SPLA"));
        emp.add(new tbl_m_employee("6119964","ARIEF DARMA PUTERA RAMADHAN","SPLA"));
        emp.add(new tbl_m_employee("6119965","DEVID ALVIANO","SPLA"));
        emp.add(new tbl_m_employee("6119966","YOGIE MUADIB ISMAIL","SPLA"));
        emp.add(new tbl_m_employee("6119967","AJI PANGESTU PUTRA","SPLA"));
        emp.add(new tbl_m_employee("6119968","CAYA CASMARA","SPLA"));
        emp.add(new tbl_m_employee("6192027","MAHFUDIN","SPRO"));
        emp.add(new tbl_m_employee("6194294","SUKOCO","SPRO"));
        emp.add(new tbl_m_employee("6196082","ANJAR SWASTONO","SENG"));
        emp.add(new tbl_m_employee("6197086","ARIS BUDI MURTOPO","SPRO"));
        emp.add(new tbl_m_employee("6197210","MUHAMMAD MIFTAHUSSURUR","SPLA"));
        emp.add(new tbl_m_employee("6197218","AGUS SUBANDANA","SSHE"));
        emp.add(new tbl_m_employee("6197255","NANANG SYAIFUDDIN","SPLA"));
        emp.add(new tbl_m_employee("6199006","BARDONO","SPRO"));
        emp.add(new tbl_m_employee("6518005","BUDI SASMITO","SHCG"));
        emp.add(new tbl_m_employee("7209010","ANDRIKA PERMANA","SPRO"));
        emp.add(new tbl_m_employee("8311051","MUHAMMAD RUSLI","SPRO"));
        emp.add(new tbl_m_employee("8402004","ISSAK","SHCG"));
        emp.add(new tbl_m_employee("8404366","MUHAMMAD THOHIR","SPRO"));
        emp.add(new tbl_m_employee("8405313","MUHAMMAD TAHAN","SPRO"));
        emp.add(new tbl_m_employee("8405320","JUMILANTO","SPLA"));
        emp.add(new tbl_m_employee("8407022","KORNELIUS","SPRO"));
        emp.add(new tbl_m_employee("8408045","AGUS SUPRIYADI","SPRO"));
        emp.add(new tbl_m_employee("8408054","YOHANIS NATAN PATANDEAN","SPRO"));
        emp.add(new tbl_m_employee("8408060","NOBER MANDA","SPRO"));
        emp.add(new tbl_m_employee("8409024","MUH. RUSDI","SPLA"));
        emp.add(new tbl_m_employee("8411023","AGUS MUSDIANTO","SPLA"));
        emp.add(new tbl_m_employee("61081029","BAMBANG WIJANARKO","SPRO"));
        emp.add(new tbl_m_employee("61081054","PANJI ASMORO","SPRO"));
        emp.add(new tbl_m_employee("61081067","RAHMAD PRIYANTO","SPLA"));
        emp.add(new tbl_m_employee("61081082","WAWAN SETIAWAN","SPLA"));
        emp.add(new tbl_m_employee("61091128","TRI WALUYO","SPRO"));
        emp.add(new tbl_m_employee("61091134","SARJONO","SPLA"));
        emp.add(new tbl_m_employee("61091135","SONI HARYONO","SPLA"));
        emp.add(new tbl_m_employee("61091137","WAHYU FRAKOSO","SPLA"));
        emp.add(new tbl_m_employee("61091138","WAHYU NUGROHO","SPLA"));
        emp.add(new tbl_m_employee("61091173","SYAHRUL JAELANI","SPRO"));
        emp.add(new tbl_m_employee("61091206","ARIF MUJIYANTO","SPRO"));
        emp.add(new tbl_m_employee("61091307","SUNARTO","SPRO"));
        emp.add(new tbl_m_employee("61091355","IGNATIUS ANTON NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("61091384","MARTHA PURWASA","SPRO"));
        emp.add(new tbl_m_employee("61091508","EKO PRASTYO","SPRO"));
        emp.add(new tbl_m_employee("61091510","MUJIYONO PRANYOTO","SPRO"));
        emp.add(new tbl_m_employee("61091570","DWI FEBRIANTO","SPRO"));
        emp.add(new tbl_m_employee("61091580","TRI YULIANTO","SPRO"));
        emp.add(new tbl_m_employee("61091593","YOGA PRATIKNO","SPRO"));
        emp.add(new tbl_m_employee("61091596","DIDI SARJUDI","SPLA"));
        emp.add(new tbl_m_employee("61091601","DEDI JULIYANTO","SPLA"));
        emp.add(new tbl_m_employee("61091626","RENDY DIRGANTORO","SPRO"));
        emp.add(new tbl_m_employee("61091672","ARI PURNIAWAN","SPRO"));
        emp.add(new tbl_m_employee("61091678","WIDHIATMOKO PRIADHITYA","SPLA"));
        emp.add(new tbl_m_employee("61091712","DADI","SPRO"));
        emp.add(new tbl_m_employee("61091725","WAHYU WIDODO","SPRO"));
        emp.add(new tbl_m_employee("61091744","RUDI YULIANTO","SPRO"));
        emp.add(new tbl_m_employee("61091875","JOHAN DWI SAPUTRO","SPLA"));
        emp.add(new tbl_m_employee("61092082","RONI BUDI PRASETYO","SPRO"));
        emp.add(new tbl_m_employee("61092147","HARGIANTO","SPRO"));
        emp.add(new tbl_m_employee("61092253","FERI HERMAWAN","SPRO"));
        emp.add(new tbl_m_employee("61092265","AGUS DWIYANTO","SPRO"));
        emp.add(new tbl_m_employee("61092287","BAYOE JOKO PARIKESIT","SPRO"));
        emp.add(new tbl_m_employee("61101101","PRISWANTO PANCA NUGRAHA","SPRO"));
        emp.add(new tbl_m_employee("61101180","AAN PAHLIANA HERTANTO","SPRO"));
        emp.add(new tbl_m_employee("61101189","YOYON DWI CAHYONO","SPRO"));
        emp.add(new tbl_m_employee("61101194","SEPTHIAN ALLAN BUDIKUSUMA","SPRO"));
        emp.add(new tbl_m_employee("61101212","IMAM HOJALI BAYU KARDITO","SPRO"));
        emp.add(new tbl_m_employee("61101224","ARIF MUSTHOFA","SPRO"));
        emp.add(new tbl_m_employee("61101290","ERMA LEFAN","SPLA"));
        emp.add(new tbl_m_employee("61101379","SUGENG PRIYONO","SPLA"));
        emp.add(new tbl_m_employee("61101383","PRAMUDYA NOFA","SPLA"));
        emp.add(new tbl_m_employee("61101387","BUDI SETYO WIBOWO","SPLA"));
        emp.add(new tbl_m_employee("61101444","SAHBANA","SPRO"));
        emp.add(new tbl_m_employee("61101550","NURUDDIN","SPRO"));
        emp.add(new tbl_m_employee("61101709","MUH. FATHUR ROHMAN","SPRO"));
        emp.add(new tbl_m_employee("61101713","RIYAN FAOZI","SPRO"));
        emp.add(new tbl_m_employee("61101773","ZAUQ LADHAT HAQ","SPRO"));
        emp.add(new tbl_m_employee("61101787","EKO MASLIAN","SPLA"));
        emp.add(new tbl_m_employee("61101791","YONGKI APRIZA","SPLA"));
        emp.add(new tbl_m_employee("61101810","SULTANSYAH","SPRO"));
        emp.add(new tbl_m_employee("61101825","RIKI GUNAWAN","SPRO"));
        emp.add(new tbl_m_employee("61101833","PRASETYA UTAMA","SPRO"));
        emp.add(new tbl_m_employee("61101835","AHMAD FADKUR ROHMAN","SPRO"));
        emp.add(new tbl_m_employee("61101836","AHMAD AZIS FAISAL","SPRO"));
        emp.add(new tbl_m_employee("61102006","ARIF SULISTIYO","SPRO"));
        emp.add(new tbl_m_employee("61102052","ARGIYANTO","SPLA"));
        emp.add(new tbl_m_employee("61102083","EKA YUDA PRATAMA","SPRO"));
        emp.add(new tbl_m_employee("61102101","MAUDIN","SPRO"));
        emp.add(new tbl_m_employee("61102151","ALEK SUSANTO","SPRO"));
        emp.add(new tbl_m_employee("61102186","AKHMAD MUBASYIR","SPLA"));
        emp.add(new tbl_m_employee("61102186","AKHMAD MUBASYIR","SPLA"));
        emp.add(new tbl_m_employee("61102229","SULCHAN FALS FANITA","SPLA"));
        emp.add(new tbl_m_employee("61102310","IRWAN PRASTIYONO","SHCG"));
        emp.add(new tbl_m_employee("61102320","WEMPI KURNIAWAN","SPMG"));
        emp.add(new tbl_m_employee("61102339","WAHYU KRISTIADI","SPLA"));
        emp.add(new tbl_m_employee("61102340","AHMAD MIFTAKHUL HUDA","SPLA"));
        emp.add(new tbl_m_employee("61102400","MOHAMAD FADILAH","SPRO"));
        emp.add(new tbl_m_employee("61102414","SUSANTO","SPRO"));
        emp.add(new tbl_m_employee("61102581","INKY DANINDRA PIDEKSO","SENG"));
        emp.add(new tbl_m_employee("61102599","SANDI BAYU PERWIRA","SSHE"));
        emp.add(new tbl_m_employee("61111051","MUHAMMAD RUSITO","SPRO"));
        emp.add(new tbl_m_employee("61111176","ABDUL QOYYUM","SPRO"));
        emp.add(new tbl_m_employee("61111177","ANDRI DWI PRATAMA","SPRO"));
        emp.add(new tbl_m_employee("61111178","BUDIONO","SPRO"));
        emp.add(new tbl_m_employee("61111179","CACU SAMSURI","SPRO"));
        emp.add(new tbl_m_employee("61111186","SEPTIANDIKA","SPRO"));
        emp.add(new tbl_m_employee("61111193","NUGROHO WARIYATNO","SPRO"));
        emp.add(new tbl_m_employee("61111197","SUKO JATI NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("61111198","YUSTINUS HERY HARYANTO","SPRO"));
        emp.add(new tbl_m_employee("61111204","IRWAN BUDIANA","SPRO"));
        emp.add(new tbl_m_employee("61111250","WAHYU BUDI NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("61111273","DARMADI","SPRO"));
        emp.add(new tbl_m_employee("61111274","DENI ALFIAN ADITAMA","SPRO"));
        emp.add(new tbl_m_employee("61111306","RIZAL AMIRUDIN","SPRO"));
        emp.add(new tbl_m_employee("61111321","ACHZAR TRI BUANA","SPRO"));
        emp.add(new tbl_m_employee("61111351","FUAT ICHSAN","SPRO"));
        emp.add(new tbl_m_employee("61111353","SATRIA GANI LISTYANTO","SPRO"));
        emp.add(new tbl_m_employee("61111363","YADIMAN","SPLA"));
        emp.add(new tbl_m_employee("61111446","JIHAN MAULAL MUQODDAS","SPLA"));
        emp.add(new tbl_m_employee("61111485","ARIS FITRIYANTO","SPRO"));
        emp.add(new tbl_m_employee("61111500","FERI SUSNANTO","SPRO"));
        emp.add(new tbl_m_employee("61111501","AGUS DWI UNTORO","SPRO"));
        emp.add(new tbl_m_employee("61111572","MUHAMMAD CHOIRIL","SPLA"));
        emp.add(new tbl_m_employee("61111592","DWI WIBOWO ADI ATMOJO","SLOG"));
        emp.add(new tbl_m_employee("61111616","SALUT RIWANDI","SPRO"));
        emp.add(new tbl_m_employee("61111622","SAMBODO SUSILO","SPRO"));
        emp.add(new tbl_m_employee("61111723","FRENDI YUDHA PRATAMA","SPRO"));
        emp.add(new tbl_m_employee("61111743","SUGENG PRAYITNO","SPRO"));
        emp.add(new tbl_m_employee("61111780","HALIM RIFA'I","SPRO"));
        emp.add(new tbl_m_employee("61111815","AGUNG WAHYU","SPRO"));
        emp.add(new tbl_m_employee("61111842","JATMIKO","SPRO"));
        emp.add(new tbl_m_employee("61111909","SUGI PRIYATIN","SPLA"));
        emp.add(new tbl_m_employee("61111933","KHOLID MURSALIN","SPRO"));
        emp.add(new tbl_m_employee("61111960","CATUR TRIMO","SPRO"));
        emp.add(new tbl_m_employee("61111990","SUPRIYADI","SPRO"));
        emp.add(new tbl_m_employee("61112021","MUHTADIN","SPLA"));
        emp.add(new tbl_m_employee("61112047","ASEP ANDIYANA","SSHE"));
        emp.add(new tbl_m_employee("61112084","NUR IMAM WIDIYANTO","SPRO"));
        emp.add(new tbl_m_employee("61112105","EKO BUDIYONO","SPRO"));
        emp.add(new tbl_m_employee("61112105","EKO BUDIYONO","SPRO"));
        emp.add(new tbl_m_employee("61112155","AHMAD SAKHOWI","SPRO"));
        emp.add(new tbl_m_employee("61112190","LUQNI ZAKARIA","SPRO"));
        emp.add(new tbl_m_employee("61112228","DWI ISKANDAR","SPLA"));
        emp.add(new tbl_m_employee("61112350","WINARNO","SPRO"));
        emp.add(new tbl_m_employee("61112364","LADI TRI HARTANTO","SPRO"));
        emp.add(new tbl_m_employee("61112366","WAHIDIYANTO HENDRA PAMUNGKAS","SPRO"));
        emp.add(new tbl_m_employee("61112407","RIZKI HENDRA PRATAMA","SPRO"));
        emp.add(new tbl_m_employee("61112577","RIZAL NUR ARIFIN","SLOG"));
        emp.add(new tbl_m_employee("61112603","CHANDRA NUGRAHA","SPRO"));
        emp.add(new tbl_m_employee("61112610","HERI JOKO PRASETYAWAN","SPRO"));
        emp.add(new tbl_m_employee("61112613","MEDI PRAYUGO","SPRO"));
        emp.add(new tbl_m_employee("61112614","EDY SANTOSO","SPRO"));
        emp.add(new tbl_m_employee("61112658","AHZAM NUR UMAM","SPRO"));
        emp.add(new tbl_m_employee("61112678","MUSYAFAK SOLIKHIN","SPRO"));
        emp.add(new tbl_m_employee("61112703","FAJAR AFRIANDI","SPRO"));
        emp.add(new tbl_m_employee("61112715","NADZARUL ANSOR","SPRO"));
        emp.add(new tbl_m_employee("61112773","SEPTA ARFIANTO","SENG"));
        emp.add(new tbl_m_employee("61112795","MUHTAMAR","SPRO"));
        emp.add(new tbl_m_employee("61112805","ASEP TRIONO","SPLA"));
        emp.add(new tbl_m_employee("61112812","YUDI HASTOMO","SPLA"));
        emp.add(new tbl_m_employee("61112812","YUDI HASTOMO","SPLA"));
        emp.add(new tbl_m_employee("61112903","RISKY TRIWIDITYA KUSUMA","SHCG"));
        emp.add(new tbl_m_employee("61112918","SEPTIYAN PUJI RAHARJO","SPLA"));
        emp.add(new tbl_m_employee("61112986","SUTARMAN","SPRO"));
        emp.add(new tbl_m_employee("61113002","NUROHMAN","SPRO"));
        emp.add(new tbl_m_employee("61113003","ANDI SUPRASTIONO","SPRO"));
        emp.add(new tbl_m_employee("61113007","SIDIK ANIANTO","SPRO"));
        emp.add(new tbl_m_employee("61113036","YUDI PRANOTO","SPRO"));
        emp.add(new tbl_m_employee("61113044","MUHAMMAD RIQI ANGGRA CIPTHA","SPRO"));
        emp.add(new tbl_m_employee("61113161","SARLAN ALAMSYAH","SPLA"));
        emp.add(new tbl_m_employee("61113189","DEDEN ARDIANSYAH","SPRO"));
        emp.add(new tbl_m_employee("61113275","MUSHOFFA","SPRO"));
        emp.add(new tbl_m_employee("61113329","ARDY WILLIANTO","SPRO"));
        emp.add(new tbl_m_employee("61113391","TOFIK HIDAYAT","SPRO"));
        emp.add(new tbl_m_employee("61113472","DANANG PRAKOSO","SHCG"));
        emp.add(new tbl_m_employee("61121021","DANIEL TEJA ARDIYAN PUTRA","SPRO"));
        emp.add(new tbl_m_employee("61121024","DIAN HERI PRAYITNO","SPRO"));
        emp.add(new tbl_m_employee("61121050","NUR FAJAR SHODIQ","SPRO"));
        emp.add(new tbl_m_employee("61121051","VERI NUGROHO","SPRO"));
        emp.add(new tbl_m_employee("61121061","DWI SUPRAJOKO SUTEJO","SPRO"));
        emp.add(new tbl_m_employee("61121072","MUHAMMAD AGUS TRI WIDYASMORO","SPRO"));
        emp.add(new tbl_m_employee("61121111","BAYU AJIE NUGROHONINGWIDI","SPRO"));
        emp.add(new tbl_m_employee("61121119","YULIANTO","SPRO"));
        emp.add(new tbl_m_employee("61121123","EKI PUTRA PRATOMO","SPRO"));
        emp.add(new tbl_m_employee("61121158","ROFIQI SYAHRUL M","SPRO"));
        emp.add(new tbl_m_employee("61121163","KUSWANTO","SPLA"));
        emp.add(new tbl_m_employee("61121172","ADI PONCO NUGROHO","SPLA"));
        emp.add(new tbl_m_employee("61121181","SUNARDI","SPLA"));
        emp.add(new tbl_m_employee("61121231","MUHAMMAD ILHAM ISNA","SLOG"));
        emp.add(new tbl_m_employee("61121247","SUHERMAN","SPRO"));
        emp.add(new tbl_m_employee("61121315","OKY HANDIKA SAPU","SPRO"));
        emp.add(new tbl_m_employee("61121321","WAHYU ALIMIN","SPRO"));
        emp.add(new tbl_m_employee("61121350","NARA ADHITYA NUGRAHA","SPRO"));
        emp.add(new tbl_m_employee("61121351","RISMANTO","SPRO"));
        emp.add(new tbl_m_employee("61121485","ANAS UMAR KHOLID","SPRO"));
        emp.add(new tbl_m_employee("61121512","RENDI BUDI UTOMO","SSHE"));
        emp.add(new tbl_m_employee("61121559","BUDI PURNOMO","SPRO"));
        emp.add(new tbl_m_employee("61121567","MUHAMMAD WANDI SUSILO","SPRO"));
        emp.add(new tbl_m_employee("61121572","ZAINAL ABIDIN","SPRO"));
        emp.add(new tbl_m_employee("61121613","IRWAN SETIAWAN","SPRO"));
        emp.add(new tbl_m_employee("61121639","ISNANTO","SPRO"));
        emp.add(new tbl_m_employee("61121743","JASWADI","SPRO"));
        emp.add(new tbl_m_employee("61121753","IBNU PEMBAYUN","SPRO"));
        emp.add(new tbl_m_employee("61121755","JOKO SULISTIYO","SPRO"));
        emp.add(new tbl_m_employee("61121760","SURANTO","SPRO"));
        emp.add(new tbl_m_employee("61121928","MUCH. NUZULUL MUALIF","SPLA"));
        emp.add(new tbl_m_employee("61121958","SHOLIKAN","SPLA"));
        emp.add(new tbl_m_employee("61121988","M DANNY SETIAWANSYAH","SSHE"));
        emp.add(new tbl_m_employee("61122025","HARRIS RAMADHAN","SHCG"));
        emp.add(new tbl_m_employee("61122026","IMANNIAR AKBAR KURNIAWAN","SHCG"));
        emp.add(new tbl_m_employee("61122028","RIDHA MAHARDIKA PERMANA","SHCG"));
        emp.add(new tbl_m_employee("61122037","RIZSKY AGUSTAMI","SLOG"));
        emp.add(new tbl_m_employee("61122079","DITTO SURYAJAYA","SPRO"));
        emp.add(new tbl_m_employee("61122081","HANAFIAH MUSTOFA","SPRO"));
        emp.add(new tbl_m_employee("61122084","JOKO KRISTIANTO","SPRO"));
        emp.add(new tbl_m_employee("61122089","MUHAMMAD NAZIH","SPRO"));
        emp.add(new tbl_m_employee("61122128","RIZKY PANDU WIGUNA","SLOG"));
        emp.add(new tbl_m_employee("61122186","BAGUS PULUNG VANDEKA","SPRO"));
        emp.add(new tbl_m_employee("61122202","APRIYANTO","SPRO"));
        emp.add(new tbl_m_employee("61122211","FARIZ GUNAWAN","SPRO"));
        emp.add(new tbl_m_employee("61122260","KIRUL SHOLEH","SPLA"));
        emp.add(new tbl_m_employee("61122349","DWI NUR PRASETIA","SPLA"));
        emp.add(new tbl_m_employee("61122802","ANGGER PRADANA","SENG"));
        emp.add(new tbl_m_employee("61122880","RIAN FIRDAUS","SPRO"));
        emp.add(new tbl_m_employee("61171293","MUHAMAD INDRA NUGRAHA","SENG"));
        emp.add(new tbl_m_employee("61171749","ORLANDO HOLOMOAN SIANTURI","SPMG"));
        emp.add(new tbl_m_employee("61181009","RIZAL ABDUL GHOFUR","SPLA"));
        emp.add(new tbl_m_employee("61181108","ADZ DZIKRAA DINANDY PUTRA","SLOG"));
        emp.add(new tbl_m_employee("61181221","M. FIRDAUS","SPLA"));
        emp.add(new tbl_m_employee("61181566","ARDHIAN FAUZA","SPLA"));
        emp.add(new tbl_m_employee("61181576","WAHYU HERMANSYAH","SENG"));
        emp.add(new tbl_m_employee("61181615","CHELVIN ADAM FAHRI","SPLA"));
        emp.add(new tbl_m_employee("61181698","HARISKAN SUKARDA","SHCG"));
        emp.add(new tbl_m_employee("61181898","FARIF EDI SAPUTRA","SPLA"));
        emp.add(new tbl_m_employee("61182473","FEBIANTO REJEKI","SFIN"));
        emp.add(new tbl_m_employee("61182488","SYAHRUL MUNIR KURNIAWAN","SLOG"));
        emp.add(new tbl_m_employee("61182888","BAYU DARMAWAN","SSHE"));
        emp.add(new tbl_m_employee("61182994","JOE ANTONIUS HARTONO SUSILO","SLOG"));
        emp.add(new tbl_m_employee("1D04060","JUWENI WIBISONO","SPRO"));
        emp.add(new tbl_m_employee("1D04067","ABDUL AZIS","SLOG"));
        emp.add(new tbl_m_employee("1D09048","RENDY ANWAR AFANDI","SPRO"));
        emp.add(new tbl_m_employee("1D09113","FERI FIRMANSYAH","SPRO"));
        emp.add(new tbl_m_employee("1D10008","ZAINUL ARIF AFANDI","SPLA"));
        emp.add(new tbl_m_employee("1E10017","FUAD HASAN","SPRO"));
        emp.add(new tbl_m_employee("1E10038","PIPIN DWI ARISETYO","SENG"));
        emp.add(new tbl_m_employee("1E17006","GITO","SENG"));
        emp.add(new tbl_m_employee("1F01017","MICHA PATA","SPRO"));
        emp.add(new tbl_m_employee("1F04002","AGUS SUPRIADI","SENG"));
        emp.add(new tbl_m_employee("1F05002","SUBAGYO","SHCG"));
        emp.add(new tbl_m_employee("1F07006","ARIS MULYO UTOMO","SPLA"));
        emp.add(new tbl_m_employee("1F09048","BAYU AGUNG PRASETYO","SPRO"));
        emp.add(new tbl_m_employee("1F11021","ARLAND JANUAR SARI","SPRO"));
        emp.add(new tbl_m_employee("1F12004","MOH. ZAINOL HOLIS","SPRO"));
        emp.add(new tbl_m_employee("1F97063","SUPRIYONO","SPRO"));
        emp.add(new tbl_m_employee("1H07009","RIYANTO","SPRO"));
        emp.add(new tbl_m_employee("1H09043","AKBAR","SPLA"));
        emp.add(new tbl_m_employee("1H09050","ZULKIFLI","SHCG"));
        emp.add(new tbl_m_employee("1H09077","ADI WALUYO","SPRO"));
        emp.add(new tbl_m_employee("1H09099","JONI LIMBU","SPRO"));
        emp.add(new tbl_m_employee("1H11045","BENI SETIYAWAN","SPRO"));
        emp.add(new tbl_m_employee("1H12030","ALIEF KRESNA UTAMA","SPMG"));
        emp.add(new tbl_m_employee("1H12037","SUKIRNO","SENG"));
        emp.add(new tbl_m_employee("1H13031","RADIANTO","SPLA"));
        emp.add(new tbl_m_employee("1I11004","DIYANTO SUTRISNO","SFIN"));
        emp.add(new tbl_m_employee("1K01024","ESTRY JUNIANTO","SENG"));
        emp.add(new tbl_m_employee("1K01024","ESTRY JUNIANTO","SENG"));
        emp.add(new tbl_m_employee("1K05003","M.ELHAMAM AFFANDY","SPRO"));
        emp.add(new tbl_m_employee("1K12078","EKO FEBRI YUDA PRATAMA","SPLA"));
        emp.add(new tbl_m_employee("1K12079","FAJAR NURDIN","SPLA"));
        emp.add(new tbl_m_employee("1K12079","FAJAR NURDIN","SPLA"));
        emp.add(new tbl_m_employee("1K92018","ARIVSON TAMBA","SPRO"));
        emp.add(new tbl_m_employee("1K96076","ARIN MAPIKA","SPRO"));
        emp.add(new tbl_m_employee("1M01092","SUDARSONO","SPRO"));
        emp.add(new tbl_m_employee("1M04010","EDWAR","SPRO"));
        emp.add(new tbl_m_employee("1M98064","SOLICHIN","SPRO"));
        emp.add(new tbl_m_employee("1M98075","EDY SOFYAN","SPRO"));
        emp.add(new tbl_m_employee("1M98141","HARIS","SPLA"));
        emp.add(new tbl_m_employee("1R05033","SUPRIYADI","SPLA"));
        emp.add(new tbl_m_employee("1R06047","RUSLI","SPRO"));
        emp.add(new tbl_m_employee("1R06058","ACHMAD PADLY","SENG"));
        emp.add(new tbl_m_employee("1R08019","KODERI","SPRO"));
        emp.add(new tbl_m_employee("1R08033","MUSLIHUDIN AGUS RUDIANTO","SPLA"));
        emp.add(new tbl_m_employee("1R08061","MUAMMAR KADAFI","SPRO"));
        emp.add(new tbl_m_employee("1R08075","TAUFIK CHIKYRIA ARIF SETIAWAN","SPRO"));
        emp.add(new tbl_m_employee("1R10034","INDRA NYONO LESTYAWAN","SPRO"));
        emp.add(new tbl_m_employee("1R10056","ACHMAD SUHARTANTO","SPRO"));
        emp.add(new tbl_m_employee("1R11002","AHMAD ABDIANSYAH","SPRO"));
        emp.add(new tbl_m_employee("1R11032","JAHARUDDIN","SPRO"));
        emp.add(new tbl_m_employee("1R96082","RASMON T.S","SPLA"));
        emp.add(new tbl_m_employee("1R96153","IDRIS","SPRO"));
        emp.add(new tbl_m_employee("1R97013","YUBFERI MATARRU","SSHE"));
        emp.add(new tbl_m_employee("1R97020","LUTHER TUDINGALO","SPRO"));
        emp.add(new tbl_m_employee("1R97053","YOHANES SUGIANTO","SPRO"));
        emp.add(new tbl_m_employee("1S10003","PARNINGOTAN PURBA","SENG"));
        emp.add(new tbl_m_employee("1S10003","PARNINGOTAN PURBA","SENG"));
        emp.add(new tbl_m_employee("1S17002","AGUS RUDIANSYAH","SENG"));
        emp.add(new tbl_m_employee("1S18047","JOKO TRIYANTO","SENG"));
        emp.add(new tbl_m_employee("1S19026","WILDA WULANDARI","SFIN"));
        emp.add(new tbl_m_employee("1S19027","ANGGA PERDANA","SHCG"));
        emp.add(new tbl_m_employee("1S19028","LANGGENG SEFTI CAHYO","SHCG"));
        emp.add(new tbl_m_employee("1S19029","ALDA NIA VERAWATI","SLOG"));
        emp.add(new tbl_m_employee("1U11003","SAMSUL ARIFIN","SENG"));
        emp.add(new tbl_m_employee("1U13005","ALFAFA AJI","SPRO"));
        emp.add(new tbl_m_employee("1W07002","AAN SIGIT PURNAMA","SPLA"));
        emp.add(new tbl_m_employee("1W07003","ARIF BUDI RAHARJA","SPLA"));
        emp.add(new tbl_m_employee("1W07006","PURWADI","SPLA"));
        emp.add(new tbl_m_employee("1W07011","YUSHEF","SPRO"));
        emp.add(new tbl_m_employee("1W09004","HENDRIKO PATANDA","SPRO"));
        emp.add(new tbl_m_employee("1W09027","SABRANI","SPLA"));
        emp.add(new tbl_m_employee("1W09029","ADI CAHYA KURNIAWAN","SPRO"));
        emp.add(new tbl_m_employee("1W09058","MUHLISON","SPRO"));
        emp.add(new tbl_m_employee("1W10007","HERU WAHYUDI","SPRO"));
        emp.add(new tbl_m_employee("1W11031","DARMINTO","SPLA"));
        emp.add(new tbl_m_employee("1W12028","JEFFRY RALISTIAWAN","SPRO"));
        emp.add(new tbl_m_employee("1W12039","HELI HANDOKO","SPRO"));
        emp.add(new tbl_m_employee("1W12045","WAHYU SULISTIYANTO","SPRO"));
        emp.add(new tbl_m_employee("KBAB18001","JUNAID","SHCG"));
        emp.add(new tbl_m_employee("KBAB18002","SUPRIADI","SHCG"));
        emp.add(new tbl_m_employee("KBAB18003","RAMA","SHCG"));
        emp.add(new tbl_m_employee("KBAB18004","MUJI BURRAHMAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB18005","ARIF RAHMADANI","SHCG"));
        emp.add(new tbl_m_employee("KBAB18006","RUDY","SHCG"));
        emp.add(new tbl_m_employee("KBAB18007","ARIANTO","SHCG"));
        emp.add(new tbl_m_employee("KBAB18008","SUPRIANOR","SHCG"));
        emp.add(new tbl_m_employee("KBAB18009","NATANIEL EDI","SHCG"));
        emp.add(new tbl_m_employee("KBAB18010","SUMARYONO","SHCG"));
        emp.add(new tbl_m_employee("KBAB18013","AHMAD SUKURI","SHCG"));
        emp.add(new tbl_m_employee("KBAB18014","HERMAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB18015","BACHTIAR","SHCG"));
        emp.add(new tbl_m_employee("KBAB19001","INDRA LESMANA","SHCG"));
        emp.add(new tbl_m_employee("KBAB19002","MAHADI PRIMA JAYA ","SHCG"));
        emp.add(new tbl_m_employee("KBAB19003","ANDI M. NUR","SHCG"));
        emp.add(new tbl_m_employee("KBAB19004","WAHYUDIN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19005","HALIMUDIN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19006","AHMAD IRAWAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19007","SYAMSUL BAHRI","SHCG"));
        emp.add(new tbl_m_employee("KBAB19008","FRENKY","SHCG"));
        emp.add(new tbl_m_employee("KBAB19009","M. TOPAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19010","YOPAN ANDREAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19011","ANDRIANSYAH","SHCG"));
        emp.add(new tbl_m_employee("KBAB19012","CHANDRA HARNIS","SHCG"));
        emp.add(new tbl_m_employee("KBAB19013","BUDIMAN","SHCG"));
        emp.add(new tbl_m_employee("KBAB19014","AKBAR BIN SIWA","SHCG"));
        emp.add(new tbl_m_employee("KBAB19015","ABDUL RAHIM","SHCG"));
        emp.add(new tbl_m_employee("KBAB19016","RICO HAMKA","SHCG"));
        emp.add(new tbl_m_employee("KBAB20001","SYAHRIAL","SHCG"));
        emp.add(new tbl_m_employee("KBAB20002","ANANG BASRI","SHCG"));
        emp.add(new tbl_m_employee("KBAB20003","A ASWIN","SHCG"));
        emp.add(new tbl_m_employee("KBAB20004","YEFTA DHIKA PRATAMA N","SHCG"));
        emp.add(new tbl_m_employee("KBAB20005","M SYAHYUNI","SHCG"));
        emp.add(new tbl_m_employee("KBAB20006","ABDUL KHAFID","SHCG"));
        emp.add(new tbl_m_employee("KBAB20007","AHMAD YUSUF","SHCG"));
        emp.add(new tbl_m_employee("KBAB20008","REZA RAMADHANI","SHCG"));
        emp.add(new tbl_m_employee("KBAB20009","MUH. GUSTI","SHCG"));
        emp.add(new tbl_m_employee("KBAB20010","SOFYAN NOOR","SHCG"));
        emp.add(new tbl_m_employee("KBAB20011","ALFIS SYAHRUL","SHCG"));
        emp.add(new tbl_m_employee("KBAB20012","JAINUL ARIFIN","SHCG"));
        emp.add(new tbl_m_employee("KBAB20013","M TAUFIK R","SHCG"));
        emp.add(new tbl_m_employee("KBAB20014","MUH AHYAR DH","SHCG"));
        emp.add(new tbl_m_employee("KBAB20015","MOHAMMAD EDO FIRNANDA","SHCG"));
        emp.add(new tbl_m_employee("KBAB20016","VIJAY SUTEJO","SHCG"));
        emp.add(new tbl_m_employee("KBAR19004","SANDI SYAHPUTRA","SHCG"));
        emp.add(new tbl_m_employee("KBBA18001","YOHANES ADI KURNIAWAN","SHCG"));
        emp.add(new tbl_m_employee("KBBA18002","MADIANSYAH","SHCG"));
        emp.add(new tbl_m_employee("KBBA19001","MUDIANSYAH","SHCG"));
        emp.add(new tbl_m_employee("KBBA19002","MARYONO","SHCG"));
        emp.add(new tbl_m_employee("KBBA19003","IRVAN OKTARIANTO","SHCG"));
        emp.add(new tbl_m_employee("KBBA19004","WISNO","SHCG"));
        emp.add(new tbl_m_employee("KBBA19005","NURDIN TAJUDDIN","SHCG"));
        emp.add(new tbl_m_employee("KBBA19006","MUKTHAR","SHCG"));
        emp.add(new tbl_m_employee("KBBA19007","SURIADI","SHCG"));
        emp.add(new tbl_m_employee("KBBA19008","ACO","SHCG"));
        emp.add(new tbl_m_employee("KBBA19009","EDI","SHCG"));
        emp.add(new tbl_m_employee("KBBA19010","JUNAIDI","SHCG"));
        emp.add(new tbl_m_employee("KBBH20001","ROBERT ARI SUSILO","SHCG"));
        emp.add(new tbl_m_employee("KBFA19001","SUYANTO INDROYONO","SHCG"));
        emp.add(new tbl_m_employee("KBFA19002","SLAMET SUDIONO","SHCG"));
        emp.add(new tbl_m_employee("KBFA19003","SISWANTO","SHCG"));
        emp.add(new tbl_m_employee("KBKA18001","SYAFRIANSYAH","SPLA"));
        emp.add(new tbl_m_employee("KBKA18002","DODY","SPLA"));
        emp.add(new tbl_m_employee("KBKA18003","ROBBY EKO PRASETYO","SPLA"));
        emp.add(new tbl_m_employee("KBKA18004","HARDIKA AMANGKURAT","SPLA"));
        emp.add(new tbl_m_employee("KBKA18005","JUAN RENOMARSEL IROTH","SPLA"));
        emp.add(new tbl_m_employee("KBKA18006","SANDY BUNNAYA DWICAHYA","SPLA"));
        emp.add(new tbl_m_employee("KBKA18007","DEDIK TRIATMOKO","SPLA"));
        emp.add(new tbl_m_employee("KBKA18008","YOGI PUTRA PRATAMA","SPLA"));
        emp.add(new tbl_m_employee("KBKA18009","YUSUF IRAWAN","SPLA"));
        emp.add(new tbl_m_employee("KBKA18010","VENDI HIDAYAT","SPLA"));
        emp.add(new tbl_m_employee("KBKA19001","MUHAMMAD ADITYA","SPLA"));
        emp.add(new tbl_m_employee("KBKA19002","YULI SETIA BUDI","SPLA"));
        emp.add(new tbl_m_employee("KBKA19003","FARIS RIZKI","SPLA"));
        emp.add(new tbl_m_employee("KBKA19004","AHMAD RIFAI","SPLA"));
        emp.add(new tbl_m_employee("KBKA19005","MUHAMMAD KHAIRUL FIKHRI","SPLA"));
        emp.add(new tbl_m_employee("KBKA19006","M RISKI HARYONO","SPLA"));
        emp.add(new tbl_m_employee("KBKA19007","BASTIAN MARULI BALIYANTO","SPLA"));
        emp.add(new tbl_m_employee("KBKA20001","MUH ADITYA PRATAMA","SPLA"));
        emp.add(new tbl_m_employee("KBKA20002","TARDIANSYAH","SPLA"));
        emp.add(new tbl_m_employee("KBKA20003","YOGA MURTI PRATAMA","SPLA"));
        emp.add(new tbl_m_employee("KBKA20004","YOYOK EPRIANTO","SPLA"));
        emp.add(new tbl_m_employee("KBKA20005","RIZAL FAUZI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20006","BILAL","SPLA"));
        emp.add(new tbl_m_employee("KBKA20007","RIYANDIKA WAHYU SAPUTRA","SPLA"));
        emp.add(new tbl_m_employee("KBKA20008","MUH IRWANSYAH","SPLA"));
        emp.add(new tbl_m_employee("KBKA20009","RANDY ASMAWI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20010","RAHMAD RESKI PP","SPLA"));
        emp.add(new tbl_m_employee("KBKA20011","MOCH ANWAR CHOTAM","SPLA"));
        emp.add(new tbl_m_employee("KBKA20012","ANDREANUS MAHIPE","SPLA"));
        emp.add(new tbl_m_employee("KBKA20013","HARIADI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20014","M BUDIMAN","SPLA"));
        emp.add(new tbl_m_employee("KBKA20015","DIKA NURGANDA BAKTI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20016","ADE YOSAFAT","SPLA"));
        emp.add(new tbl_m_employee("KBKA20017","MARSONO","SPLA"));
        emp.add(new tbl_m_employee("KBKA20018","PAJRI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20019","DANDI","SPLA"));
        emp.add(new tbl_m_employee("KBKA20020","MUH FIQRI FAURIQ","SPLA"));
        emp.add(new tbl_m_employee("KBKN18001","ISMAIL","SLOG"));
        emp.add(new tbl_m_employee("KBKN18002","NAZWAR MAULANA","SHCG"));
        emp.add(new tbl_m_employee("KBKN18003","MUH. FAJAR RIDWAN","SLOG"));
        emp.add(new tbl_m_employee("KBKN18004","NURKHALISAH","SENG"));
        emp.add(new tbl_m_employee("KBKN18005","ROBERTUS BUHA","SENG"));
        emp.add(new tbl_m_employee("KBKN18006","JEPRI ARMADA","SPRO"));
        emp.add(new tbl_m_employee("KBKN18007","TRI WAHYUDI","SLOG"));
        emp.add(new tbl_m_employee("KBKN18008","ADE KURNIAWAN","SLOG"));
        emp.add(new tbl_m_employee("KBKN18009","MUHLIDA SIREGAR","SPLA"));
        emp.add(new tbl_m_employee("KBKN18010","HENDRA NOVRIANDI SAPUTRA","SPLA"));
        emp.add(new tbl_m_employee("KBKN18011","MULYONO","SPLA"));
        emp.add(new tbl_m_employee("KBKN18012","NULDANI","SPLA"));
        emp.add(new tbl_m_employee("KBKN18013","ALFIAN","SPLA"));
        emp.add(new tbl_m_employee("KBKN18014","MUHAEMIN","SPLA"));
        emp.add(new tbl_m_employee("KBKN18015","HENDRI SUSANTO","SLOG"));
        emp.add(new tbl_m_employee("KBKN18016","JUSLI","SPRO"));
        emp.add(new tbl_m_employee("KBKN18017","MUCH FAUZI SYAHRANI","SPRO"));
        emp.add(new tbl_m_employee("KBKN18018","M NOOREFANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBKN18019","NOVIYANTI","SHCG"));
        emp.add(new tbl_m_employee("KBKN18020","IRPANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBKN18021","ANDIKA","SENG"));
        emp.add(new tbl_m_employee("KBKN18022","M ASWAR","SLOG"));
        emp.add(new tbl_m_employee("KBKN18023","ADI MAULANA","SENG"));
        emp.add(new tbl_m_employee("KBKN18024","JERRY YOGA SAPUTRA","SLOG"));
        emp.add(new tbl_m_employee("KBKN18025","HARLAN EFENDI","SLOG"));
        emp.add(new tbl_m_employee("KBKN18026","ISDAR","SHCG"));
        emp.add(new tbl_m_employee("KBKN18027","ROVICO GERALDIANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBKN18028","ANDI GUSTI JAYADI","SHCG"));
        emp.add(new tbl_m_employee("KBKN18029","RUSLI","SPRO"));
        emp.add(new tbl_m_employee("KBKN18030","SURYANI","SHCG"));
        emp.add(new tbl_m_employee("KBKN19001","ABDUL GANI","SHCG"));
        emp.add(new tbl_m_employee("KBKN19002","JUMARDIANTO","SPLA"));
        emp.add(new tbl_m_employee("KBKN19003","RISKI WINDA PRATIWI","SHCG"));
        emp.add(new tbl_m_employee("KBKN19004","DODIK AFRIANTO","SLOG"));
        emp.add(new tbl_m_employee("KBKN19005","ARYS ZULFIKAR ALI A","SLOG"));
        emp.add(new tbl_m_employee("KBKN19006","ISKADIR","SLOG"));
        emp.add(new tbl_m_employee("KBKN19007","NUR ASIYAH","SSHE"));
        emp.add(new tbl_m_employee("KBKN19008","MUDAHTSIR","SENG"));
        emp.add(new tbl_m_employee("KBKN19009","JULYSTIANSYAH","SENG"));
        emp.add(new tbl_m_employee("KBKN19010","FERNANDO OZY IYAN SAPUTRA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19011","ARI KRIS HARTANTO","SPLA"));
        emp.add(new tbl_m_employee("KBKN19012","DECKY RADITYANATA","SHCG"));
        emp.add(new tbl_m_employee("KBKN19013","ARI SUHUD","SPLA"));
        emp.add(new tbl_m_employee("KBKN19014","ANDIKA PRASTIYO BUDI UTOMO","SPLA"));
        emp.add(new tbl_m_employee("KBKN19015","RAHMAT","SPLA"));
        emp.add(new tbl_m_employee("KBKN19016","DENI FIRNANDA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19017","MARIANI","SPRO"));
        emp.add(new tbl_m_employee("KBKN19018","LISNAYANTI PATADUNGAN","SSHE"));
        emp.add(new tbl_m_employee("KBKN19019","ZULKIFLI","SPRO"));
        emp.add(new tbl_m_employee("KBKN19020","MUHAMMAD ILHAM MAULANA","SPRO"));
        emp.add(new tbl_m_employee("KBKN19021","ASRISAL","SENG"));
        emp.add(new tbl_m_employee("KBKN19022","NUR MUHAMMAD ARIF","SENG"));
        emp.add(new tbl_m_employee("KBKN19023","ICA TIARA FISKA","SLOG"));
        emp.add(new tbl_m_employee("KBKN19024","EKO PRASETYO","SPRO"));
        emp.add(new tbl_m_employee("KBKN19025","HARRYS ZULFIKAL ALI A","SPRO"));
        emp.add(new tbl_m_employee("KBKN19026","KURNIAWAN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19027","ARDIANTO","SPRO"));
        emp.add(new tbl_m_employee("KBKN19028","ANDRIAS EKA SETYAWAN","SLOG"));
        emp.add(new tbl_m_employee("KBKN19029","ANDI GUNAWAN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19030","M. ANDI RISKY G","SPLA"));
        emp.add(new tbl_m_employee("KBKN19031","M. TAMRIN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19032","JAMIL ANSA RIZKI","SLOG"));
        emp.add(new tbl_m_employee("KBKN19033","HERLAN WARDANA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19034","ELHIS WANDY","SENG"));
        emp.add(new tbl_m_employee("KBKN19035","KUKUH FAULANDI PRADANA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19036","MARLIN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19037","DEDDI PEBRI S","SPRO"));
        emp.add(new tbl_m_employee("KBKN19038","NAZADI MUBARAK","SENG"));
        emp.add(new tbl_m_employee("KBKN19039","WAWAN TAHIR SAPUTRA","SENG"));
        emp.add(new tbl_m_employee("KBKN19040","MUCHLIS M AMALI","SLOG"));
        emp.add(new tbl_m_employee("KBKN19041","DODY HENDRA JAYA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19042","DEDY IRWANTO","SPLA"));
        emp.add(new tbl_m_employee("KBKN19043","RAJUDDIN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19044","ARIS SETIAWAN","SLOG"));
        emp.add(new tbl_m_employee("KBKN19045","ARIADI PRATAMA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19046","SAINUDDIN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19047","WANA SOPIAN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19048","HERWIN","SLOG"));
        emp.add(new tbl_m_employee("KBKN19049","JEVI CEVIG","SLOG"));
        emp.add(new tbl_m_employee("KBKN19050","RAHMAD CANDRA HANAFI","SLOG"));
        emp.add(new tbl_m_employee("KBKN19051","LINDA YASINTA","SPRO"));
        emp.add(new tbl_m_employee("KBKN19052","ARYAN FATURUSI","SPLA"));
        emp.add(new tbl_m_employee("KBKN19053","HANAFI","SPLA"));
        emp.add(new tbl_m_employee("KBKN19054","SURYA ARTA WIJAYA","SPLA"));
        emp.add(new tbl_m_employee("KBKN19055","WILLIAM PARULIAN","SPLA"));
        emp.add(new tbl_m_employee("KBKN19056","INSYAP","SPLA"));
        emp.add(new tbl_m_employee("KBKN19057","MUH. ARDIANSYAH K.","SPLA"));
        emp.add(new tbl_m_employee("KBKN19058","RIZAL JUHANSYAH","SPLA"));
        emp.add(new tbl_m_employee("KBKN19059","NUR IRFANDI","SPLA"));
        emp.add(new tbl_m_employee("KBKN19060","RAMDANIANSYAH","SPLA"));
        emp.add(new tbl_m_employee("KBKN19061","RIZKY HARIADI F.","SPLA"));
        emp.add(new tbl_m_employee("KBKN20001","RIMSON FRIHATIN S","SHCG"));
        emp.add(new tbl_m_employee("KBKN20002","EKO SUPRIYONO","SHCG"));
        emp.add(new tbl_m_employee("KBKO19001","ABDUL RAHMAD","SHCG"));
        emp.add(new tbl_m_employee("KBKO19002","JULIUS SUTRA","SHCG"));
        emp.add(new tbl_m_employee("KBKO19003","KISTO BASTARI SIMBOLON","SHCG"));
        emp.add(new tbl_m_employee("KBKO20001","HAERUDDIN","SHCG"));
        emp.add(new tbl_m_employee("KBKO20002","M JAFAR","SHCG"));
        emp.add(new tbl_m_employee("KBMA20001","HAIDI NURHADINATA","SPMG"));
        emp.add(new tbl_m_employee("KBPA18001","ABDUL ROHMAN","SPLA"));
        emp.add(new tbl_m_employee("KBPA18002","DWI WAHYONO","SPLA"));
        emp.add(new tbl_m_employee("KBPA19001","ADITYA SURAHMAN","SPLA"));
        emp.add(new tbl_m_employee("KBPA19002","OKI FERNANDA","SPLA"));
        emp.add(new tbl_m_employee("KBPA19003","AGUS SUPRIYATNO","SPLA"));
        emp.add(new tbl_m_employee("KBPK19001","ABDUL GANI","SPRO"));
        emp.add(new tbl_m_employee("KBPK19002","ARI ZULFITRIAWAN NUR","SPRO"));
        emp.add(new tbl_m_employee("KBPK19003","DELIYANTO PURNAMA S","SPRO"));
        emp.add(new tbl_m_employee("KBPK19004","DUWI YANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBPK19005","HENDRA","SPRO"));
        emp.add(new tbl_m_employee("KBPK19006","JONATHAN","SPRO"));
        emp.add(new tbl_m_employee("KBPK19007","M IRSANDI PRAYUDHA","SPRO"));
        emp.add(new tbl_m_employee("KBPK19008","RAHMAT HIDAYAT","SPRO"));
        emp.add(new tbl_m_employee("KBPK19009","SUPRY","SPRO"));
        emp.add(new tbl_m_employee("KBPK19010","SAIFUL BAHTIAR","SPRO"));
        emp.add(new tbl_m_employee("KBPK19011","IRWANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBPK19012","ARIYADI","SPRO"));
        emp.add(new tbl_m_employee("KBPK19013","M KURNIAWAN MAULANA E","SPRO"));
        emp.add(new tbl_m_employee("KBPK19014","IKOK HERISON WIDODO","SPRO"));
        emp.add(new tbl_m_employee("KBPK19015","ALIMUDDIN","SPRO"));
        emp.add(new tbl_m_employee("KBPK20001","NANDYANSYAH","SPRO"));
        emp.add(new tbl_m_employee("KBPK20002","RANO","SPRO"));
        emp.add(new tbl_m_employee("KBPK20003","IRPAN MAULANA","SPRO"));
        emp.add(new tbl_m_employee("KBPK20004","WELDY","SPRO"));
        emp.add(new tbl_m_employee("KBPK20005","DHIKA PUTRA","SPRO"));
        emp.add(new tbl_m_employee("KBPK20006","ZULFAJRIANNUR","SPRO"));
        emp.add(new tbl_m_employee("KBSN20001","SAHAT PARULIAN S","SPMG"));
        emp.add(new tbl_m_employee("KBSN20002","IRFANSYAH HASMA DILLAH","SPMG"));
        emp.add(new tbl_m_employee("KBSN20003","MUHAMMAD AINI MURNI","SPMG"));
        emp.add(new tbl_m_employee("KBTJ18001","SUYANDI","SLOG"));
        emp.add(new tbl_m_employee("KBTJ18002","ILHAM","SLOG"));
        emp.add(new tbl_m_employee("KBTJ18003","ISMAIL","SLOG"));
        emp.add(new tbl_m_employee("KBTJ18006","SARWIN","SLOG"));
        emp.add(new tbl_m_employee("KBTJ19001","ENGGAR SURYA PRADISTA","SLOG"));
        emp.add(new tbl_m_employee("KBTJ19003","NIYEL MARTEN","SLOG"));
        emp.add(new tbl_m_employee("KBTJ19004","RIZKI FEBRIAN","SLOG"));
        emp.add(new tbl_m_employee("KBTJ20002","ANWAR AKBAR","SLOG"));

        for(tbl_m_employee item : emp) {
            tbl_m_employee iTbl = new tbl_m_employee();
            iTbl.setNrp(item.nrp);
            iTbl.setNama(item.nama);
            iTbl.setDepartment(item.Department);
            iTbl.save();
        }

    }

    private void handleDetectEndScrolling(RecyclerView lv_employee) {
        lv_employee.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !onTypeSearch && !waitingAsync) {
                    int countData = 5;
                    countEndScroll = (countEndScroll + 1);
                    paged = (countEndScroll + 1);
                    waitingAsync = true;
                    Log.d(ACTIVITY, "paged = " + paged);
//                    setTo = (countEndScroll * countData);
//                    setFrom = setTo - countData + 1;

//                    List<tbl_m_employee> iTblAll = queryShowData();
//
//                    for (tbl_m_employee itblItem : iTblAll){
//                        listEmployee.add(new Employee(itblItem.getNrp(), itblItem.getNama(), itblItem.getDepartment()));
//                    }

                    typeCalling = "scrolling";
                    getDataApi(lv_post);

//                    List<tbl_m_post> iTblAll = queryShowDataRefresh();
//
//                    for (tbl_m_post itblItem : iTblAll){
//                        listPost.add(new Post(
//                                itblItem.getId(),
//                                itblItem.getTitle(),
//                                itblItem.getImage(),
//                                itblItem.getContent(),
//                                itblItem.getDate(),
//                                itblItem.getType()
//                        ));
//                    }

//                    adapter.notifyDataSetChanged();
//
//                    View currentFocus = ((MainActivity)MainActivity.this).getCurrentFocus();
//                    if (currentFocus != null) {
//                        currentFocus.clearFocus();
//                    }

                    Log.d("ACTIVITY", ""+ Integer.toString(countEndScroll));
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
}