package com.example.my.ubuddy.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostView extends AppCompatActivity {
    LinearLayout singlePostLayout,commentListLayout,likeunlikeButton;
    ProgressBar progressBar;
    TextView name, postdate, post, likenumber, likeunlikeTextView;
    int responseCode;
    ArrayList<String> nameList, content, date;

    Button  submitCommetnButton, eventView;
    EditText commnetEdittext;
    Boolean isLiked, hasEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        super.setTitle("Post");
        Initialiation();
        buttonMethod();

        if(checkInternet())  new PostView.getAllComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments?api_token="+SavedData.api_token,null,null);
    }

    public void Initialiation(){
        singlePostLayout=(LinearLayout)findViewById(R.id.singlepostLayout);
        commentListLayout=(LinearLayout)findViewById(R.id.viewcommentLayout);
        progressBar=(ProgressBar)findViewById(R.id.singlepostProgressBar);

        name=(TextView)findViewById(R.id.nameView);
        postdate=(TextView)findViewById(R.id.dateView);
        post=(TextView)findViewById(R.id.contentTextView);
        likenumber=(TextView)findViewById(R.id.likenumber);
        likeunlikeTextView=(TextView)findViewById(R.id.likeunlikeTextView);

        likeunlikeButton=(LinearLayout) findViewById(R.id.likeButton);
        submitCommetnButton=(Button)findViewById(R.id.submitcommentButton);
        eventView=(Button)findViewById(R.id.showEventButton);

        eventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostView.this, EventView.class));
            }
        });

        commnetEdittext=(EditText)findViewById(R.id.commnetEdittext);


    }

    public void buttonMethod(){
        submitCommetnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(commnetEdittext.getText())){
                    Toast.makeText(PostView.this, "Please write your commnet", Toast.LENGTH_SHORT).show();
                }
                else if(checkInternet()) new PostView.makeComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments/create?api_token="+SavedData.api_token,null,null);
            }
        });

        likeunlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likeunlikeTextView.getText().equals("Like")){
                    likeunlikeTextView.setText("Unlike");
                    int like = Integer.parseInt(likenumber.getText().toString());
                    likenumber.setText(""+(like+1));
                    new LikeUnlike().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/like?api_token="+SavedData.api_token);
                }
                else{
                    likeunlikeTextView.setText("Like");
                    int like = Integer.parseInt(likenumber.getText().toString());
                    likenumber.setText(""+(like-1));
                    new LikeUnlike().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/unlike?api_token="+SavedData.api_token);
                }
            }
        });
    }

    public boolean checkInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        else{
            AlertDialog.Builder alertDialog  = new AlertDialog.Builder(PostView.this);
            alertDialog.setTitle("No Internet");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Please check your internet Connection and try again");
            alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
            alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(checkInternet())  new PostView.getAllComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments?api_token="+SavedData.api_token,null,null);
                }
            });
            alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onBackPressed();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }



        return false;
    }

    private class getAllComment extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            singlePostLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new getPost().execute(APILink.LikeUnlikeAPI+SavedData.postid+"?api_token="+SavedData.api_token);
        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.connect();
                responseCode=httpURLConnection.getResponseCode();

                if(responseCode==422){
                    in = httpURLConnection.getErrorStream();
                }
                else {
                    in = httpURLConnection.getInputStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(in);

                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line;
                while((line=bufferedReader.readLine())!=null){
                    data.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseCode==200){
                try {
                    nameList=new ArrayList<>();
                    date=new ArrayList<>();
                    content=new ArrayList<>();

                    JSONArray comments = new JSONArray(s);

                    for(int i=0;i<comments.length();i++){
                        JSONObject singlePost = comments.getJSONObject(i);
                        content.add(singlePost.getString("text"));
                        date.add(singlePost.getString("postedAt"));
                        nameList.add(singlePost.getString("user_name"));
                    }

                    for(int i=0;i<content.size();i++){
                        commentListLayout.addView(listData(i));
                    }

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

                singlePostLayout.setVisibility(View.VISIBLE);

            }
            else{
                try {

                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(PostView.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(checkInternet())  new PostView.getAllComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments?api_token="+SavedData.api_token,null,null);
                        }
                    });
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            progressBar.setVisibility(View.GONE);
        }

    }

    private class getPost extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.connect();
                responseCode=httpURLConnection.getResponseCode();

                if(responseCode==422){
                    in = httpURLConnection.getErrorStream();
                }
                else {
                    in = httpURLConnection.getInputStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(in);

                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line;
                while((line=bufferedReader.readLine())!=null){
                    data.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseCode==200){
                try {
                    JSONObject singlePost = new JSONObject(s);
                    post.setText(singlePost.getString("text"));
                    postdate.setText(singlePost.getString("postedAt"));
                    likenumber.setText(singlePost.getString("likesCount"));
                    if(singlePost.getBoolean("isLiked")){
                        likeunlikeTextView.setText("Unlike");
                    }
                    else{
                        likeunlikeTextView.setText("Like");
                    }

                    JSONObject user = singlePost.getJSONObject("user");
                    name.setText(user.getString("firstname")+" "+user.getString("lastname"));

                    if(singlePost.getBoolean("has_event")){
                        JSONObject event = singlePost.getJSONObject("event_details");
                        eventView.setText(event.getString("event_type_text")+" @ "+event.getString("event_time"));
                        eventView.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(PostView.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(checkInternet())  new PostView.getAllComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments?api_token="+SavedData.api_token,null,null);
                        }
                    });
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public View listData(final int position){
        LayoutInflater inflater=this.getLayoutInflater();
        View v = inflater.inflate(R.layout.list_comment_style, null,true);

        LinearLayout layout=(LinearLayout) v.findViewById(R.id.commentlistLayout);

        final TextView name = (TextView) v.findViewById(R.id.nameView);
        final TextView time = (TextView) v.findViewById(R.id.dateView);
        TextView post = (TextView) v.findViewById(R.id.contentTextView);

        name.setText(nameList.get(position));
        time.setText(date.get(position));
        post.setText(content.get(position));

        return v;
    }

    private class LikeUnlike extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.connect();

                responseCode=httpURLConnection.getResponseCode();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

    }

    private class makeComment extends AsyncTask<String, String, String> {
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            singlePostLayout.setVisibility(View.GONE);

            Map<String,Object> params = new LinkedHashMap<>();
            params.put("text", commnetEdittext.getText().toString());

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                try {
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            try {
                postDataBytes = postData.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder data = new StringBuilder();

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.getOutputStream().write(postDataBytes);
                httpURLConnection.connect();
                responseCode=httpURLConnection.getResponseCode();

                if(responseCode==422){
                    in = httpURLConnection.getErrorStream();
                }
                else {
                    in = httpURLConnection.getInputStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(in);

                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line;
                while((line=bufferedReader.readLine())!=null){
                    data.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseCode==200){
                new PostView.getAllComment().execute(APILink.LikeUnlikeAPI+SavedData.postid+"/comments?api_token="+SavedData.api_token,null,null);
                commnetEdittext.setText(null);
            }
            else{
                progressBar.setVisibility(View.GONE);
                singlePostLayout.setVisibility(View.VISIBLE);
                AlertDialog.Builder alertDialog  = new AlertDialog.Builder(PostView.this);
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Sorry, something went wrong. Please try again");
                alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                alertDialog.setPositiveButton("Ok",null);
                alertDialog.create();
                alertDialog.show();
            }
        }

    }
}
