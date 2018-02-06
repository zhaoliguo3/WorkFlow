package com.xzl.cloud.workflow.dto;

import java.io.Serializable;
import java.util.Date;

//Task的dto
public class TaskDTO implements Serializable{
    private String id;
    private String processInsId; //流程实例id
    private String name;
    private Date startTime;
    private Date endTime;

    private  String key;
    private  String businessKey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getProcessInsId() {
        return processInsId;
    }

    public void setProcessInsId(String processInsId) {
        this.processInsId = processInsId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @param id
     * @param name
     * @param dates 下标0为startTime,1为endTime
     */
    public TaskDTO(String id, String name,String processInsId,String key,String businessKey,Date... dates) {
        this.id = id;
        this.name = name;
        this.processInsId = processInsId;
        this.key=key;
        this.businessKey=businessKey;
        if (dates!=null){
            switch (dates.length){
                case 1:
                    startTime = dates[0];
                    break;
                case 2:
                    startTime = dates[0];
                    endTime = dates[1];
                    break;
                default:
                    break;
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "id='" + id + '\'' +
                ", processInsId='" + processInsId + '\'' +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", key='" + key + '\'' +
                ", businessKey='" + businessKey + '\'' +
                '}';
    }
}
