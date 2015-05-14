package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.TopListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * Fragment that shows a top list for posts, sorted by likes
 */
public class TopListFragment extends ListFragment implements
        TopListAdapter.OnLikeButtonClickedListener {

    private OnTopListFragmentInteraction mListener;

    private ArrayList<JSONObject> posts;
    private BaseAdapter adapter;

    public TopListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toplist, container, false);
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
        // init our array, init corresponding adapter and pass it the array
        posts = new ArrayList<>();
        getTopListPosts();
        adapter = new TopListAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);
    }

    /**
     * notify our listener that listitem was clicked and call the handling method
     */
    public void onListItemClicked(int position) {
        if (mListener != null) {
            mListener.onTopListItemClicked(posts.get(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTopListFragmentInteraction) activity;
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
     * sets our array of posts to a sorted list of posts, retrieved from server
     */
    private void getTopListPosts() {
        String url = MainActivity.SERVER_URL + "get_posts_ordered_by_likes";
        posts.clear();
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("post_top_list");
            for (int i = 0; i < jsonArray.length(); i++) {
                posts.add(jsonArray.getJSONObject(i));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * when we receive notification from the adapter that like was clicked
     * in the adapter, we need to respond by updating the list, as order may have changed
     */
    @Override
    public void onLikeButtonClickedInAdapter() {
        //we update the order now since it may be different
        getTopListPosts();
        adapter.notifyDataSetChanged();
    }

    public interface OnTopListFragmentInteraction {
        public void onTopListItemClicked(JSONObject post);
    }

}
