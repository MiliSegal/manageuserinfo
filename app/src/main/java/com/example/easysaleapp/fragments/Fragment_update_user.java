package com.example.easysaleapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.easysaleapp.Database.DatabaseClient;
import com.example.easysaleapp.R;
import com.example.easysaleapp.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_update_user#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_update_user extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextEmail;
    private EditText editTextId;

    public Fragment_update_user() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_update_user.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_update_user newInstance(String param1, String param2) {
        Fragment_update_user fragment = new Fragment_update_user();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_user, container, false);

        editTextFirstName = view.findViewById(R.id.firstName);
        editTextLastName = view.findViewById(R.id.lastName);
        editTextEmail = view.findViewById(R.id.email);
        editTextId = view.findViewById(R.id.id);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        if (getArguments() != null) {
            editTextFirstName.setText(getArguments().getString("firstName"));
            editTextLastName.setText(getArguments().getString("lastName"));
            editTextEmail.setText(getArguments().getString("email"));
            editTextId.setText(getArguments().getString("id"));
        }

        buttonSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void saveUserData() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String idText = editTextId.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || idText.isEmpty()) {
            Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = Integer.parseInt(idText);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFirstName(firstName);
        updatedUser.setLastName(lastName);
        updatedUser.setEmail(email);

        new Thread(() -> {
            DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().updateUser(updatedUser);

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }
}