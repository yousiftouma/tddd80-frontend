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
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.TopListAdapter;
import com.example.yousiftouma.myapp.helperclasses.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.example.yousiftouma.myapp.TopListFragment.OnTopListFragmentInteraction} interface
 * to handle interaction events.
 */
public class TopListFragment extends ListFragment implements
        TopListAdapter.OnLikeButtonClickedListener {

    private OnTopListFragmentInteraction mListener;

    private ArrayList<JSONObject> posts;
    private ListView list;
    private BaseAdapter adapter;
    private User mLoggedInUser;

    public TopListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toplist, container, false);
        mLoggedInUser = User.getInstance();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(position);
            }
        });
        posts = new ArrayList<>();
        getTopListPosts();
        adapter = new TopListAdapter(getActivity(), posts, this);
        list.setAdapter(adapter);
    }

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
