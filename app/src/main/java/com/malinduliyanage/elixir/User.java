package com.malinduliyanage.elixir;

public class User {
    public String name;
    public String area;
    public String status;
    public String profilepic;
    public String compressedProfilePic;
    public String creationDate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name,String area, String status, String profilepic, String compressedProfilePic, String creationDate) {
        this.name = name;
        this.area = area;
        this.status = status;
        this.profilepic = profilepic;
        this.compressedProfilePic = compressedProfilePic;
        this.creationDate = creationDate;
    }
}

