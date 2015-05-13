package com.example.yousiftouma.myapp.helperclasses;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yousiftouma.myapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Populates top list of posts, extends FeedAdapter because of the similarity
 * refer to FeedAdapter for more docs and comments
 */
public class TopListAdapter extends FeedAdapter {


    public TopListAdapter(Context context, ArrayList<JSONObject> posts,
                          OnLikeButtonClickedListener fragment) {
        super(context, posts, fragment);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.toplist_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.location = (TextView) view.findViewById(R.id.location);
            // also has a position view
            viewHolder.position = (TextView) view.findViewById(R.id.position);
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
        // set position to be the index in the list but plus one as index starts at 0
        viewHolder.position.setText(String.valueOf(position + 1));
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
                fragment.onLikeButtonClickedInAdapter();
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

}


