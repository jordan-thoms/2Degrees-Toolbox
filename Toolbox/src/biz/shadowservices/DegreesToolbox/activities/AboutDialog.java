package biz.shadowservices.DegreesToolbox.activities;

import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.widget.TextView;

import biz.shadowservices.DegreesToolbox.R;

public class AboutDialog {
	/* code from http://www.itkrauts.com/archives/26-Creating-a-simple-About-Dialog-in-Android-1.6.html */
	public static AlertDialog create( Context context ) throws NameNotFoundException {
		// Try to load the a package matching the name of our own package
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		String versionInfo = pInfo.versionName;

		String aboutTitle = String.format("About %s", context.getString(R.string.app_name));
		String versionString = String.format("Version: %s", versionInfo);
		String aboutText = context.getResources().getString(R.string.about_dialog_text);

		// Set up the TextView
		final TextView message = new TextView(context);
		// We'll use a spannablestring to be able to make links clickable
		final SpannableString s = new SpannableString(aboutText);

		// Set some padding
		message.setPadding(5, 5, 5, 5);
		// Set up the final string
		message.setText(versionString + "\n\n" + s);
		// Now linkify the text
		Linkify.addLinks(message, Linkify.ALL);
		
		message.setTextColor(Color.WHITE);

		return new AlertDialog.Builder(context).setTitle(aboutTitle).setCancelable(true).setIcon(R.drawable.icon).setPositiveButton(
				context.getString(android.R.string.ok), null).setView(message).create();
	}
}
