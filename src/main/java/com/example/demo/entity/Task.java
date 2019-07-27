package com.example.demo.entity;

import java.util.Date;

public class Task {
    private Integer taskId;

    private Integer userId;

    private Integer fileId;

    private Integer imsId;

    private Integer ausId;

    private Integer clarity;

    private Boolean isFrameSpeed;

    private Date createTime;

    private Integer estimateTime;

    private Date startTime;

    private Date finishTime;

    private String taskType;

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getImsId() {
        return imsId;
    }

    public void setImsId(Integer imsId) {
        this.imsId = imsId;
    }

    public Integer getAusId() {
        return ausId;
    }

    public void setAusId(Integer ausId) {
        this.ausId = ausId;
    }

    public Integer getClarity() {
        return clarity;
    }

    public void setClarity(Integer clarity) {
        this.clarity = clarity;
    }

    public Boolean getIsFrameSpeed() {
        return isFrameSpeed;
    }

    public void setIsFrameSpeed(Boolean isFrameSpeed) {
        this.isFrameSpeed = isFrameSpeed;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getEstimateTime() {
        return estimateTime;
    }

    public void setEstimateTime(Integer estimateTime) {
        this.estimateTime = estimateTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType == null ? null : taskType.trim();
    }
}