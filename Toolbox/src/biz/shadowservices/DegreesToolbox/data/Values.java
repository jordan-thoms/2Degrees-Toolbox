package biz.shadowservices.DegreesToolbox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.R.drawable;
import biz.shadowservices.DegreesToolbox.data.ValuePack.Type;
import biz.shadowservices.DegreesToolbox.widgets.AbstractWidgetUpdater;

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
	public static Double INCLUDED = -100.0;
	public static PackTreeNode purchaseValuePacks;
	public static HashMap<String, ValuePack[]> valuePacks;
	public static String CONFIRMDATA = "Are you sure you wish to purchase the data pack ";
	public static String CONFIRMDATACANCEL = "Are you sure you wish to cancel " ;
	public static String CONFIRMTALK = "Are you sure you wish to purchase the talk pack ";
	static {
		purchaseValuePacks = new PackTreeNode(null, "Choose a value pack to buy:");
		purchaseValuePacks.addChild( new PackTreeLeaf("$10 2000 Texts", "Are sure you wish to buy ", "buy 10txt", 1000));
		purchaseValuePacks.addChild( new PackTreeLeaf("$19 Text Combo", "Are sure you wish to buy ", "buy 19text", 1900));
		purchaseValuePacks.addChild( new PackTreeLeaf("$19 Data Combo", "Are sure you wish to buy ", "buy 19data", 1900));
		purchaseValuePacks.addChild( new PackTreeLeaf("$19 Chat Combo", "Are sure you wish to buy ", "buy 19chat", 1900));
		PackTreeNode nationalData = new PackTreeNode("National Data", "Choose national data pack:");
		nationalData.addChild(new PackTreeLeaf("$6 50MB National Data", CONFIRMDATA, "buy 50MB", 600));
		nationalData.addChild(new PackTreeLeaf("$10 100MB National Data", CONFIRMDATA, "buy 100MB", 1000));
		purchaseValuePacks.addChild(nationalData);
		PackTreeNode bbZone = new PackTreeNode("BB Zone Data", "Choose BB Zone data pack:");
		bbZone.addChild(new PackTreeLeaf("$20 1GB Zone Data", CONFIRMDATA, "buy 1GB", 2000));
		bbZone.addChild(new PackTreeLeaf("$50 3GB Zone Data", CONFIRMDATA, "buy 3GB", 5000));
		bbZone.addChild(new PackTreeLeaf("$150 12GB Zone Data", CONFIRMDATA, "buy 12GB", 15000));
		purchaseValuePacks.addChild(bbZone);
		PackTreeNode talkPacks = new PackTreeNode("Talk Packs", "Choose talk pack:");
		talkPacks.addChild(new PackTreeLeaf("$30 Everyone100", CONFIRMTALK, "buy every100", 3000));
		talkPacks.addChild(new PackTreeLeaf("$10 China120", CONFIRMTALK, "buy china120", 1000));
		talkPacks.addChild(new PackTreeLeaf("$10 India120", CONFIRMTALK, "buy india120", 1000));
		purchaseValuePacks.addChild(talkPacks);
		PackTreeNode cancelData = new PackTreeNode("Cancel Data renew", "Choose data pack to cancel renewals for:");
		cancelData.addChild(new PackTreeLeaf("National Data", CONFIRMDATACANCEL, "Stop Nat", 0));
		cancelData.addChild(new PackTreeLeaf("1GB Pack", CONFIRMDATACANCEL, "Stop 1GB", 0));
		cancelData.addChild(new PackTreeLeaf("3GB Pack", CONFIRMDATACANCEL, "Stop 3GB", 0));
		cancelData.addChild(new PackTreeLeaf("12GB Pack", CONFIRMDATACANCEL, "Stop 12GB", 0));
		purchaseValuePacks.addChild(cancelData);
		
		valuePacks = new HashMap<String, ValuePack[]>();
		valuePacks.put("10TXT", new ValuePack[] {new ValuePack(Type.TEXTS, 2500)});
		valuePacks.put("19combo", new ValuePack[] {new ValuePack(Type.TEXTS, 2500), new ValuePack(Type.MINS, 30), new ValuePack(Type.NATDATA, 50)});
		valuePacks.put("EVERYONE100", new ValuePack[] {new ValuePack(Type.MINS, 100)});
		valuePacks.put("50MB", new ValuePack[] {new ValuePack(Type.NATDATA, 50)});
		valuePacks.put("100MB", new ValuePack[] {new ValuePack(Type.NATDATA, 100)});
		valuePacks.put("1GB", new ValuePack[] {new ValuePack(Type.ZONEDATA, 1024)});
		valuePacks.put("3GB", new ValuePack[] {new ValuePack(Type.ZONEDATA, 3072)});
		valuePacks.put("12GB", new ValuePack[] {new ValuePack(Type.ZONEDATA, 12288)});
		
		
	}

}
