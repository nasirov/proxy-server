package nasirov.yv.proxy.server.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import nasirov.yv.proxy.server.parser.RawHttpParserI;
import nasirov.yv.proxy.server.service.HttpClientServiceI;
import nasirov.yv.proxy.server.service.ProxyAuthenticationServiceI;
import nasirov.yv.proxy.server.utils.HttpStatus;
import nasirov.yv.proxy.server.utils.ThreadPoolUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by nasirov.yv
 */
@RunWith(MockitoJUnitRunner.class)
public class SocketThreadTest {

	@Mock
	private Socket socket;

	@Mock
	private HttpClientServiceI httpClient;

	@Mock
	private RawHttpParserI rawHttpParser;

	@Mock
	private ProxyAuthenticationServiceI proxyAuthenticationService;

	@InjectMocks
	private SocketThread socketThread;

	@Test
	@SneakyThrows
	public void shouldReturnSocketResponseForAuthenticatedRequest() {
		//given
		String rawHttpSocketRequest = "raw http request";
		ByteArrayOutputStream socketOutput = new ByteArrayOutputStream();
		mockSocket(rawHttpSocketRequest, socketOutput);
		mockProxyAuthenticationService(true, rawHttpSocketRequest);
		Request request = new Request.Builder().url("http://foo.bar")
				.method("GET", null)
				.build();
		String rawHttpResponse = "raw http response";
		Response response = new Response.Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.request(request)
				.build();
		mockRawHttpParser(rawHttpSocketRequest, request, rawHttpResponse, response);
		mockHttpClient(request, response);
		//when
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(socketThread)
				.get();
		//then
		assertEquals(rawHttpResponse, getActualSocketResponse(socketOutput));
	}

	@Test
	@SneakyThrows
	public void shouldReturnSocketResponseForUnauthenticatedRequest() {
		//given
		String rawHttpSocketRequest = "raw http request";
		ByteArrayOutputStream socketOutput = new ByteArrayOutputStream();
		mockSocket(rawHttpSocketRequest, socketOutput);
		mockProxyAuthenticationService(false, rawHttpSocketRequest);
		//when
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(socketThread)
				.get();
		//then
		assertEquals("HTTP/1.1 407 Proxy Authentication Required\r\nProxy-Authenticate: Basic realm=\"Access to internal site\"\r\n\r\nProxy "
				+ "Authentication Required\r\n", getActualSocketResponse(socketOutput));
	}

	@Test
	@SneakyThrows
	public void shouldFailOnException() {
		//given
		mockException();
		//when
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(socketThread)
				.get();
		//then
		verify(proxyAuthenticationService, never()).isAuthenticated(any(String.class));
	}

	@SneakyThrows
	private void mockSocket(String rawHttpSocketRequest, ByteArrayOutputStream socketOutput) {
		doReturn(new ByteArrayInputStream(rawHttpSocketRequest.getBytes(StandardCharsets.UTF_8))).when(socket)
				.getInputStream();
		doReturn(socketOutput).when(socket)
				.getOutputStream();
	}

	private void mockProxyAuthenticationService(boolean authenticated, String rawHttpSocketRequest) {
		doReturn(authenticated).when(proxyAuthenticationService)
				.isAuthenticated(rawHttpSocketRequest);
	}

	private void mockRawHttpParser(String rawHttpSocketRequest, Request request, String rawHttpResponse, Response response) {
		doReturn(request).when(rawHttpParser)
				.parseFromRawHttp(rawHttpSocketRequest);
		doReturn(rawHttpResponse).when(rawHttpParser)
				.parseToRawHttp(response);
	}

	private void mockHttpClient(Request request, Response response) {
		doReturn(response).when(httpClient)
				.executeRequest(request);
	}

	@SneakyThrows
	private String getActualSocketResponse(ByteArrayOutputStream socketOutput) {
		return socketOutput.toString(StandardCharsets.UTF_8.name());
	}

	@SneakyThrows
	private void mockException() {
		doThrow(new IOException("some exception cause")).when(socket)
				.getInputStream();
	}
}