package ru.metrikawidget.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Counter {

    private int id;
    private String status;
    @SerializedName("owner_login")
    private String ownerLogin;
    @SerializedName("code_status")
    private String codeStatus;
    private String name;
    private String site;
    private String type; //
    private int favorite; // 0 or 1
    private String permission;
    private List<String> mirrors;
    private String createTime;
    private String code;
    @SerializedName("filter_robots")
    private Integer filterRobots;
    @SerializedName("time_zone_name")
    private String timeZoneName;
    @SerializedName("visit_threshold")
    private Integer visitTreshold;
    @SerializedName("update_time")
    private String updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getCodeStatus() {
        return codeStatus;
    }

    public void setCodeStatus(String codeStatus) {
        this.codeStatus = codeStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getMirrors() {
        return mirrors;
    }

    public void setMirrors(List<String> mirrors) {
        this.mirrors = mirrors;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getFilterRobots() {
        return filterRobots;
    }

    public void setFilterRobots(Integer filterRobots) {
        this.filterRobots = filterRobots;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }

    public Integer getVisitTreshold() {
        return visitTreshold;
    }

    public void setVisitTreshold(Integer visitTreshold) {
        this.visitTreshold = visitTreshold;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
