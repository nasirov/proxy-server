package nasirov.yv.proxy.server.service;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by nasirov.yv
 */
public interface HttpClientServiceI {

	Response executeRequest(Request request);
}
