package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.UserAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * Displays users for a user
 */
public class FollowersFragment extends ListFragment {
    private static final String USER_ID = "userId";

    private int mUserId;

    private ArrayList<JSONObject> users;
    private UserAdapter adapter;

    private TextView mFollowersCountView;
    private Button mFollowersButton, mFollowingButton;

    private OnFragmentInteractionListener mListener;

    public static FollowersFragment newInstance(int userId) {
        FollowersFragment fragment = new FollowersFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public FollowersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        mFollowersCountView = (TextView) view.findViewById(R.id.numberOfFollowersView);
        mFollowersButton = (Button) view.findViewById(R.id.button_toggle_followers);
        mFollowingButton = (Button) view.findViewById(R.id.button_toggle_following);

        /**
         * issue a slight delay so the click can be registered properly
         * and the "tab" highlighted
         */
        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFollowersButton.requestFocus();
                mFollowersButton.performClick();
                mFollowersButton.setPressed(true);
            }
        }, 1000);

        mFollowersButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                v.performClick();
                return true;
            }
        });
        mFollowingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setPressed(true);
                v.performClick();
                return true;
            }
        });
        mFollowersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowingButton.setPressed(false);
                if (users != null) {
                    setUsersToFollowers();
                }
                adapter.notifyDataSetChanged();
            }
        });
        mFollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowersButton.setPressed(false);
                if (users != null) {
                    setUsersToFollowing();
                }
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    /**
     * Gives feedback that page is loading, so we get a chance to set everything up before
     * a button is set to pressed and thus filling the list
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ProgressDialog mProgressDialog = ProgressDialog.show(getActivity(), "Loading",
                "Loading page", true);

        // retrieve listview, set a listener to the items in it
        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(position);
            }
        });
        // init our array, fill it, init corresponding adapter and pass it the array
        users = new ArrayList<>();
        adapter = new UserAdapter(getActivity(), users);
        list.setAdapter(adapter);


        final android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 1500);
    }

    public void onItemClicked(int position) {
        JSONObject user = users.get(position);
        if (mListener != null) {
            try {
                mListener.onUserClicked(user.getInt("id"));
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
     * Fills the list of users to be followers
     */
    private void setUsersToFollowers() {
            String url = MainActivity.SERVER_URL + "get_following_users_by_id/"
                    + mUserId;
            users.clear();
            try {
                String response = new DynamicAsyncTask().execute(url).get();
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray jsonArray = jsonResponse.getJSONArray("following_users");
                for (int i = 0; i < jsonArray.length(); i++) {
                    users.add(jsonArray.getJSONObject(i));
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
                e.getMessage();
            }
            mFollowersCountView.setText("Followers: " + users.size());
        }

    /**
     * Fills the list of users to be following
     */
    private void setUsersToFollowing() {
        String url = MainActivity.SERVER_URL + "get_all_followed_users_by_id/"
                + mUserId;
        users.clear();
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("followed_users");
            for (int i = 0; i < jsonArray.length(); i++) {
                users.add(jsonArray.getJSONObject(i));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }
        mFollowersCountView.setText("Following: " + users.size());
    }

    public interface OnFragmentInteractionListener {
        public void onUserClicked(int userId);
    }

}
