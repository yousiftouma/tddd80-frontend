package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.yousiftouma.myapp.misc.CommentAdapter;
import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
import com.example.yousiftouma.myapp.misc.User;
import com.google.android.gms.maps.internal.IMapFragmentDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

    private boolean mIsCommentHighlighted;
    private JSONObject mPost;

    private TextView mAuthorView, mTitleView, mDescriptionView, mLikesView,
                        mCommentsView, mSongTimeView;
    private ImageButton mLikeButton, mPlayButton, mPauseButton, mFastForwardButton,
                            mRewindButton;
    private Button mCommentButton;
    private EditText mCommentField;
    private SeekBar mSeekBar;
    private ArrayList<Integer> mUserLikes;

    private User mLoggedInUser = User.getInstance();

    private ArrayList<JSONObject> comments;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param post JSONObject post to display
     * @param isCommentHighlighted If comment field should be highlighted
     * @return A new instance of fragment PostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostFragment newInstance(JSONObject post, boolean isCommentHighlighted) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(POST, post.toString());
        args.putBoolean(IS_COMMENT_HIGHLIGHTED, isCommentHighlighted);
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
            try {
                mPost = new JSONObject(getArguments().getString(POST));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_page, container, false);

        mAuthorView = (TextView) view.findViewById(R.id.username);
        mTitleView = (TextView) view.findViewById(R.id.title);
        mDescriptionView = (TextView) view.findViewById(R.id.description);
        mLikesView = (TextView) view.findViewById(R.id.likesView);
        mCommentsView = (TextView) view.findViewById(R.id.commentsView);
        mSongTimeView = (TextView) view.findViewById(R.id.songtimeView);

        mLikeButton = (ImageButton) view.findViewById(R.id.button_like);
        mRewindButton = (ImageButton) view.findViewById(R.id.button_rewind);
        mPlayButton = (ImageButton) view.findViewById(R.id.button_play);
        mPauseButton = (ImageButton) view.findViewById(R.id.button_pause);
        mFastForwardButton = (ImageButton) view.findViewById(R.id.button_fastforward);

        mCommentButton = (Button) view.findViewById(R.id.button_comment);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);

        mCommentField = (EditText) view.findViewById(R.id.comment_edittext);

        try {
            mAuthorView.setText(mPost.getString("artist"));
            mTitleView.setText(mPost.getString("title"));
            mDescriptionView.setText(mPost.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mIsCommentHighlighted) {
            mCommentField.requestFocus();
        }
        else {
            mPlayButton.requestFocus();
        }

        mUserLikes = getUserLikes();

        try {
            if (mUserLikes.contains(mPost.getInt("id"))){
                mLikeButton.setImageDrawable(getResources()
                        .getDrawable(R.mipmap.liked_50));
                mLikeButton.setTag(R.id.like_status, "unlike");
            }
            // else post is not liked, our "default"
            // (needed, otherwise buttons may be set wrongly)
            else {
                mLikeButton.setImageDrawable(getResources()
                        .getDrawable(R.mipmap.not_liked_50));
                mLikeButton.setTag(R.id.like_status, "like");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setNumberOfLikes();

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonStatus = (String) v.getTag(R.id.like_status);
                doLikeOrUnlike(buttonStatus);
            }
        });
        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentField.requestFocus();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        ArrayList<JSONObject> comments = getComments();
        CommentAdapter adapter = new CommentAdapter(getActivity(), comments);
        listView.setAdapter(adapter);


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
            url = "http://mytestapp-youto814.openshift.ida.liu.se/get_comments_for_post_by_id/" +
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

    private void doLikeOrUnlike(String buttonStatus) {
        String url;
        String response = null;
        String JsonString = createJsonForAction();

        if (buttonStatus.equals("like") ) {
            url = "http://mytestapp-youto814.openshift.ida.liu.se/like/";
        }
        else { // It says unlike and we do that
            url = "http://mytestapp-youto814.openshift.ida.liu.se/unlike/";
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
            case "liked":
                mLikeButton.setImageDrawable(getResources().getDrawable(R.mipmap.liked_50));
                break;
            case "unliked":
                mLikeButton.setImageDrawable(getResources().getDrawable(R.mipmap.not_liked_50));
                break;
        }
    }

    private String createJsonForAction() {
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

    private void setNumberOfLikes() {
        String responseAsString;
        JSONObject responseAsJson;
        String numberOfLikes = null;
        try {
            String url = "http://mytestapp-youto814.openshift.ida.liu.se/" +
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

    private ArrayList<Integer> getUserLikes() {
        String url = "http://mytestapp-youto814.openshift.ida.liu.se/get_user_likes_by_id/"
                + mLoggedInUser.getId();
        String responseAsString;
        JSONObject responseAsJson;
        ArrayList<Integer> likes = new ArrayList<>();
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("post_ids");
            for (int i = 0; i < jsonArray.length(); i++) {
                likes.add(jsonArray.getInt(i));
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        } return likes;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


}
