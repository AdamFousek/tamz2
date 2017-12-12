package com.example.adamfousek.tickitoprojekt.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by adamfousek on 09.12.17.
 */

public class Codes {

    @SerializedName("codes")
    private Map<String, Date> codes;
    private int eventId;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Map<String, Date> getCodes() {
        return codes;
    }

    public void setCodes(Map<String, Date> codes) {
        this.codes = codes;
    }
}
