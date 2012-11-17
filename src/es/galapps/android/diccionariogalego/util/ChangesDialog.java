package es.galapps.android.diccionariogalego.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import es.galapps.android.diccionariogalego.R;

public class ChangesDialog {

	public static AlertDialog create(Context context) {

		View messageView = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.galego_changes, null, false);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.changes));
		builder.setView(messageView);
		builder.setPositiveButton(context.getString(android.R.string.ok), null);
		return builder.create();

	}
}