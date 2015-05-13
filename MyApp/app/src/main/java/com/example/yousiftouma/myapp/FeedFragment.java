package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.app.Fragment;
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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FeedFragment extends ListFragment implements FeedAdapter.OnLikeButtonClickedListener{

    private ArrayList<JSONObject> posts;
    private User mLoggedInUser;
    private OnFragmentInteractionListener mListener;
    private Button mNewPostButton;

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
        mNewPostButton = (Button) view.findViewById(R.id.button_post);
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

        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(position);
            }
        });

        ArrayList<JSONObject> posts = getPosts();
        FeedAdapter adapter = new FeedAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);
    }

    public void onListItemClicked(int position) {
        if (mListener != null) {
            mListener.onFeedFragmentItemClicked(posts.get(position));
        }
    }

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

    @Override
    public void onLikeButtonClickedInAdapter() {
        // no need to do anything as feed already updates what is necessary to update
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
        public void onFeedFragmentItemClicked(JSONObject post);
        public void onNewPostButtonClicked();
    }

}
