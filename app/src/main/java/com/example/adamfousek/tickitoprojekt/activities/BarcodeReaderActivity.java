package com.example.adamfousek.tickitoprojekt.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.example.adamfousek.tickitoprojekt.AESCrypt;
import com.example.adamfousek.tickitoprojekt.R;
import com.example.adamfousek.tickitoprojekt.models.ApiClient;
import com.example.adamfousek.tickitoprojekt.models.Code;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

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

    // SharedPreferences
    private SharedPreferences mySharedPref;
    private SharedPreferences.Editor mySharedEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nastavení layoutu pro kameru
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    // @TODO Naskenovaný kód se uloží lokálně a odešle se na api a zjistí jestli již byl použitý
    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();
        CheckCodeTask mAuthTask = new CheckCodeTask(myResult);
        mAuthTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Menu pro rozsvicení světla a zadání kódu ručně
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        scannerView.stopCameraPreview();
        int id = item.getItemId();
        if(id == R.id.flashlight){
            // Rozsvícení světla
            // @TODO Je třeba zapracovat na fixu kdy to nefunguje hezky - když se autofocusuje nejde zapnout světlo
            scannerView.resumeCameraPreview(this);
            scannerView.toggleFlash();
            return true;
        }
        if(id == R.id.manulaScan){
            // Zobrazí se Dialog na zadání kódu ručně
            final EditText input = new EditText(this);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Title");

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
            scannerView.resumeCameraPreview(this);
        }
        scannerView.resumeCameraPreview(this);
        return super.onOptionsItemSelected(item);

    }

    /**
     * Classa na AsyncTask - kontrola údajů api
     */
    public class CheckCodeTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;
        private final String codeS;

        CheckCodeTask(String code){
            mySharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
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
            Call<Code> call = userClient.checkCode(authHeader, codeS);

            try {
                Response<Code> response = call.execute();
                Log.d("BYLOUSPESNE?", "BYLONEBONE: "+response.isSuccessful());
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

            if(success){
                // Vypsání kódu a jestli byl použitý nebo ne
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeReaderActivity.this);
                if(code.getUsed() == null){
                    builder.setTitle("Kod přijat");
                    builder.setMessage(codeS + " uplatněn!");
                }else {
                    builder.setTitle("Kod nebyl přijat");
                    builder.setMessage("Kód již byl použit "+ DateFormat.format("yyyy-MM-dd hh:mm:ss", code.getUsed()));
                }

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
                    }
                });

                AlertDialog alert1 = builder.create();
                alert1.show();

            }else{
                // Nemělo by nastat
            }

        }

        @Override
        protected void  onCancelled() {
        }

    }

}
