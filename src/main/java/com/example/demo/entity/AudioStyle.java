package com.example.demo.entity;

public class AudioStyle {
    private Integer ausId;

    private String ausName;

    private Integer ausUsedCount;

    private Integer ausEstimatedTime;

    private Integer ausAortPriority;

    private String ausShowPath;

    private String ausParameterValues;

    private String ausDescription;

    public Integer getAusId() {
        return ausId;
    }

    public void setAusId(Integer ausId) {
        this.ausId = ausId;
    }

    public String getAusName() {
        return ausName;
    }

    public void setAusName(String ausName) {
        this.ausName = ausName == null ? null : ausName.trim();
    }

    public Integer getAusUsedCount() {
        return ausUsedCount;
    }

    public void setAusUsedCount(Integer ausUsedCount) {
        this.ausUsedCount = ausUsedCount;
    }

    public Integer getAusEstimatedTime() {
        return ausEstimatedTime;
    }

    public void setAusEstimatedTime(Integer ausEstimatedTime) {
        this.ausEstimatedTime = ausEstimatedTime;
    }

    public Integer getAusAortPriority() {
        return ausAortPriority;
    }

    public void setAusAortPriority(Integer ausAortPriority) {
        this.ausAortPriority = ausAortPriority;
    }

    public String getAusShowPath() {
        return ausShowPath;
    }

    public void setAusShowPath(String ausShowPath) {
        this.ausShowPath = ausShowPath == null ? null : ausShowPath.trim();
    }

    public String getAusParameterValues() {
        return ausParameterValues;
    }

    public void setAusParameterValues(String ausParameterValues) {
        this.ausParameterValues = ausParameterValues == null ? null : ausParameterValues.trim();
    }

    public String getAusDescription() {
        return ausDescription;
    }

    public void setAusDescription(String ausDescription) {
        this.ausDescription = ausDescription == null ? null : ausDescription.trim();
    }
}