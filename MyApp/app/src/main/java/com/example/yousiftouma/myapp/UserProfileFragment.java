package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PROFILE_USER = "profile_user";

    // TODO: Rename and change types of parameters
    private int mProfileUserId;

    private User mLoggedInUser;
    private TextView mProfileUserNameView;
    private ImageView mProfileUserImageView;
    private ProgressDialog mProgressDialog;

    private ArrayList<JSONObject> posts;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * mLoggedInUser The logged in user object
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

        ArrayList<JSONObject> posts = getPosts();
        ArrayList<Integer> likes = getUserLikes();
        FeedAdapter adapter = new FeedAdapter(getActivity(), posts, likes);
        list.setAdapter(adapter);
        mProgressDialog.dismiss();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onListItemClicked(int position) {
        if (mListener != null) {
            try {
                mListener.onFeedItemSelected(posts.get(position).getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        public void onFeedItemSelected(int postId);
    }

    private ArrayList<JSONObject> getPosts() {
        String url = "http://mytestapp-youto814.openshift.ida.liu.se/get_posts_by_id/" + mProfileUserId;
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

    private ArrayList<Integer> getUserLikes() {
        String url = "http://mytestapp-youto814.openshift.ida.liu.se/get_user_likes_by_id/"
                + mLoggedInUser.getId();
        System.out.println("Signed in user id: " + mLoggedInUser.getId());
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

    private void setProfileDetails() {
        String url = "http://mytestapp-youto814.openshift.ida.liu.se/get_user_by_id/"
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
}
