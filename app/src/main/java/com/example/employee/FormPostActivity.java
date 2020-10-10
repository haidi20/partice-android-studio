package com.example.employee;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.employee.model.Post;
import com.example.employee.table.tbl_m_post;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import static java.lang.Integer.parseInt;

public class FormPostActivity extends AppCompatActivity {

    String ACTIVITY = "ACTIVITY";
    Button btnAdd;
    Spinner dropdown;
    EditText txtTitle, txtContent, txtImage, txtCategory;

    String titleText, contentText, setId, imageText, nameCategory;
    private ImageView btnCategory;

    SharedPrefManager sharedPrefManager;
//    private String dateNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_post);

        Toast.makeText(FormPostActivity.this, "on Create", Toast.LENGTH_LONG).show();

        sharedPrefManager = new SharedPrefManager(this);

        txtTitle = findViewById(R.id.form_title);
        txtCategory = findViewById(R.id.form_category);
        txtContent = findViewById(R.id.form_content);
        txtImage = findViewById(R.id.form_image);
        btnAdd = findViewById(R.id.btn_add);
        btnCategory = findViewById(R.id.btn_category);

        txtCategory.setEnabled(false);
        txtCategory.setTextColor(Color.BLACK);

        // handle edit
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(FormPostActivity.this);
                builder1.setMessage("Apakah anda yakin data yang ada ?");
                builder1.setCancelable(true);

                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateNow = dateFormat.format(date);

                builder1.setPositiveButton(
                        "YA",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Long getId = null;

                                if(sharedPrefManager.getModeEdit()) {
                                    getId = sharedPrefManager.getId();
                                }

                                try{
                                    if(getId != null) {
                                        Log.d("ACTIVITY", "id klik yes = "+ getId);

                                        tbl_m_post iTbl = tbl_m_post.findById(tbl_m_post.class, getId);
                                        iTbl.setTitle(txtTitle.getText().toString());
                                        iTbl.setContent(txtContent.getText().toString());
                                        iTbl.setImage(txtImage.getText().toString());
                                        iTbl.setDate(dateNow);
                                        iTbl.setType("local");
                                        iTbl.setCategory(txtCategory.getText().toString());
                                        iTbl.save();
                                    }else{
                                        Log.d("ACTIVITY", "harusnya id kosong = "+ getId);
                                        tbl_m_post iTbl = new tbl_m_post();
                                        iTbl.setTitle(txtTitle.getText().toString());
                                        iTbl.setContent(txtContent.getText().toString());
                                        iTbl.setImage(txtImage.getText().toString());
                                        iTbl.setDate(dateNow);
                                        iTbl.setType("local");
                                        iTbl.setCategory(txtCategory.getText().toString());
                                        iTbl.save();
                                    }

//                                    Intent intent = new Intent(FormActivity.this, MainActivity.class);
                                    Intent intent = new Intent(FormPostActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("form", "oke");
                                    startActivity(intent);
                                    FormPostActivity.this.finish();
                                }catch (Exception e) {
                                    Toast.makeText(FormPostActivity.this, "Maaf, Error : " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                    txtTitle.setText("");
                                    txtContent.setText("");
//                                    dropdown.setSelection(0);
                                }

                                Toast.makeText(FormPostActivity.this, "Data Berhasil Dikirim", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                                closeKeyboard();
                            }
                        });

                builder1.setNegativeButton(
                        "TIDAK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FormPostActivity.this, CategoryActivity.class);
                startActivityForResult(intent, 1);
                overridePendingTransition( R.anim.slide_left_up, R.anim.slide_right_up );

                sharedPrefManager.setSaveCache(sharedPrefManager.SP_SAVE_CACHE, true);

                sharedPrefManager.setTitle(sharedPrefManager.SP_TITLE, txtTitle.getText().toString());
                sharedPrefManager.setUrlImage(sharedPrefManager.SP_URL_IMAGE, txtImage.getText().toString());
                sharedPrefManager.setContent(sharedPrefManager.SP_CONTENT, txtContent.getText().toString());
            }
        });


        if(bundle != null){
            ArrayList<Post> editPost = new ArrayList<>();
            if(bundle.containsKey("namecategory")) {
                nameCategory = bundle.getString("namecategory");
                txtCategory.setText(nameCategory);
            }

            if(bundle.containsKey("id")) {
                setId = bundle.getString("id");
                sharedPrefManager.setModeEdit(sharedPrefManager.SP_MODE_EDIT, true);
                sharedPrefManager.setId(sharedPrefManager.SP_ID, Long.parseLong(setId));
                Log.d(ACTIVITY, "ada ID " + setId);

                List<tbl_m_post> iTbl = tbl_m_post.find(tbl_m_post.class, "id = ?", setId);

                Date date = (Date) Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateNow = dateFormat.format(date);

                if(iTbl.size() > 0) {
                    for(tbl_m_post item : iTbl) {
                        editPost.add(new Post(
                                item.getId(),
                                item.getTitle(),
                                item.getImage(),
                                item.getContent(),
                                dateNow,
                                item.getType(),
                                item.getCategory()
                        ));

                        titleText = editPost.get(0).getTitle();
                        contentText = editPost.get(0).getContent();
                        imageText = editPost.get(0).getImage();
                        nameCategory = editPost.get(0).getCategory();

                        txtTitle.setText(titleText);
                        txtContent.setText(contentText);
                        txtImage.setText(imageText);
                        txtCategory.setText(nameCategory);
                    }
                }
            }
        }

        if(sharedPrefManager.getSaveCache()) {
            txtTitle.setText(sharedPrefManager.getTitle());
            txtImage.setText(sharedPrefManager.getUrlImage());
            txtContent.setText(sharedPrefManager.getContent());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("message", "This is my message to be reloaded");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(FormPostActivity.this, MainActivity.class);
//               intent.putExtra("value", menuItem.toString() );
        startActivity(intent);
        overridePendingTransition( R.anim.slide_left_up, R.anim.slide_right_up );
//        this.overridePendingTransition(R.anim.animation_leave,
//                R.anim.animation_enter);

        FormPostActivity.this.finish();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}