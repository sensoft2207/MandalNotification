package com.android.mandalchat.model;



public class User {
    public String name;
    public String tokenn;
    public String email;
    public String joining;
    public String profession;
    public String mobile;
    public String avata;
    public Status status;
    public Message message;


    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
