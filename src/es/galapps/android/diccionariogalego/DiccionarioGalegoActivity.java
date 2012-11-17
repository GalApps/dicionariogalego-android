package es.galapps.android.diccionariogalego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import es.galapps.android.diccionariogalego.model.HtmlDefinition;
import es.galapps.android.diccionariogalego.util.AboutDialog;
import es.galapps.android.diccionariogalego.util.GetLastURLTask;
import es.galapps.android.diccionariogalego.util.News;
import es.galapps.android.diccionariogalego.util.SearchDefinitionTask;

public class DiccionarioGalegoActivity extends Activity {

	private static final String VERSION = "VERSION:";

	private TextView mIntroText;
	protected EditText mVerb;
	private ImageButton mLaunchButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitlebar);

		new GetLastURLTask().execute();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (prefs.getInt(VERSION, 1) < getPackageVersion()) {
			News.showNews(getLayoutInflater(), this);
		}

		this.mIntroText = (TextView) findViewById(R.id.introDataText);
		this.mIntroText.setTypeface(Typeface
				.createFromAsset(getAssets(), "fonts/CantarellBold.ttf"));

		this.mVerb = (EditText) findViewById(R.id.input);
		this.mVerb.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					DiccionarioGalegoActivity.this.search(DiccionarioGalegoActivity.this.mVerb
							.getText().toString());
					return true;
				}
				return false;
			}
		});

		final Drawable x = getResources().getDrawable(R.drawable.presence_offline);
		x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
		mVerb.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

				mVerb.setCompoundDrawables(null, null, mVerb.getText().toString().equals("") ? null
						: x, null);

			}

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

		});

		mVerb.setCompoundDrawables(null, null, null, null);
		mVerb.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (mVerb.getCompoundDrawables()[2] == null) {
					return false;
				}
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}
				if (event.getX() > mVerb.getWidth() - mVerb.getPaddingRight()
						- x.getIntrinsicWidth()) {
					mVerb.setText("");
					mVerb.setCompoundDrawables(null, null, null, null);
				}
				return false;
			}
		});

		this.mLaunchButton = (ImageButton) findViewById(R.id.search);
		this.mLaunchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				DiccionarioGalegoActivity.this.search(DiccionarioGalegoActivity.this.mVerb
						.getText().toString());
			}

		});

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String word = extras.getString("word");
			if (word != null) {
				search(word);
			}
		}

	}

	protected void search(final String text) {

		if (!text.equals("")) {
			if (!haveInternet()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DiccionarioGalegoActivity.this);
				builder.setTitle(R.string.noDataConnection);
				builder.setMessage(R.string.noDataConnectionMessage);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.configureNetwork,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dlalog, int arg1) {

								startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
							}

						});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dlalog, int arg1) {

						Toast.makeText(DiccionarioGalegoActivity.this, R.string.noDataDecided,
								Toast.LENGTH_SHORT).show();
					}

				});
				builder.create().show();
			} else {
				new SearchDefinitionTask(DiccionarioGalegoActivity.this) {

					@Override
					protected void onPostExecuteOptions(HtmlDefinition result) {

						final String[] options = result.getHtml().split("\n");

						AlertDialog.Builder builder = new AlertDialog.Builder(
								DiccionarioGalegoActivity.this);
						builder.setTitle(R.string.wordNotFoundOptions);
						DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								DiccionarioGalegoActivity.this.search(options[which]);

							}
						};
						builder.setItems(options, listener);
						builder.create().show();
					}

					@Override
					protected void onPostExecuteDefinition(HtmlDefinition result) {

						Intent definitionsIntent = new Intent(getApplicationContext(),
								Definitions.class);
						definitionsIntent.putExtra("definition", result);
						definitionsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(definitionsIntent);
					}

					@Override
					protected void onPostExecuteNotFound() {

						Toast.makeText(
								DiccionarioGalegoActivity.this,
								DiccionarioGalegoActivity.this.getString(R.string.wordNotFound,
										text), Toast.LENGTH_SHORT).show();
					}

					@Override
					protected void onPostExecuteConnectionError() {

						Toast.makeText(DiccionarioGalegoActivity.this,
								getString(R.string.connectionError), Toast.LENGTH_LONG).show();
					}

					@Override
					protected void onPostExecuteUnknownError() {

						Toast.makeText(DiccionarioGalegoActivity.this,
								getString(R.string.unknownError), Toast.LENGTH_LONG).show();
					}
				}.execute(text.replace(" ", "%20"));
			}
		}
	}

	protected boolean haveInternet() {

		NetworkInfo info = ((ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnectedOrConnecting()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return true;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.about:
			AlertDialog builder;
			try {
				builder = AboutDialog.create(this);
				builder.show();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		case R.id.share:
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					this.getString(R.string.shareTitle));
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					this.getString(R.string.galappsURL));

			startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
			return true;
		case R.id.advice:
			News.showNews(getLayoutInflater(), this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {

		super.onPause();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Editor edit = prefs.edit();
		edit.putInt(VERSION, getPackageVersion());
		edit.commit();

	}

	private final int getPackageVersion() {

		try {

			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

			return pinfo.versionCode;

		} catch (NameNotFoundException e) {
			// Empty on purpose
		}

		return 0;

	}
}