package com.example.lab2.myowntube;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.example.lab2.myowntube.GooglePlusDomainApi;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "LoginActivity";
    private SignInButton signInBtn;
    private String _accessToken;
    GoogleCredential credential;
    String userEmail;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;

    final static int REQUEST_CODE_PICK_ACCOUNT = 1000;
    //
    private final static String PROFILE_ME = "https://www.googleapis.com/auth/youtube";

    private static final String SCOPE = "oauth2:" + PROFILE_ME;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInBtn = (SignInButton)findViewById(R.id.btn_sign_in);
        signInBtn.setOnClickListener(this);
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, true, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    public void  getUserName(){
        if(userEmail == null){
            pickUserAccount();
        } else {
            _accessToken = null;
            new RetrieveTokenTask().execute();
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            LoginActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_CODE_PICK_ACCOUNT){
            if(resultCode == RESULT_OK){
                userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                ApplicationLoad.setUserEmail(userEmail);
                Log.i(TAG,"Email : "+userEmail);
                getUserName();
            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Pick Account", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        if(v.getId()==R.id.btn_sign_in){
            pickUserAccount();
        }
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), userEmail, SCOPE);
                ApplicationLoad.setAccessToken(token);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                handleException(e);
                Log.i(TAG,"Error : " + e.toString());
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            _accessToken = s;
            Log.i(TAG, "Access Token onPostExec : " + _accessToken);

            credential = new GoogleCredential().setAccessToken(_accessToken);
            new GooglePlusDomainApi(credential);
            startMainActivity();
        }

        public void startMainActivity(){
            Intent goToMain = new Intent(LoginActivity.this, MainActivity.class);
            goToMain.putExtra("user", userEmail);
            goToMain.putExtra("accessToken",_accessToken);
            startActivity(goToMain);
            finish();
        }
    }
}