package com.madcamp.petclub.Friends.model;

public class UserModel {
    public String userid;
    public String ID;
    public String name;
    public String token;
    public String userphoto;
    public String usermsg;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUid() {
        return ID;
    }

    public void setUid(String ID) {
        this.ID = ID;
    }

    public String getUsernm() {
        return name;
    }

    public void setUsernm(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserphoto() {
        return userphoto;
    }

    public void setUserphoto(String userphoto) {
        this.userphoto = userphoto;
    }

    public String getUsermsg() {
        return usermsg;
    }

    public void setUsermsg(String usermsg) {
        this.usermsg = usermsg;
    }
}