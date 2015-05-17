package com.example.yousiftouma.myapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.yousiftouma.myapp.helperclasses.Constants;
import com.example.yousiftouma.myapp.helperclasses.DynamicAsyncTask;
import com.example.yousiftouma.myapp.helperclasses.FeedAdapter;
import com.example.yousiftouma.myapp.helperclasses.FetchAddressIntentService;
import com.example.yousiftouma.myapp.helperclasses.SearchAdapter;
import com.example.yousiftouma.myapp.helperclasses.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * our main activity to keep hold of fragments and switch between them
 * in the actual app
 */
public class MainActivity extends ActionBarActivity implements
        UserProfileFragment.OnUserProfileFragmentInteractionListener,
        FeedAdapter.OnCommentButtonClickedListener,
        FeedAdapter.OnUsernameClickedListener,
        PostFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        SearchAdapter.OnSearchItemClickedListener,
        TopListFragment.OnTopListFragmentInteraction,
        DoPostFragment.OnPostFragmentInteractionListener,
        LikedPostsFragment.OnFragmentInteractionListener,
        SearchView.OnQueryTextListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private User mLoggedInUser;
    private FragmentManager fm = getFragmentManager();
    private Fragment newFragment;
    public static String SERVER_URL = "http://mytestapp-youto814.openshift.ida.liu.se/";

    private List<String> users = getAllUsers();

    private Menu menu;
    private MenuItem searchMenuItem;

    // fields related to our address service
    private AddressResultReceiver mResultReceiver;
    protected boolean mAddressRequested;
    protected static final String TAG = "main";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected String mAddressOutput;
    protected static String mLocationAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoggedInUser = User.getInstance();

        mResultReceiver = new AddressResultReceiver(new Handler());

        mAddressRequested = false;
        mAddressOutput = "";
        updateValuesFromSavedState(savedInstanceState);

        buildGoogleApiClient();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // If restored from previous state, don't do anything to avoid
            // overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            newFragment = new FeedFragment();

            // Add fragment to container
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, newFragment).commit();
        }
    }

    /**
     * when we need to save state
     * @param savedInstanceState bundle with info for the state
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * retrieve values from the saved state
     */
    private void updateValuesFromSavedState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                setAddressOutput();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed, error code = " + connectionResult.getErrorCode());
    }

    public class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Get the address string,
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            setAddressOutput();
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }
        }
    }

    private void setAddressOutput() {
        mLocationAddress = mAddressOutput;
    }

    private void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    /**
     * init the fetch address intent
     */
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * creates the menu bar and init the search field
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            searchMenuItem = menu.findItem(R.id.search);

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
                    loadHistory(query);
                    return true;
                }
            });
        }
        return true;
    }

    /**
     * makes sure the list is updated when updating search query
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadHistory(String query) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            String[] columns = new String[]{"_id", "text"};
            Object[] temp = new Object[]{0, "default"};

            MatrixCursor cursor = new MatrixCursor(columns);

            // we create a temporary list of users that match current query
            ArrayList<String> tempMembers = new ArrayList<>();

            // normalize input with data to match even if you type lowercase and name
            // is uppercase
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).toLowerCase().contains(query.toLowerCase())) {
                    tempMembers.add(users.get(i));
                }
            }

            for (int i = 0; i < tempMembers.size(); i++) {
                temp[0] = i;
                temp[1] = tempMembers.get(i);
                cursor.addRow(temp);
            }
            final SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
            search.setSuggestionsAdapter(new SearchAdapter(this, cursor, tempMembers));
        }
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        hideKeyboard();
        switch (id) {
            case R.id.action_top_list:
                newFragment = new TopListFragment();
                replaceFragment();
                break;
            case R.id.action_do_post:
                newFragment = new DoPostFragment();
                replaceFragment();
                break;
            case R.id.action_discover:
                newFragment = PostFragment.newInstance();
                replaceFragment();
                break;
            case R.id.action_feed:
                newFragment = new FeedFragment();
                replaceFragment();
                break;
            case R.id.action_profile:
                newFragment = UserProfileFragment.newInstance(mLoggedInUser.getId());
                replaceFragment();
                break;
            case R.id.action_logout:
                mLoggedInUser.clearInfo();
                fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                logOutUser();
                break;
            case R.id.action_my_liked_posts:
                newFragment = LikedPostsFragment.newInstance(mLoggedInUser.getId());
                replaceFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOutUser(){
        Intent logOutIntent = new Intent(this, LoginActivity.class);
        startActivity(logOutIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() != 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * returns a list of all users
     */
    private List<String> getAllUsers() {
        String url = SERVER_URL + "get_users";
        String responseAsString;
        JSONObject responseAsJson;
        ArrayList<String> userNames = new ArrayList<>();
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("users");
            for (int i = 0; i < jsonArray.length(); i++) {
                userNames.add(jsonArray.getJSONObject(i).getString("username"));
            }
        } catch (InterruptedException | JSONException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return userNames;
    }

    /**
     * add the current newFragment to the fragment container and pushes current
     * fragment to BackStack
     */
    private void replaceFragment() {
        fm.beginTransaction()
                .replace(R.id.fragment_container, newFragment).addToBackStack(null).commit();
    }

    // Methods below are listener methods for the implemented interfaces

    @Override
    public void onUserLikesButtonClicked(int userId) {
        newFragment = LikedPostsFragment.newInstance(userId);
        replaceFragment();
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
    public void onUsernameClickedInAdapter(int userId) {
        newFragment = UserProfileFragment.newInstance(userId);
        replaceFragment();
    }

    @Override
    public void onFeedFragmentItemClicked(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void onPostClickedInLikedPostsFragment(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void onNewPostButtonClicked() {
        newFragment = new DoPostFragment();
        replaceFragment();
    }

    @Override
    public void onSearchItemClicked(int userId) {
        newFragment = UserProfileFragment.newInstance(userId);
        replaceFragment();
        searchMenuItem.collapseActionView();
    }

    @Override
    public void onTopListItemClicked(JSONObject post) {
        newFragment = PostFragment.newInstance(post, false);
        replaceFragment();
    }

    @Override
    public void OnAddNewPostGetAddress() {
        startIntentService();
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
