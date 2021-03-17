package com.marius.jobfinder;

public class Job {
    private String id, title, typeOfJob, description, money, imageUrl, typeOfDeadline,
            address,  postcode, longitude, latitude;

    private String dueDate, completedDate="Pending";

    private String postedById, postedByName, acceptedByName, acceptedById, completedBy, status;

    public Job(){}

    public Job(String id, String title, String typeOfJob, String description, String money, String imageUrl,
               String typeOfDeadline, String address,
               String postcode, String longitude, String latitude, String dueDate, String postedById, String status) {
        this.id = id;
        this.title = title;
        this.typeOfJob = typeOfJob;
        this.description = description;
        this.money = money;
        this.imageUrl = imageUrl;
        this.typeOfDeadline = typeOfDeadline;
        this.address = address;
        this.postcode = postcode;
        this.longitude = longitude;
        this.latitude = latitude;
        this.dueDate = dueDate;
        this.postedById = postedById;
        this.status = status;

    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
    }

    public String getAcceptedById() {
        return acceptedById;
    }

    public void setAcceptedById(String acceptedById) {
        this.acceptedById = acceptedById;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostedById() {
        return postedById;
    }

    public void setPostedById(String postedById) {
        this.postedById = postedById;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTypeOfJob() {
        return typeOfJob;
    }

    public void setTypeOfJob(String typeOfJob) {
        this.typeOfJob = typeOfJob;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTypeOfDeadline() {
        return typeOfDeadline;
    }

    public void setTypeOfDeadline(String typeOfDeadline) {
        this.typeOfDeadline = typeOfDeadline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getAcceptedByName() {
        return acceptedByName;
    }

    public void setAcceptedByName(String acceptedByName) {
        this.acceptedByName = acceptedByName;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
