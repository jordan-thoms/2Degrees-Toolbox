package biz.shadowservices.DegreesToolbox;

import java.io.Serializable;


public class WidgetInstance implements Serializable {
	private static final long serialVersionUID = -7813357103780623893L;
	private String widgetType;
	private int widgetId;
	public WidgetInstance(int widgetId, String widgetType) {
		this.widgetId = widgetId;
		this.widgetType = widgetType;
	}
	@Override
	public String toString() {
		return widgetType + " - " +  widgetId;
	}
	
}
