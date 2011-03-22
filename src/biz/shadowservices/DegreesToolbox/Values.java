package biz.shadowservices.DegreesToolbox;

import java.util.ArrayList;
import java.util.List;

public class Values {

	// List of widget updaters to call.
	public static List<AbstractWidgetUpdater> widgetUpdaters = new ArrayList<AbstractWidgetUpdater>();
	static int TIMEOUT = 10000;
	public static Integer[] backgroundIds = {
	        R.drawable.widget_frame,
	        R.drawable.widget_frame_green,
	        R.drawable.widget_frame_metal,
	        R.drawable.widget_frame_wood,
	};

}
