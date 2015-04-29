package com.example.yousiftouma.myapp.misc;

import com.example.yousiftouma.myapp.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Singleton class to hold the currently logged in user.
 */
public class User {

    private int id;
    private String username;
    private String email;
    private String profilePic;
    private ArrayList<Integer> likes = new ArrayList<>();
    private ArrayList<Integer> follows = new ArrayList<>();

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
            setLikes();
            setFollows();

        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    private ArrayList<Integer> allLikesByUser() {
        String url = MainActivity.SERVER_URL + "get_user_likes_by_id/"
                + id;
        ArrayList<Integer> likes = new ArrayList<>();
        try {
            String responseAsString = new DynamicAsyncTask().execute(url).get();
            JSONObject responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("post_ids");
            for (int i = 0; i < jsonArray.length(); i++) {
                likes.add(jsonArray.getInt(i));
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        } return likes;
    }

    private ArrayList<Integer> allFollowedByUser() {
        String url = MainActivity.SERVER_URL + "get_all_followed_by_id/"
                + id;
        ArrayList<Integer> follows = new ArrayList<>();
        try {
            String responseAsString = new DynamicAsyncTask().execute(url).get();
            JSONObject responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("user_ids");
            for (int i = 0; i < jsonArray.length(); i++) {
                follows.add(jsonArray.getInt(i));
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        } return follows;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Integer> getLikes() {
        return likes;
    }

    public ArrayList<Integer> getFollows() {
        return follows;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setLikes() {
        likes.clear();
        likes.addAll(allLikesByUser());
    }

    public void setFollows() {
        follows.clear();
        follows.addAll(allFollowedByUser());
    }
}
