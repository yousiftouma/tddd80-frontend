package com.example.yousiftouma.myapp.misc;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yousiftouma.myapp.MainActivity;
import com.example.yousiftouma.myapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        username.setBackgroundColor(Color.BLACK);
        username.setTextColor(Color.WHITE);
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.LTGRAY);
                ((TextView) v).setTextColor(Color.BLACK);
                Context context = v.getContext();
                String TAG = "SearchItemClicked Error";
                String username = ((TextView) v).getText().toString();
                int userId = getUserId(username);

                if (context instanceof OnSearchItemClickedListener) {
                    ((OnSearchItemClickedListener) context).onSearchItemClicked(userId);
                } else {
                    Log.w(TAG, "Activity should implement OnSearchItemClickedListener:"
                            + context.getClass().getName());
                }
            }
        });
    }

    private int getUserId(String username) {

        String url = MainActivity.SERVER_URL + "get_user_by_username/"
                + username;
        int id = -1;
        try {
            String response = new DynamicAsyncTask().execute(url).get();
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("user");
            JSONObject user = jsonArray.getJSONObject(0);
            id = user.getInt("id");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            e.getMessage();
        }
        assert (id != -1) : "could not get proper id";
        return id;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_listview_members, parent, false);

        username = (TextView) view.findViewById(R.id.member);
        return view;
    }

    public interface OnSearchItemClickedListener {
        public void onSearchItemClicked(int userId);
    }

}
