package com.example.yousiftouma.myapp.misc;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * AsyncTask used for post and get to database
 * Has two constructors, one without params which implies GET
 * and one with a json formatted string implying POST
 */
public class DynamicAsyncTask extends AsyncTask<String, Void, String> {

    private String response;
    private ProgressDialog pDialog;
    private String method;
    private String JsonString = null;


    /**
     * This constructor implies POST
     * @param JsonString the string to send with post
     */
    public DynamicAsyncTask(String JsonString) {
        this.method = "POST";
        this.JsonString = JsonString;
    }

    /**
     * This constructor implies GET
     */
    public DynamicAsyncTask() {
        this.method = "GET";
    }


    /**
     * Making service call
     *
     * @param url - url to make request
     */
    public String makeServiceCall(String url) {
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method.equals("POST")) {
                StringEntity stringEntity = new StringEntity(JsonString, HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(stringEntity);
                // Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpResponse = httpClient.execute(httpPost);

            } else if (method.equals("GET")) {
                // appending params to url
                httpResponse = httpClient.execute(new HttpGet(url));
            }

            assert httpResponse != null: "Null httpResponse";
            InputStream inputStream = httpResponse.getEntity().getContent();

            response = convertInputStreamToString(inputStream);

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            e.getMessage();
        }
        assert response != null: "Null string response when converting to string";
        return response;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    @Override
    protected String doInBackground(String... params) {
        return makeServiceCall(params[0]);
    }
}

