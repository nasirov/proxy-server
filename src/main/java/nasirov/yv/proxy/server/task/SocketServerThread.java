package nasirov.yv.proxy.server.task;

import java.net.ServerSocket;
import java.net.Socket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.parser.impl.RawHttpParser;
import nasirov.yv.proxy.server.service.impl.HttpClientService;
import nasirov.yv.proxy.server.service.impl.ProxyAuthenticationService;
import nasirov.yv.proxy.server.utils.ThreadPoolUtils;

/**
 * Created by nasirov.yv
 */
@Slf4j
@RequiredArgsConstructor
public class SocketServerThread implements Runnable {

	private final int port;

	private final int backlog;

	@Override
	public void run() {
		try (ServerSocket server = new ServerSocket(port, backlog)) {
			log.info("Proxy Socket Server is running on port [{}]", server.getLocalPort());
			while (true) {
				log.info("Waiting for a socket...");
				Socket socket = server.accept();
				log.info("A socket was accepted. Trying to execute via thread pool...");
				ThreadPoolUtils.EXECUTOR_SERVICE.submit(new SocketThread(socket,
						HttpClientService.getInstance(),
						RawHttpParser.getInstance(),
						ProxyAuthenticationService.getInstance()));
			}
		} catch (Exception e) {
			log.error("Exception has occurred in SocketServerThread", e);
		}
	}
}
