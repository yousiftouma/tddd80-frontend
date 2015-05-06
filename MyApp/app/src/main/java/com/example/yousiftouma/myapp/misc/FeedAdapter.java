package com.example.yousiftouma.myapp.misc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yousiftouma.myapp.MainActivity;
import com.example.yousiftouma.myapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Populates feed
 */
public class FeedAdapter extends BaseAdapter {

    protected ArrayList<JSONObject> posts;
    protected LayoutInflater mInflater;
    protected Context context;
    protected JSONObject post;
    protected ViewHolder viewHolder;
    protected User mLoggedInUser = User.getInstance();

    public FeedAdapter(Context context, ArrayList<JSONObject> posts) {
        super();
        this.posts = posts;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    protected class ViewHolder {
        TextView position; // only exists in subclass TopListAdapter
        TextView username;
        TextView title;
        TextView description;
        TextView numberOfLikes;
        TextView numberOfComments;
        ImageButton buttonLike;
        Button buttonComment;
        TextView location;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.feed_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.location = (TextView) view.findViewById(R.id.location);
            viewHolder.buttonLike = (ImageButton) view.findViewById(R.id.button_like);
            viewHolder.buttonComment = (Button) view.findViewById(R.id.button_comment);
            viewHolder.username = (TextView) view.findViewById(R.id.username);
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            viewHolder.description = (TextView) view.findViewById(R.id.description);
            viewHolder.numberOfLikes = (TextView) view.findViewById(R.id.likesView);
            viewHolder.numberOfComments = (TextView) view.findViewById(R.id.commentsView);

            view.setTag(viewHolder);
        }

        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        /**
         * Gets a JSON object containing a post, gets the data
         * from every key and sets it to the corresponding TextView
         */
        post = posts.get(position);
        viewHolder.buttonLike.setTag(R.id.button_position_in_feed, position);
        viewHolder.buttonComment.setTag(position);
        try {
            viewHolder.username.setText(post.getString("artist"));
            viewHolder.title.setText(post.getString("title"));
            viewHolder.description.setText(post.getString("description"));
            viewHolder.numberOfLikes.setText(getNumberOfLikes(post.getInt("id")));
            viewHolder.numberOfComments.setText(getNumberOfComments(post.getInt("id")));
            viewHolder.location.setText("Uploaded from " + post.getString("location"));
            // if post is already liked
            if (mLoggedInUser.getLikes().contains(post.getInt("id"))){
                viewHolder.buttonLike.setImageDrawable(context.getResources()
                        .getDrawable(R.mipmap.liked_50));
                viewHolder.buttonLike.setTag(R.id.like_status, "unlike");
            }
            // else post is not liked, our "default"
            // (needed, otherwise buttons may be set wrongly)
            else {
                viewHolder.buttonLike.setImageDrawable(context.getResources()
                    .getDrawable(R.mipmap.not_liked_50));
                viewHolder.buttonLike.setTag(R.id.like_status, "like");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            e.getMessage();
        }

        viewHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag(R.id.button_position_in_feed);
                String buttonStatus = (String) v.getTag(R.id.like_status);
                doLikeOrUnlike(buttonStatus, position, (ImageButton) v);
                mLoggedInUser.setLikes();
                // run getView again for this particular row so it is updated
                getView(position, view, parent);
            }
        });

        viewHolder.buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                onCommentButtonClicked(v, position);

            }
        });

        return view;
    }

    public interface OnCommentButtonClickedListener {
        public void onCommentClickedInFeedAdapter(JSONObject post);
    }

    public void onCommentButtonClicked(View view, int position) {
        Context context = view.getContext();
        String TAG = "CommentButtonClicked Error";
        JSONObject post = posts.get(position);

        if (context instanceof OnCommentButtonClickedListener) {
            ((OnCommentButtonClickedListener) context).onCommentClickedInFeedAdapter(post);
        }
        else {
            Log.w(TAG, "Activity should implement OnCommentButtonClickedListener:"
                    + context.getClass().getName());
        }
    }

    protected void doLikeOrUnlike(String buttonStatus, int pos, ImageButton button) {
        String url;
        String response = null;
        String JsonString = createJsonForLikeOrUnlike(pos);

        if (buttonStatus.equals("like") ) {
            url = MainActivity.SERVER_URL + "like/";
        }
        else { // It says unlike and we do that
            url = MainActivity.SERVER_URL + "unlike/";
        }
        try {
            String responseJsonString = new DynamicAsyncTask(JsonString).execute(url).get();
            JSONObject responseAsJson = new JSONObject(responseJsonString);
            response = responseAsJson.getString("result");
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        switch (response) {
            case "liked":
                button.setImageDrawable(context.getResources().getDrawable(R.mipmap.liked_50));
                break;
            case "unliked":
                button.setImageDrawable(context.getResources().getDrawable(R.mipmap.not_liked_50));
                break;
        }
    }

    protected String createJsonForLikeOrUnlike(int pos) {
        int actionUserId = mLoggedInUser.getId();
        JSONObject finishedAction = null;
        try {
            int postId = posts.get(pos).getInt("id");

            JSONObject action = new JSONObject();
            action.put("user_id", actionUserId);
            action.put("post_id", postId);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(action);

            finishedAction = new JSONObject();
            finishedAction.put("action", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert finishedAction != null: "Some JSONException when trying to create" +
                "object to send to like/unlike";
        return finishedAction.toString();
    }

    protected String getNumberOfLikes(int postId) {
        String url = MainActivity.SERVER_URL + "get_number_of_likes_for_post/"
                + postId;
        String responseAsString;
        JSONObject responseAsJson;
        String numberOfLikes = null;
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            numberOfLikes = responseAsJson.getString("number_of_likes");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return numberOfLikes;
    }

    protected String getNumberOfComments(int postId) {
        String responseAsString;
        JSONObject responseAsJson;
        String numberOfComments = null;
        try {
            String url = MainActivity.SERVER_URL +
                    "get_number_of_comments_for_post_by_id/"
                    + postId;
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            numberOfComments = responseAsJson.getString("number_of_comments");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return numberOfComments;
    }
}
