package com.example.my.ubuddy.View.Authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.View.MainActivity;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import org.json.JSONArray;
import org.json.JSONException;

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

public class UniversityActivity extends AppCompatActivity {

    Spinner universitySpinner, facultySpinner, departmentSpinner, sessionSpinner;
    LinearLayout universityLayout, facultyLayout, departmentLayout, sessionLayout;
    Button submitButton;
    int university, faculty, department, session;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<String> universityList, facultyList, departmentList, sessionList;
    ArrayList<Integer> universityid, facultyid, departmentid, sessionid;
    private int responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTitle("Information");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        sharedPreferences=getSharedPreferences("LoginDetails",0);
        editor=sharedPreferences.edit();

        initialization();

        new getUniversity().execute(APILink.UniversityAPI,null,null);
    }

    public void initialization(){
        universitySpinner=(Spinner)findViewById(R.id.universitySpinner);
        departmentSpinner=(Spinner)findViewById(R.id.deppartmentSpinner);
        facultySpinner=(Spinner)findViewById(R.id.facultySpinner);
        sessionSpinner=(Spinner)findViewById(R.id.sessionSpinner);

        submitButton=(Button)findViewById(R.id.InfoSubmitButton);

        universityLayout=(LinearLayout)findViewById(R.id.universityLayout);
        departmentLayout=(LinearLayout)findViewById(R.id.departmentLayout);
        facultyLayout=(LinearLayout)findViewById(R.id.facultyLayout);
        sessionLayout=(LinearLayout)findViewById(R.id.sessionLayout);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getAndCheckData()){
                    new saveSession().execute(APILink.SaveSessionAPI+SavedData.api_token);
                }
            }
        });

    }

    public boolean getAndCheckData(){

        if(universitySpinner.getSelectedItem().toString().equals("Select University")){
            Toast.makeText(this, "Please select universty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(facultySpinner.getSelectedItem().toString().equals("Select Faculty")){
            Toast.makeText(this, "Please select faculty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(departmentSpinner.getSelectedItem().toString().equals("Select Department")) {
            Toast.makeText(this, "Please select department", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(sessionSpinner.getSelectedItem().toString().equals("Select Session")) {
            Toast.makeText(this, "Please select session", Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;

    }

    private class getUniversity extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            progressDialog.show();
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
                    JSONArray parent = new JSONArray(s);
                    universityList =new ArrayList<>();
                    universityid =new ArrayList<>();
                    universityList.add("Select University");

                    for(int i=0;i<parent.length();i++){
                        universityList.add(parent.getJSONObject(i).getString("name"));
                        universityid.add(parent.getJSONObject(i).getInt("id"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UniversityActivity.this, android.R.layout.simple_spinner_item, universityList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    universitySpinner.setAdapter(adapter);
                    progressDialog.cancel();

                    universitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i==0){
                                Toast.makeText(UniversityActivity.this, "Please select your university", Toast.LENGTH_SHORT).show();
                                facultyLayout.setVisibility(View.INVISIBLE);
                                submitButton.setVisibility(View.INVISIBLE);
                                departmentLayout.setVisibility(View.INVISIBLE);
                                sessionLayout.setVisibility(View.INVISIBLE);
                            }
                            else{
                                new getFaculty().execute(APILink.FacultyAPI+universityid.get(i-1),null,null);
                                university=universityid.get(i-1);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    progressDialog.cancel();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(UniversityActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private class getFaculty extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            progressDialog.show();
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
                    JSONArray parent = new JSONArray(s);
                    facultyList =new ArrayList<>();
                    facultyid =new ArrayList<>();
                    facultyList.add("Select Faculty");

                    for(int i=0;i<parent.length();i++){
                        facultyList.add(parent.getJSONObject(i).getString("name"));
                        facultyid.add(parent.getJSONObject(i).getInt("id"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UniversityActivity.this, android.R.layout.simple_spinner_item, facultyList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    facultySpinner.setAdapter(adapter);
                    facultyLayout.setVisibility(View.VISIBLE);
                    progressDialog.cancel();

                    facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i==0){
                                Toast.makeText(UniversityActivity.this, "Please select faculty", Toast.LENGTH_SHORT).show();
                                submitButton.setVisibility(View.INVISIBLE);
                                departmentLayout.setVisibility(View.INVISIBLE);
                                sessionLayout.setVisibility(View.INVISIBLE);
                            }
                            else{
                                new getDepartment().execute(APILink.DepartmentAPI+facultyid.get(i-1),null,null);
                                faculty=facultyid.get(i-1);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    progressDialog.cancel();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(UniversityActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private class getDepartment extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            progressDialog.show();
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
                    JSONArray parent = new JSONArray(s);
                    departmentList =new ArrayList<>();
                    departmentid =new ArrayList<>();
                    departmentList.add("Select Department");

                    for(int i=0;i<parent.length();i++){
                        departmentList.add(parent.getJSONObject(i).getString("name"));
                        departmentid.add(parent.getJSONObject(i).getInt("id"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UniversityActivity.this, android.R.layout.simple_spinner_item, departmentList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    departmentSpinner.setAdapter(adapter);
                    departmentLayout.setVisibility(View.VISIBLE);
                    progressDialog.cancel();

                    departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i==0){
                                Toast.makeText(UniversityActivity.this, "Please select your department", Toast.LENGTH_SHORT).show();
                                sessionLayout.setVisibility(View.INVISIBLE);
                                submitButton.setVisibility(View.INVISIBLE);
                            }
                            else{
                                new getSession().execute(APILink.SessionAPI+departmentid.get(i-1)+"?api_token="+SavedData.api_token,null,null);
                                department=departmentid.get(i-1);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    progressDialog.cancel();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(UniversityActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private class getSession extends AsyncTask<String, String, String> {
        InputStream in;
        @Override
        protected void onPreExecute() {
            progressDialog.show();
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
                    JSONArray parent = new JSONArray(s);
                    sessionList =new ArrayList<>();
                    sessionid =new ArrayList<>();
                    sessionList.add("Select Session");

                    for(int i=0;i<parent.length();i++){
                        sessionList.add(parent.getJSONObject(i).getString("session_years"));
                        sessionid.add(parent.getJSONObject(i).getInt("id"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UniversityActivity.this, android.R.layout.simple_spinner_item, sessionList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sessionSpinner.setAdapter(adapter);
                    sessionLayout.setVisibility(View.VISIBLE);
                    progressDialog.cancel();

                    sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if (i==0){
                                Toast.makeText(UniversityActivity.this, "Please select session", Toast.LENGTH_SHORT).show();
                                submitButton.setVisibility(View.INVISIBLE);
                            }
                            else{
                                session=sessionid.get(i-1);
                                submitButton.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    progressDialog.cancel();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(UniversityActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private class saveSession extends AsyncTask<String, String, String> {
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("department", department);
            params.put("faculty", faculty);
            params.put("session", session);
            params.put("university", university);


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
            TextView t = (TextView)findViewById(R.id.test);
            t.setText(s);
            if(responseCode==200){
                editor.putInt("department",department);
                editor.putInt("faculty",faculty);
                editor.putInt("session",session);
                editor.putInt("university",university);
                editor.putBoolean("SaveLogin",true);
                editor.commit();
                startActivity(new Intent(UniversityActivity.this, MainActivity.class));
            }
            else{
                try {
                    progressDialog.cancel();
                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(UniversityActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Somethig went wrong. Please try again");
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
