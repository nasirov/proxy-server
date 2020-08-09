package nasirov.yv.proxy.server.service.impl;

import nasirov.yv.proxy.server.service.ProxyAuthServiceI;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nasirov.yv
 */
public class ProxyAuthService implements ProxyAuthServiceI {

	private static volatile ProxyAuthService INSTANCE;

	private final String proxyAuthHeader;

	private ProxyAuthService() {
		proxyAuthHeader = "Proxy-Authorization: Basic " + System.getenv("PROXY_CREDENTIALS");
	}

	@Override
	public boolean isAuthorized(String rawHttp) {
		return StringUtils.contains(rawHttp, proxyAuthHeader);
	}

	public static ProxyAuthService getInstance() {
		if (INSTANCE == null) {
			synchronized (ProxyAuthService.class) {
				if (INSTANCE == null) {
					INSTANCE = new ProxyAuthService();
				}
			}
		}
		return INSTANCE;
	}
}
