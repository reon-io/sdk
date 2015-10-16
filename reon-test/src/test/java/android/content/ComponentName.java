package android.content;

public class ComponentName {
	private final String name;

	public ComponentName(String p, String c) {
		name = p+"/"+c;
	}
}
