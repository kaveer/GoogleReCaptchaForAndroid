package com.kavsoftware.kaveer.googlerecaptcha.Activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.kavsoftware.kaveer.googlerecaptcha.R;
import com.kavsoftware.kaveer.googlerecaptcha.ViewModel.VerifyUserResponseViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final String SiteKey = "6LekPzcUAAAAAFkZBuBJrRcsi2UYlytSkILFQ-OZ";
    final String SecretKey  = "6LekPzcUAAAAAIAG4dwtQQInV5d9QbdgD7B-4QxS";
    private GoogleApiClient mGoogleApiClient;
    VerifyUserResponseViewModel response = new VerifyUserResponseViewModel();

    HttpURLConnection connection = null;
    BufferedReader reader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTitle(R.string.AppName);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            if(isNetworkConnected()){
                ConnectToGoogleApi();
            }
            else {
                DisplayToast("No internet Connection");
            }



            Button buttonViewAccidentHistory   = findViewById(R.id.BtnSignUp);
            buttonViewAccidentHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SafetyNet.SafetyNetApi.verifyWithRecaptcha(mGoogleApiClient, SiteKey)
                            .setResultCallback(new ResultCallback<SafetyNetApi.RecaptchaTokenResult>() {
                                @Override
                                public void onResult(SafetyNetApi.RecaptchaTokenResult result) {
                                    Status status = result.getStatus();

                                    if ((status != null) && status.isSuccess()) {

                                        if (!result.getTokenResult().isEmpty()) {
                                            String endPoint = BuildEndPoint(result.getTokenResult());
                                            String jsonObject = ValidateUserToken(endPoint);

                                            DeserializeJsonObject(jsonObject);

                                            if (response.isSuccess() == true){
                                                //save user info
                                                DisplayToast("Data saved");
                                            }

                                        }else{
                                            DisplayToast("Fail to validate user");
                                        }

                                    } else {
                                        DisplayToast("Fail to validate user");
                                    }
                                }
                            });


                    DisplayToast("Loading Google ReCaptcha...");
                }
            });
        }
        catch (Exception ex){
            DisplayToast(ex.getMessage());
        }


    }

    private void DeserializeJsonObject(String jsonObject) {
        try {

            JSONObject jsonResult = new JSONObject(jsonObject);
            response.setSuccess(jsonResult.getBoolean("success"));
            response.setChallenge_ts(jsonResult.getString("challenge_ts"));
            response.setApk_package_name(jsonResult.getString("apk_package_name"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String ValidateUserToken(String endPoint) {
        String result = "";

        if(isNetworkConnected()){

            try {
                result = new GetRaceCardFromApi().execute(endPoint).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            DisplayToast("No internet connection");
        }

        return  result;
    }

    private class GetRaceCardFromApi extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String jsonObject = "";
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String line ="";

                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                jsonObject = buffer.toString();

            } catch (Exception e) {

                Log.e("MainActivity", e.getMessage(), e);

            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            DisplayToast("Loading please wait..");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }

    private String BuildEndPoint(String tokenResult) {
        return "https://www.google.com/recaptcha/api/siteverify?secret=" + SecretKey + "&response=" + tokenResult;
    }

    private void DisplayToast(String message) {
        Toast messageBox = Toast.makeText(this , message , Toast.LENGTH_LONG);
        messageBox.show();
    }

    private void ConnectToGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();

        mGoogleApiClient.connect();
    }

    protected boolean isNetworkConnected() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return (mNetworkInfo == null) ? false : true;

        }catch (NullPointerException e){
            return false;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
