package cz.mammahelp.handy;

public enum SQLiteDataTypes {
	INTEGER("integer"), TEXT("text"), REAL("real"), BLOB("blob");

	private String typeName;

	private SQLiteDataTypes(String typeName) {
		this.typeName = typeName;
	}

	public String getType() {
		return typeName;
	}
}
