package com.example.my.ubuddy.View;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddEvent extends AppCompatActivity {
    EditText titleEdittext, descriptionEdittext;
    Button setDateButton, setTimeButton, createEventButton;
    Spinner typeSpinner;
    TextView timeTextView, dateTextView;

    int day, month, year, hour, minute;
    ProgressDialog progressDialog;
    private int responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        super.setTitle("New Event");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        Initialization();
        ButtonMethod();
    }

    public void Initialization(){
        titleEdittext=(EditText)findViewById(R.id.eventTitleEdittext);
        descriptionEdittext=(EditText)findViewById(R.id.eventDesriptionEdittext);

        setDateButton=(Button) findViewById(R.id.eventDateButton);
        setTimeButton=(Button) findViewById(R.id.eventTimeButton);
        createEventButton=(Button)findViewById(R.id.createEentButton);

        timeTextView=(TextView) findViewById(R.id.eventTimeTextView);
        dateTextView=(TextView) findViewById(R.id.eventDateTextView);

        typeSpinner=(Spinner)findViewById(R.id.eventTypeSpinner);


    }

    public void ButtonMethod(){
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getandCheckData()) new SendDatatoServer().execute(APILink.CreateEventAPI+ SavedData.api_token,null,null);
            }
        });

        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);
                View v = getLayoutInflater().inflate(R.layout.date_picker,null);
                final DatePicker datePicker = (DatePicker)v.findViewById(R.id.datePicker);
                builder.setView(v);
                builder.setCancelable(false);
                builder.setTitle("Set Date");
                final AlertDialog alertDialog=builder.create();
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                         day = datePicker.getDayOfMonth();
                         month = datePicker.getMonth() + 1;
                         year = datePicker.getYear();
                         dateTextView.setText(day+"-"+month+"-"+year);
                         alertDialog.cancel();
                    }
                });
                builder.show();
            }
        });

        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);
                View v = getLayoutInflater().inflate(R.layout.time_picker,null);
                final TimePicker timePicker = (TimePicker) v.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                builder.setView(v);
                builder.setCancelable(false);
                builder.setTitle("Set Time");
                final AlertDialog alertDialog=builder.create();
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hour = timePicker.getCurrentHour();
                        minute = timePicker.getCurrentMinute();
                        timeTextView.setText(hour+":"+minute);
                        alertDialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    public boolean getandCheckData(){
        boolean result=true;
        if(TextUtils.isEmpty(titleEdittext.getText())){
            titleEdittext.setError("Enter Title");
            result=false;
        }
        if(TextUtils.isEmpty(descriptionEdittext.getText())){
            descriptionEdittext.setError("Enter Description");
            result=false;
        }
        if(TextUtils.isEmpty(timeTextView.getText())){
            Toast.makeText(this, "Please set event time", Toast.LENGTH_SHORT).show();
            result=false;
        }
        if(TextUtils.isEmpty(dateTextView.getText())){
            Toast.makeText(this, "Please set event date", Toast.LENGTH_SHORT).show();
            result=false;
        }
        if (typeSpinner.getSelectedItemPosition()==0){
            Toast.makeText(this, "Please select event type", Toast.LENGTH_SHORT).show();
            result=false;
        }
        return result;
    }

    private class SendDatatoServer extends AsyncTask<String, String, String> {
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("title", titleEdittext.getText().toString());
            params.put("date", year+"-"+month+"-"+day);
            params.put("time", hour+":"+minute+":00");
            params.put("type", typeSpinner.getSelectedItemPosition());
            params.put("description", descriptionEdittext.getText().toString());

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
            progressDialog.cancel();
            if(responseCode==200){
                startActivity(new Intent(AddEvent.this, MainActivity.class));
                finish();
            }
            else{
                android.app.AlertDialog.Builder alertDialog  = new android.app.AlertDialog.Builder(AddEvent.this);
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
