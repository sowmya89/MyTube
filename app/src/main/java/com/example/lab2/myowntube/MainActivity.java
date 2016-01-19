package com.example.lab2.myowntube;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.example.lab2.myowntube.R;
import com.example.lab2.myowntube.GooglePlusDomainApi;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // Tab titles

    String userEmail;
    String accessToken;
    GoogleCredential credential;
    Person mePerson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userEmail = intent.getStringExtra("user");

        // Initilization
        //       Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabsPagerAdapter pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }


    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = accessToken;
            try {
                GoogleAuthUtil.clearToken(getApplicationContext(), token);
                token = null;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }

            return token;

        }

//        @Override
//        protected void onPostExecute(String s) {
////            super.onPostExecute(accessToken);
////            accessToken = null;
//            Intent goToLogin = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(goToLogin);
//            finish();
//        }
    }

    public void signout()  {
        new RetrieveTokenTask().execute();
        Intent goToLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(goToLogin);
        finish();
    }

    public void delete(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.delete:
//                delete();
//                return true;
            case R.id.signout:
                signout();
                return true;
            case R.id.action_settings:
//                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }
}