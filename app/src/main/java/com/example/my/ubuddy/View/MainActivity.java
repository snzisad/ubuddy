package com.example.my.ubuddy.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
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

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.BusinessLogic.SavedData;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.View.Authentication.LoginActivity;

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

public class MainActivity extends AppCompatActivity {
    EditText postEdittext;
    String postText;
    ProgressBar progressBar;
    AlertDialog alertDialog;
    TextView departmentTextView, sessionTextView, universityTextView, addeventButton;
    Button refreshButton, scheduleButton, eventButtons, singleEventButton, submitpostButton;
    ProgressBar groupnewsfeedProgressBar;
    LinearLayout groupnewsfeedLayout, postlistLayout;
    int responseCode;

    ArrayList <String> nameList, date, content, eventType, eventTime, likeCount, commentCount;
    ArrayList <Integer> id;
    ArrayList <Boolean> isLiked, hasEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.setTitle("UBuddy");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View v=getLayoutInflater().inflate(R.layout.activity_create_post,null);
                builder.setView(v);

                alertDialog=builder.create();
                alertDialog.show();;

                postEdittext=(EditText)v.findViewById(R.id.postEdittext);
                submitpostButton=(Button) v.findViewById(R.id.submitpostButton);
                addeventButton=(TextView) v.findViewById(R.id.addeventButton);
                progressBar=(ProgressBar)v.findViewById(R.id.progressBar);
                addeventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, AddEvent.class));
                    }
                });

                submitpostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postText=postEdittext.getText().toString();
                        if(TextUtils.isEmpty(postText)){
                            postEdittext.setError("Please write your post");
                        }
                        else{
                            new SendDatatoServer().execute(APILink.CreatePostAPI+ SavedData.api_token);
                        }
                    }
                });

            }
        });


        //Initialize all object
        Initialization();

        //Load Data
        if(checkInternet())  new getNewsFeed().execute(APILink.GroupNewsFeedAPI+SavedData.api_token,null,null);

    }

    public void Initialization(){
        departmentTextView=(TextView)findViewById(R.id.departmentName);
        sessionTextView=(TextView)findViewById(R.id.sessionName);
        universityTextView=(TextView)findViewById(R.id.universityName);



        groupnewsfeedProgressBar=(ProgressBar) findViewById(R.id.groupnewsfeedProgressBar);

        groupnewsfeedLayout=(LinearLayout) findViewById(R.id.groupnewsfeedLayout);
        postlistLayout=(LinearLayout) findViewById(R.id.AllpostLayout);

        refreshButton=(Button)findViewById(R.id.refreshButton);
        scheduleButton=(Button) findViewById(R.id.scheduleButton);
        eventButtons=(Button) findViewById(R.id.eventButton);

        eventButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ViewEventList.class));
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
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
            AlertDialog.Builder alertDialog  = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("No Internet");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Please check your internet Connection and try again");
            alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
            alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(checkInternet())  new getNewsFeed().execute(APILink.GroupNewsFeedAPI+SavedData.api_token,null,null);
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v=getLayoutInflater().inflate(R.layout.activity_finish,null);
        builder.setView(v);

        LinearLayout exitButton=(LinearLayout)v.findViewById(R.id.exitButton);
        LinearLayout logoutButton=(LinearLayout)v.findViewById(R.id.logoutButton);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                System.exit(0);
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=getSharedPreferences("LoginDetails",0).edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        builder.create();
        builder.show();
    }


    private class getNewsFeed extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            groupnewsfeedLayout.setVisibility(View.GONE);
            groupnewsfeedProgressBar.setVisibility(View.VISIBLE);
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
            groupnewsfeedProgressBar.setVisibility(View.GONE);
            super.onPostExecute(s);
            if(responseCode==200){
                try {
                    JSONObject parent = new JSONObject(s);

                    String department=parent.getString("departmentName");
                    department=department.replace("\r\n"," ");
                    departmentTextView.setText(department);

                    universityTextView.setText(parent.getString("universityName"));
                    sessionTextView.setText("Session: "+parent.getString("session_years"));

                    nameList=new ArrayList<>();
                    date=new ArrayList<>();
                    likeCount=new ArrayList<>();
                    commentCount=new ArrayList<>();
                    content=new ArrayList<>();
                    id=new ArrayList<>();
                    eventTime=new ArrayList<>();
                    eventType=new ArrayList<>();
                    hasEvent=new ArrayList<>();
                    isLiked=new ArrayList<>();

                    JSONArray posts = parent.getJSONArray("posts");

                    for(int i=0;i<posts.length();i++){
                        JSONObject singlePost = posts.getJSONObject(i);
                        id.add(singlePost.getInt("id"));
                        content.add(singlePost.getString("text"));
                        isLiked.add(singlePost.getBoolean("isLiked"));
                        likeCount.add(singlePost.getString("likesCount"));
                        date.add(singlePost.getString("postedAt"));
                        commentCount.add(singlePost.getString("commentsCount"));
                        JSONObject user = singlePost.getJSONObject("user");
                        nameList.add(user.getString("firstname")+" "+user.getString("lastname"));

                        if(singlePost.getBoolean("has_event")){
                            hasEvent.add(true);
                            JSONObject event_details = singlePost.getJSONObject("event_details");
                            eventType.add(event_details.getString("event_type_text"));
                            eventTime.add(event_details.getString("event_time"));
                        }
                        else{
                            hasEvent.add(false);
                            eventType.add(null);
                            eventTime.add(null);
                        }
                    }

                    for(int i=0;i<id.size();i++){
                        postlistLayout.addView(listData(i));
                    }

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

                groupnewsfeedLayout.setVisibility(View.VISIBLE);

            }
            else{
                try {

                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Your session has expired. Please Log in to cotiunue.");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor=getSharedPreferences("LoginDetails",0).edit();
                            editor.clear();
                            editor.commit();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
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
        View v = inflater.inflate(R.layout.list_post_style, null,true);

        LinearLayout layout=(LinearLayout) v.findViewById(R.id.postlistLayout);
        LinearLayout likeButton=(LinearLayout) v.findViewById(R.id.likeButton);
        LinearLayout commentButton=(LinearLayout) v.findViewById(R.id.commentLayout);

        final TextView name = (TextView) v.findViewById(R.id.nameView);
        final TextView time = (TextView) v.findViewById(R.id.dateView);
        TextView post = (TextView) v.findViewById(R.id.contentTextView);
        final TextView totalLike = (TextView) v.findViewById(R.id.likenumberTextview);
        TextView totalComment = (TextView) v.findViewById(R.id.commentnumberTextview);
        final TextView likeText = (TextView)v.findViewById(R.id.likeunlikeTextView);
        singleEventButton=(Button) v.findViewById(R.id.showEventButton);

        name.setText(nameList.get(position));
        time.setText(date.get(position));
        post.setText(content.get(position));
        totalLike.setText(likeCount.get(position));
        totalComment.setText(commentCount.get(position));
        final int postid=id.get(position);

        singleEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedData.postid=postid;
                startActivity(new Intent(MainActivity.this, EventView.class));
            }
        });

        if(isLiked.get(position)){
            likeText.setText("Unlike");
        }
        else{
            likeText.setText("Like");
        }

        if (hasEvent.get(position)){
            singleEventButton.setText(eventType.get(position)+" @ "+eventTime.get(position));
            singleEventButton.setVisibility(View.VISIBLE);
        }

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedData.postid=postid;
                startActivity(new Intent(MainActivity.this, PostView.class));
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedData.postid=postid;
                startActivity(new Intent(MainActivity.this, PostView.class));
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likeText.getText().equals("Like")){
                    likeText.setText("Unlike");
                    int like = Integer.parseInt(totalLike.getText().toString());
                    totalLike.setText(""+(like+1));
                    new LikeUnlike().execute(APILink.LikeUnlikeAPI+postid+"/like?api_token="+SavedData.api_token);
                }
                else{
                    likeText.setText("Like");
                    int like = Integer.parseInt(totalLike.getText().toString());
                    totalLike.setText(""+(like-1));
                    new LikeUnlike().execute(APILink.LikeUnlikeAPI+postid+"/unlike?api_token="+SavedData.api_token);
                }
            }
        });

        return v;
    }

    private class SendDatatoServer extends AsyncTask<String, String, String> {
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("text", postEdittext.getText().toString());

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
            progressBar.setVisibility(View.GONE);
            if(responseCode==200){
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
            else{
                AlertDialog.Builder alertDialog  = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Sorry, something went wrong. Please try again");
                alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                alertDialog.setPositiveButton("Ok",null);
                alertDialog.create();
                alertDialog.show();
            }
        }

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

//            Toast.makeText(MainActivity.this, "Code: "+responseCode, Toast.LENGTH_SHORT).show();
        }

    }
}
