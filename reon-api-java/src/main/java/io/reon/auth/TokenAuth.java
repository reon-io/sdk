package io.reon.auth;

import java.security.SecureRandom;
import java.util.HashMap;

public class TokenAuth extends AbstractAuth {
	public static final String TOKEN_AUTH = "Token ";
	public static final String TOKEN = "token";
	private final String token;
	private final String realm;

	public TokenAuth(String token, String realm) {
		this.token = token;
		this.realm = realm;
	}

	public TokenAuth(String realm) {
		this(toHex(new SecureRandom().generateSeed(16)), realm);
	}

	public String getRealm() {
		return realm;
	}

	public String getToken() {
		return token;
	}

	@Override
	public String getAuthName() {
		return TOKEN_AUTH;
	}

	@Override
	public String challenge() {
		return TOKEN_AUTH + REALM + EQ + realm + EOT;
	}

	@Override
	public String response(String auth) {
		if (auth.startsWith(TOKEN_AUTH))
			return TOKEN_AUTH + REALM + EQ + realm + EOS + TOKEN + EQ + token + EOT;
		return null;
	}

	@Override
	public boolean verify(String auth) {
		if (auth.startsWith(TOKEN_AUTH)) {
			HashMap<String, String> attrs = parse(auth);
			if (realm.equals(attrs.get(REALM))) {
				return token.equals(attrs.get(TOKEN));
			}
		}
		return false;
	}

}

