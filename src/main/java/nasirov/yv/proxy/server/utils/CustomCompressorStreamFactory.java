package nasirov.yv.proxy.server.utils;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Created by nasirov.yv
 */
public class CustomCompressorStreamFactory extends CompressorStreamFactory {

	private static volatile CustomCompressorStreamFactory INSTANCE;

	public static final String GZIP_FULL = "gzip";

	@Override
	public CompressorInputStream createCompressorInputStream(String name, InputStream in, boolean actualDecompressConcatenated)
			throws CompressorException {
		try {
			if (GZIP_FULL.equalsIgnoreCase(name)) {
				return new GzipCompressorInputStream(in, actualDecompressConcatenated);
			}
		} catch (IOException e) {
			throw new CompressorException("Could not create CompressorInputStream.", e);
		}
		return super.createCompressorInputStream(name, in, actualDecompressConcatenated);
	}

	public static CustomCompressorStreamFactory getInstance() {
		if (INSTANCE == null) {
			synchronized (CustomCompressorStreamFactory.class) {
				if (INSTANCE == null) {
					INSTANCE = new CustomCompressorStreamFactory();
				}
			}
		}
		return INSTANCE;
	}
}
