package io.reon.processor.model;

public class ParsedFilter {
	protected final String destClass;
	private final String value;

	private final String destMethod;
	private boolean before;
	public ParsedFilter(String value, String destClass, String destMethod, boolean before) {
		this.destClass = destClass;
		this.value = value;
		this.destMethod = destMethod;
		this.before = before;
	}

	public String getGeneratedClassName() {
		return getDestClassSimple() + "_" + getDestMethod();
	}

	public String getValue() {
		return value;
	}

	public String getDestMethod() {
		return destMethod;
	}

	public boolean isBefore() {
		return before;
	}

	public String getDestClassSimple() {
		return getDestClass().substring(getDestClass().lastIndexOf(".") + 1).trim();
	}

	public String getPackage() {
		return getDestClass().substring(0, getDestClass().lastIndexOf("."));
	}

	public String getDestClass() {
		return destClass;
	}
}
