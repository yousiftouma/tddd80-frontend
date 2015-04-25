package com.example.yousiftouma.myapp.misc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Singleton class to hold the currently logged in user.
 */
public class User {

    private int id;
    private String username;
    private String email;
    private String profilePic;

    // Private constructor. Prevents instantiation from other classes.
    private User() {}

    /**
     * Initializes singleton.
     * UserHolder is loaded when User.getInstance() is first executed or when
     * UserHolder.INSTANCE is accessed.
     */
    private static class UserHolder {
        private static final User INSTANCE = new User();
    }

    public static User getInstance() {
        return UserHolder.INSTANCE;
    }

    public void setInfo(JSONObject userData){
        try {
            id = userData.getInt("id");
            profilePic = userData.getString("profile_pic");
            username = userData.getString("username");
            email = userData.getString("email");
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
