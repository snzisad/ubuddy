package com.example.my.ubuddy.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EventView extends AppCompatActivity {

    TextView eventTitle, eventDescription, eventCreator, eventType, eventTime, eventRemainingTime;
    LinearLayout singleEventLayout;
    ProgressBar progressBar;
    int responseCode;
    private final String api=APILink.SingleEventAPI+SavedData.postid+"?api_token="+SavedData.api_token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        super.setTitle("Eventt");

        Initialization();

        new getEvent().execute(api,null,null);
    }

    public void Initialization(){
        eventTitle=(TextView)findViewById(R.id.eventTitleTextview);
        eventDescription=(TextView)findViewById(R.id.eventDescriptionTextview);
        eventCreator=(TextView)findViewById(R.id.eventCreatorTextview);
        eventType=(TextView)findViewById(R.id.eventTypeTextview);
        eventTime=(TextView)findViewById(R.id.eventTimeTextview);
        eventRemainingTime=(TextView)findViewById(R.id.eventRemainingtimeTextview);

        progressBar=(ProgressBar) findViewById(R.id.singleEventProgressBar);

        singleEventLayout=(LinearLayout)findViewById(R.id.singleEventLayout);
    }

    private class getEvent extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            singleEventLayout.setVisibility(View.GONE);
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
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(s);
            if(responseCode==200){
                try {
                    JSONObject singleEvent = new JSONObject(s);
                    JSONObject event_details=singleEvent.getJSONObject("event_details");
                    eventTitle.setText(event_details.getString("title"));
                    eventDescription.setText(event_details.getString("description"));
                    eventTime.setText(event_details.getString("time"));
                    eventRemainingTime.setText(event_details.getString("event_time"));
                    eventType.setText(event_details.getString("event_type_text"));

                    JSONObject user = singleEvent.getJSONObject("user");
                    eventCreator.setText(user.getString("firstname")+" "+user.getString("lastname"));
                    singleEventLayout.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(EventView.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new getEvent().execute(api,null,null);
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
}
