package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
import com.example.yousiftouma.myapp.misc.FeedAdapter;
import com.example.yousiftouma.myapp.misc.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends ListFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PROFILE_USER = "profile_user";

    private int mProfileUserId;

    private User mLoggedInUser;
    private TextView mProfileUserNameView;
    private ImageView mProfileUserImageView;
    private ProgressDialog mProgressDialog;
    private ImageButton mFollowButton;
    private Button mRecentPosts, mMostLikedPosts;

    private FeedAdapter adapter;

    private ArrayList<JSONObject> posts;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param profileUserId id of user who owns profile to be viewed
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        mFollowButton = (ImageButton) view.findViewById(R.id.button_follow);
        mMostLikedPosts = (Button) view.findViewById(R.id.button_most_liked);
        mRecentPosts = (Button) view.findViewById(R.id.button_most_recent);

        mRecentPosts.setPressed(true);

        mMostLikedPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMostLikedPosts.setPressed(true);
                mRecentPosts.setPressed(false);
                posts = getPostsSortedByLikes();
                adapter.notifyDataSetChanged();
            }
        });

        mRecentPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecentPosts.setPressed(true);
                mMostLikedPosts.setPressed(false);
                posts = getPostsSortedByDate();
                adapter.notifyDataSetChanged();
            }
        });

        if (mProfileUserId == mLoggedInUser.getId()) {
            mFollowButton.setEnabled(false);
            mFollowButton.setImageDrawable(getResources().getDrawable(R.mipmap.cant_follow_50));
        }
        else {
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
                mLoggedInUser.setFollows();

            }
        });

        return view;
    }

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

        posts = getPostsSortedByDate();
        adapter = new FeedAdapter(getActivity(), posts);
        list.setAdapter(adapter);

        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 3000);
    }

    public void onListItemClicked(int position) {
        if (mListener != null) {
        mListener.onUserProfileFeedListItemSelected(posts.get(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onUserProfileFeedListItemSelected(JSONObject post);
    }

    private ArrayList<JSONObject> getPostsSortedByDate() {
        String url = MainActivity.SERVER_URL + "get_posts_by_id/"
                + mProfileUserId;
        posts = new ArrayList<>();
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

    private ArrayList<JSONObject> getPostsSortedByLikes() {
        String url = MainActivity.SERVER_URL + "get_user_posts_ordered_by_likes/"
                + mProfileUserId;
        posts = new ArrayList<>();
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
            System.out.println(responseJsonString);
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
