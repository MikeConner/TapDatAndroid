package co.tapdat.tapdatapp;

import co.tapdat.tapdatapp.util.SystemUiHider;
import co.tapdat.tapdatapp.util.UrlJsonAsyncTask;
import co.tapdat.tapdatapp.util.TapUser;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.provider.Settings.Secure;
import co.tapdat.tapdatapp.R;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class HomeActivity extends Activity {
//    private static final String TASKS_URL = "http://10.0.2.2:3000/api/v1/tasks.json";
//    private final static String LOGIN_API_ENDPOINT_URL = "http://10.0.2.2:3000/api/v1/sessions.json";


    private SharedPreferences mPreferences;
    private String mPhoneSecret;
    private TapUser mTapUser;
    private String mAuthToken;

    private static final boolean AUTO_HIDE = false;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);


//TAP CODE
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mTapUser = new TapUser();

        boolean bNetwork =   isNetworkAvailable();
        if (bNetwork) {
            //ToastThis( "Yup, we gotz da net");
        } else
        {
            ToastThis( "FML.  No Net");
        }
//END OF CUSTOM CODE


        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }




@Override
public void onResume() {
    super.onResume();

    if (!mPreferences.contains("AuthToken")) {

//START OF CUSTOM CODE
        if (mPreferences.contains("PhoneSecret")) {
            mPhoneSecret = mPreferences.getString("PhoneSecret", "");
        } else {

            mTapUser.GeneratePhoneSecret(HomeActivity.this);
            mPhoneSecret = mPreferences.getString("PhoneSecret", "");
            mTapUser.CreateUser(HomeActivity.this);

        }
        mAuthToken = mPreferences.getString("AuthToken", "");
        ToastThis(mAuthToken);
        mTapUser.LoadUser(HomeActivity.this, mAuthToken);
        //       ToastThis(mTapUser.getNickname());
        //get balance

        //  loadTasksFromAPI(TASKS_URL);
        //we're good to go

    } else {
        mAuthToken = mPreferences.getString("AuthToken", "");
        mTapUser.LoadUser(HomeActivity.this, mAuthToken);


        //go get a new auth
    }
}
public void loadit(View view){


        TextView txt1 = (TextView) findViewById(R.id.txtConfirmations);
        txt1.setText(mTapUser.getNickname());
        TextView txt2 = (TextView) findViewById(R.id.txtLoadAddress);
        txt2.setText(mTapUser.getBTCinbound());
}

    public void newNick(View view){
        mTapUser.NewNetNickName(HomeActivity.this, mAuthToken);
    }
public void goToSettings (View view){
    Intent i = new Intent(this, AccountActivity.class);
    i.putExtra("AuthToken", mAuthToken);
    startActivity(i);
}

public void goToTap (View view){
    Intent i = new Intent(this, TapArm.class);
    startActivity(i);
}



public void ToastThis(String strToastin){

Context context = getApplicationContext();
CharSequence text = strToastin;
int duration = Toast.LENGTH_SHORT;

Toast toast = Toast.makeText(context, text, duration);
toast.show();
}


public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    // if no network is available networkInfo will be null
    // otherwise check if we are connected
    if (networkInfo != null && networkInfo.isConnected()) {
        return true;
    }
    return false;
}
}

