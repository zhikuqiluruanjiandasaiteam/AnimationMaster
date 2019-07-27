package com.example.demo.entity;

public class ImageStyle {
    private Integer imsId;

    private String imsName;

    private Integer imsUsedCount;

    private Integer imsEstimatedTime;

    private String imsDescription;

    public Integer getImsId() {
        return imsId;
    }

    public void setImsId(Integer imsId) {
        this.imsId = imsId;
    }

    public String getImsName() {
        return imsName;
    }

    public void setImsName(String imsName) {
        this.imsName = imsName == null ? null : imsName.trim();
    }

    public Integer getImsUsedCount() {
        return imsUsedCount;
    }

    public void setImsUsedCount(Integer imsUsedCount) {
        this.imsUsedCount = imsUsedCount;
    }

    public Integer getImsEstimatedTime() {
        return imsEstimatedTime;
    }

    public void setImsEstimatedTime(Integer imsEstimatedTime) {
        this.imsEstimatedTime = imsEstimatedTime;
    }

    public String getImsDescription() {
        return imsDescription;
    }

    public void setImsDescription(String imsDescription) {
        this.imsDescription = imsDescription == null ? null : imsDescription.trim();
    }
}