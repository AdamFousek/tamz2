package com.example.adamfousek.tickitoprojekt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
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

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    // Komponenty layoutu
    private Button buttonLogin;
    private EditText loginText;
    private EditText passwordText;
    private TextView wrong;

    // Informace o uživateli
    private User user = new User();

    // SharedPreferences uložení dat o uživateli
    private SharedPreferences mySharedPref;
    private SharedPreferences.Editor mySharedEditor;

    // Retrofit cool knihovna na api
    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.tickito.cz/")
            .addConverterFactory(GsonConverterFactory.create());
    private final Retrofit retrofit = builder.build();

    Handler mHandler = new Handler();
    boolean isRunning = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreference - po prvním přihlášení se nastaví cache na 1 den
        mySharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String name = mySharedPref.getString("name", "");
        String password = mySharedPref.getString("password", "");
        long timestamp = mySharedPref.getLong("timestamp", 1);
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        try {
            password = AESCrypt.decrypt(password);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(timestamp > currentTimestamp){
            if(!name.isEmpty() && !password.isEmpty()) {
                UserLoginTask mAuthTask = new UserLoginTask(name, password);
                mAuthTask.execute((Void) null);
            }
        }
        // Zjištění componentů + jejích eventy
        wrong = (TextView) findViewById(R.id.wrongLogin);

        // Kontrola připojení
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRunning) {
                    try {
                        Thread.sleep(5000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                // Write your code here to update the UI.
                                displayData();
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();

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

        // Zjištění přihlašovácího buttonu, jeho touchlistener
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        // Effekt tlačítka :)
                        buttonLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_selector_pressed, null));
                        wrong.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        buttonLogin.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_selector, null));
                        // Kontrola jestli uživatel vyplnil text pokud ano, udělá se AsyncTask
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

    /**
     * Schování klávesnice když kliknu mimo text componenty
     * @param view
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Kontrola připojení
    private void displayData() {
        ConnectivityManager cn=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf=cn.getActiveNetworkInfo();
        if(nf != null && nf.isConnected()==true )
        {
            wrong.setText("");
        }
        else
        {
            wrong.setText("Zkontrolujte prosím připojení k internetu");
        }
    }

    /**
     * Classa na AsyncTask - kontrola údajů api
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;
        private Boolean logedIn = false;

        UserLoginTask(String name, String password){
            this.name = name;
            this.password = password;
        }

        // Získání údajů z API
        @Override
        protected Boolean doInBackground(Void... voids) {
            UserClient userClient = retrofit.create(UserClient.class);

            String base = name + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            Call<User> call = userClient.getUser(authHeader);

            try {
                Response<User> response = call.execute();
                if(response.isSuccessful()){
                    user = response.body();
                    user.setName(name);
                    // Ukládání údajů do sharedPreferences - na 1 den
                    mySharedEditor = mySharedPref.edit();
                    mySharedEditor.putString("name", name);
                    mySharedEditor.putString("password", AESCrypt.encrypt(password));
                    mySharedEditor.putLong("timestamp", (System.currentTimeMillis() / 1000L)*24*60*60);
                    mySharedEditor.apply();
                    logedIn = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return logedIn;
        }

        @Override
        protected void onPostExecute(final Boolean success){

            if(success){
                // Uživateli zobrazí Úspěch přepne do nové aktivity s User objektem a aktuální aktivitu ukončí
                Toast.makeText(getApplicationContext(), "Přihlášení úspěšné", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ListOfEventsActivity.class);
                intent.putExtra("User", user);
                startActivity(intent);
                finish();
            }else{
                // Špatné jméno nebo heslo oznámí uživateli v TextView
                wrong.setText("Špatné jméno nebo heslo");
            }

        }

        @Override
        protected void  onCancelled() {
        }

    }
}