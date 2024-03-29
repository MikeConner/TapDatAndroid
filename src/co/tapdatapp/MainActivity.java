package co.tapdatapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.nfc.NfcAdapter;
import android.widget.Toast;


public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
/*		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mNfcAdapter.isEnabled()) {
            if (null == mCamera) {
                try {
            	    //mCamera = Camera.open(); // locks it to stop other applications from using it
                }
                catch(Exception e) {
                	//Log.e("bob", "Can't disable the camera");
                }
            }        	
        } else {
//            mTextView.setText(R.string.explanation);
			Toast.makeText(this, "NFC DISABLED!", Toast.LENGTH_LONG).show();
        }
*/
		
        handleIntent(getIntent());
	}
	
    /*protected void onStart();
    
    protected void onRestart();

    protected void onResume();

    protected void onPause();

    protected void onStop();*/

    
    private void handleIntent(Intent intent) {
	    // TODO: handle Intent
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//*Log In Code goes here
	public void logInToApp(View view){
		
		Intent i = new Intent(this, LogInToTap.class);
		startActivity(i);
		
	}

	public void registerForTap(View view){
		
		Intent i = new Intent(this, RegisterToTap.class);
		startActivity(i);
		
	}

	
	public void writeTag(View view){
		
		Intent i = new Intent(this, WriteTag.class);
		startActivity(i);
		
	}
	private NfcAdapter mNfcAdapter;
}
