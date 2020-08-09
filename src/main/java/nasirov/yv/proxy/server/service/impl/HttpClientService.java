package nasirov.yv.proxy.server.service.impl;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.service.HttpClientServiceI;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class HttpClientService implements HttpClientServiceI {

	private static volatile HttpClientService INSTANCE;

	private final OkHttpClient okHttpClient;

	private HttpClientService() {
		okHttpClient = new OkHttpClient();
	}

	@Override
	public Response executeRequest(Request request) {
		Response result = null;
		Call call = okHttpClient.newCall(request);
		try {
			result = call.execute();
		} catch (IOException e) {
			log.error("Exception has occurred during a http request", e);
		}
		return result;
	}

	public static HttpClientService getInstance() {
		if (INSTANCE == null) {
			synchronized (HttpClientService.class) {
				if (INSTANCE == null) {
					INSTANCE = new HttpClientService();
				}
			}
		}
		return INSTANCE;
	}
}
