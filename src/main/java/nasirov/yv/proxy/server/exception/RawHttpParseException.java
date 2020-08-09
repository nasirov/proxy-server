package nasirov.yv.proxy.server.exception;

/**
 * Created by nasirov.yv
 */
public class RawHttpParseException extends IllegalArgumentException {

	public RawHttpParseException() {
		super();
	}
	public RawHttpParseException(String msg) {
		super(msg);
	}
}
