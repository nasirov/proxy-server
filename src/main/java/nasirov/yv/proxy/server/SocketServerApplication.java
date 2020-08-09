package nasirov.yv.proxy.server;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.service.impl.SocketServerService;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketServerApplication {

	public static void main(String[] args) {
		log.info("Trying to start Proxy Socket Server...");
		SocketServerService.getInstance()
				.startSocketServer();
		log.info("Proxy Socket Server was stopped.");
	}
}
