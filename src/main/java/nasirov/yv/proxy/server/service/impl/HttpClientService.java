package nasirov.yv.proxy.server.service.impl;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.service.HttpClientServiceI;
import nasirov.yv.proxy.server.utils.HttpStatus;

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
		Response result;
		Call call = okHttpClient.newCall(request);
		try {
			result = call.execute();
		} catch (Exception e) {
			result = buildErrorResponse(request);
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

	private Response buildErrorResponse(Request request) {
		return new Response.Builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.protocol(Protocol.HTTP_1_1)
				.request(request)
				.body(ResponseBody.create(MediaType.parse("text/plain"), "Exception has occurred during call " + request.urlString()))
				.build();
	}
}
