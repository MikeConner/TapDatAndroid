package co.tapdatapp;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.nfc.NfcAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class TapReady extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tap_ready);
		
		
		NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		Object mNfcPendingIntent = PendingIntent.getActivity(this, 0,
		    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Intent filters for exchanging over p2p.
		IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
		    ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}
		IntentFilter[] mNdefExchangeFilters = new IntentFilter[] { ndefDetected };
		
		Toast.makeText(this, mNdefExchangeFilters.toString(), Toast.LENGTH_LONG).show();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tap_ready, menu);
		return true;
	}
	public void readyForTap(View view){

	
	}
}
