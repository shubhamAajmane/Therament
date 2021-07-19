package com.opd.therament.datamodels;

import org.jetbrains.annotations.NotNull;

public class TimeSlotDataModel {

    String id,time,status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NotNull
    @Override
    public String toString() {
        return time;
    }
}
