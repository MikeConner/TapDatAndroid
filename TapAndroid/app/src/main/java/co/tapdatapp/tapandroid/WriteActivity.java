package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        //if first time creation, going to create a yapa from thumbnail image
        if(mTapTag.YapaCount() == 0){


        }
    }
    @Override
    public void onResume(){
        super.onResume();
        EditText edName = (EditText) findViewById(R.id.edTagName);
        edName.setText(mTapTag.getTagName());
        TextView tvID = (TextView) findViewById(R.id.tvID);
        tvID.setText(mTapTag.getTagID());
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        ArrayList<TapYapa> myYappas = mTapTag.myYappas();

        EditText edMessage = (EditText) findViewById(R.id.dtYapaMessage);
        edMessage.setText(myYappas.get(0).getContent());
        if(myYappas.size() > 0) {
            new TapCloud.DownloadImageTask(iv)
                    .execute(myYappas.get(0).getThumbYapa());
        }
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

        edMessage.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (!hasFocus){
                    EditText g = (EditText) v;
                    mTapTag.myYappas().get(0).setContent("booya");
                    mTapTag.myYappas().get(0).updateYapa(mAuthToken);
                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.write, menu);
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


    /*
    * Writes an NdefMessage to a NFC tag
    */
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
