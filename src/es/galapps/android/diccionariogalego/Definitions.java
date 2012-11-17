package es.galapps.android.diccionariogalego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import es.galapps.android.diccionariogalego.model.HtmlDefinition;
import es.galapps.android.diccionariogalego.util.AboutDialog;
import es.galapps.android.diccionariogalego.util.ChangesDialog;
import es.galapps.android.diccionariogalego.util.CustomSlidingDrawer;
import es.galapps.android.diccionariogalego.util.InstalledApplicationsUtils;
import es.galapps.android.diccionariogalego.util.News;
import es.galapps.android.diccionariogalego.util.SearchDefinitionTask;

public class Definitions extends Activity {

	private static final int CONJUGATE_REQUEST_CODE = 1;

	protected Typeface boldFont;
	private WebView definition;
	protected TextView conjugateText;
	private ImageButton conjugateButton;
	protected TextView translateText;
	private ImageButton translateButton;
	private CustomSlidingDrawer drawer;

	protected String word;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.definitions);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitlebar);

		this.boldFont = Typeface.createFromAsset(getAssets(), "fonts/CantarellBold.ttf");

		this.conjugateButton = (ImageButton) this.findViewById(R.id.conjugate);
		this.conjugateText = (TextView) this.findViewById(R.id.conjugateText);
		this.conjugateText.setTypeface(this.boldFont);

		this.translateButton = (ImageButton) this.findViewById(R.id.translate);
		this.translateText = (TextView) this.findViewById(R.id.translateText);
		this.translateText.setTypeface(this.boldFont);

		this.definition = (WebView) findViewById(R.id.definition);
		this.definition.getSettings().setBuiltInZoomControls(false);
		this.definition.setBackgroundColor(Color.TRANSPARENT);
//		this.definition.setBackgroundResource(R.drawable.fondo);

		ViewGroup zoom = (ViewGroup) findViewById(R.id.zoom);
		zoom.addView(this.definition.getZoomControls());

		Bundle extras = getIntent().getExtras();

		if (extras.containsKey("definition")) {
			final HtmlDefinition htmlDefinition = (HtmlDefinition) extras
					.getSerializable("definition");
			this.word = htmlDefinition.getWord();
			this.printResult(htmlDefinition.getHtml());

			final ImageView handle = (ImageView) this.findViewById(R.id.handle);
			this.drawer = (CustomSlidingDrawer) this.findViewById(R.id.drawer);
			this.drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

				@Override
				public void onDrawerOpened() {

					handle.setImageResource(R.drawable.handle_on);
				}
			});
			this.drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

				@Override
				public void onDrawerClosed() {

					handle.setImageResource(R.drawable.handle_off);
				}
			});

			this.translateText.setText(getString(R.string.translateWith, htmlDefinition.getWord()));

			this.translateButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {

					if (InstalledApplicationsUtils.isTradutorGalegoInstalled(Definitions.this)) {
						Intent translateIntent = new Intent(Intent.ACTION_VIEW);
						translateIntent.setComponent(new ComponentName(
								"es.galapps.android.tradutorgalego",
								"es.galapps.android.tradutorgalego.TradutorGalegoActivity"));
						translateIntent.putExtra("word", htmlDefinition.getWord());
						startActivity(translateIntent);
					} else {
						new AlertDialog.Builder(Definitions.this)
								.setTitle(R.string.downloadTradutorGalego)
								.setMessage(R.string.downloadTradutorGalegoMessage)
								.setCancelable(true)
								.setPositiveButton(Definitions.this.getString(android.R.string.ok),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface arg0, int arg1) {

												Intent goToMarket = new Intent(
														Intent.ACTION_VIEW,
														Uri.parse("market://details?id=es.galapps.android.tradutorgalego"));
												startActivity(goToMarket);
											}

										})
								.setNegativeButton(Definitions.this.getString(R.string.cancel),
										null).create().show();
					}
				}

			});

			if (htmlDefinition.isVerb()) {
				this.conjugateText.setText(getString(R.string.conjugateWith,
						htmlDefinition.getWord()));

				this.conjugateButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {

						if (InstalledApplicationsUtils.isConxuGalegoInstalled(Definitions.this)) {
							Intent conjugateIntent = new Intent(Intent.ACTION_VIEW);
							conjugateIntent.setComponent(new ComponentName(
									"es.sonxurxo.android.conxugalego",
									"es.sonxurxo.android.conxugalego.Verbs"));
							conjugateIntent.putExtra("infinitive", htmlDefinition.getWord());
							startActivity(conjugateIntent);
						} else {
							new AlertDialog.Builder(Definitions.this)
									.setTitle(R.string.downloadConxuGalego)
									.setMessage(R.string.downloadConxuGalegoMessage)
									.setCancelable(true)
									.setPositiveButton(
											Definitions.this.getString(android.R.string.ok),
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface arg0, int arg1) {

													Intent goToMarket = new Intent(
															Intent.ACTION_VIEW,
															Uri.parse("market://details?id=es.sonxurxo.android.conxugalego"));
													startActivity(goToMarket);
												}

											})
									.setNegativeButton(Definitions.this.getString(R.string.cancel),
											null).create().show();
						}
					}

				});
			} else {
				((LinearLayout) findViewById(R.id.conjugateContent)).setVisibility(View.GONE);
			}
		} else if (extras.containsKey("word")) {
			this.findViewById(R.id.drawer).setVisibility(View.GONE);
			this.word = extras.getString("word");
			this.search(this.word);
		}

		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	}

	protected void search(final String theWord) {

		new SearchDefinitionTask(Definitions.this) {

			@Override
			protected void onPostExecuteOptions(HtmlDefinition result) {

				/*
				 * Puede llegar aqu� desde la otra aplicaci�n, si se le dice que
				 * busque la definici�n de un verbo y este tiene varias
				 * acepciones
				 */
				final String[] options = result.getHtml().split("\n");

				AlertDialog.Builder builder = new AlertDialog.Builder(Definitions.this);
				builder.setTitle(R.string.wordNotFoundOptions);
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String chosenWord = options[which];
						Definitions.this.search(chosenWord);
					}
				};
				builder.setItems(options, listener);
				builder.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {

						Definitions.this.finish();
					}

				});
				builder.create().show();
			}

			@Override
			protected void onPostExecuteDefinition(HtmlDefinition result) {

				Definitions.this.printResult(result.getHtml());
			}

			@Override
			protected void onPostExecuteNotFound() {

				Intent data = new Intent();
				data.putExtra("infinitive", theWord);
				setResult(-1, data);
				finish();
			}

			@Override
			protected void onPostExecuteConnectionError() {

				Intent data = new Intent();
				setResult(-2, data);
				finish();
			}

            @Override
            protected void onPostExecuteUnknownError() {

            	Intent data = new Intent();
				setResult(-2, data);
				finish();
            }

		}.execute(theWord.replace(" ", "%20"));
	}

	protected void printResult(String theDefinition) {

		String def = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" /></head><body><div class=\"cuadro\"></div>"
				+ theDefinition + "</body></html>";
		this.definition.loadDataWithBaseURL("file:///android_asset/", def, "text/html",
				"UTF-8", "");
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
					this.getString(R.string.webURL, this.word));

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case CONJUGATE_REQUEST_CODE:
			switch (resultCode) {
			case -1:
				Toast.makeText(this,
						getString(R.string.conjugateNotFound, data.getStringExtra("word")),
						Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(this, getString(R.string.connectionError), Toast.LENGTH_LONG).show();
				break;
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {

		if (this.drawer != null && this.drawer.isOpened()) {

			this.drawer.close();

		} else {

			super.onBackPressed();
		}
	}
}
