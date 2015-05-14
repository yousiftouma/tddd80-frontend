package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
 * Shows the relevant feed for the logged in user
 * i.e. posts by you and people you follow
 */
public class FeedFragment extends ListFragment implements FeedAdapter.OnLikeButtonClickedListener{

    private ArrayList<JSONObject> posts;
    private User mLoggedInUser;
    private OnFragmentInteractionListener mListener;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLoggedInUser = User.getInstance();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        Button mNewPostButton = (Button) view.findViewById(R.id.button_post);
        mNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPostButtonClicked();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // retrieve the listview and set item listener
        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(position);
            }
        });

        // we get the array that should be in the feed and pass it to the adapter, which we set
        // to the list
        ArrayList<JSONObject> posts = getPosts();
        FeedAdapter adapter = new FeedAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);
    }

    /**
     * if a list item is clicked, we notify our listener activity so it can do what it needs to do
     * which is change fragment to the postfragment for that post
     * @param position position in the feed, mapping to a post in the array
     */
    public void onListItemClicked(int position) {
        if (mListener != null) {
            mListener.onFeedFragmentItemClicked(posts.get(position));
        }
    }

    /**
     * if post button is clicked, we notify the activity so it can change fragments
     */
    public void onPostButtonClicked() {
        if (mListener != null) {
            mListener.onNewPostButtonClicked();
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

    // gets the relevant feed posts for this logged in user
    private ArrayList<JSONObject> getPosts() {
        String url = MainActivity.SERVER_URL + "get_feed_posts_by_id/"
                + mLoggedInUser.getId();
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

    /**
     * this fragment doesn't need to handle this interface method since it doesn't
     * change when something is liked, that is handled in the adapter itself
     */
    @Override
    public void onLikeButtonClickedInAdapter() {
    }


    /**
     * any activity that uses this fragment needs to handle both these events
     */
    public interface OnFragmentInteractionListener {
        public void onFeedFragmentItemClicked(JSONObject post);
        public void onNewPostButtonClicked();
    }

}
