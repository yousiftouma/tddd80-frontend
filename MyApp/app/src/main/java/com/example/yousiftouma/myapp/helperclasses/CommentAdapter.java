package com.example.yousiftouma.myapp.helperclasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yousiftouma.myapp.MainActivity;
import com.example.yousiftouma.myapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Populates comments ListView
 */
public class CommentAdapter extends BaseAdapter {

    private ArrayList<JSONObject> comments;
    private LayoutInflater mInflater;

    public CommentAdapter(Context context, ArrayList<JSONObject> comments) {
        super();
        this.comments = comments;
        this.mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView username;
        TextView comment;
    }

    /**
     * executed on each item in the ListView
     * @param position position in the list
     * @param convertView the dynamic view for that position
     * @return the view for this item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View view;

        ViewHolder viewHolder;

        // if this is the first time the method is executed on this view
        // we need to fetch the different views in the list item first
        if (convertView == null) {
            view = mInflater.inflate(R.layout.comment_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.username);
            viewHolder.comment = (TextView) view.findViewById(R.id.comment);

            // tag the view with its ViewHolder so we can access the views next time
            // and update them
            view.setTag(viewHolder);
        }

        // we have access to the view and need to (re)populate it
        // so we fetch it
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        JSONObject comment = comments.get(position);
        // we set the views
        try {
            viewHolder.username.setText(getAuthor(comment.getInt("user_id")));
            viewHolder.comment.setText(comment.getString("text"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    // gets the username from user id
    private String getAuthor(int userId){
        String url = MainActivity.SERVER_URL + "get_user_by_id/"
                + userId;
        String responseAsString;
        JSONObject responseAsJson;
        String username = null;
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            JSONArray jsonArray = responseAsJson.getJSONArray("user");
            JSONObject user = jsonArray.getJSONObject(0);
            username = user.getString("username");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return username;
    }
}
