package es.galapps.android.diccionariogalego.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import es.galapps.android.diccionariogalego.R;

public class News {

	public static final void showNews(LayoutInflater inflater, Context context) {

		View messageView = inflater.inflate(R.layout.news, null, false);

		TextView textView = (TextView) messageView.findViewById(R.id.news_title);
		textView.setTextColor(Color.WHITE);

		TextView textView2 = (TextView) messageView.findViewById(R.id.news_text);
		textView2.setTextColor(Color.WHITE);

		textView2.setText(context.getString(R.string.news_2_0_2) + "\n\n"
				+ context.getString(R.string.news_2_0_1) + "\n\n"
				+ context.getString(R.string.news_2_0));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.app_name);
		builder.setView(messageView);
		builder.setPositiveButton(R.string.ok,
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						((AlertDialog) dialog).getButton(which).setVisibility(View.INVISIBLE);
					}
				});
		builder.create();
		builder.show();
	}
}
