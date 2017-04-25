package ru.metrikawidget.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ByTimeResponse {

    @SerializedName("total_rows")
    private Long totalRows;
    private Boolean sampled;
    @SerializedName("sample_share")
    private Double sampleShare;
    @SerializedName("max_sample_share")
    private Double maxSampleShare;
    @SerializedName("sample_size")
    private Long sampleSize;
    @SerializedName("sample_space")
    private Long sampleSpace;
    @SerializedName("data_lag")
    private Integer dataLag;
    private List<ByTimeData> data;
    private List<List<Double>> totals;
    @SerializedName("time_intervals")
    private List<List<String>> timeIntervals;

    public Long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }

    public Boolean getSampled() {
        return sampled;
    }

    public void setSampled(Boolean sampled) {
        this.sampled = sampled;
    }

    public Double getSampleShare() {
        return sampleShare;
    }

    public void setSampleShare(Double sampleShare) {
        this.sampleShare = sampleShare;
    }

    public Double getMaxSampleShare() {
        return maxSampleShare;
    }

    public void setMaxSampleShare(Double maxSampleShare) {
        this.maxSampleShare = maxSampleShare;
    }

    public Long getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(Long sampleSize) {
        this.sampleSize = sampleSize;
    }

    public Long getSampleSpace() {
        return sampleSpace;
    }

    public void setSampleSpace(Long sampleSpace) {
        this.sampleSpace = sampleSpace;
    }

    public Integer getDataLag() {
        return dataLag;
    }

    public void setDataLag(Integer dataLag) {
        this.dataLag = dataLag;
    }

    public List<ByTimeData> getData() {
        return data;
    }

    public void setData(List<ByTimeData> data) {
        this.data = data;
    }

    public List<List<Double>> getTotals() {
        return totals;
    }

    public void setTotals(List<List<Double>> totals) {
        this.totals = totals;
    }

    public List<List<String>> getTimeIntervals() {
        return timeIntervals;
    }

    public void setTimeIntervals(List<List<String>> timeIntervals) {
        this.timeIntervals = timeIntervals;
    }

}
