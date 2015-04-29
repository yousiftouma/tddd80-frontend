package com.example.yousiftouma.myapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yousiftouma.myapp.misc.FeedAdapter;
import com.example.yousiftouma.myapp.misc.User;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements
        UserProfileFragment.OnFragmentInteractionListener,
        FeedAdapter.OnCommentButtonClickedListener,
        PostFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener {

    private User loggedInUser;
    private FragmentManager fm = getFragmentManager();
    private Fragment newFragment;
    public static String SERVER_URL = "http://mytestapp-youto814.openshift.ida.liu.se/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loggedInUser = User.getInstance();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // If restored from previous state, don't do anything to avoid
            // overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            // TODO: Create a new Fragment to be placed in the activity layout
            //newFragment = UserProfileFragment.newInstance(loggedInUser.getId());
            //newFragment = UserProfileFragment.newInstance(2);
            newFragment = new FeedFragment();

            // Add fragment to container
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, newFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() != 0) {
            fm.popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void replaceFragment() {
        fm.beginTransaction()
                .replace(R.id.fragment_container, newFragment).addToBackStack(null).commit();
    }

    @Override
    public void onUserProfileFeedListItemSelected(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void onCommentClickedInFeedAdapter(JSONObject post) {
        newFragment = PostFragment.newInstance(post, true);
        replaceFragment();
    }

    @Override
    public void onPostFragmentUsernameButtonClicked() {
        // TODO: Change to profile of the user you clicked in PostFragment
    }

    @Override
    public void onFeedFragmentItemClicked(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void onNewPostButtonClicked() {
        // TODO: Change to MakePostFragment
    }
}
