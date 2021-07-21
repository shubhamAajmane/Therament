package com.opd.therament.datamodels;

import org.jetbrains.annotations.NotNull;

public class DateDataModel {

    String id,date,total;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    @NotNull
    @Override
    public String toString() {
        return date;
    }
}
