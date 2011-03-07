package biz.shadowservices.DegreesToolbox;

import android.content.Context;
import android.util.Log;

public class Util {
	// Couple of utility functions
	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		Log.d("2DegreesSCALE", Float.toString(scale));
		int size = (int) (dp * scale + 0.5f);
		Log.d("2DegreesDp", Float.toString(dp));
		Log.d("2DegreesSize", Integer.toString(size));
		return size;
	}
	public static int pxToSp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		Log.d("2DegreesSCALESP", Float.toString(scale));
		int size = (int) (px * scale + 0.5f);
		Log.d("2DegreesPx", Float.toString(px));
		Log.d("2DegreesSizeSp", Integer.toString(size));

		return  size;
	}
	public static int dpToSp(Context context, int dp) {
		return pxToSp(context, dpToPx(context,dp));
	}
}
