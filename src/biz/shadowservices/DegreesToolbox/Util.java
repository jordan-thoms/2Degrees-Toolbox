package biz.shadowservices.DegreesToolbox;

import android.content.Context;

public class Util {
	public static int dpToPx(Context context, int px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int size = (int) (px * scale + 0.5f);
		return size;
	}
}
