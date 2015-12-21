package io.reon.auth;

import java.util.HashMap;

import io.reon.util.Hex;

public abstract class AbstractAuth implements HttpAuth {
	public static final String REALM = "realm";
	public static final String RESPONSE = "response";
	public static final String NONCE = "nonce";
	public static final String PK = "pk";
	public static final String EQ = "=\"";
	public static final String EOS = "\", ";
	public static final String EOT = "\"";

	public static HashMap<String, String> parse(String text) {
		HashMap<String, String> attrs = new HashMap<>();
		try {
			String[] fields = text.substring(text.indexOf(' ') + 1).split(",");
			for (String field : fields) {
				String[] kv = field.split("=");
				attrs.put(kv[0].trim(), kv[1].replace('"', ' ').trim());
			}
		} catch (RuntimeException ex) {
			// ignore
		}
		return attrs;
	}

	public static byte[] fromHex(String value) {
		return Hex.decodeHex(value);
	}

	public static String toHex(byte[] value) {
		return Hex.encodeHex(value, false);
	}
}
