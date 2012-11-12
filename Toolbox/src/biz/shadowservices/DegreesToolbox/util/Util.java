/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package biz.shadowservices.DegreesToolbox.util;

import android.content.Context;
import android.util.Log;
import java.text.DecimalFormat;

public class Util {
	public static DecimalFormat money = new DecimalFormat("$0.00");
	// Couple of utility functions
	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int size = (int) (dp * scale + 0.5f);
		return size;
	}
	public static int pxToSp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		int size = (int) (px * scale + 0.5f);
		return  size;
	}
	public static int dpToSp(Context context, int dp) {
		return pxToSp(context, dpToPx(context,dp));
	}
}
