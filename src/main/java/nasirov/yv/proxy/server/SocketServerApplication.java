package nasirov.yv.proxy.server;

import java.util.concurrent.ForkJoinTask;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.task.SocketServerAction;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class SocketServerApplication {

	public static void main(String[] args) {
		log.info("Trying to start Proxy Socket Server...");
		ForkJoinTask.invokeAll(new SocketServerAction(getEntProp("PORT"), getEntProp("BACKLOG")));
		log.info("Proxy Socket Server was stopped.");
	}

	private static int getEntProp(String envName) {
		return Integer.parseInt(System.getenv(envName));
	}
}
