package com.example.yousiftouma.myapp;

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
        FeedAdapter.OnCommentButtonClickedListener{

    private User loggedInUser;

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

            // Create a new Fragment to be placed in the activity layout
            UserProfileFragment firstFragment = UserProfileFragment.newInstance(loggedInUser.getId());

            // Add fragment to container
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
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
    public void onFeedItemSelected(JSONObject post) {
        System.out.println("From activity, changing fragment to post for: " + post);
    }

    @Override
    public void onCommentClicked(JSONObject post) {
        System.out.println("From activity, changing fragment to post with comment" +
                "for: " + post);
    }
}
