package com.malinduliyanage.elixir;

public class User {
    private String userId;

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

    public User(String name, String profilepic, String compressedProfilePic) {
        this.name = name;
        this.profilepic = profilepic;
        this.compressedProfilePic = compressedProfilePic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getprofilepic() {
        return profilepic;
    }

    public void setprofilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getCompressedProfilePic() {
        return compressedProfilePic;
    }

    public void setCompressedProfilePic(String compressedProfilePic) {
        this.compressedProfilePic = compressedProfilePic;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}

