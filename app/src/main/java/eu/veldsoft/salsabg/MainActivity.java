package eu.veldsoft.salsabg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
	/**
	 * Request code for the settings activity.
	 */
	private static final int SETTINGS_ACTIVITY = 0x01;
	/**
	 * Pseudo-random number generator.
	 */
	private static final Random PRNG = new Random();
	/**
	 * Web browser view component.
	 */
	private WebView browser = null;
	/**
	 * Moment in time to wake up if there is a blocking.
	 */
	private long wakeupAt = System.currentTimeMillis() + 10000;
	/**
	 * Is running flag.
	 */
	private boolean running = false;
	/**
	 * Track bot states.
	 */
	private WebPageState state = WebPageState.LOGGED_OUT;

	/**
	 * User name of the profile.
	 */
	private String username = "";

	/**
	 * Password of the profile.
	 */
	private String password = "";

	/**
	 * Title of the message which will be sent.
	 */
	private String title = "";

	/**
	 * Message which will be sent.
	 */
	private String message = "";


	/**
	 * Time out after message send.
	 */
	private int timeout = -1;
	/**
	 * Time out for wake-up after blocking.
	 */
	private int wakeup = -1;
	/**
	 * Message send flag.
	 */
	private boolean messageSend = false;
	/**
	 * Friendship send flag.
	 */
	private boolean friendshipSend = false;

	/**
	 * Show test point toast.
	 *
	 * @param number Number of the test point.
	 */
	private void debug(int number) {
		//TODO Control it with preferences.
		Toast.makeText(MainActivity.this, "Test point " + number + " ...", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show text as toast.
	 *
	 * @param text Number of the test point.
	 */
	private void print(String text) {
		Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Initialize working parameters from the shared preferences.
	 */
	private void initialize() {
		username = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("username", "");
		password = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("password", "");
		title = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("title", "");
		message = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("message", "");
		timeout = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getInt("timeout", 5000);
		wakeup = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getInt("wakeup", Integer.MAX_VALUE);
		messageSend = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getBoolean("message_send", true);
		friendshipSend = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getBoolean("friendship_send", false);
	}

	/**
	 * Load URL address in the web browser web view.
	 *
	 * @param url  Address to load.
	 * @param time Milliseconds to wait before loading.
	 */
	private void loadUrl(final String url, final long time) {
		if (running == false) {
			return;
		}

		/*
		 * Wait for a while before to proceed.
		 */
		new CountDownTimer(time, time) {
			public void onFinish() {
				browser.loadUrl(url);
			}

			public void onTick(long millisUntilFinished) {
				/* Nothing is done on ticking. */
			}
		}.start();
	}

	/**
	 * Load URL address in the web browser web view.
	 *
	 * @param url Address to load.
	 */
	private void loadUrl(String url) {
		if (running == false) {
			return;
		}

		browser.loadUrl(url);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialize();

		/*
		 * Prepare web browser object.
		 */
		browser = (WebView) findViewById(R.id.browser);
		browser.getSettings()
						.setUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US;rv:1.9.0.4) Gecko/20100101 Firefox/4.0");
		browser.getSettings().setJavaScriptEnabled(true);
		browser.getSettings().setDomStorageEnabled(true);

		/*
		 * Obtain inner HTML text.
		 */
		browser.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void showHTML(final String html) {
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						/*
						 * Manage logged in or logged out conditions.
						 */
						if (running == false) {
							return;
						}

						wakeupAt = System.currentTimeMillis() + wakeup;

						/*
						 * Set user name, password and login.
						 */
						if (state == WebPageState.LOGGED_OUT) {
							debug(12);
							state = WebPageState.LOGGED_IN;

							loadUrl("javascript:{var uselessvar = document.getElementById('username').value = '"+username+"';}");
							loadUrl("javascript:{var uselessvar = document.getElementById('pass').value = '"+username+"';}");
							loadUrl(
											"javascript:{var uselessvar = document.getElementById('login').click();}", timeout);

							loadUrl("https://wwww.salsa.bg/", timeout);
						} else if (state == WebPageState.LOGGED_IN) {
							debug(13);
							state = WebPageState.BEFORE_SEARCH;

							loadUrl("https://www.salsa.bg/index.php?page=searchb", timeout);
						} else if (state == WebPageState.BEFORE_SEARCH) {
							debug(14);
							state = WebPageState.SEARCH_DONE;

							loadUrl(
											"javascript:{var uselessvar = (document.getElementsByName('btnSearch')[0]).click();}", timeout);
						} else if (state == WebPageState.SEARCH_DONE) {
							debug(15);
							state = WebPageState.PROFILE_SELECTED;
							loadUrl("https://www.salsa.bg/index.php?page=newMessage&userid=120383", timeout);
						} else if (state == WebPageState.PROFILE_SELECTED) {
							debug(16);
							state = WebPageState.MESSAGE_SENT;

							if (messageSend == true) {
								loadUrl(
												"javascript:{var uselessvar = (document.getElementsByName('msgTopic')[0]).value = '"+title+"';}");
								loadUrl(
												"javascript:{var uselessvar = document.getElementsByName('msgMessage')[0].value = '"+message+"';}");
								loadUrl(
												"javascript:{var uselessvar = (document.getElementsByName('sendMessage')[0]).click();}", timeout);
							}
							if (friendshipSend == true) {
							}

						} else if (state == WebPageState.MESSAGE_SENT) {
							debug(17);
							state = WebPageState.LOGGED_IN;

							loadUrl("https://www.salsa.bg/index.php?page=searchb", timeout);
						} else {
							debug(18);
							state = WebPageState.LOGGED_IN;

							loadUrl("https://wwww.salsa.bg/", timeout);
						}
					}
				});
			}
		}, "HTMLViewer");

		browser.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				/*
				 * Load inner HTML text.
				 */
				view.loadUrl(
								"javascript:HTMLViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
			}
		});
	}

	/**
	 * Create option menu.
	 *
	 * @param menu Menu information.
	 * @return Success of the creation.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main_options, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	/**
	 * On menu item selected handler.
	 *
	 * @param item Item which was selected.
	 * @return Is the handling successful.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			/*
			 * Handle start of the bot.
			 */
			case R.id.home_page:
				running = false;
				state = WebPageState.LOGGED_OUT;
				loadUrl("https://wwww.salsa.bg/");
				break;

			/*
			 * Handle start of the bot.
			 */
			case R.id.runnig_on:
				wakeupAt = System.currentTimeMillis() + wakeup;
				running = true;
				loadUrl("https://wwww.salsa.bg/");
				break;

			/*
			 * Handle stop of the bot.
			 */
			case R.id.runnig_off:
				running = false;
				break;

			/*
			 * Report found ids by email.
			 */
			case R.id.report:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tol@abv.bg"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "Report - " + new java.util.Date() + " ...");
				String message = "";
				message += "visited: ";
				List<Integer> list = new ArrayList<Integer>();
				Collections.sort(list);
				message += list.toString();
				message += "\n";
				intent.putExtra(Intent.EXTRA_TEXT, message);
				try {
					startActivity(Intent.createChooser(intent, "Send visited profiles report..."));
				} catch (android.content.ActivityNotFoundException ex) {
				}
				break;

			/*
			 * Run settings activity.
			 */
			case R.id.settings:
				startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY);
				break;
		}

		return true;
	}

	/**
	 * Web pages states.
	 */
	private enum WebPageState {
		LOGGED_OUT, LOGGED_IN, BEFORE_SEARCH, SEARCH_DONE, MESSAGE_BOX, PROFILE_SELECTED, MESSAGE_SENT,
	}

	/**
	 * User profile gender set.
	 */
	private enum UserGender {
		NONE, MALE, FEMALE
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTINGS_ACTIVITY) {
			initialize();
		}
	}
}
