package es.galapps.android.diccionariogalego.util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;

public class GetLastURLTask extends AsyncTask<Void, Void, Void> {

	private static String lastUrl = null;

	public GetLastURLTask() {
		super();
	}

	@Override
	protected Void doInBackground(Void... words) {
		try {
			Document doc = Jsoup.connect("http://galapps.es/dicionario/lastUrl").get();
			lastUrl = doc.text();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getLastURL() {
		return lastUrl;
	}

}
