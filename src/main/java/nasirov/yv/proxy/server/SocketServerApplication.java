package nasirov.yv.proxy.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.task.SocketServerThread;
import nasirov.yv.proxy.server.utils.ThreadPoolUtils;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketServerApplication {

	@SneakyThrows
	public static void main(String[] args) {
		log.info("Trying to start Proxy Socket Server...");
		ThreadPoolUtils.EXECUTOR_SERVICE.submit(new SocketServerThread(getEntProp("PORT"), getEntProp("BACKLOG")))
				.get();
		log.info("Proxy Socket Server was stopped.");
	}

	private static int getEntProp(String envName) {
		return Integer.parseInt(System.getenv(envName));
	}
}
