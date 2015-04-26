package com.example.yousiftouma.myapp.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yousiftouma.myapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * populates comments
 */
public class CommentAdapter extends BaseAdapter {

    private ArrayList<JSONObject> comments;
    private LayoutInflater mInflater;
    private ViewHolder viewHolder;
    private JSONObject comment;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.comment_item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.username);
            viewHolder.comment = (TextView) view.findViewById(R.id.comment);

            view.setTag(viewHolder);
        }

        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        comment = comments.get(position);
        try {
            viewHolder.username.setText(getAuthor(comment.getInt("user_id")));
            viewHolder.comment.setText(comment.getString("text"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    private String getAuthor(int userId){
        String url = "http://mytestapp-youto814.openshift.ida.liu.se/get_user_by_id/"
                + userId;
        String responseAsString;
        JSONObject responseAsJson;
        String username = null;
        try {
            responseAsString = new DynamicAsyncTask().execute(url).get();
            responseAsJson = new JSONObject(responseAsString);
            JSONObject user = responseAsJson.getJSONObject("user");
            username = user.getString("username");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        return username;
    }
}
