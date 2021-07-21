package com.opd.therament.datamodels;

import org.jetbrains.annotations.NotNull;

public class TimeSlotDataModel {

    String id, timeSlot, status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String time) {
        this.timeSlot = time;
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
        return timeSlot;
    }
}
