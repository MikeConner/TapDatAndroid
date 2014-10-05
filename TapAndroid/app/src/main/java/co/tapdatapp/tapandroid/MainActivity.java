package co.tapdatapp.tapandroid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;
import co.tapdatapp.tapandroid.service.TapTag;
import co.tapdatapp.tapandroid.service.TapTxn;



public class MainActivity extends Activity implements Account.OnFragmentInteractionListener, History.OnFragmentInteractionListener, Arm.OnFragmentInteractionListener, ActionBar.TabListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private SharedPreferences mPreferences;
    private String mPhoneSecret;
    private String mAuthToken;
    private TapUser mTapUser;
    private TapTag mTapTag;
    private TapTxn mTapTxn;
    private TapCloud mTapCloud;

    private Account mAccountFrag;
    private Fragment mHistoryFrag;
    private Fragment mTapFrag;

    private float fAmount;
    private int fUnit;
    private TextView txAmount;
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mNdefExchangeFilters;
    private PendingIntent mNfcPendingIntent;
    private boolean mArmed = false;
    private ArmedFragment mArmFrag;
    public   TapUser getUserContext(){
        return mTapUser;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fAmount = 1;
        fUnit = 1;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter smartwhere = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);



        try {
            smartwhere.addDataType("tapdat/performer");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException  e) {
            throw new RuntimeException("fail", e);
        }
        mNdefExchangeFilters = new IntentFilter[] { smartwhere };

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

        mTapCloud = new TapCloud();
        mTapUser = TapCloud.getTapUser(this);
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

            if (mPreferences.contains("AuthToken")) {
                if (!mPreferences.getString("AuthToken", "").isEmpty()){
                    mAuthToken = mPreferences.getString("AuthToken", "");
                mTapUser.LoadUser(mAuthToken);
                //TODO: Failure case for when auth token has expired -> get error, get new auth token based on secret
            }
                else {
                    mAuthToken =  mTapUser.CreateUser(mPhoneSecret);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("AuthToken", mAuthToken);
                    editor.putString("NickName", mTapUser.getNickname());
                    editor.commit();


                }
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
               // mAccountFrag.setTapUser(mTapUser);
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
      //  Toast.makeText(this, (CharSequence) (mTapUser.getNickname()), Toast.LENGTH_SHORT).show();
        txAmount = (TextView) findViewById(R.id.txtAmount);
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                    mNdefExchangeFilters, null);
            //if (!mNfcAdapter.isEnabled()) {
            //LayoutInflater inflater = getLayoutInflater();
            //View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,(ViewGroup) findViewById(R.id.nfc_settings_layout));
                /*new AlertDialog.Builder(this).setView(dialoglayout)
                        .setPositiveButton("Update Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                finish(); // exit application if user cancels
                            }
                        }).create().show();
            }*/
            // } else {
            //     Toast.makeText(getApplicationContext(), "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
            // }
        }

    }
    @Override
    public void onPause(){
        super.onPause();
        if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
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
                    //frag = mAccountFrag;
                    break;
                case 1:
                    frag = new Arm().newInstance("1","2");
                   // frag = mTapFrag;
                    break;
                case 2:
                    frag = new History().newInstance("1","2");
                    //frag = mHistoryFrag ;
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
    public void getImage(View view){

       // Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      //  startActivityForResult(takePicture, 0);//zero can be replaced with any action code

       // Intent pickPhoto = new Intent(Intent.ACTION_PICK,
       //         android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
       // startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
        selectImage();
    }


    public void myTags(View view){
        Tags mTags = new Tags();
        Intent i = new Intent(this,TagActivity.class);
        i.putExtra("AuthToken", mAuthToken);
        startActivity(i);

    }
    public void newNickNameMe(View view){
        EditText et = (EditText) findViewById(R.id.etNickName);
        et.setText(        mTapUser.getNewNickname(mAuthToken));

    }
    public void writeUser(View view){
        EditText edName = (EditText) findViewById(R.id.etNickName);
        EditText edEmail = (EditText) findViewById(R.id.etEmail);
        EditText edWithDraw = (EditText) findViewById(R.id.etWithdraw);

        mTapUser.UpdateUser(mAuthToken, edName.getText().toString(), edEmail.getText().toString(), edWithDraw.getText().toString());

    }


    String mCurrentPhotoPath;
    boolean mFromCamera = false;
    static final int REQUEST_TAKE_PHOTO = 1;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    mFromCamera = true;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File

                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }

                    }
                } else if (items[item].equals("Choose from Library")) {
                    mFromCamera = false;
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent , 1);//one can be replaced with any action code
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mFromCamera) {
             //
             String b = mCurrentPhotoPath;
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                setPic();
            }
            else {
//            Bundle b = data.getExtras();
           //     Bundle extras = data.getData();
                Uri mContentURI = data.getData();
                //         String mRealPath = getRealPathFromURI(mContentURI);
//            Bitmap imageBitmap = (Bitmap)
                ImageView mImageView = (ImageView) findViewById(R.id.imageView);
                mImageView.setImageURI(mContentURI);
                String newURL = mTapCloud.uploadToS3withURI(mContentURI, TapUser.getRandomString(16), this);
                mTapUser.setBTCoutbound(newURL );
                mTapUser.UpdateUser(mAuthToken);

            }
        }
    }
    private void setPic() {
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }






    public void armOrSend(View v){
        mArmed=true;
        showDialog();

    }

    void showDialog() {
     //  mStackLevel++;

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        mArmFrag =  new ArmedFragment(mAuthToken, fAmount);
        mArmFrag.show(ft, "dialog");
    }

    private void changeAmount(int change_value, boolean addition){
        if (addition) {
            fUnit = change_value;
            fAmount += fUnit;

            if (fAmount > 500 ) {fAmount = 500;} //MAX VALUE FOR TIP
            txAmount = (TextView) findViewById(R.id.txtAmount);
            txAmount.setText("$" + String.valueOf(fAmount));
        }
        else{
            fUnit = change_value;
            fAmount-= fUnit;
            if (fAmount < 1) {fAmount = 1;}
            txAmount = (TextView) findViewById(R.id.txtAmount);
            txAmount.setText("$" + String.valueOf(fAmount));
        }


    }
    public void tapPlus(View v){
        changeAmount(fUnit, true);
    }
    public void tapMinus(View v){
        changeAmount(fUnit, false);
    }
    private void selectMe(View v){
        Button btnOne = (Button) findViewById(R.id.btnOne);
        Button btnFive = (Button) findViewById(R.id.btnFive);
        Button btnTen = (Button) findViewById(R.id.btnTen);
        Button btnTwenty = (Button) findViewById(R.id.btnTwenty);
        Button btnFifty = (Button) findViewById(R.id.btnFifty);
        Button btnHundred = (Button) findViewById(R.id.btnHundred);

        Resources res = getResources();
        Drawable selected = res.getDrawable(R.drawable.circleselected);
        Drawable normal = res.getDrawable(R.drawable.circle);
        btnOne.setBackground(normal);
        btnFive.setBackground(normal);
        btnTen.setBackground(normal);
        btnTwenty.setBackground(normal);
        btnFifty.setBackground(normal);
        btnHundred.setBackground(normal);

        v.setBackground(selected);

    }
    public void tapOne(View v){
        selectMe(v);
        changeAmount(1,true);
    }
    public void tapFive(View v){
        selectMe(v);
        changeAmount(5,true);


    }
    public void tapTen(View v){
        selectMe(v);

        changeAmount(10,true);


    }
    public void tapTwenty(View v){
        selectMe(v);

        changeAmount(20, true);

    }
    public void tapFifty(View v){
        selectMe(v);

        changeAmount(50,true);


    }
    public void tapHundred(View v){
        selectMe(v);

        changeAmount(100,true);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            }
            if(messages[0] != null) {
                String result="";
                byte[] payload = messages[0].getRecords()[0].getPayload();
                // this assumes that we get back am SOH followed by host/code
                for (int b = 0; b<payload.length; b++) { // skip SOH
                    result += (char) payload[b];
                }
                Toast.makeText(getApplicationContext(), "Tag Contains " + result, Toast.LENGTH_SHORT).show();
                if (mArmed){
                    TapTxn txn = new TapTxn();
                    txn.TapAfool(mAuthToken, result.replaceAll("-",""),fAmount );
                    mArmed = false;
                    mArmFrag.setValues(txn.getMessage(), txn.getPayloadURL().toString());
                    //tv.setText(txn.getMessage());

                } else {

                    //show tap screen, change button to SEND?
                }
                //Intent i = new Intent(this, TapArm.class);
              //  i.putExtra("TAGID", result);
              //  i.putExtra("TIPAMOUNT", fltTipAmount);
              //  startActivity(i);

            }
        }
    }


}
