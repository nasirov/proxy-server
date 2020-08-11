package nasirov.yv.proxy.server.task;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.parser.RawHttpParserI;
import nasirov.yv.proxy.server.service.HttpClientServiceI;
import nasirov.yv.proxy.server.service.ProxyAuthServiceI;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketThread implements Runnable {

	private static final String PROXY_UNAUTHORIZED_RESPONSE = "HTTP/1.1 407 Proxy Authentication Required\r\n\r\nProxy Authentication Required\r\n";

	private final Socket socket;

	private final HttpClientServiceI httpClient;

	private final RawHttpParserI rawHttpParser;

	private final ProxyAuthServiceI proxyAuthService;

	public SocketThread(Socket socket, HttpClientServiceI httpClient, RawHttpParserI rawHttpParser, ProxyAuthServiceI proxyAuthService) {
		this.socket = socket;
		this.httpClient = httpClient;
		this.rawHttpParser = rawHttpParser;
		this.proxyAuthService = proxyAuthService;
	}

	@Override
	public void run() {
		log.info("Starting SocketThread...");
		try (BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()),
						StandardCharsets.UTF_8))) {
			String rawHttpRequest = getRawHttpRequest(socketInput);
			log.info("Received a msg from client:\n{}", rawHttpRequest);
			String rawHttpResponse = getRawHttpResponse(rawHttpRequest);
			log.info("Response:\n{}", rawHttpResponse);
			socketOutput.write(rawHttpResponse);
			socketOutput.flush();
		} catch (Exception e) {
			log.error("Exception has occurred", e);
		}
		log.info("Stop SocketThread.");
	}

	private String getRawHttpRequest(BufferedReader input) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		while (input.ready()) {
			stringBuilder.append((char) input.read());
		}
		return stringBuilder.toString();
	}

	private String getRawHttpResponse(String rawHttpRequest) {
		String rawHttpResponse;
		if (proxyAuthService.isAuthorized(rawHttpRequest)) {
			Request externalRequest = rawHttpParser.parseFromRawHttp(rawHttpRequest);
			Response externalResponse = httpClient.executeRequest(externalRequest);
			rawHttpResponse = rawHttpParser.parseToRawHttp(externalResponse);
		} else {
			rawHttpResponse = PROXY_UNAUTHORIZED_RESPONSE;
		}
		return rawHttpResponse;
	}
}