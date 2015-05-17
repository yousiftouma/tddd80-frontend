package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.FeedAdapter;
import com.example.yousiftouma.myapp.helperclasses.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * Fragment to show a users liked posts
 */
public class LikedPostsFragment extends ListFragment implements
        FeedAdapter.OnLikeButtonClickedListener {
    private static final String USER_ID = "user_id";

    private int mUserId;
    private ArrayList<JSONObject> posts;
    private FeedAdapter adapter;
    private User mLoggedInUser;

    private OnFragmentInteractionListener mListener;

    public static LikedPostsFragment newInstance(int userId) {
        LikedPostsFragment fragment = new LikedPostsFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public LikedPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggedInUser = User.getInstance();
        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liked_posts, container, false);
    }

    public void onListItemClicked(int position) {
        if (mListener != null) {
            mListener.onPostClickedInLikedPostsFragment(posts.get(position));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // retrieve listview, set a listener to the items in it
        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(position);
            }
        });
        // init our array, fill it, init corresponding adapter and pass it the array
        posts = new ArrayList<>();
        getPosts();
        adapter = new FeedAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);
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
     * sets our array of posts to all liked posts for this user
     */
    private void getPosts() {
        String url = MainActivity.SERVER_URL + "get_liked_posts_by_id/" + mUserId;
        posts.clear();
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("liked_posts");
            for (int i = 0; i < jsonArray.length(); i++) {
                posts.add(jsonArray.getJSONObject(i));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * If this is logged in users own list of posts,
     * the list should update when unliking a post.
     * Else, it should only update the like itself in the adapter
     */
    @Override
    public void onLikeButtonClickedInAdapter() {
        if (mUserId == mLoggedInUser.getId()) {
            getPosts();
            adapter.notifyDataSetChanged();
        }
    }

    public interface OnFragmentInteractionListener {
        public void onPostClickedInLikedPostsFragment(JSONObject post);
    }

}
