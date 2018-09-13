package eu.veldsoft.salsabg;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		((Button) findViewById(R.id.done)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SettingsActivity.this.finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		((EditText) findViewById(R.id.username)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("username", ""));
		((EditText) findViewById(R.id.password)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("password", ""));
		((EditText) findViewById(R.id.title)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("title", ""));
		((EditText) findViewById(R.id.message)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("message", ""));
		((EditText) findViewById(R.id.timeout)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getInt("timeout", 0));
		((EditText) findViewById(R.id.wakeup)).setText("" + getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getInt("wakeup", Integer.MAX_VALUE));
		if (getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getBoolean("message_send", false) == false) {
			((CheckBox) findViewById(R.id.message_send)).setChecked(false);
		} else {
			((CheckBox) findViewById(R.id.message_send)).setChecked(true);
		}
		if (getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getBoolean("friendship_send", false) == false) {
			((CheckBox) findViewById(R.id.friendship_send)).setChecked(false);
		} else {
			((CheckBox) findViewById(R.id.friendship_send)).setChecked(true);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences.Editor editor = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).edit();

		editor.putString("username", ((EditText) findViewById(R.id.username)).getText().toString());
		editor.putString("password", ((EditText) findViewById(R.id.password)).getText().toString());
		editor.putString("title", ((EditText) findViewById(R.id.title)).getText().toString());
		editor.putString("message", ((EditText) findViewById(R.id.message)).getText().toString());
		editor.putInt("timeout", Integer.valueOf(((EditText) findViewById(R.id.timeout)).getText().toString()));
		editor.putInt("wakeup", Integer.valueOf(((EditText) findViewById(R.id.wakeup)).getText().toString()));
		editor.putBoolean("message_send", ((CheckBox) findViewById(R.id.message_send)).isChecked());
		editor.putBoolean("friendship_send", ((CheckBox) findViewById(R.id.friendship_send)).isChecked());

		editor.commit();
	}
}
