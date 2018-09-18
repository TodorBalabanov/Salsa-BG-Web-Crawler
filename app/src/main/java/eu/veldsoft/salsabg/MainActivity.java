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
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
	 * Name of the file used to store ids.
	 */
	private static final String IDS_FILE_NAME = "ids.bin";

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
	 * Reporitng email.
	 */
	private String email = "";

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
	 * Id of the user to which to write a message.
	 */
	private int userid = -1;

	/**
	 * Set of ids found.
	 */
	private Set<Integer> ids = new HashSet<Integer>();

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
		email = getSharedPreferences(MainActivity.class.getName(), MODE_PRIVATE).getString("email", "");
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

		/* Load ids found in previous sessions. */
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(getFilesDir() + IDS_FILE_NAME));
			ids = (HashSet<Integer>) in.readObject();
			in.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}

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

							loadUrl("javascript:{var uselessvar = document.getElementById('username').value = '" + username + "';}");
							loadUrl("javascript:{var uselessvar = document.getElementById('pass').value = '" + username + "';}");
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

							/* Keep list of the ids. */
							try {
								ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getFilesDir() + IDS_FILE_NAME));
								out.writeObject(ids);
								out.close();
							} catch (IOException e) {
							}

							loadUrl(
											"javascript:{var uselessvar = (document.getElementsByName('btnSearch')[0]).click();}", timeout);
						} else if (state == WebPageState.SEARCH_DONE) {
							debug(15);

							/* Handling of the HTML text to find information written inside the webpage. */
							browser.evaluateJavascript(
											"(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
											new ValueCallback<String>() {
												@Override
												public void onReceiveValue(String html) {
													userid = -1;
													int index = 0;
													while ((index = html.indexOf("newMessage&userid=", index)) != -1) {
														int end = html.indexOf("\"", index);
														int id = Integer.parseInt(html.substring(index, end));

														print("" + id);
														/* Check is the ide used. */
														if (ids.contains(id) == false) {
															userid = id;
															break;
														}

														index++;
													}
												}
											});

							if (userid == -1) {
								state = WebPageState.BEFORE_SEARCH;
								loadUrl("https://www.salsa.bg/index.php?page=searchb", timeout);
							} else {
								state = WebPageState.PROFILE_SELECTED;
								ids.add(userid);
								loadUrl("https://www.salsa.bg/index.php?page=newMessage&userid=" + userid, timeout);
								userid = -1;
							}
						} else if (state == WebPageState.PROFILE_SELECTED) {
							debug(16);
							state = WebPageState.MESSAGE_SENT;

							if (messageSend == true) {
								loadUrl(
												"javascript:{var uselessvar = (document.getElementsByName('msgTopic')[0]).value = '" + title + "';}");
								loadUrl(
												"javascript:{var uselessvar = document.getElementsByName('msgMessage')[0].value = '" + message + "';}");
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

		/* Handling page finished event. */
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
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
				intent.putExtra(Intent.EXTRA_SUBJECT, "Report - " + new java.util.Date() + " ...");
				String message = "";
				message += "visited: ";
				List<Integer> list = new ArrayList<Integer>(ids);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTINGS_ACTIVITY) {
			initialize();
		}
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
}
