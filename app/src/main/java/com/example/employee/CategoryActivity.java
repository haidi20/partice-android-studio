package com.example.employee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.employee.adapter.ListItemCategory;
import com.example.employee.adapter.ListItemPost;
import com.example.employee.model.Category;
import com.example.employee.model.Post;
import com.example.employee.table.tbl_m_category;
import com.example.employee.table.tbl_m_post;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CategoryActivity extends AppCompatActivity {

    String filterNameCategory = "";
    EditText search;
    ListItemCategory adapter;
    RecyclerView lv_category;
    ProgressBar animateLoading;
    String ACTIVITY = "ACTIVITY";
    ArrayList<Category> listCategory = new ArrayList<Category>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        lv_category = (RecyclerView) findViewById(R.id.lv_category);
        animateLoading = (ProgressBar) findViewById(R.id.animate_loading);

        adapter = new ListItemCategory(listCategory, CategoryActivity.this);
        lv_category.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
        lv_category.setAdapter(adapter);

        getDataApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_category, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nextText) {
//                adapter.getFilter().filter(nextText);
                filterNameCategory = nextText;
                listCategory.clear();
                List<tbl_m_category> iTblAll = queryShowData();

                for (tbl_m_category itblItem : iTblAll) {
                    if(itblItem.getName() != "null") {
                        listCategory.add(new Category(itblItem.getName()));
                    }
                }

//                RecyclerView lv_category = (RecyclerView) findViewById(R.id.lv_category);
//
                adapter.notifyDataSetChanged();
//                lv_category.setAdapter(adapter);
//                lv_category.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
                return true;
            }
        });


        return true;
    }



    private void getDataApi() {

        animateLoading.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://youlead.id/wp-json/barav/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        CategoryInterface postInterface = retrofit.create(CategoryInterface.class);

        Call<String> call = postInterface.STRING_CALL();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful() && response.body() != null) {
                    tbl_m_category iTbl = new tbl_m_category();
                    iTbl.deleteAll(tbl_m_category.class);

                    animateLoading.setVisibility(View.GONE);
                    try{
                        JSONArray jsonArray = new JSONArray(response.body());

                        parseResult(jsonArray);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Log.d(ACTIVITY, "koneksi gagal atau tidak ada isinya");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(CategoryActivity.this, "Koneksi bermasalah", Toast.LENGTH_LONG).show();

                Log.d(ACTIVITY, "error = "+ t.toString());
            }
        });
    }

    private void parseResult(JSONArray jsonArray) {

        for(int itemJson = 0; itemJson < jsonArray.length(); itemJson++) {
            try {
                JSONObject object = jsonArray.getJSONObject(itemJson);

                tbl_m_category iTbl = new tbl_m_category();
                iTbl.setName(object.getString("name").toString());
                iTbl.save();

//                listCategory.add(new Category(object.getString("name")));
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        List<tbl_m_category> iTblAll = queryShowData();

        for (tbl_m_category itblItem : iTblAll) {
//            Log.d(ACTIVITY, itblItem.getName());
            if(itblItem.getName() != "null") {
                listCategory.add(new Category(itblItem.getName()));
            }
        }

        adapter.notifyDataSetChanged();
//        Log.d(ACTIVITY, listCategory.toString());

    }

    private List<tbl_m_category> queryShowData() {
        return Select
                .from(tbl_m_category.class)
                .where(Condition.prop("name").like("%" + filterNameCategory + "%"))
                .list();
    }
}