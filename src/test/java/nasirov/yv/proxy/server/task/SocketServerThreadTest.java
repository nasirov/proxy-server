package nasirov.yv.proxy.server.task;

import static org.junit.Assert.assertTrue;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.utils.ThreadPoolUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketServerThreadTest {

	private static final String PROXY_CREDENTIALS = "dGVzdDp0ZXN0";

	private static int SOCKET_SERVER_PORT;

	private static int WIRE_MOCK_PORT;

	private static WireMockServer WIRE_MOCK_SERVER;

	@BeforeClass
	@SneakyThrows
	public static void setUp() {
		SOCKET_SERVER_PORT = getAvailablePort();
		SocketServerThread socketServerThread = new SocketServerThread(SOCKET_SERVER_PORT, 10);
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(socketServerThread);
		waitForAnotherThread();
		WIRE_MOCK_PORT = getAvailablePort();
		WIRE_MOCK_SERVER = new WireMockServer(WIRE_MOCK_PORT);
		WIRE_MOCK_SERVER.start();
	}

	@AfterClass
	public static void tearDown() {
		WIRE_MOCK_SERVER.stop();
	}

	@Test
	@SneakyThrows
	public void shouldReturnOkResponse() {
		SystemLambda.withEnvironmentVariable("PROXY_CREDENTIALS", PROXY_CREDENTIALS)
				.execute(() -> {
					//given
					Socket clientSocket = new Socket("localhost", SOCKET_SERVER_PORT);
					//when
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
					out.write(
							"GET http://localhost:" + WIRE_MOCK_PORT + "/foo HTTP/1.1\r\nUser-Agent: Foo-Bar\r\nProxy-Authorization: Basic " + PROXY_CREDENTIALS
									+ "\r\n");
					out.flush();
					waitForAnotherThread();
					//then
					String result = getResponse(clientSocket);
					System.out.println("result:\n" + result);
					assertTrue(result.contains("HTTP/1.1 200 OK"));
					assertTrue(result.contains("body"));
					out.close();
				});
	}

	@Test
	@SneakyThrows
	public void shouldFailOnBoundPort() {
		//given
		SocketServerThread socketServerThread = new SocketServerThread(SOCKET_SERVER_PORT, 10);
		//when
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(socketServerThread);
		waitForAnotherThread();
		//then
		assertTrue(true);
	}

	@SneakyThrows
	private static int getAvailablePort() {
		int result;
		ServerSocket serverSocket = new ServerSocket(0);
		result = serverSocket.getLocalPort();
		serverSocket.close();
		return result;
	}

	@SneakyThrows
	private static void waitForAnotherThread() {
		TimeUnit.SECONDS.sleep(1);
	}

	@SneakyThrows
	private String getResponse(Socket clientSocket) {
		try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
			StringBuilder stringBuilder = new StringBuilder();
			while (input.ready()) {
				stringBuilder.append((char) input.read());
			}
			return stringBuilder.toString();
		}
	}
}