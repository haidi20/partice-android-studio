package com.example.employee;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.employee.adapter.CustomItemSpinner;
import com.example.employee.adapter.ListItemEmployee;
import com.example.employee.model.Department;
import com.example.employee.model.Employee;
import com.example.employee.table.tbl_m_employee;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

public class FormActivity extends AppCompatActivity {

    Button btnAdd;
    Spinner dropdown;
    EditText txtNrp, txtName;

    ArrayAdapter<String> adapter;
    ArrayList listDepartment;

    Department nameDepartment;
    String nrpText, nameText, depardtmentText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        txtNrp = findViewById(R.id.form_nrp);
        txtName = findViewById(R.id.form_name);
        dropdown = (Spinner) findViewById(R.id.form_department);
//
        btnAdd = findViewById(R.id.btn_add);

        ArrayList<String> listDepartments = new ArrayList<String>();
        listDepartments.add("IT");
        listDepartments.add("HCGS");
        listDepartments.add("FA");

        listDepartment = new ArrayList<Department>();
        listDepartment.add(new Department("IT"));
        listDepartment.add(new Department("HCGS"));
        listDepartment.add(new Department("FA"));

        final CustomItemSpinner adapter = new CustomItemSpinner(FormActivity.this,
                R.layout.list_item_department, listDepartment);

        dropdown.setAdapter(adapter);

        // handle edit
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            CustomItemSpinner adapter2 = new CustomItemSpinner(FormActivity.this,
                    R.layout.list_item_department, listDepartment);

            ArrayList<Employee> editDepartment = new ArrayList<>();
            String nrp = bundle.getString("nrp");

            List<tbl_m_employee> iTbl = tbl_m_employee.find(tbl_m_employee.class, "nrp = ?", nrp);

            for(tbl_m_employee item : iTbl) {
                editDepartment.add(new Employee(item.getNrp().toString(), item.getNama().toString(), item.getDepartment().toString()));
            }

            nrpText = editDepartment.get(0).getNrp();
            nameText = editDepartment.get(0).getName();
            depardtmentText = editDepartment.get(0).getDepartment();

            txtNrp.setEnabled(false);
            txtNrp.setTextColor(Color.BLACK);
            txtNrp.setText(editDepartment.get(0).getNrp());
            txtName.setText(editDepartment.get(0).getName());
            dropdown.setAdapter(adapter2);
//            dropdown.setSelection(listDepartments.indexOf(editDepartment.get(0).getDepartment()));

//            Log.d("ACTIVITY", ""+listDepartments.indexOf("FA"));
        }

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
               Department item = (Department) parent.getSelectedItem();
//
                nameDepartment = item;

//                Log.d("ACTIVITY", "item yang di pilih = "+item);
//                dropdown.setText(item);
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(FormActivity.this);
                builder1.setMessage("Apakah anda yakin data yang ada ?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "YA",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

//                                Log.d("ACTIVITY", ""+ nameDepartment);

                                try{
                                    if(nrpText != null) {
                                        tbl_m_employee data = Select.from(tbl_m_employee.class)
                                                .where(Condition.prop("nrp").eq(nrpText))
                                                .first();

                                        Log.d("ACTIVITY", "id = "+data.getId().toString());

                                        tbl_m_employee iTbl = tbl_m_employee.findById(tbl_m_employee.class, data.getId());
                                        iTbl.setNrp(txtNrp.getText().toString());
                                        iTbl.setNama(txtName.getText().toString());
                                        iTbl.setDepartment(nameDepartment.getName());
                                        iTbl.save();
                                    }else{
                                        tbl_m_employee iTbl = new tbl_m_employee();
                                        iTbl.setNrp(txtNrp.getText().toString());
                                        iTbl.setNama(txtName.getText().toString());
                                        iTbl.setDepartment(nameDepartment.getName());
                                        iTbl.save();
                                    }

//                                    Intent intent = new Intent(FormActivity.this, MainActivity.class);
                                    Intent intent = new Intent(FormActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    FormActivity.this.finish();
                                }catch (Exception e) {
                                    Toast.makeText(FormActivity.this, "Maaf, Error : " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                    txtNrp.setText("");
                                    txtName.setText("");
                                    dropdown.setSelection(0);
                                }

                                Toast.makeText(FormActivity.this, "Data Berhasil Dikirim", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(FormActivity.this, MainActivity.class);
//               intent.putExtra("value", menuItem.toString() );
        startActivity(intent);

        FormActivity.this.finish();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}