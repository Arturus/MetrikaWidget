package ru.metrikawidget.api;

import java.util.List;

public class ByTimeData {

    private List<Dimension>    dimensions;
    private List<List<Double>> metrics;

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<List<Double>> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<List<Double>> metrics) {
        this.metrics = metrics;
    }

}
