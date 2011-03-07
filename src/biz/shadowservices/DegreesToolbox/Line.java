package biz.shadowservices.DegreesToolbox;

public class Line {
	// Used when constructing the lines for the widgets.
	private String lineContent;
	private float size = -1;
	Line(String lineContent) {
		this.lineContent = lineContent;
	}
	String getLineContent() {
		return lineContent;
	}
	void setSize(float size) {
		this.size = size;
	}
	float getSize() {
		return size;
	}
}
