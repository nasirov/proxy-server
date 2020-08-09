package nasirov.yv.proxy.server.service.impl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.parser.impl.RawHttpParser;
import nasirov.yv.proxy.server.service.SocketServerServiceI;
import nasirov.yv.proxy.server.task.SocketThread;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketServerService implements SocketServerServiceI {

	private static volatile SocketServerService INSTANCE;

	private SocketServerService() {
	}

	@Override
	public void startSocketServer() {
		try (ServerSocket server = new ServerSocket(getEntProp("PORT"), getEntProp("BACKLOG"))) {
			log.info("Proxy Socket Server is running on port [{}]", server.getLocalPort());
			while (true) {
				log.info("Waiting for a socket...");
				Socket socket = server.accept();
				log.info("A socket was accepted. Trying to execute via common pool...");
				ForkJoinPool.commonPool()
						.execute(new SocketThread(socket, HttpClientService.getInstance(), RawHttpParser.getInstance(), ProxyAuthService.getInstance()));
			}
		} catch (Exception e) {
			log.error("Exception has occurred", e);
		}
	}

	public static SocketServerService getInstance() {
		if (INSTANCE == null) {
			synchronized (SocketServerService.class) {
				if (INSTANCE == null) {
					INSTANCE = new SocketServerService();
				}
			}
		}
		return INSTANCE;
	}

	private int getEntProp(String envName) {
		return Integer.parseInt(System.getenv(envName));
	}
}
