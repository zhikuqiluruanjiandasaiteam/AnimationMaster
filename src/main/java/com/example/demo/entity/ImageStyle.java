package com.example.demo.entity;

public class ImageStyle {
    private Integer imsId;

    private String imsName;

    private Integer imsUsedCount;

    private Integer imsEstimatedTime;

    private Integer imsAortPriority;

    private String imsShowPath;

    private String imsParameterValues;

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

    public Integer getImsAortPriority() {
        return imsAortPriority;
    }

    public void setImsAortPriority(Integer imsAortPriority) {
        this.imsAortPriority = imsAortPriority;
    }

    public String getImsShowPath() {
        return imsShowPath;
    }

    public void setImsShowPath(String imsShowPath) {
        this.imsShowPath = imsShowPath == null ? null : imsShowPath.trim();
    }

    public String getImsParameterValues() {
        return imsParameterValues;
    }

    public void setImsParameterValues(String imsParameterValues) {
        this.imsParameterValues = imsParameterValues == null ? null : imsParameterValues.trim();
    }

    public String getImsDescription() {
        return imsDescription;
    }

    public void setImsDescription(String imsDescription) {
        this.imsDescription = imsDescription == null ? null : imsDescription.trim();
    }
}