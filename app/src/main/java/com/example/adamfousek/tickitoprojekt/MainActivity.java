package com.example.adamfousek.tickitoprojekt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;


import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    Button buttonLogin;
    EditText loginText;
    EditText passwordText;
    TextView wrong;

    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.tickito.cz/")
            .addConverterFactory(GsonConverterFactory.create());

    private final Retrofit retrofit = builder.build();

    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toast.makeText(getApplicationContext(), "toast", Toast.LENGTH_SHORT).show();

        wrong = (TextView) findViewById(R.id.wrongLogin);

        loginText = (EditText) findViewById(R.id.editTextLogin);
        loginText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        passwordText = (EditText) findViewById(R.id.editTextPassword);
        passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        buttonLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_selector_pressed, null));
                        wrong.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        buttonLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_selector, null));
                        if(loginText.getText().toString().isEmpty() || passwordText.getText().toString().isEmpty()){
                            wrong.setText("Vyplňte prosím pole");
                        }else{
                            UserLoginTask mAuthTask = new UserLoginTask(loginText.getText().toString(), passwordText.getText().toString());
                            mAuthTask.execute((Void) null);
                        }
                        break;
                    }
                }

                return true;
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;
        private Boolean logedIn = false;

        UserLoginTask(String name, String password){
            this.name = name;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            UserClient userClient = retrofit.create(UserClient.class);

            String base = name + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            Call<User> call = userClient.getUser(authHeader);

            try {
                Response<User> response = call.execute();
                user = response.body();
                if(!user.getEvents().isEmpty()){
                    logedIn = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return logedIn;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            //mAuthTask = null;

            if(success){
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ListOfEventsActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
                //Correct data
            }else{
                //Toast.makeText(getApplicationContext(), "NO Success", Toast.LENGTH_SHORT).show();
                wrong.setText("Špatné jméno nebo heslo");
                //Incorrect data
            }

        }

        @Override
        protected void  onCancelled() {
            //mAuthTask = null;
        }

    }
}