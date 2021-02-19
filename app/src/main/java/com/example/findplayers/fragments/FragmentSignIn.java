package com.example.findplayers.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findplayers.R;
import com.google.android.material.textfield.TextInputLayout;

import static android.content.Context.MODE_PRIVATE;

public class FragmentSignIn extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private SharedPreferences sharedPreferences;

    public FragmentSignIn() {

    }

    public static FragmentSignIn newInstance(String param1, String param2) {
        FragmentSignIn fragment = new FragmentSignIn();
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
        View view = inflater.inflate(R.layout.sign_in_fragment, container, false);
        
        //shared prefernces,check for user logged in
        sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);//create shared prefernces file(like cookies/localStorge),all the activities know sharedPrefernces
        if (sharedPreferences.getString("keyUser", null) != null) {
            //initialize sign in text input layouts for shared pref
            TextInputLayout emailText = view.findViewById(R.id.email_et_sign_in);
            TextInputLayout passwordText = view.findViewById(R.id.password_et_sign_in);

            emailText.getEditText().setText(sharedPreferences.getString("keyUser", null));//edit email text to email that saved in SP
            passwordText.getEditText().setText(sharedPreferences.getString("keyPass", null));//edit pass text to pass that saved in S

        }
        return view;
    }
}