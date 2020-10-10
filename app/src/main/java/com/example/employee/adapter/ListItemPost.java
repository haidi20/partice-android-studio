package com.example.employee.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.employee.FormPostActivity;
import com.example.employee.R;
import com.example.employee.model.Employee;
import com.example.employee.model.Post;
import com.example.employee.table.tbl_m_post;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListItemPost extends  RecyclerView.Adapter<ListItemPost.ViewHolder>  {

    private final List<Post> mPostModels;
    ArrayList<Employee> listPost;
    ImageView btnClose;
    private Activity activity;


    public ListItemPost(List<Post> PostModels, Context context) {
        mPostModels = PostModels;
    }

    @NonNull
    @Override
    public ListItemPost.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item_post, parent, false);

        // Return a new holder instance
        ListItemPost.ViewHolder viewHolder = new ListItemPost.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemPost.ViewHolder holder, int position) {
        final Post postModels = mPostModels.get(position);
        TextView titleView = holder.titleView;
        TextView contentView = holder.contentView;
        TextView indexView = holder.indexView;
        TextView nameCategoryView = holder.nameCategoryView;
        CircleImageView imageView = holder.imageView;

        int index = (position + 1);

        titleView.setText(postModels.getLimitTitle());
        indexView.setText(Integer.toString(index));
        contentView.setText(postModels.getLimitContent());
        nameCategoryView.setText(postModels.getCategory());

        Log.d("ACTIVITY", ""+ postModels.getImage());

        try {
            Glide.with(holder.itemView.getContext())
                .load(postModels.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_person_empty).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnClose = (ImageView) holder.btnCloseView;
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int position = holder.getAdapterPosition();
                final Post postModels = mPostModels.get(position);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(view.getContext());
                builder1.setMessage("Apakah anda yakin ingin menghapus data "+ postModels.getTitle());
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "YA",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                tbl_m_post iTbl = new tbl_m_post();
                                iTbl.setId(postModels.getId());
                                iTbl.delete(iTbl);

                                mPostModels.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mPostModels.size());

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
                Intent intent = new Intent(v.getContext(), FormPostActivity.class);
//                Bundle bundle = new Bundle(v.getContext());
                intent.putExtra("id", postModels.getId().toString());
                v.getContext().startActivity(intent);

                Log.d("ACTIVITY", "id = "+ postModels.getId().toString());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Intent intent = new Intent(v.getContext(), FormActivity.class);
//                intent.putExtra("key", true);
//                v.getContext().startActivity(intent);
//
//                Log.d("ACTIVITY", "id = "+ postModels.getId().toString());

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPostModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView titleView;
        public TextView indexView;
        public TextView contentView;
        public TextView nameCategoryView;
        public CircleImageView imageView;
        public ImageView btnCloseView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.text_title);
            indexView = (TextView) itemView.findViewById(R.id.text_index);
            btnCloseView = (ImageView) itemView.findViewById(R.id.btn_close);
            imageView = (CircleImageView) itemView.findViewById(R.id.image_show);
            contentView = (TextView) itemView.findViewById(R.id.text_content);
            nameCategoryView = (TextView) itemView.findViewById(R.id.text_category);
        }
    }
}
