package com.example.my.ubuddy.View.Authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.R;
import com.example.my.ubuddy.BusinessLogic.SavedData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    String firstname, lastname, username, email, password, type;
    EditText fnameEdittext, lnameEdittext, usernameEdittext, emailEdittext, passwordEdittext;
    RadioGroup usertypeGroup;
    Button loginButton, registerButton;
    RadioButton typeButton;

    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTitle("Registration");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        sharedPreferences=getSharedPreferences("LoginDetails",0);
        editor=sharedPreferences.edit();

        initialization();
    }

    public void initialization(){
        fnameEdittext=(EditText)findViewById(R.id.firstnameEdittext);
        lnameEdittext=(EditText)findViewById(R.id.lastnameEdittext);
        usernameEdittext=(EditText)findViewById(R.id.usernameEdittext);
        emailEdittext=(EditText)findViewById(R.id.emailEdittext);
        passwordEdittext=(EditText)findViewById(R.id.passEdittext);

        loginButton=(Button)findViewById(R.id.loginButton);
        registerButton=(Button)findViewById(R.id.registerButton);

        usertypeGroup=(RadioGroup)findViewById(R.id.usertypeRadioGroup) ;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getAndCheckData() && checkInternet()){
                    if(getAndCheckData() && checkInternet()){
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        new SendDatatoServer().execute(APILink.RegistrationAPI,null ,null);

                    }
                }
            }
        });
    }
    public boolean getAndCheckData(){
        boolean result=true;

        String[] inputData={firstname,lastname,username,email,password};
        EditText[] inputEditText={fnameEdittext,lnameEdittext,usernameEdittext,emailEdittext,passwordEdittext};

        typeButton=(RadioButton)findViewById(usertypeGroup.getCheckedRadioButtonId());
        type=typeButton.getText().toString();

        for(int i=0;i<inputData.length;i++){
            inputData[i]=inputEditText[i].getText().toString();
            if(TextUtils.isEmpty(inputData[i])){
                inputEditText[i].setError("Required");
                result=false;
            }
        }
        return result;
    }

    public boolean checkInternet(){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        else{
            connected = false;
            AlertDialog.Builder alertDialog  = new AlertDialog.Builder(RegistrationActivity.this);
            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Please check your internet Connection and try again");
            alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
            alertDialog.setPositiveButton("Ok",null);
            alertDialog.create();
            alertDialog.show();
        }



        return connected;
    }

    private class SendDatatoServer extends AsyncTask<String, String, String> {
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("firstname", fnameEdittext.getText().toString());
            params.put("lastname", lnameEdittext.getText().toString());
            params.put("username", usernameEdittext.getText().toString());
            params.put("password", passwordEdittext.getText().toString());
            params.put("password_confirmation", passwordEdittext.getText().toString());
            params.put("email", emailEdittext.getText().toString());


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

                editor.putBoolean("LoggedOnce",true);
                try {

                    JSONObject parent = new JSONObject(s);
                    JSONObject data=parent.getJSONObject("data");

                    SavedData.api_token=data.getString("api_token");

                    editor.putString("id",data.getString("id"));
                    editor.putString("email",data.getString("email"));
                    editor.putString("firstname",data.getString("firstname"));
                    editor.putString("lastname",data.getString("lastname"));
                    editor.putString("api_token",SavedData.api_token);
                    editor.putString("username",data.getString("username"));
                    editor.commit();

                    startActivity(new Intent(RegistrationActivity.this, UniversityActivity.class));

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }

            }
            else{
                try {
                    JSONObject  parent = new JSONObject(s);
                    JSONObject errors = parent.getJSONObject("errors");

                    StringBuilder stringBuilder = new StringBuilder();
                    Iterator keysToCopyIterator = errors.keys();
                    while(keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        stringBuilder.append(errors.getJSONArray(key).getString(0));
                        stringBuilder.append("\n");
                    }


                    AlertDialog.Builder alertDialog  = new AlertDialog.Builder(RegistrationActivity.this);
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage(stringBuilder.toString());
                    alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                    alertDialog.setPositiveButton("Ok",null);
                    alertDialog.create();
                    alertDialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }



}
