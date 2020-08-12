package nasirov.yv.proxy.server.service;

/**
 * Created by nasirov.yv
 */
public interface ProxyAuthenticationServiceI {

	boolean isAuthenticated(String rawHttpRequest);
}
