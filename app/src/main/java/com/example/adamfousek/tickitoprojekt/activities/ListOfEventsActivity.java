package com.example.adamfousek.tickitoprojekt.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.adamfousek.tickitoprojekt.AESCrypt;
import com.example.adamfousek.tickitoprojekt.models.ApiClient;
import com.example.adamfousek.tickitoprojekt.models.Code;
import com.example.adamfousek.tickitoprojekt.models.Codes;
import com.example.adamfousek.tickitoprojekt.models.Event;
import com.example.adamfousek.tickitoprojekt.EventAdapter;
import com.example.adamfousek.tickitoprojekt.R;
import com.example.adamfousek.tickitoprojekt.models.User;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListOfEventsActivity extends AppCompatActivity {

    // Retrofit
    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.tickito.cz/")
            .addConverterFactory(GsonConverterFactory.create());
    private final Retrofit retrofit = builder.build();

    // SharedPreferences
    private SharedPreferences mySharedPref;
    private SharedPreferences.Editor mySharedEditor;

    // Informace o uživateli
    private User user = new User();
    private Codes codes = new Codes();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_events);

        // Ziskání SharedPreferences
        mySharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);

        // Získání informací o uživateli
        Intent intent = getIntent();
        user = (User)intent.getSerializableExtra("User");

        /*CodesTask mAuthTask = new CodesTask();
        mAuthTask.execute((Void) null);*/

        // Vyplnění layoutu akcema
        // @TODO jestli není žádná vypsat text!!!
        EventAdapter adapter = new EventAdapter(ListOfEventsActivity.this,R.layout.list_event_layout, user.getEvents());
        ListView lv = (ListView)findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event event = (Event) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(view.getContext(), BarcodeReaderActivity.class);
                startActivity(intent);
            }
        });
    }

    // Přídání menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.settings){
            return true;
        }
        if(id == R.id.logout){
            // Při odhlášení smažeme SharedPreferences aby se uživatel znovu nepřihlásil
            mySharedEditor = mySharedPref.edit();
            mySharedEditor.putString("name", "");
            mySharedEditor.putString("password", "");
            mySharedEditor.putLong("timestamp", (System.currentTimeMillis() / 1000L));
            mySharedEditor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Classa na AsyncTask - kontrola údajů api
     */
    public class CodesTask extends AsyncTask<Void, Void, Boolean> {

        private final String name;
        private final String password;
        private Boolean logedIn = false;

        CodesTask(){
            mySharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
            this.name = mySharedPref.getString("name", "");
            String pass = mySharedPref.getString("password", "");
            try {
                pass = AESCrypt.decrypt(pass);
            } catch (Exception e){
                e.printStackTrace();
            }
            this.password = pass;
        }

        // Získání údajů z API
        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiClient userClient = retrofit.create(ApiClient.class);

            String base = name + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            Call<Codes> call = userClient.getCodes(authHeader, "6");

            try {
                Response<Codes> response = call.execute();
                Log.d("BYLOUSPESNE?", "BYLONEBONE: "+response.isSuccessful());
                if(response.isSuccessful()){
                    codes = response.body();
                    // Ukládání údajů do sharedPreferences - na 1 den
                    for (String c : codes.getCodes()){
                        Log.d("TUTO", "TU: "+c);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return logedIn;
        }

        @Override
        protected void onPostExecute(final Boolean success){

            if(success){

            }else{
                // Nemělo by k tomuto dojít
            }

        }

        @Override
        protected void  onCancelled() {
        }

    }
}
