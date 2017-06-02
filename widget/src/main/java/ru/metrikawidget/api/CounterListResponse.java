package ru.metrikawidget.api;

import java.util.ArrayList;

public class CounterListResponse {

    private int rows;
    private ArrayList<Counter> counters;

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public ArrayList<Counter> getCounters() {
        return counters;
    }

    public void setCounters(ArrayList<Counter> counters) {
        this.counters = counters;
    }
}
