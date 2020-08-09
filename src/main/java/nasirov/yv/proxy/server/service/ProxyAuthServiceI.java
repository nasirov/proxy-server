package nasirov.yv.proxy.server.service;

/**
 * Created by nasirov.yv
 */
public interface ProxyAuthServiceI {

	boolean isAuthorized(String rawHttp);
}
