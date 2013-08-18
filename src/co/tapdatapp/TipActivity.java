package co.tapdatapp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TipActivity extends Activity {
	private String s_tip;
	
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
		i.putExtra("tip_amount",s_tip);
		startActivity(i);
	}
	
	public void selectTip (View view){
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setVisibility(View.VISIBLE);
		String tip_amount = (String) view.getTag();
		EditText edit_text1 = (EditText) findViewById(R.id.editText1); 
		
		edit_text1.setText(tip_amount);
		s_tip = tip_amount;
		
	}
}
