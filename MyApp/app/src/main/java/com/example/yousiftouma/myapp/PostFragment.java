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

import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
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
        public void onFragmentInteraction(Uri uri);
    }


}
