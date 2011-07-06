package biz.shadowservices.DegreesToolbox;

import java.util.ArrayList;
import java.util.List;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

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
	public static PackTreeNode valuePacks;
	public static String CONFIRMDATA = "Are you sure you wish to purchase the data pack ";
	public static String CONFIRMDATACANCEL = "Are you sure you wish to cancel " ;
	public static String CONFIRMTALK = "Are you sure you wish to purchase the talk pack ";
	static {
		valuePacks = new PackTreeNode(null, "Choose a value pack to buy:");
		valuePacks.addChild( new PackTreeLeaf("$10 2000 Texts", "Are sure you wish to buy ", "buy 10txt", 1000));
		valuePacks.addChild( new PackTreeLeaf("$19 Combo Pack", "Are sure you wish to buy ", "buy 19combo", 1900));
		PackTreeNode nationalData = new PackTreeNode("National Data", "Choose national data pack:");
		nationalData.addChild(new PackTreeLeaf("$6 50MB National Data", CONFIRMDATA, "buy 50MB", 600));
		nationalData.addChild(new PackTreeLeaf("$10 100MB National Data", CONFIRMDATA, "buy 100MB", 1000));
		valuePacks.addChild(nationalData);
		PackTreeNode bbZone = new PackTreeNode("BB Zone Data", "Choose BB Zone data pack:");
		bbZone.addChild(new PackTreeLeaf("$20 1GB Zone Data", CONFIRMDATA, "buy 1GB", 2000));
		bbZone.addChild(new PackTreeLeaf("$50 3GB Zone Data", CONFIRMDATA, "buy 3GB", 5000));
		bbZone.addChild(new PackTreeLeaf("$150 12GB Zone Data", CONFIRMDATA, "buy 3GB", 1500));
		valuePacks.addChild(bbZone);
		PackTreeNode talkPacks = new PackTreeNode("Talk Packs", "Choose talk pack:");
		talkPacks.addChild(new PackTreeLeaf("$30 Everyone100", CONFIRMTALK, "buy every100", 3000));
		talkPacks.addChild(new PackTreeLeaf("$10 China120", CONFIRMTALK, "buy china120", 1000));
		talkPacks.addChild(new PackTreeLeaf("$10 India120", CONFIRMTALK, "buy india120", 1000));
		valuePacks.addChild(talkPacks);
		PackTreeNode cancelData = new PackTreeNode("Cancel Data renew", "Choose data pack to cancel renewals for:");
		cancelData.addChild(new PackTreeLeaf("National Data", CONFIRMDATACANCEL, "Stop Nat", 0));
		cancelData.addChild(new PackTreeLeaf("1GB Pack", CONFIRMDATACANCEL, "Stop 1GB", 0));
		cancelData.addChild(new PackTreeLeaf("3GB Pack", CONFIRMDATACANCEL, "Stop 3GB", 0));
		cancelData.addChild(new PackTreeLeaf("12GB Pack", CONFIRMDATACANCEL, "Stop 12GB", 0));
		valuePacks.addChild(cancelData);
	}

}
