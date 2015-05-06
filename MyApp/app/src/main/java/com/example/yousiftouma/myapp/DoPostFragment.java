package com.example.yousiftouma.myapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
import com.example.yousiftouma.myapp.misc.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class DoPostFragment extends Fragment {

    private User mLoggedInUser;
    private Button mDoPostButton;
    private EditText mDescriptionView;
    private EditText mTitleView;
    private String title;
    private String description;
    private String location = "";

    private OnPostFragmentInteractionListener mListener;

    public DoPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggedInUser = User.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_do_post, container, false);

        mDoPostButton = (Button) view.findViewById(R.id.button_do_post);
        mDescriptionView = (EditText) view.findViewById(R.id.description);
        mTitleView = (EditText) view.findViewById(R.id.title);
        mTitleView.requestFocus();

        mDoPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = mTitleView.getText().toString();
                description = mDescriptionView.getText().toString();
                onDonePressedGetAddress();
                addNewPost();
            }
        });

        mTitleView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mDescriptionView.requestFocus();
                }
                return true;
            }
        });

        mDescriptionView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    title = mTitleView.getText().toString();
                    description = mDescriptionView.getText().toString();
                    addNewPost();
                    hideKeyboard(v);
                    return true;
                }
                return false;
            }
        });

        mTitleView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // issue a slight delay so keyboard can be shown when fragment is created
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mTitleView, 0);
            }
        },50);

        return view;
    }

    private void onDonePressedGetAddress(){
        if (mListener != null) {
            location = mListener.OnAddNewPostGetAddress();
            System.out.println("loc= " + location);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPostFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPostFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void addNewPost() {
        View focusView = null;
        Boolean cancel = false;
        if (title.trim().isEmpty()) {
            mTitleView.setError(getString(R.string.error_field_required));
            focusView = mTitleView;
            cancel = true;
        }
        if (description.trim().isEmpty()) {
            mDescriptionView.setError(getString(R.string.error_field_required));
            focusView = mDescriptionView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            String url = MainActivity.SERVER_URL + "add_post";
            String jsonPostString = createJsonForPost();
            String responseAsString = null;
            JSONObject responseAsJson;
            try {
                responseAsString = new DynamicAsyncTask(jsonPostString).execute(url).get();
                responseAsJson = new JSONObject(responseAsString);
                responseAsString = responseAsJson.getString("result");
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
            assert responseAsString != null : "responseAsString is null";
            if (!responseAsString.equals("ok")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown),
                        Toast.LENGTH_SHORT).show();
             // if post is added we automatically go back to fragment we came from
            } else {
                getFragmentManager().popBackStack();
                hideKeyboard(getView());
            }
        }
    }

    private String createJsonForPost() {
        int user_id = mLoggedInUser.getId();
        JSONObject finishedPost = null;
        try {
            JSONObject postObj = new JSONObject();
            postObj.put("title", title);
            postObj.put("description", description);
            postObj.put("user_id", user_id );
            postObj.put("mediafile_path","standard");
            postObj.put("location", location);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(postObj);

            finishedPost = new JSONObject();
            finishedPost.put("song_post", jsonArray);
            System.out.println("post= " + finishedPost);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        assert finishedPost != null : "jsonobject for post is null, got jsonexception";
        return finishedPost.toString();
    }

    public interface OnPostFragmentInteractionListener{
        public String OnAddNewPostGetAddress();
    }

}
