package com.marius.jobfinder;

import java.util.ArrayList;
import java.util.List;

public class User implements Cloneable {
    private String firstName, lastName, phone, email, password, accountType;
    private String profilePhoto="";
    private String id = "";
    private List<String> completedJobs = new ArrayList<>();
    private List<String> pendingJobs = new ArrayList<>();
    private List<String> typesOfOfferedServices = new ArrayList<>();

    public User(){}




    public User(String firstName, String lastName, String phone, String email, String password, String accountType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.accountType = accountType;
    }


    public void addTypeOfOfferedService(String service){
        typesOfOfferedServices.add(service);
    }

    public List<String> getTypesOfOfferedServices() {
        return typesOfOfferedServices;
    }

    public void setTypesOfOfferedServices(List<String> typesOfOfferedServices) {
        this.typesOfOfferedServices = typesOfOfferedServices;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addPendingJob(String jobID){
        this.pendingJobs.add(jobID);
    }

    public void addCompletedJob(String jobID){
        this.completedJobs.add(jobID);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setCompletedJobs(List<String> completedJobs) {
        this.completedJobs = completedJobs;
    }

    public void setPendingJobs(List<String> pendingJobs) {
        this.pendingJobs = pendingJobs;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public List getCompletedJobs() {
        return completedJobs;
    }

    public List getPendingJobs() {
        return pendingJobs;
    }
}
