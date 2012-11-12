package biz.shadowservices.DegreesToolbox.widgets;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class WidgetInstance implements Serializable {
	private static final long serialVersionUID = -7813357103780623893L;
	private String widgetType;
	private int widgetId;

	public WidgetInstance(int widgetId, String widgetType) {
		this.widgetId = widgetId;
		this.widgetType = widgetType;
	}
	
	public int getWidgetId() {
		return widgetId;
	}
	
	public int getSelectedBackgroundId(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.getInt("widgetSettings[" + widgetId + "][backgroundId]", 0);
	}
	
	public int getTransparency(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.getInt("widgetSettings[" + widgetId + "][transparency]", 0);
	}
	
	public int getTextColor(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.getInt("widgetSettings[" + widgetId + "][textColor]", 0xffffffff);
	}

	public void setSelectedBackgroundId(Context c, int id) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putInt("widgetSettings[" + widgetId + "][backgroundId]", id);
		e.commit();
	}
	
	public void setTransparency(Context c, int transparency) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putInt("widgetSettings[" + widgetId + "][transparency]", transparency);
		e.commit();
	}
	
	public void setTextColor(Context c, int color) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putInt("widgetSettings[" + widgetId + "][textColor]", color);
		e.commit();
	}


	@Override
	public String toString() {
		return widgetType + " - " +  widgetId;
	}
	
}
