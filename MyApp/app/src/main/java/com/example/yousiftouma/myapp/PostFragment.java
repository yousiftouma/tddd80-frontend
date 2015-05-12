package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yousiftouma.myapp.misc.CommentAdapter;
import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
import com.example.yousiftouma.myapp.misc.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends ListFragment {

    private static final String POST = "post";
    private static final String IS_COMMENT_HIGHLIGHTED = "is_comment_highlighted";
    private static final String IS_DICOVER_MODE = "is_discover_mode";

    private boolean mIsCommentHighlighted;
    private JSONObject mPost;
    private boolean mIsDiscoverMode;

    private TextView mAuthorView, mTitleView, mDescriptionView, mLikesView,
                        mCommentsView, mSongTimeView, mLocationView;
    private ImageButton mLikeButton, mPlayButton, mPauseButton, mFastForwardButton,
                            mRewindButton;
    private Button mCommentButton;
    private EditText mCommentField;
    private SeekBar mSeekBar;

    private User mLoggedInUser = User.getInstance();

    private ArrayList<JSONObject> comments;
    private CommentAdapter adapter;

    private ArrayList<JSONObject> postsNotSeenYet;
    private int latestShownPost = -1;

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private SensorEventListener mSensorListener = null;


    private OnFragmentInteractionListener mListener;

    /**
     * factory method for postfragment without discover mode
     * @param post post to show
     * @param isCommentHighlighted if came through comment
     * @return new fragment
     */
    public static PostFragment newInstance(JSONObject post, boolean isCommentHighlighted) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(POST, post.toString());
        args.putBoolean(IS_COMMENT_HIGHLIGHTED, isCommentHighlighted);
        args.putBoolean(IS_DICOVER_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * factory method when using postfragment for discover mode
     * @return new fragment
     */
    public static PostFragment newInstance(){
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(POST, null);
        args.putBoolean(IS_COMMENT_HIGHLIGHTED, false);
        args.putBoolean(IS_DICOVER_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsCommentHighlighted = getArguments().getBoolean(IS_COMMENT_HIGHLIGHTED);
            mIsDiscoverMode = getArguments().getBoolean(IS_DICOVER_MODE);
            // only assign post if not discover mode, otherwise redundant
            if (!mIsDiscoverMode) {
                try {
                    mPost = new JSONObject(getArguments().getString(POST));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                activateShakeSensor();
                mPost = getRandomPost();
                Toast.makeText(getActivity().getApplicationContext(),
                        getResources().getString(R.string.shake_for_post),
                        Toast.LENGTH_SHORT).show();
            }
        }

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    private void activateShakeSensor() {
        mSensorListener = new SensorEventListener() {

            public void onSensorChanged(SensorEvent se) {
                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta; // perform low-cut filter
                if (mAccel > 6 && mIsDiscoverMode) {
                    Toast.makeText(getActivity().getApplicationContext(), getResources().
                                    getString(R.string.new_post_retrieved),
                            Toast.LENGTH_SHORT).show();
                    mPost = showRandomPost();
                    setPostData();
                    comments.clear();
                    comments.addAll(getComments());
                    adapter.notifyDataSetChanged();
                    mAccel = 0;
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_page, container, false);

        mAuthorView = (TextView) view.findViewById(R.id.username);
        mAuthorView.setTextColor(Color.BLUE);
        mTitleView = (TextView) view.findViewById(R.id.title);
        mDescriptionView = (TextView) view.findViewById(R.id.description);
        mLikesView = (TextView) view.findViewById(R.id.likesView);
        mCommentsView = (TextView) view.findViewById(R.id.commentsView);
        mSongTimeView = (TextView) view.findViewById(R.id.songtimeView);
        mLocationView = (TextView) view.findViewById(R.id.location);

        mLikeButton = (ImageButton) view.findViewById(R.id.button_like);
        mRewindButton = (ImageButton) view.findViewById(R.id.button_rewind);
        mPlayButton = (ImageButton) view.findViewById(R.id.button_play);
        mPauseButton = (ImageButton) view.findViewById(R.id.button_pause);
        mFastForwardButton = (ImageButton) view.findViewById(R.id.button_fastforward);

        mCommentButton = (Button) view.findViewById(R.id.button_comment);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);

        mCommentField = (EditText) view.findViewById(R.id.comment_edittext);

        setPostData();

        // check if we came to fragment through comment button or through post item clicked
        if (mIsCommentHighlighted) {
            mCommentField.requestFocus();
            mCommentField.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // issue a slight delay so keyboard can be shown when fragment is created
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mCommentField, 0);
                }
            },50);

        }
        else {
            mPlayButton.requestFocus();
        }

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonStatus = (String) v.getTag(R.id.like_status);
                doLikeOrUnlike(buttonStatus);
                setNumberOfLikes();
                mLoggedInUser.setLikes();
            }
        });
        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentField.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mCommentField, InputMethodManager.SHOW_FORCED);
            }
        });

        mCommentField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String comment = mCommentField.getText().toString();
                if ((actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE)
                        && !comment.trim().isEmpty()){
                    System.out.println("pressed done");
                    mCommentField.setText("");
                    postComment(comment);
                    return false;
                }
                else {
                    Toast.makeText(getActivity(), "Try writing something!", Toast.LENGTH_LONG)
                            .show();
                    return true;
                }
            }
        });

        mCommentField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        mAuthorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.LTGRAY);
                try {
                    onUsernamePressed(mPost.getInt("user_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void setPostData() {
        try {
            mAuthorView.setText(mPost.getString("artist"));
            mTitleView.setText(mPost.getString("title"));
            mDescriptionView.setText(mPost.getString("description"));
            mLocationView.setText("Uploaded from " + mPost.getString("location"));

            // check if post is liked, to know what button to display
            if (mLoggedInUser.getLikes().contains(mPost.getInt("id"))){
                mLikeButton.setImageDrawable(getResources()
                        .getDrawable(R.mipmap.liked_50));
                mLikeButton.setTag(R.id.like_status, "unlike");
            }
            // else post is not liked, our "default"
            else {
                mLikeButton.setImageDrawable(getResources()
                        .getDrawable(R.mipmap.not_liked_50));
                mLikeButton.setTag(R.id.like_status, "like");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setNumberOfLikes();
        setNumberOfComments();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        //we create one object for comments and fill it with all comments
        //from getComments(). Then we use manipulate using this pointer.
        //This means notifyDataSetChanged() works when the array is updated
        comments = new ArrayList<>();
        comments.addAll(getComments());
        adapter = new CommentAdapter(getActivity(), comments);
        listView.setAdapter(adapter);
    }

    public void onUsernamePressed(int userId) {
        if (mListener != null) {
            mListener.onPostFragmentUsernameButtonClicked(userId);
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

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.
                getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }


    private ArrayList<JSONObject> getComments() {
        int postId = -1;
        String url;
        try {
            postId = mPost.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        comments = new ArrayList<>();
        if (postId != -1) {
            url = MainActivity.SERVER_URL + "get_comments_for_post_by_id/" +
                    postId;
            String responseAsString;
            JSONObject responseAsJson;
            try {
                responseAsString = new DynamicAsyncTask().execute(url).get();
                responseAsJson = new JSONObject(responseAsString);
                JSONArray jsonArray = responseAsJson.getJSONArray("comments");
                for (int i = 0; i < jsonArray.length(); i++) {
                    comments.add(jsonArray.getJSONObject(i));
                }
            } catch (InterruptedException | JSONException | ExecutionException e) {
                e.printStackTrace();
                e.getMessage();
            }
        }
        return comments;
    }

    private void postComment(String comment) {
        String url = MainActivity.SERVER_URL + "add_comment";
        String response = null;
        String JsonString = createJsonForComment(comment);

        try {
            // do post
            String responseJsonString = new DynamicAsyncTask(JsonString).execute(url).get();
            System.out.println("server response: " + responseJsonString);
            JSONObject responseAsJson = new JSONObject(responseJsonString);
            System.out.println("jsonresponse: " + responseAsJson);
            response = responseAsJson.getString("result");
            System.out.println("response (ok): " + response);

        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        switch (response) {
            case "ok":
                System.out.println("comment made!");
                //repopulate the list with new comment included and notify
                //adapter of updated list. Also update comment count
                comments.clear();
                comments.addAll(getComments());
                adapter.notifyDataSetChanged();
                setNumberOfComments();
                Toast.makeText(getActivity(), "Comment added!",
                        Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(getActivity(), "Could not add comment...",
                        Toast.LENGTH_LONG).show();
        }


    }

    private void doLikeOrUnlike(String buttonStatus) {
        String url;
        String response = null;
        String JsonString = createJsonForLikeOrUnlike();

        if (buttonStatus.equals("like") ) {
            System.out.println("like?: " + buttonStatus);
            url = MainActivity.SERVER_URL + "like/";
            mLikeButton.setTag(R.id.like_status, "unlike");
        }
        else { // It says unlike and we do that
            System.out.println("unlike?: " + buttonStatus);
            url = MainActivity.SERVER_URL + "unlike/";
            mLikeButton.setTag(R.id.like_status, "like");
        }
        try {
            // do post
            String responseJsonString = new DynamicAsyncTask(JsonString).execute(url).get();
            System.out.println("responsejsonstring: " + responseJsonString);
            JSONObject responseAsJson = new JSONObject(responseJsonString);
            response = responseAsJson.getString("result");
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        assert response != null: "response is null";
        switch (response) {
            case "liked":
                mLikeButton.setImageDrawable(getResources().getDrawable(R.mipmap.liked_50));
                break;
            case "unliked":
                mLikeButton.setImageDrawable(getResources().getDrawable(R.mipmap.not_liked_50));
                break;
        }
    }

    private String createJsonForLikeOrUnlike() {
        int actionUserId = mLoggedInUser.getId();
        JSONObject finishedAction = null;
        try {
            int postId = mPost.getInt("id");

            JSONObject action = new JSONObject();
            action.put("user_id", actionUserId);
            action.put("post_id", postId);

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

    private String createJsonForComment(String text) {
        int commentUserId = mLoggedInUser.getId();
        JSONObject finishedAction = null;
        try {
            int postId = mPost.getInt("id");

            JSONObject comment = new JSONObject();
            comment.put("user_id", commentUserId);
            comment.put("song_post_id", postId);
            comment.put("text", text);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(comment);

            finishedAction = new JSONObject();
            finishedAction.put("comment", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert finishedAction != null: "Some JSONException when trying to create" +
                "object to send to postComment";
        return finishedAction.toString();
    }

    private void setNumberOfLikes() {
        String responseAsString;
        JSONObject responseAsJson;
        String numberOfLikes = null;
        try {
            String url = MainActivity.SERVER_URL +
                    "get_number_of_likes_for_post/"
                    + mPost.getInt("id");
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            numberOfLikes = responseAsJson.getString("number_of_likes");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        mLikesView.setText(numberOfLikes);
    }

    private void setNumberOfComments() {
        String responseAsString;
        JSONObject responseAsJson;
        String numberOfComments = null;
        try {
            String url = MainActivity.SERVER_URL +
                    "get_number_of_comments_for_post_by_id/"
                    + mPost.getInt("id");
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            numberOfComments = responseAsJson.getString("number_of_comments");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        mCommentsView.setText(numberOfComments);
    }

    private JSONObject getRandomPost() {
        String url = MainActivity.SERVER_URL + "get_discover_posts/" + mLoggedInUser.getId();
        String responseAsString;
        JSONObject responseAsJson;
        Random rand = new Random();
        JSONObject post = null;
        int curr_post_id;
        int index;
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("posts");

            index = rand.nextInt(jsonArray.length());
            post = jsonArray.getJSONObject(index);
            curr_post_id = post.getInt("id");
            while (curr_post_id == latestShownPost) {
                index = rand.nextInt(jsonArray.length());
                post = jsonArray.getJSONObject(index);
                curr_post_id = post.getInt("id");
            }

            latestShownPost = curr_post_id;

              // add all posts that we can see in discover mode in a list
              // and remove the one we are showing
            postsNotSeenYet = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                if (i != index) {
                    postsNotSeenYet.add(jsonArray.getJSONObject(i));
                }
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return post;
    }

    private JSONObject showRandomPost() {
        Random rand = new Random();
        JSONObject post;
        if (!postsNotSeenYet.isEmpty()){
            int index = rand.nextInt(postsNotSeenYet.size());
            post = postsNotSeenYet.get(index);
            postsNotSeenYet.remove(index);
            try {
                latestShownPost = post.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return getRandomPost();
        }
        return post;
    }

    public interface OnFragmentInteractionListener {
        public void onPostFragmentUsernameButtonClicked(int userId);
    }

}
