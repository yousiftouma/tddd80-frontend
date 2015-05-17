package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.FeedAdapter;
import com.example.yousiftouma.myapp.helperclasses.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * fragment to show the profile of a user
 */
public class UserProfileFragment extends ListFragment implements
        FeedAdapter.OnLikeButtonClickedListener{
    // the fragment initialization parameters
    private static final String PROFILE_USER = "profile_user";

    private int mProfileUserId;

    private User mLoggedInUser;
    private TextView mProfileUserNameView;
    private ImageView mProfileUserImageView;
    private ProgressDialog mProgressDialog;
    private Button mRecentPosts;
    private Button mMostLikedPosts;

    private FeedAdapter adapter;

    private ArrayList<JSONObject> posts;

    private OnUserProfileFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param profileUserId id of user who owns profile to be viewed
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserProfileFragment newInstance(int profileUserId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(PROFILE_USER, profileUserId);
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProfileUserId = getArguments().getInt(PROFILE_USER);
            this.mLoggedInUser = User.getInstance();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mProfileUserNameView = (TextView) view.findViewById(R.id.profile_username_view);
        mProfileUserImageView = (ImageView) view.findViewById(R.id.profile_pic_view);
        ImageButton mFollowButton = (ImageButton) view.findViewById(R.id.button_follow);
        ImageButton mChangeProfilePicButton = (ImageButton)
                view.findViewById(R.id.button_take_new_profile_pic);
        mMostLikedPosts = (Button) view.findViewById(R.id.button_most_liked);
        Button mLikedPostsByUser = (Button) view.findViewById(R.id.button_likes);
        mRecentPosts = (Button) view.findViewById(R.id.button_most_recent);

        /**
         * issue a slight delay so the click can be registered properly
         * and the "tab" highlighted
         */
        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecentPosts.requestFocus();
                mRecentPosts.performClick();
                mRecentPosts.setPressed(true);
            }
        }, 1000);

        mMostLikedPosts.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                v.performClick();
                return true;
            }
        });
        mMostLikedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecentPosts.setPressed(false);
                if (posts != null) {
                    posts.clear();
                    posts.addAll(getPostsSortedByLikes());
                }
                adapter.notifyDataSetChanged();
            }
        });

        mRecentPosts.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                v.performClick();
                return true;
            }
        });
        mRecentPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMostLikedPosts.setPressed(false);
                if (posts != null) {
                    posts.clear();
                    posts.addAll(getPostsSortedByDate());
                }
                adapter.notifyDataSetChanged();
            }
        });
        mLikedPostsByUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikesButtonClicked();
            }
        });

        // disable follow button if own profile
        // enable edit profile pic button
        if (mProfileUserId == mLoggedInUser.getId()) {
            mChangeProfilePicButton.setEnabled(true);
            mFollowButton.setEnabled(false);
            mFollowButton.setImageDrawable(getResources().getDrawable(R.mipmap.cant_follow_50));
        }
        // else leave follow enabled and check what status it should have, depending on if
        // logged in user is following this user or not
        // also disable edit profile pic button
        else {
            mChangeProfilePicButton.setEnabled(false);
            mChangeProfilePicButton.setImageDrawable(getResources().
                    getDrawable(R.mipmap.cant_edit_pic_50));
            mFollowButton.setEnabled(true);
            if (mLoggedInUser.getFollows().contains(mProfileUserId)) {
                mFollowButton.setTag(R.id.follow_status, "unfollow");
                mFollowButton.setImageDrawable(getResources().getDrawable(R.mipmap.unfollow_50));
            }
            else {
                mFollowButton.setTag(R.id.follow_status, "follow");
                mFollowButton.setImageDrawable(getResources().getDrawable(R.mipmap.follow_50));
            }
        }

        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String followStatus = (String) v.getTag(R.id.follow_status);
                doFollowOrUnfollow(followStatus, (ImageButton) v);
                // update list of followed for this user object
                mLoggedInUser.setFollows();

            }
        });


        return view;
    }

    /**
     * Gives user feedback that profile page is loading
     * sets adapter with posts to show
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressDialog = ProgressDialog.show(getActivity(), "Loading",
                "Getting profile info", true);
        setProfileDetails();

        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(position);
            }
        });
        posts = new ArrayList<>();
        adapter = new FeedAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);

        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 1500);
    }

    public void onListItemClicked(int position) {
        if (mListener != null) {
        mListener.onUserProfileFeedListItemSelected(posts.get(position));
        }
    }

    public void onLikesButtonClicked(){
        if (mListener != null) {
            mListener.onUserLikesButtonClicked(mProfileUserId);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnUserProfileFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * adapter has notified us that a like was performed and we should sort
     * the list again accordingly
     */
    @Override
    public void onLikeButtonClickedInAdapter() {
        if (mMostLikedPosts.isPressed() && (posts != null)) {
            posts.clear();
            posts.addAll(getPostsSortedByLikes());
            adapter.notifyDataSetChanged();
        }
    }

    public interface OnUserProfileFragmentInteractionListener {
        public void onUserProfileFeedListItemSelected(JSONObject post);
        public void onUserLikesButtonClicked(int userId);
    }

    /**
     * returns a list of posts sorted by date, most recent first
     */
    private ArrayList<JSONObject> getPostsSortedByDate() {
        String url = MainActivity.SERVER_URL + "get_posts_by_id/"
                + mProfileUserId;
        ArrayList<JSONObject> posts = new ArrayList<>();
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("posts");
            for (int i = 0; i < jsonArray.length(); i++) {
                posts.add(jsonArray.getJSONObject(i));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return posts;
    }

    /**
     * returns a list of posts sorted by number of likes, most liked first
     */
    private ArrayList<JSONObject> getPostsSortedByLikes() {
        String url = MainActivity.SERVER_URL + "get_user_posts_ordered_by_likes/"
                + mProfileUserId;
        ArrayList<JSONObject> posts = new ArrayList<>();
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("user_post_top_list");
            for (int i = 0; i < jsonArray.length(); i++) {
                posts.add(jsonArray.getJSONObject(i));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return posts;
    }

    /**
     * sets the various views with current profile user information
     */
    private void setProfileDetails() {
        String url = MainActivity.SERVER_URL + "get_user_by_id/"
                + mProfileUserId;
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("user");
            JSONObject user = jsonArray.getJSONObject(0);
            mProfileUserNameView.setText(user.getString("username"));
            if (user.getString("profile_pic").equals("my default file path")) {
                mProfileUserImageView.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * performs a follow or unfollow depending on the status of the button
     * and changed the appearance and status accordingly
     * @param buttonStatus current status of the button
     * @param button the actual button so we can change its status and appearance
     */
    private void doFollowOrUnfollow(String buttonStatus, ImageButton button) {
        String url;
        String response = null;
        String JsonString = createJsonForFollowOrUnfollow();

        if (buttonStatus.equals("follow") ) {
            url = MainActivity.SERVER_URL + "follow/";
        }
        else { // It says unlike and we do that
            url = MainActivity.SERVER_URL + "unfollow/";
        }
        try {
            String responseJsonString = new DynamicAsyncTask(JsonString).execute(url).get();
            JSONObject responseAsJson = new JSONObject(responseJsonString);
            response = responseAsJson.getString("result");
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        switch (response) {
            case "followed":
                button.setImageDrawable(getResources().getDrawable(R.mipmap.unfollow_50));
                button.setTag(R.id.follow_status, "unfollow");
                break;
            case "unfollowed":
                button.setImageDrawable(getResources().getDrawable(R.mipmap.follow_50));
                button.setTag(R.id.follow_status, "follow");
                break;
        }
    }

    /**
     * creates a json string to be sent to server as post arg
     * @return json string
     */
    private String createJsonForFollowOrUnfollow() {
        int FollowerUserId = mLoggedInUser.getId();
        JSONObject finishedAction = null;
        try {
            int FollowedUserId = mProfileUserId;

            JSONObject action = new JSONObject();
            action.put("follower_id", FollowerUserId);
            action.put("followed_id", FollowedUserId);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(action);

            finishedAction = new JSONObject();
            finishedAction.put("action", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert finishedAction != null: "Some JSONException when trying to create" +
                "object to send to like/unlike";
        return finishedAction.toString();
    }
}
