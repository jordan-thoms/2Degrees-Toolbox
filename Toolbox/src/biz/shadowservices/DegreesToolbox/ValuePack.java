package biz.shadowservices.DegreesToolbox;

public class ValuePack {
	enum Type {
		NATDATA ("National Data"),
		ZONEDATA ("Broadband Zone Data"),
		TEXTS ("Texts"),
		MINS ("NZ minutes");
		
		public final String id;
		Type(String id) {
			this.id = id;
		}
	}
	public Type type;
	public double value;
	public ValuePack(Type type, double value) {
		this.type = type;
		this.value = value;
	}
}
