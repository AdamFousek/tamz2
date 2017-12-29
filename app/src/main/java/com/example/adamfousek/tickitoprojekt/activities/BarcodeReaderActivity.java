package com.example.adamfousek.tickitoprojekt.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import com.example.adamfousek.tickitoprojekt.AESCrypt;
import com.example.adamfousek.tickitoprojekt.CustomCameraSettings;
import com.example.adamfousek.tickitoprojekt.R;
import com.example.adamfousek.tickitoprojekt.models.ApiClient;
import com.example.adamfousek.tickitoprojekt.models.Code;
import com.example.adamfousek.tickitoprojekt.models.Tickets;
import com.example.adamfousek.tickitoprojekt.models.Event;
import com.example.adamfousek.tickitoprojekt.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BarcodeReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    // Retrofit
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.tickito.cz/")
            .addConverterFactory(GsonConverterFactory.create(gson));
    private final Retrofit retrofit = builder.build();

    // ScannerView - pracuje s kamerou
    private ZXingScannerView scannerView;
    private String codeString = "";
    private Code code = new Code();
    private Tickets tickets;
    private ArrayList<String> usedTickets= new ArrayList<>();
    private Event event;
    private User user;

    // SharedPreferences
    private SharedPreferences mySharedPref;
    private SharedPreferences.Editor mySharedEditor;

    private boolean activeBR = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        event = (Event)intent.getSerializableExtra("Event");
        user = (User)intent.getSerializableExtra("User");

        // Nastavení layoutu pro kameru
        scannerView = new ZXingScannerView(this) {

            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomCameraSettings(context);
            }

        };
        setContentView(scannerView);
        scannerView.setResultHandler(this);
        scannerView.startCamera();
        activeBR = true;
        checkConnection();

        syncCodes();
    }

    @Override
    public void onStart() {
        super.onStart();
        activeBR = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        activeBR = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activeBR = false;
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeBR = true;
        scannerView.startCamera();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        activeBR = false;
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();
        if(isInternet()){
            CheckCodeTask mAuthTask = new CheckCodeTask(myResult);
            mAuthTask.execute();
        } else {
            if(tickets != null) {
                if (tickets.getCodes().containsKey(myResult)) {
                    AlertDialog.Builder builder;
                    if (tickets.getCodes().get(myResult) == null) {
                        builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.SuccessDialogTheme);
                        builder.setTitle("Kód přijat");
                        Code tmp = tickets.getCodes().get(myResult);
                        tmp.setUsed(new Date());
                        tickets.getCodes().put(myResult, tmp);
                        usedTickets.add(myResult);
                    } else {
                        builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.FailDialogTheme);
                        builder.setTitle("Kód nebyl přijat");
                        builder.setMessage("Kód již byl použit " + DateFormat.format("HH:mm:ss dd.MM.yyyy", tickets.getCodes().get(myResult).getUsed()));
                    }
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                        }
                    });

                    AlertDialog alertCode = builder.create();
                    alertCode.setCancelable(false);
                    alertCode.setCanceledOnTouchOutside(false);
                    alertCode.show();
                } else {
                    // Když kód k dané akci neexistuje
                    AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.FailDialogTheme);

                    builder.setTitle("Kód nebyl přijat");
                    builder.setMessage("Kód pro danou akci neexistuje");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                        }
                    });

                    AlertDialog alertCode = builder.create();
                    alertCode.setCancelable(false);
                    alertCode.setCanceledOnTouchOutside(false);
                    alertCode.show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.FailDialogTheme);

                builder.setTitle("Vstupenky nebyly staženy");
                builder.setMessage("Prosím, obnovte internetové spojení pro stažení lístků!");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                    }
                });

                AlertDialog alertCode = builder.create();
                alertCode.setCancelable(false);
                alertCode.setCanceledOnTouchOutside(false);
                alertCode.show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Menu pro rozsvicení světla a zadání kódu ručně
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.flashlight){
            // Rozsvícení světla
            scannerView.stopCameraPreview();
            scannerView.toggleFlash();
            if(scannerView.getFlash()){
                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.flashlight_on, null));
            } else {
                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.flashlight, null));
            }
            scannerView.resumeCameraPreview(this);
            return true;
        }
        if(id == R.id.manulaScan){
            // Zobrazí se Dialog na zadání kódu ručně
            final EditText input = new EditText(this);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Zadejte kód");

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    codeString = input.getText().toString();
                    CheckCodeTask mAuthTask = new CheckCodeTask(codeString);
                    mAuthTask.execute((Void) null);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);

    }

    private void syncCodes() {
        final Handler handler = new Handler();
        final Timer timer = new Timer();


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(!activeBR){
                            timer.cancel();
                            timer.purge();
                        }
                        if (isInternet()) {
                            TicketsSyncTask ticketsSync = new TicketsSyncTask();
                            ticketsSync.execute();
                        } else {
                            Toast.makeText(getApplicationContext(), "Zkontrolujte připojení k internetu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 60*1000);  // interval of one minute

        if(!activeBR){
            timer.cancel();
        }
    }

    /**
     * Kontrola připojení.
     */
    private void checkConnection() {
        final Handler handler = new Handler();
        final Timer timer = new Timer();


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(!activeBR){
                            timer.cancel();
                            timer.purge();
                        }
                        if (isInternet()) {

                        } else {
                            Toast.makeText(getApplicationContext(), "Zkontrolujte připojení k internetu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 5000);  // interval of one minute

        if(!activeBR){
            timer.cancel();
        }
    }

    /**
     * Je k dispozici internet
     * @return boolean
     */
    private boolean isInternet(){
        ConnectivityManager cn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf = cn.getActiveNetworkInfo();
        return nf != null && nf.isConnected() == true;
    }

    /**
     * Classa na AsyncTask - kontrola lístků
     */
    public class CheckCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;
        private final String codeS;

        CheckCodeTask(String code){
            mySharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            this.name = mySharedPref.getString("name", "");
            String pass = mySharedPref.getString("password", "");
            try {
                pass = AESCrypt.decrypt(pass);
            } catch (Exception e){
                e.printStackTrace();
            }
            this.password = pass;
            this.codeS=code;
        }

        // Získání údajů z API
        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiClient userClient = retrofit.create(ApiClient.class);

            String base = name + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            Call<Code> call = userClient.checkCode(authHeader, event.getId(), codeS);

            try {
                Response<Code> response = call.execute();
                if(response.isSuccessful()){
                    code = response.body();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            scannerView.stopCameraPreview();
            if(success){
                // Vypsání kódu a jestli byl použitý nebo ne
                AlertDialog.Builder builder;
                if(code.getUsed() == null){
                    builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.SuccessDialogTheme);
                    builder.setTitle("Kód přijat");
                    builder.setMessage(code.getLine_first() + "\n" + code.getLine_second());
                    Code tmp = tickets.getCodes().get(code.getCode());
                    tmp.setUsed(new Date());
                    tickets.getCodes().put(code.getCode(), tmp);
                }else {
                    builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.FailDialogTheme);
                    builder.setTitle("Kód nebyl přijat");
                    builder.setMessage("Kód již byl použit "+ DateFormat.format("HH:mm:ss dd.MM.yyyy", code.getUsed()));
                }
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                    }
                });

                AlertDialog alertCode = builder.create();
                alertCode.setCancelable(false);
                alertCode.setCanceledOnTouchOutside(false);
                alertCode.show();

            }else{
                // Když kód k dané akci neexistuje
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeReaderActivity.this, R.style.FailDialogTheme);

                builder.setTitle("Kód nebyl přijat");
                builder.setMessage("Kód pro danou akci neexistuje");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                    }
                });

                AlertDialog alertCode = builder.create();
                alertCode.setCancelable(false);
                alertCode.setCanceledOnTouchOutside(false);
                alertCode.show();
            }

        }

        @Override
        protected void  onCancelled() {
        }

    }

    /**
     * Classa na AsyncTask - Code
     */
    public class TicketsSyncTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;

        TicketsSyncTask(){
            this.password = user.getPassword();
            this.name = user.getName();
        }

        // Získání údajů z API
        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiClient userClient = retrofit.create(ApiClient.class);

            String base = name + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

            for(String s : usedTickets){
                Call<Code> call = userClient.checkCode(authHeader, event.getId(), s);

                try {
                    Response<Code> response = call.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            usedTickets.clear();

            Call<Tickets> call = userClient.getCodes(authHeader, event.getId());

            try {
                Response<Tickets> response = call.execute();
                if(response.isSuccessful()){
                    tickets = response.body();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            if(success){
                // Uložení proběhlo v pořádku
            }else{
                // Nějaká chyba
            }

        }

        @Override
        protected void  onCancelled() {
        }

    }


}
