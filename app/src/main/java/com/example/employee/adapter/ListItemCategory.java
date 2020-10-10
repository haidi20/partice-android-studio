package com.example.employee.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employee.FormPostActivity;
import com.example.employee.R;
import com.example.employee.model.Category;
import com.example.employee.model.Employee;
import com.example.employee.model.Post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ListItemCategory extends RecyclerView.Adapter<ListItemCategory.ViewHolder> implements Filterable{

    private final List<Category> mCategoryModels;
    private final List<Category> categoryListAll;
    final String ACTIVITY = "ACTIVITY";
    Context context;

    public ListItemCategory(List<Category> mCategoryModels, Context context) {
        this.mCategoryModels = mCategoryModels;
        this.categoryListAll = mCategoryModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item_category, parent, false);

        // Return a new holder instance
        ListItemCategory.ViewHolder viewHolder = new ListItemCategory.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Category categoryModels = mCategoryModels.get(position);
        TextView nameView = holder.nameView;

        if(categoryModels.getName() != "null") {
            nameView.setText(categoryModels.getName());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FormPostActivity.class);
//                Bundle bundle = new Bundle(v.getContext());
                intent.putExtra("namecategory", categoryModels.getName());
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

//                ((Activity) context).setResult(RESULT_OK, intent);
//                ((Activity) context).finish();

                Log.d("ACTIVITY", "name category = "+ categoryModels.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategoryModels.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Category> filterList = new ArrayList<>();

            if(charSequence == null || charSequence.length() == 0) {
                Log.d(ACTIVITY, "hapus");
                filterList.addAll(categoryListAll);
            }else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(Category item: mCategoryModels) {
                    Log.d(ACTIVITY, "" + item.getName());
                    if(item.getName().toLowerCase().contains(filterPattern)) {
                        filterList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            categoryListAll.clear();
//            mCategoryModels.addAll((Collection<? extends Category>) filterResults.values);
            categoryListAll.addAll((List) filterResults.values);

            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.text_name_category);
        }
    }
}
