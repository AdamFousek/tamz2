package com.example.adamfousek.tickitoprojekt;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by adamfousek on 28.11.17.
 */

public class User implements Serializable {

    @SerializedName("events")
    @Expose
    private ArrayList<Event> events = new ArrayList<Event>();

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
