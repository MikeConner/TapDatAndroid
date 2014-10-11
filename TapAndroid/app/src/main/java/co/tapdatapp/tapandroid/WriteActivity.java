package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapTag;
import co.tapdatapp.tapandroid.service.TapYapa;


public class WriteActivity extends Activity {

    private String mAuthToken;
    private TapTag mTapTag;
  //  private String mTagID;
 //   private String mTagName;
    boolean mWriteMode = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        mTapTag = new TapTag();
        Intent intent = getIntent();
        mAuthToken = intent.getStringExtra("AuthToken");
        mTapTag.setTagID( intent.getStringExtra("TagID"));
        mTapTag.setTagName(intent.getStringExtra("TagName"));
        mTapTag.loadYapa(mAuthToken);


    }
    @Override
    public void onResume(){
        super.onResume();
        EditText edName = (EditText) findViewById(R.id.edTagName);
        edName.setText(mTapTag.getTagName());
        TextView tvID = (TextView) findViewById(R.id.tvID);
        tvID.setText(mTapTag.getTagID());

        ArrayList<TapYapa> myYappas = mTapTag.myYappas();

        edName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    EditText g = (EditText) v;

                    mTapTag.updateTag(mAuthToken,mTapTag.getTagID().replaceAll("-",""), g.getText().toString() );
//                    Toast.makeText(WriteActivity.this, "lost it", Toast.LENGTH_LONG);
                }
            }
        });

        //first Yapa
        if(myYappas.size() > 0) {
            EditText edMessage = (EditText) findViewById(R.id.dtYapaMessage);
            ImageView iv = (ImageView) findViewById(R.id.imageView);

            edMessage.setText(myYappas.get(0).getContent());
            new TapCloud.DownloadImageTask(iv)
                    .execute(myYappas.get(0).getThumbYapa());


            edMessage.setOnFocusChangeListener( new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus){
                    if (!hasFocus){
                        EditText g = (EditText) v;
                        mTapTag.myYappas().get(0).setContent(((EditText) v).getText().toString());
                        mTapTag.myYappas().get(0).updateYapa(mAuthToken, mTapTag.getTagID());
                    }

                }
            });
            //get the second on in here
            if (myYappas.size() > 1){
                EditText edMessageBonus = (EditText) findViewById(R.id.edBonusYapa);
                ImageView ivBonus = (ImageView) findViewById(R.id.imageView2);

                //threshhold
                //text
                //image


            }
        }

    }
    public void saveMyYapa(View v){


    }

    //Image stuff
    public void selectImageYapa1(View v){
        selectImage(0);
    }
    public void selectImageYapa2(View v){
        selectImage(1);
    }
    private boolean mFromCamera = false;
    private int mImageID = 0;
    private void selectImage(int ImageID) {
        mImageID = ImageID;
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    mFromCamera = true;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
    /*                    try {
       //                     photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File

                        }
  */                      // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
//                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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

    //MENU STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.write, menu);
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

    //NFC TAG STUFF
    public void startWrite (View view){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(WriteActivity.this);
        mNfcPendingIntent = PendingIntent.getActivity(WriteActivity.this, 0,
                new Intent(WriteActivity.this, WriteActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        enableTagWriteMode();

        new AlertDialog.Builder(WriteActivity.this).setTitle("Touch tag to write")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        disableTagWriteMode();
                    }

                }).create().show();


        //  Intent i = new Intent(this, WriteActivity.class);
        //  startActivity(i);

    }
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Get ID from local storage
            // write ID to tag
            String strMime = "tapdat/performer";
            String strID = mTapTag.getTagID();
            NdefRecord record = NdefRecord.createMime( strMime, strID.getBytes());
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            if (writeDaTag(message, detectedTag)) {
                Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
    public boolean writeDaTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(getApplicationContext(),
                            "Error: tag not writable",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(getApplicationContext(),
                            "Error: tag too small",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }




}
