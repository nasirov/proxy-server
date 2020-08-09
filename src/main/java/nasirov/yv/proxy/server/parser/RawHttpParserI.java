package nasirov.yv.proxy.server.parser;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by nasirov.yv
 */
public interface RawHttpParserI {

	Request parseFromRawHttp(String rawHttp);

	String parseToRawHttp(Response response);
}
