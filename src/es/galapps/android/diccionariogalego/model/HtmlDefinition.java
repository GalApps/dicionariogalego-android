package es.galapps.android.diccionariogalego.model;

import java.io.Serializable;

public class HtmlDefinition implements Serializable {

	private static final long serialVersionUID = 6811960876686465249L;

	private String word;
	private String html;
	private boolean isVerb;

	public HtmlDefinition(String word, String html, boolean isVerb) {
		super();
		this.word = word;
		this.html = html;
		this.isVerb = isVerb;
	}

	public String getWord() {
		return this.word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getHtml() {
		return this.html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public boolean isVerb() {
		return this.isVerb;
	}

	public void setVerb(boolean isVerb) {
		this.isVerb = isVerb;
	}
}
