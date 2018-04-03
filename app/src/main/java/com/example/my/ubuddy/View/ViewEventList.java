package com.example.my.ubuddy.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewEventList extends AppCompatActivity {

    Button backtoDiscussion;
    LinearLayout eventListLayout;
    ProgressBar progressBar;
    ArrayList<String> eventtitle, eventInfo;
    ArrayList<Integer> eventID;
    int responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_list);
        backtoDiscussion=(Button)findViewById(R.id.discussionButton);
        super.setTitle("Event List");
        backtoDiscussion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ViewEventList.this, MainActivity.class));
                finish();
            }
        });

        eventListLayout=(LinearLayout)findViewById(R.id.eventlistLsyout);
        progressBar=(ProgressBar)findViewById(R.id.eventListProgressBar);

        new getEvent().execute(APILink.AllEventAPI+SavedData.api_token,null,null);
    }

    private class getEvent extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            eventListLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
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
                    eventtitle=new ArrayList<>();
                    eventInfo=new ArrayList<>();
                    eventID=new ArrayList<>();

                    JSONArray eventArray = new JSONArray(s);

                    for(int i=0;i<eventArray.length();i++){
                        JSONObject singleEvent = eventArray.getJSONObject(i);
                        eventID.add(singleEvent.getInt("id"));

                        JSONObject singleEventDetails = singleEvent.getJSONObject("event_details");

                        eventtitle.add(singleEventDetails.getString("title"));
                        eventInfo.add(singleEventDetails.getString("event_type_text")+" @ "+singleEventDetails.getString("event_time"));

//
                    }

                    for(int i=0;i<eventID.size();i++){
                        eventListLayout.addView(listData(i));
                    }

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

                eventListLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }
            else{
                try {
                    progressBar.setVisibility(View.GONE);
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(ViewEventList.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new getEvent().execute(APILink.AllEventAPI+ SavedData.api_token,null,null);
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
        View v = inflater.inflate(R.layout.event_list_style, null,true);

        LinearLayout layout=(LinearLayout) v.findViewById(R.id.singleEventLayout);

        final TextView title = (TextView) v.findViewById(R.id.eventTitle);
        final TextView info = (TextView) v.findViewById(R.id.eventinfo);

        title.setText(eventtitle.get(position));
        info.setText(eventInfo.get(position));


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedData.postid=eventID.get(position);
                startActivity(new Intent(ViewEventList.this, EventView.class));
            }
        });

        return v;
    }

}
