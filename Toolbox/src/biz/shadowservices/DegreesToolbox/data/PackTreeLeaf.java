package biz.shadowservices.DegreesToolbox.data;

public class PackTreeLeaf  extends PackTreeNode {
	private int value;
	private String message;
	private String confirmText;
	public PackTreeLeaf(String title,  String confirmText, String message, int value ) {
		super(title, null);
		this.confirmText = confirmText;
		this.message = message;
		this.value = value;
	}
	public String getMessage() {
		return message;
	}
	public int getValue() {
		return value;
	}
	public String getConfirmText() {
		return confirmText;
	}

}