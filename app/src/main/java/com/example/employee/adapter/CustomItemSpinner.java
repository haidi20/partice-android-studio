package com.example.employee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employee.R;
import com.example.employee.model.Department;

import java.util.List;

public class CustomItemSpinner extends ArrayAdapter<Department> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<Department> items;
    private final int mResource;


    public CustomItemSpinner(@NonNull Context context, @LayoutRes int resource,
                             @NonNull List objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return createItemView(position, convertView, parent);

        if( convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_spinner_department, parent, false);
        }

        Department department = items.get(position);
        TextView nameSpinner = convertView.findViewById(R.id.name_department_spinner);
        if(department != null) {
            nameSpinner.setText(department.getName());
        }

        return convertView;

    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView nameDepartment = (TextView) view.findViewById(R.id.name_department);

        Department department = items.get(position);

        nameDepartment.setText(department.getName());

        return view;
    }

}
