package nasirov.yv.proxy.server.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.experimental.UtilityClass;

/**
 * Created by nasirov.yv
 */
@UtilityClass
public class ThreadPoolUtils {

	public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
}
