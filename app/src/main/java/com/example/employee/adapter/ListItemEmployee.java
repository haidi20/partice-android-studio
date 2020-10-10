package com.example.employee.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employee.FormActivity;
import com.example.employee.MainActivity;
import com.example.employee.R;
import com.example.employee.model.Employee;
import com.example.employee.table.tbl_m_employee;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;


public class ListItemEmployee extends  RecyclerView.Adapter<ListItemEmployee.ViewHolder>  {

    private final List<Employee> mEmployeeModels;
    ArrayList<Employee> listEmployee;
    ImageView btnClose;


    public ListItemEmployee(List<Employee> EmployeeModels, Context context) {
        mEmployeeModels = EmployeeModels;
    }

    @NonNull
    @Override
    public ListItemEmployee.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item_employee, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ListItemEmployee.ViewHolder holder, final int position) {
        // Get the data model based on position
        final Employee employeeModels = mEmployeeModels.get(position);

        // Inflate the custom layout

        // Set item views based on your views and data model
        TextView nameView = holder.nameView;
        TextView indexView = holder.indexView;
        TextView nrpView = holder.nrpView;
        TextView departmentView = holder.departmentView;

        nameView.setText(employeeModels.getName());
        indexView.setText(Integer.toString((position + 1)));
        nrpView.setText(employeeModels.getNrp());
        departmentView.setText(employeeModels.getDepartment());

        btnClose = (ImageView) holder.btnCloseView;
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int position = holder.getAdapterPosition();
                final Employee employeeModels = mEmployeeModels.get(position);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                builder1.setMessage("Apakah anda yakin ingin menghapus data "+ employeeModels.getName());
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "YA",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                tbl_m_employee dataEmployee = Select.from(tbl_m_employee.class)
                                        .where(Condition.prop("nrp").eq(employeeModels.getNrp()))
                                        .first();

                                tbl_m_employee iTbl = new tbl_m_employee();
                                iTbl.setId(dataEmployee.getId());
                                iTbl.delete(iTbl);

                                mEmployeeModels.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mEmployeeModels.size());

                                dialog.cancel();
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent = new Intent(v.getContext(), FormActivity.class);
                intent.putExtra("nrp", employeeModels.getNrp());
                v.getContext().startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEmployeeModels.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameView;
        public TextView nrpView;
        public TextView indexView;
        public TextView departmentView;
        public ImageView btnCloseView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.text_name);
            indexView = (TextView) itemView.findViewById(R.id.text_index);
            nrpView = (TextView) itemView.findViewById(R.id.text_nrp);
            departmentView = (TextView) itemView.findViewById(R.id.text_department);
            btnCloseView = (ImageView) itemView.findViewById(R.id.btn_close);

        }
    }
}
