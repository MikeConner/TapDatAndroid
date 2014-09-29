package co.tapdatapp.tapandroid;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;
import co.tapdatapp.tapandroid.service.TapTag;
import co.tapdatapp.tapandroid.service.TapTxn;



public class MainActivity extends Activity implements Account.OnFragmentInteractionListener, History.OnFragmentInteractionListener, Arm.OnFragmentInteractionListener, ActionBar.TabListener {


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private SharedPreferences mPreferences;
    private String mPhoneSecret;
    private String mAuthToken;
    private TapUser mTapUser;
    private TapTag mTapTag;
    private TapTxn mTapTxn;
    private TapCloud mTapCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //Start of Tap Network Operations
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mTapUser = new TapUser();
        mTapCloud = new TapCloud();

        if (mPreferences.contains("PhoneSecret")) {
            mPhoneSecret = mPreferences.getString("PhoneSecret", "");
        }
        else {
            mPhoneSecret =  mTapUser.generatePhoneSecret();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("PhoneSecret", mPhoneSecret);
            editor.commit();
        }
        // at this point we have a Phone Secret, let's try some network shit

        Boolean mNetwork = mTapCloud.isNetworkAvailable(this);
        if (mNetwork) {

            if (mPreferences.contains("AuthToken")){
                mAuthToken = mPreferences.getString("AuthToken", "");
                mTapUser.LoadUser(mAuthToken);
                //TODO: Failure case for when auth token has expired -> get error, get new auth token based on secret

            }
            else{
                //Get Auth Token
                mAuthToken =  mTapUser.CreateUser(mPhoneSecret);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("AuthToken", mAuthToken);
                editor.putString("NickName", mTapUser.getNickname());
                editor.commit();


                //We know user is null, but let's load user anyway to be consistent with above
                mTapUser.LoadUser(mAuthToken);

                //TODO: Delete Auth Token on kill of application, so it gets a new one when it comes back
            }
         //   mTapUser.getNewNickname(mAuthToken);



        }
        else {
            Toast.makeText(this, (CharSequence) ("No NETWORK!  Going Home!"), Toast.LENGTH_SHORT).show();
            //TODO: Code to send message, kill app, or figure out what to do next?
        }
        //end of Tap network Ops
    }
    @Override
    public void onResume(){
        super.onResume();
        Toast.makeText(this, (CharSequence) (mTapUser.getNickname()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_arm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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

            Fragment frag;
            switch (position) {
                case 0:
                    frag = new Account().newInstance("1","2");
                    break;
                case 1:
                    frag = new Arm().newInstance("1","2");
                    break;
                case 2:
                    frag = new History().newInstance("1","2");
                    break;

                default: throw new IllegalArgumentException("Invalid Section Number");
            }
            return frag;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {
        // we need this for fragments / menus
        //not sure what we have to do here if anything
    }


}
