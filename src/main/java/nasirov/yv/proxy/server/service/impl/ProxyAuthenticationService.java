package nasirov.yv.proxy.server.service.impl;

import nasirov.yv.proxy.server.service.ProxyAuthenticationServiceI;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nasirov.yv
 */
public class ProxyAuthenticationService implements ProxyAuthenticationServiceI {

	private static volatile ProxyAuthenticationService INSTANCE;

	private final String proxyAuthHeader;

	private ProxyAuthenticationService() {
		proxyAuthHeader = "Proxy-Authorization: Basic " + System.getenv("PROXY_CREDENTIALS");
	}

	@Override
	public boolean isAuthenticated(String rawHttpRequest) {
		return StringUtils.contains(rawHttpRequest, proxyAuthHeader);
	}

	public static ProxyAuthenticationService getInstance() {
		if (INSTANCE == null) {
			synchronized (ProxyAuthenticationService.class) {
				if (INSTANCE == null) {
					INSTANCE = new ProxyAuthenticationService();
				}
			}
		}
		return INSTANCE;
	}
}
