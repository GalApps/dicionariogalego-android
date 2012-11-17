package es.galapps.android.diccionariogalego.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import es.galapps.android.diccionariogalego.R;
import es.galapps.android.diccionariogalego.model.HtmlDefinition;

public abstract class SearchDefinitionTask extends AsyncTask<String, Void, HtmlDefinition> {

	// protected static final String SERVER_URL =
	// "http://www.edu.xunta.es/diccionarios/BuscaTermo.jsp?Termo=";
	// protected static final String SERVER_URL =
	// "http://www.realacademiagalega.org/rag_diccionario/searchNoun.do?nounTitle=";
	// protected static final String SERVER_URL =
	// "http://www.realacademiagalega.org/dicionario#searchNoun.do?nounTitle=";
	protected static final String SERVER_URL = "http://www.realacademiagalega.org/rag_dicionario/searchNoun.do?nounTitle=";
	// protected static final String SERVER_URL =
	// "http://coruna.des.udc.es/definition2.html?nounTitle=";
	protected static final String SEARCH_NOUNS_URL = "http://www.realacademiagalega.org/rag_dicionario/searchNouns.do?term=";
	protected static final String OPTIONS = "OPTIONS:";
	protected static final String CONNECTION_ERROR = "CONNECTION_ERROR";
	protected static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	private final ProgressDialog dialog;
	private final Context context;

	public SearchDefinitionTask(Context context) {
		super();
		this.context = context;
		this.dialog = new ProgressDialog(context);
		this.dialog.setCancelable(true);
		this.dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {

				SearchDefinitionTask.this.cancel(true);
			}

		});
		this.dialog.setMessage(this.context.getString(R.string.loadingData));
	}

	@Override
	protected void onPreExecute() {

		this.dialog.show();
	}

	@Override
	protected HtmlDefinition doInBackground(String... words) {

		String serverURL = SEARCH_NOUNS_URL;

		InputStream is = null;
		JSONArray definitions = null;
		String json = "";

		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(serverURL
					+ URLEncoder.encode(words[0], "UTF-8").toLowerCase());

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			definitions = new JSONArray(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		if (definitions == null || definitions.length() == 0) {
			return null;
		}

		try {

			boolean isVerb = false;
			String html = "";
			// looping through All Contacts
			for (int i = 0; i < definitions.length(); i++) {
				JSONObject c = definitions.getJSONObject(i);

				// Storing each json item in variable
				String simpleTitle = c.getString("simpleTitle");
				
				if (!words[0].equalsIgnoreCase(simpleTitle)) {
					continue;
				}
					
				String htmlDefinition = c.getString("htmlDescription");

				html = html + "<div class=\"title\"><div class=\"word\">" + encode(words[0]);
				if (definitions.length() > 1) {
					html = html + "<sup class=\"miniSup\">" + String.valueOf(i + 1) + "</sup>";
				}
				html = html + "</div></div><div class=\"content\">" + htmlDefinition + "</div>";
				html = html.replaceAll("¿", "");
				html = html.replaceAll("&iquest;", "");
				if (!isVerb) {
					if (htmlDefinition.contains("v.i.") || htmlDefinition.contains("v.t.")
							|| htmlDefinition.contains("v.p.")) {
						isVerb = true;
					}
				}

			}
			if (html.equals(""))
			{
				return null;
			}

			return new HtmlDefinition(words[0], html, isVerb);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		//
		// try {
		//
		// String serverURL = SEARCH_NOUNS_URL;
		// Document doc = Jsoup
		// .connect(serverURL + URLEncoder.encode(words[0],
		// "UTF-8").toLowerCase())
		// .ignoreContentType(true)
		// .header("User-Agent",
		// "Mozilla/5.0 (X11; Linux i686; rv:7.0.1) Gecko/20100101 Firefox/7.0.1")
		// .header("Referer",
		// "http://www.edu.xunta.es/diccionarios/BuscaTermo.jsp").get();
		//
		// Elements elements = doc.getElementsByTag("simpleTitle");
		//
		// Log.i("Lalalala", doc.text());
		//
		//
		// Elements notFound = doc.select("div[class=not_found]");
		// if (notFound.size() != 0) {
		// return null;
		// }
		// Elements base = doc.select("div[class=description]");
		//
		// boolean isVerb = false;
		//
		// Elements all = base.get(0).getAllElements();
		// boolean firstW = true;
		// for (int i = 0; i < all.size(); i++) {
		// Element element = all.get(i);
		//
		// if (!isVerb) {
		// String html = element.html();
		// if (html.contains("v.i.") || html.contains("v.t.") ||
		// html.contains("v.p.")) {
		// isVerb = true;
		// }
		// }
		// String html = element.html();
		// if (element.hasClass("usg") && element.hasClass("smbol")) {
		// element.html(" · ");
		// element.addClass("bold");
		// }
		// if (element.hasClass("cit")) {
		// element.addClass("italic");
		// }
		// if (element.hasClass("syn")) {
		// element.addClass("bold");
		// }
		// if (element.hasClass("w")) {
		// if (firstW) {
		// firstW = false;
		// }
		// else {
		// element.prepend("<br/>");
		// element.prepend("<br/>");
		// element.append("<br/>");
		// element.append("<br/>");
		// }
		// element.addClass("little_italic");
		// }
		// if (isNumber(html) && !element.hasClass("superindex")) {
		// /*
		// * Para cada "sub-acepcion", meterle un salto de linea
		// */
		// element.prepend("<br/>");
		// element.addClass("number");
		// }
		// if (element.hasClass("noun")) {
		// element.remove();
		// }
		// }
		// /*
		// * Arreglos para crear un div class="content" para el contenido
		// */
		//
		// String html = "<div class=\"title\"><div class=\"word\">" +
		// encode(words[0])
		// + "</div></div><div class=\"content\">" + base.outerHtml()
		// + "</div>";
		// html = html.replaceAll("¿", "");
		// html = html.replaceAll("&iquest;", "");
		// return new HtmlDefinition(words[0], html, isVerb);
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// return new HtmlDefinition(words[0], CONNECTION_ERROR, false);
		// } catch (Exception e) {
		// return new HtmlDefinition(words[0], CONNECTION_ERROR, false);
		// }

		//
		// try {
		//
		// String serverURL = GetLastURLTask.getLastURL() != null ?
		// GetLastURLTask.getLastURL() : SERVER_URL;
		// Document doc = Jsoup
		// .connect(serverURL + URLEncoder.encode(words[0],
		// "UTF-8").toLowerCase())
		// .header("User-Agent",
		// "Mozilla/5.0 (X11; Linux i686; rv:7.0.1) Gecko/20100101 Firefox/7.0.1")
		// .header("Referer",
		// "http://www.edu.xunta.es/diccionarios/BuscaTermo.jsp").get();
		//
		// Elements notFound = doc.select("div[class=not_found]");
		// if (notFound.size() != 0) {
		// return null;
		// }
		// Elements base = doc.select("div[class=description]");
		//
		// boolean isVerb = false;
		//
		// Elements all = base.get(0).getAllElements();
		// boolean firstW = true;
		// for (int i = 0; i < all.size(); i++) {
		// Element element = all.get(i);
		//
		// if (!isVerb) {
		// String html = element.html();
		// if (html.contains("v.i.") || html.contains("v.t.") ||
		// html.contains("v.p.")) {
		// isVerb = true;
		// }
		// }
		// String html = element.html();
		// if (element.hasClass("usg") && element.hasClass("smbol")) {
		// element.html(" · ");
		// element.addClass("bold");
		// }
		// if (element.hasClass("cit")) {
		// element.addClass("italic");
		// }
		// if (element.hasClass("syn")) {
		// element.addClass("bold");
		// }
		// if (element.hasClass("w")) {
		// if (firstW) {
		// firstW = false;
		// }
		// else {
		// element.prepend("<br/>");
		// element.prepend("<br/>");
		// element.append("<br/>");
		// element.append("<br/>");
		// }
		// element.addClass("little_italic");
		// }
		// if (isNumber(html) && !element.hasClass("superindex")) {
		// /*
		// * Para cada "sub-acepcion", meterle un salto de linea
		// */
		// element.prepend("<br/>");
		// element.addClass("number");
		// }
		// if (element.hasClass("noun")) {
		// element.remove();
		// }
		// }
		// /*
		// * Arreglos para crear un div class="content" para el contenido
		// */
		//
		// String html = "<div class=\"title\"><div class=\"word\">" +
		// encode(words[0])
		// + "</div></div><div class=\"content\">" + base.outerHtml()
		// + "</div>";
		// html = html.replaceAll("¿", "");
		// html = html.replaceAll("&iquest;", "");
		// return new HtmlDefinition(words[0], html, isVerb);
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// return new HtmlDefinition(words[0], CONNECTION_ERROR, false);
		// } catch (Exception e) {
		// return new HtmlDefinition(words[0], CONNECTION_ERROR, false);
		// }

	}

	protected boolean isNumber(String s) {
		try {
			s = s.replace(".", "");
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected void onPostExecute(HtmlDefinition result) {

		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}

		if (result == null) {
			this.onPostExecuteNotFound();
		} else if (result.getHtml().startsWith(OPTIONS)) {
			result.setHtml(result.getHtml().replace(OPTIONS, ""));
			this.onPostExecuteOptions(result);
		} else if (result.getHtml().equals(CONNECTION_ERROR)) {
			this.onPostExecuteConnectionError();
		} else if (result.getHtml().equals(UNKNOWN_ERROR)) {
			this.onPostExecuteUnknownError();
		} else {
			this.onPostExecuteDefinition(result);
		}
	}

	protected abstract void onPostExecuteNotFound();

	protected abstract void onPostExecuteConnectionError();

	protected abstract void onPostExecuteUnknownError();

	protected abstract void onPostExecuteOptions(HtmlDefinition result);

	protected abstract void onPostExecuteDefinition(HtmlDefinition result);

	@Override
	protected void onCancelled() {

		super.onCancelled();
		Toast.makeText(this.context, R.string.cancelled, Toast.LENGTH_SHORT).show();
	}

	private String decode(String word) {

		String encodeWord = word.replace("&aacute;", "á");
		encodeWord = encodeWord.replace("&eacute;", "é");
		encodeWord = encodeWord.replace("&iacute;", "í");
		encodeWord = encodeWord.replace("&oacute;", "ó");
		encodeWord = encodeWord.replace("&uacute;", "ú");
		encodeWord = encodeWord.replace("&ntilde;", "ñ");
		encodeWord = encodeWord.replace("&Uuml;", "ü");
		return encodeWord;
	}

	private String encode(String word) {

		String encodeWord = word.replace("á", "&aacute;");
		encodeWord = encodeWord.replace("é", "&eacute;");
		encodeWord = encodeWord.replace("í", "&iacute;");
		encodeWord = encodeWord.replace("ó", "&oacute;");
		encodeWord = encodeWord.replace("ú", "&uacute;");
		encodeWord = encodeWord.replace("ñ", "&ntilde;");
		encodeWord = encodeWord.replace("ü", "&Uuml;");
		encodeWord = encodeWord.replace("%20", " ");
		return encodeWord;
	}

}
