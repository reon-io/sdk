package io.reon.processor.model;

public class Exported {
	protected final String destClass;

	public Exported(String destClass) {
		this.destClass = destClass;
	}

	public String getGeneratedClassName() {
		return getDestClassSimple() + "_exp";
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
