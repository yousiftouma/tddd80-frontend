package com.example.yousiftouma.myapp.helperclasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yousiftouma.myapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Populates a list of users
 */
public class UserAdapter extends BaseAdapter {

    private ArrayList<JSONObject> users;
    private LayoutInflater mInflater;

    public UserAdapter(Context context, ArrayList<JSONObject> users) {
        super();
        this.users = users;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        TextView username;
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
            view = mInflater.inflate(R.layout.useradapter_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) view.findViewById(R.id.username);

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
        JSONObject user = users.get(position);
        // we set the view
        try {
            viewHolder.username.setText(user.getString("username"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }
}
