package com.example.yousiftouma.myapp.misc;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yousiftouma.myapp.R;

import java.util.List;

/**
 * populates search
 */
public class SearchAdapter extends CursorAdapter {

    private List<String> members;

    private TextView username;

    public SearchAdapter(Context context, Cursor cursor, List<String> members) {
        super(context, cursor, false);
        this.members = members;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        username.setText(members.get(cursor.getPosition()));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_listview_members, parent, false);

        username = (TextView) view.findViewById(R.id.member);
        return view;
    }

}
