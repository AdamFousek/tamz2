package com.example.adamfousek.tickitoprojekt;

import java.util.Date;

/**
 * Created by adamfousek on 28.11.17.
 * Třída pro daný kód
 */

public class Code {

    private String code;
    private Date used;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getUsed() {
        return used;
    }

    public void setUsed(Date used) {
        this.used = used;
    }
}
