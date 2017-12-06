package com.example.adamfousek.tickitoprojekt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListOfEventsActivity extends AppCompatActivity {

    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://www.tickito.cz/")
            .addConverterFactory(GsonConverterFactory.create());

    private final Retrofit retrofit = builder.build();

    private User user = new User();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_events);

        Intent intent = getIntent();
        user = (User)intent.getSerializableExtra("User");


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
}
