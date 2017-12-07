package com.example.adamfousek.tickitoprojekt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_events);

        // Ziskání SharedPreferences
        mySharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);

        // Získání informací o uživateli
        Intent intent = getIntent();
        user = (User)intent.getSerializableExtra("User");

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
}
