package com.example.jgraham.kitabureg1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jgraham.backend.myApi.*;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "Kitabu_preferences";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Intent intent = getIntent();
        String sender = intent.getStringExtra("sender");
        if(sender!= null && sender.equals("login"))
        {
            SharedPreferences sharedPreferences = getSharedPreferences("Kitabu_preferences",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("id", intent.getStringExtra("id"));
            editor.putString("phoneno", intent.getStringExtra("phoneno"));
            editor.putString("email", intent.getStringExtra("email"));
            editor.putString("name", intent.getStringExtra("name"));
            editor.commit();
        }
        else
        {
            SharedPreferences sharedPreferences = getSharedPreferences("Kitabu_preferences",
                    Context.MODE_PRIVATE);
            String name = sharedPreferences.getString("name",null);
            if(name != null)
                Toast.makeText(this, "Welcome back, "+name, Toast.LENGTH_SHORT).show();
        }
        new GcmRegistrationAsyncTask(this).execute();

        /*
         TODO: Add ShowCaseView here.
         */
//        new ShowcaseView.Builder(this)
//                //.setTarget(new ActionViewTarget(this, ActionViewTarget.Type.TITLE))
//                .setTarget(new TextView(getApplicationContext(), TextView.LAYER_TYPE_NONE))
//                .setContentTitle("ShowcaseView")
//                .setContentText("This is highlighting the Home button")
//                .hideOnTouchOutside()
//                .build();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
     * Clear all shared preferences and go to the Welcome Screen.
     */
    void signout()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Kitabu_preferences",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
        Toast.makeText(this, "Sign out successful,\n sorry to see you go! :(", Toast.LENGTH_SHORT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_signout)
        {
            signout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Tab1 tab1 = new Tab1();
                    return tab1;
                case 1:
                    Tab2 tab2 = new Tab2();
                    return tab2;
                case 2:
                    Tab3 tab3 = new Tab3();
                    return tab3;
            }
            return null;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Tab 1";
                case 1:
                    return "Tab 2";
                case 2:
                    return "Tab 3";
            }
            return null;
        }
    }


}
class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
    private MyApi regService = null;
    private GoogleCloudMessaging gcm;
    private Context context;

    private static final String SENDER_ID = "609569899467";

    public GcmRegistrationAsyncTask(Context context) {
        this.context = context;
    }

    boolean register_gcm(String regId) {
        SharedPreferences sharedPreference = context.getSharedPreferences("Kitabu_preferences",
                Context.MODE_PRIVATE);
        boolean msg = false;
        String phoneno = sharedPreference.getString("phoneno", null);
        String serv_res = "";
        if (phoneno != null) {

            Map<String, String> params = new HashMap<String, String>();
            params.put("phoneno", phoneno);
            params.put("regId", regId);

            // Send to server
            try {
                serv_res = ServerUtil.get("http://kitabu.prashant.at/api/gcm", params);
                if (serv_res.contains("false")) {
                    msg = false;
                } else if (serv_res.contains("true")) {
                    msg = true;
                }
            } catch (IOException e) {
                Log.d("LOGIN", "Sending to server did not work");
                e.printStackTrace();
                msg = false;
            }
        }
        return msg;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (regService == null) {
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                    // otherwise they can be skipped
                    .setRootUrl("https://kitabu-dartmouth.appspot.com//_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end of optional local run code
            regService = builder.build();
        }

        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String regId = gcm.register(SENDER_ID);
            SharedPreferences sharedPreferences = context.getSharedPreferences("Kitabu_preferences",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("regId", regId);
            editor.commit();
            // Receive the regid
            // Send the regid to our server API
            // Server stores our regid with the user table
            // Server can now send push messages
            if(regId != null)
            {
                if(register_gcm(regId))
                {
                    msg = "Login Successful!";
                }
                else
                {
                    msg = "Login unsuccessful!";
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            msg = "Error: " + ex.getMessage();
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
    }
}
