package com.example.yousiftouma.myapp;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yousiftouma.myapp.misc.DynamicAsyncTask;
import com.example.yousiftouma.myapp.misc.FeedAdapter;
import com.example.yousiftouma.myapp.misc.SearchAdapter;
import com.example.yousiftouma.myapp.misc.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity implements
        UserProfileFragment.OnFragmentInteractionListener,
        FeedAdapter.OnCommentButtonClickedListener,
        PostFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        SearchView.OnQueryTextListener{

    private User loggedInUser;
    private FragmentManager fm = getFragmentManager();
    private Fragment newFragment;
    public static String SERVER_URL = "http://mytestapp-youto814.openshift.ida.liu.se/";
    private List<String> members = getAllMembers();
    private Menu menu;

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

    private List<String> getAllMembers() {
        String url = SERVER_URL + "get_users";
        String responseAsString;
        JSONObject responeAsJson;
        members = new ArrayList<>();
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responeAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responeAsJson.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                members.add(jsonArray.getJSONObject(i).getString("username"));
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return members;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            MenuItem searchMenuItem = menu.findItem(R.id.search);

            SearchView search = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setIconifiedByDefault(false);

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String query) {
                    System.out.println("memberlista: " + members);
                    loadHistory(query);
                    return true;
                }
            });
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            String[] columns = new String[] { "_id", "text" };
            Object[] temp = new Object[] { 0, "default" };

            MatrixCursor cursor = new MatrixCursor(columns);

            for(int i = 0; i < members.size(); i++) {
                temp[0] = i;
                temp[1] = members.get(i);
                cursor.addRow(temp);
            }
            final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
            search.setSuggestionsAdapter(new SearchAdapter(this, cursor, members));
        }
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
    public void onPostFragmentUsernameButtonClicked(int userId) {
        newFragment = UserProfileFragment.newInstance(userId);
        replaceFragment();
    }

    @Override
    public void onFeedFragmentItemClicked(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void onNewPostButtonClicked() {
        newFragment = new DoPostFragment();
        replaceFragment();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}
