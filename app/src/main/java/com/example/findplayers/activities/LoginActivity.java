package com.example.findplayers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findplayers.R;
import com.example.findplayers.classes.Player;
import com.example.findplayers.fragments.FragmentSignIn;
import com.example.findplayers.fragments.FragmentSignUp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    //sign in fragment
    TextInputLayout emailText;
    TextInputLayout passwordText;
    MaterialButton buttonSignIn;

    //sign up fragment
    private int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //fragment manager
        fragmentManager = getSupportFragmentManager();
        FragmentSignIn fragmentSignIn = new FragmentSignIn();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentCon,fragmentSignIn).commit();

        //firebase
        mAuth = FirebaseAuth.getInstance();
    }



    //transfer to sign up frag
    public void loadSignUpFrag(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentCon,new FragmentSignUp()).addToBackStack(null).commit();
    }

    //auth to sign in with user,pass
    public void signInFunc(View view) {
        buttonSignIn = findViewById(R.id.sign_in_btn);
        emailText = findViewById(R.id.email_et_sign_in);
        passwordText = findViewById(R.id.password_et_sign_in);

        String email = emailText.getEditText().getText().toString();
        String password = passwordText.getEditText().getText().toString();

        //progress dialog
        ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this,"Signing in","Please wait..");


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //shared prefernces, put user + key
                            sharedPreferences = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();//create an editor to edit him
                            editor.putString("keyUser", email);//add email
                            editor.putString("keyPass", password);//edd pass
                            editor.apply();
                            //transfer to main map activity
                            Intent intent = new Intent(LoginActivity.this, MainMapActivity.class);
                            startActivity(intent);
                            //stop progress dialog
                            progressDialog.dismiss();
                            //show success dilaog
                            android.app.AlertDialog alertDialog;
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            LayoutInflater inflater = LoginActivity.this.getLayoutInflater();
                            View view = inflater.inflate(R.layout.succeed_dialog, null);
                            TextView succeedBodyTv = view.findViewById(R.id.succeed_body_tv);
                            succeedBodyTv.setText("You Logged In Successfully");
                            alertDialog = builder.setView(view).show();
                            Button buttonOk = view.findViewById(R.id.succeed_dialog_close_btn);
                            buttonOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            android.app.AlertDialog alertDialog;
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            LayoutInflater inflater = LoginActivity.this.getLayoutInflater();
                            View view = inflater.inflate(R.layout.fail_dialog, null);
                            TextView failBodyTv = view.findViewById(R.id.fail_body_tv);
                            failBodyTv.setText("Login Failed");
                            alertDialog = builder.setView(view).show();
                            Button buttonClose = view.findViewById(R.id.fail_dialog_close_btn);
                            buttonClose.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    //create user in database
    public void signUpFunc(View view) {
        TextInputLayout emailText = findViewById(R.id.email_et_sign_up);
        final String email = emailText.getEditText().getText().toString();

        TextInputLayout passwordText = findViewById(R.id.password_et_sign_up);
        String password = passwordText.getEditText().getText().toString();

        TextInputLayout fullnameText = findViewById(R.id.fullname_et_sign_up);
        final String fullname = fullnameText.getEditText().getText().toString();

        TextInputLayout phoneText = findViewById(R.id.phone_et_sign_up);
        final int phone = Integer.parseInt(phoneText.getEditText().getText().toString());

        TextInputLayout countryText = findViewById(R.id.country_et_sign_up);
        final String country = countryText.getEditText().getText().toString();



        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Register Succeed",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();//get the user that register
                            String uid = user.getUid();//get id of the user

                            // Write a message to the realtime database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();//ref to the firebase database
                            DatabaseReference myRef = database.getReference("players").child(uid);//ref to the user in database that created by his uid under persons, the uid is key that connect us to the user in db
                            Player p = new Player(email,fullname,age,phone,country);
                            myRef.setValue(p);//set values to the user that created in database
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragmentCon,new FragmentSignIn()).addToBackStack(null).commit();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Register failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //pick date of birth in sign up frag
    public void pickDateOfBirth(View view) {
        MaterialButton dateBtn = (MaterialButton)findViewById(R.id.date_btn_sign_up);
        Calendar calendar = Calendar.getInstance(); //today's date
        int yearToday = calendar.get(Calendar.YEAR);
        int monthToday = calendar.get(Calendar.MONTH);
        int dayToday = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(LoginActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateBtn.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                age = yearToday - year;
            }
        }, yearToday, monthToday, dayToday);

        dpd.show(); //shows dialog
    }
}