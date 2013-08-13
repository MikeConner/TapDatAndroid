package co.tapdatapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class TipActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Intent intent = getIntent();
		//String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		setContentView(R.layout.activity_tip);

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tip, menu);
		return true;
	}
	public void readyForTap (View view){
		Intent i = new Intent(this, TapReady.class);
		startActivity(i);
	}
}
