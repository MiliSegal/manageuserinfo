package com.example.easysaleapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easysaleapp.R;
import com.example.easysaleapp.adapter.UserAdapter;
import com.example.easysaleapp.models.User;
import com.example.easysaleapp.models.UserResponse;
import com.example.easysaleapp.service.ApiService;
import com.example.easysaleapp.Database.DatabaseClient;
import com.example.easysaleapp.service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    private SearchView searchView;


    public UserListFragment() {

    }

    public static UserListFragment newInstance(String param1, String param2) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = view.findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userAdapter = new UserAdapter(new ArrayList<>());
        recyclerView.setAdapter(userAdapter);

        searchView = view.findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                // Handle search submission (if needed)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes in the search view
                filter(newText);
                return true;
            }
        });

        fetchUsersFromApi();

        Button buttonShowPopup = view.findViewById(R.id.AddUser);
        buttonShowPopup.setOnClickListener(v -> showPopup(v));

        return view;
    }

    private void filter(String query) {
        userAdapter.getFilter().filter(query);
    }

    private void fetchUsersFromApi() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.getUsers(2);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    if (users != null && !users.isEmpty()) {
                        userAdapter.updateData(users);
                        saveUsersToDatabase(users);
                        loadUsersFromDatabase();
                    } else {
                        Toast.makeText(getActivity(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to load users from API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUsersToDatabase(List<User> users) {
        new Thread(() -> {
            DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().insertUsers(users);
        }).start();
    }

    private void loadUsersFromDatabase() {
        new Thread(() -> {
            List<User> users = DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().getAllUsers();
            getActivity().runOnUiThread(() -> userAdapter.updateData(users));
        }).start();
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            container = (View) popupWindow.getContentView().getParent();
        } else {
            container = popupWindow.getContentView();
        }
        if (popupWindow.getBackground() != null) {
            container = (View) container.getParent();
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    private void showPopup(View view) {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_user, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehind(popupWindow);

        EditText idText = popupView.findViewById(R.id.id);
        EditText firstNameText = popupView.findViewById(R.id.firstName);
        EditText lastNameText = popupView.findViewById(R.id.lastName);
        EditText emailText = popupView.findViewById(R.id.email);
        Button buttonAddImage = popupView.findViewById(R.id.buttonSelectAvatar);
        Button buttonSaveUser = popupView.findViewById(R.id.buttonSave);

        buttonAddImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
            pickPhoto.setType("image/*");
            startActivityForResult(pickPhoto, PICK_IMAGE);
        });

        buttonSaveUser.setOnClickListener(v -> {
            String id = idText.getText().toString().trim();
            String firstName = firstNameText.getText().toString().trim();
            String lastName = lastNameText.getText().toString().trim();
            String email = emailText.getText().toString().trim();

            if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User();
            newUser.setId(Integer.parseInt(id));
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setAvatar(selectedImageUri != null ? selectedImageUri.toString() : "");

            new Thread(() -> {
                DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().insertUser(newUser);

                List<User> updatedUserList = DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().getAllUsers();

                getActivity().runOnUiThread(() -> {
                    userAdapter.updateData(updatedUserList);
                    popupWindow.dismiss();
                });
            }).start();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ImageView avatarImageView = getView().findViewById(R.id.avatarImageView);
            if (selectedImageUri != null) {
                avatarImageView.setImageURI(selectedImageUri);
            }
        }
    }
}
