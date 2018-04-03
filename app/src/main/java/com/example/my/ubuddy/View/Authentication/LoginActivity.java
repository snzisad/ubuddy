package com.example.my.ubuddy.View.Authentication;

import android.annotation.SuppressLint;
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

import com.example.my.ubuddy.BusinessLogic.APILink;
import com.example.my.ubuddy.View.MainActivity;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    String email,password;
    EditText emailEdittext, passwordEdittext;
    Button loginButton, registerButton;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int responseCode;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        super.setTitle("UBuddy");

        progressDialog = new ProgressDialog(this);
        sharedPreferences=getSharedPreferences("LoginDetails",0);
        editor=sharedPreferences.edit();

        if(sharedPreferences.getBoolean("SaveLogin",false)){
            SavedData.api_token=sharedPreferences.getString("api_token",null);

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }

        initialization();
    }

    public void initialization(){
        emailEdittext=(EditText)findViewById(R.id.emailEdittext);
        passwordEdittext=(EditText)findViewById(R.id.passEdittext);
        loginButton=(Button)findViewById(R.id.loginButton);
        registerButton=(Button)findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getAndCheckData() && checkInternet()){
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new SendDatatoServer().execute(APILink.loginAPI,null ,null);

                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }
    public boolean getAndCheckData(){
        boolean result=true;

        String[] inputData={email,password};
        EditText[] inputEditText={emailEdittext,passwordEdittext};
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
            AlertDialog.Builder alertDialog  = new AlertDialog.Builder(LoginActivity.this);
            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Please check your internet Connection and try again");
            alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
            alertDialog.setPositiveButton("Ok",null);
            alertDialog.create();
            alertDialog.show();
        }



        return connected;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
        finish();
    }

    private class SendDatatoServer extends AsyncTask<String, String, String>{
        byte[] postDataBytes;
        InputStream in;

        @Override
        protected void onPreExecute() {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("login", emailEdittext.getText().toString());
            params.put("password", passwordEdittext.getText().toString());

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

                try {

                    JSONObject parent = new JSONObject(s);
                    JSONObject data=parent.getJSONObject("data");

                    int department=data.getInt("department");
                    int faculty=data.getInt("faculty");
                    int session=data.getInt("session");
                    int university=data.getInt("university");
                    SavedData.api_token=data.getString("api_token");

                    editor.putString("id",data.getString("id"));
                    editor.putString("email",data.getString("email"));
                    editor.putString("firstname",data.getString("firstname"));
                    editor.putString("lastname",data.getString("lastname"));
                    editor.putString("api_token",SavedData.api_token);
                    editor.putString("username",data.getString("username"));
                    editor.putInt("isTeacher",data.getInt("isTeacher"));

                    if(department==0 || university==0 || faculty==0 || session==0){
                        editor.commit();
                        startActivity(new Intent(LoginActivity.this, UniversityActivity.class));
                    }
                    else{
                        editor.putInt("department",department);
                        editor.putInt("faculty",faculty);
                        editor.putInt("session",session);
                        editor.putInt("university",university);
                        editor.putBoolean("SaveLogin",true);
                        editor.commit();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }

                } catch (JSONException e) {
                    Log.e("Error","Error");
                }


            }
            else{
                AlertDialog.Builder alertDialog  = new AlertDialog.Builder(LoginActivity.this);
                alertDialog.setTitle("Error");
                alertDialog.setMessage("The given data was invalid");
                alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
                alertDialog.setPositiveButton("Ok",null);
                alertDialog.create();
                alertDialog.show();
            }
        }

    }


}
