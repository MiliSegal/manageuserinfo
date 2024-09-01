package com.example.easysaleapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.easysaleapp.Database.DatabaseClient;
import com.example.easysaleapp.fragments.Fragment_update_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easysaleapp.R;
import com.example.easysaleapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private List<User> dataset;
    private List<User> datasetFull;

    public UserAdapter(List<User> dataSet) {
        this.dataset = dataSet;
        this.datasetFull = new ArrayList<>(dataSet);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User currentUser = dataset.get(position);

        holder.textFirstName.setText(currentUser.getFirstName());
        holder.textLastName.setText(currentUser.getLastName());
        holder.textEmail.setText(currentUser.getEmail());
        holder.textId.setText(String.valueOf(currentUser.getId()));

        Glide.with(holder.itemView.getContext())
                .load(currentUser.getAvatar())
                .placeholder(R.drawable.donut)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textFirstName;
        TextView textLastName;
        TextView textEmail;
        TextView textId;
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textFirstName = itemView.findViewById(R.id.firstName);
            textLastName = itemView.findViewById(R.id.lastName);
            textEmail = itemView.findViewById(R.id.email);
            textId = itemView.findViewById(R.id.userId);
            imageView = itemView.findViewById(R.id.imageView);

            Button updateButton = itemView.findViewById(R.id.updateInfo);
            updateButton.setOnClickListener(v -> {
                Fragment_update_user updateUserFragment = new Fragment_update_user();
                Bundle bundle = new Bundle();
                bundle.putString("firstName", textFirstName.getText().toString());
                bundle.putString("lastName", textLastName.getText().toString());
                bundle.putString("email", textEmail.getText().toString());
                bundle.putString("id", textId.getText().toString());
                updateUserFragment.setArguments(bundle);

                FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, updateUserFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            });

            Button deleteButton = itemView.findViewById(R.id.deleteUser);
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = dataset.get(position);

                    new Thread(() -> {
                        DatabaseClient.getInstance(itemView.getContext())
                                .getAppDatabase()
                                .userDao()
                                .deleteUser(user);

                        ((AppCompatActivity) itemView.getContext()).runOnUiThread(() -> {
                            deleteUser(user);
                        });
                    }).start();
                }
            });
        }
    }

    public void deleteUser(User user) {
        int position = dataset.indexOf(user);
        if (position != -1) {
            dataset.remove(position);
            datasetFull.remove(user);
            notifyItemRemoved(position);
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(datasetFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (User item : datasetFull) {
                        if (item.getFirstName().toLowerCase().contains(filterPattern) ||
                                item.getLastName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataset.clear();
                dataset.addAll((List<User>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<User> newData) {
        this.dataset.clear();
        this.dataset.addAll(newData);
        this.datasetFull.clear();
        this.datasetFull.addAll(newData);
        notifyDataSetChanged();
    }
}
