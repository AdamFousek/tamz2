package com.example.adamfousek.tickitoprojekt.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by adamfousek on 09.12.17.
 */

public class Codes {

    @SerializedName("codes")
    private ArrayList<String> codes = new ArrayList<String>();
    private int eventId;

    public ArrayList<String> getCodes() {
        return codes;
    }

    public void setCodes(ArrayList<String> codes) {
        this.codes = codes;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
