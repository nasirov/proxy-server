package nasirov.yv.proxy.server.parser.impl;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.proxy.server.exception.RawHttpParseException;
import nasirov.yv.proxy.server.parser.RawHttpParserI;
import nasirov.yv.proxy.server.utils.CommonConst;
import nasirov.yv.proxy.server.utils.HttpStatus;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

/**
 * Created by nasirov.yv
 */
@Slf4j
public class RawHttpParser implements RawHttpParserI {

	private static volatile RawHttpParser INSTANCE;

	private static final Pattern METHOD_AND_URL_PATTERN = Pattern.compile("^(?<method>\\w+)\\s(?<url>\\S+)\\s\\S+$");

	private static final Pattern HEADERS_PATTERN = Pattern.compile("^(?<header>\\S+):\\s(?<headerValue>.+)$");

	private RawHttpParser() {
	}

	@Override
	public Request parseFromRawHttp(String rawHttp) {
		List<String> lines = Arrays.stream(rawHttp.split(CommonConst.RAW_HTTP_DELIMITER))
				.collect(Collectors.toList());
		String url = null;
		String method = null;
		Map<String, String> headers = new LinkedHashMap<>();
		boolean bodyMark = false;
		StringBuilder body = new StringBuilder();
		for (String line : lines) {
			if (url == null) {
				url = getMethodOrUrl(line, "url");
			}
			if (method == null) {
				method = getMethodOrUrl(line, "method");
			}
			enrichHeaders(line, headers);
			if (line.isEmpty()) {
				bodyMark = true;
			}
			if (bodyMark) {
				body.append(line);
			}
		}
		validateUrlAndMethod(url, method);
		Builder builder = new Builder().url(url)
				.method(method, buildRequestBody(body, headers.get(HttpHeaders.CONTENT_TYPE)));
		headers.forEach(builder::addHeader);
		return builder.build();
	}

	@Override
	public String parseToRawHttp(Response response) {
		String firstLine = buildResponseFirstLine(response);
		String headers = concatResponseHeaders(response);
		String body = buildResponseBody(response);
		return firstLine + CommonConst.RAW_HTTP_DELIMITER + headers + CommonConst.RAW_HTTP_DELIMITER + CommonConst.RAW_HTTP_DELIMITER + body;
	}

	public static RawHttpParser getInstance() {
		if (INSTANCE == null) {
			synchronized (RawHttpParser.class) {
				if (INSTANCE == null) {
					INSTANCE = new RawHttpParser();
				}
			}
		}
		return INSTANCE;
	}

	private String getMethodOrUrl(String rawHttp, String group) {
		String result = null;
		Matcher matcher = METHOD_AND_URL_PATTERN.matcher(rawHttp);
		if (matcher.find()) {
			result = matcher.group(group);
		}
		return result;
	}

	private void enrichHeaders(String rawHttp, Map<String, String> headers) {
		Matcher matcher = HEADERS_PATTERN.matcher(rawHttp);
		if (matcher.find()) {
			String header = matcher.group("header");
			if (!StringUtils.equals(HttpHeaders.PROXY_AUTHORIZATION, header)) {
				headers.put(header, matcher.group("headerValue"));
			}
		}
	}

	private RequestBody buildRequestBody(StringBuilder body, String contentType) {
		String bodyValue = body.toString();
		return StringUtils.isBlank(bodyValue) ? null : RequestBody.create(MediaType.parse(contentType), bodyValue);
	}

	private void validateUrlAndMethod(String url, String method) {
		if (url == null || method == null) {
			throw new RawHttpParseException("url or method is null");
		}
	}

	private String concatResponseHeaders(Response response) {
		return response.headers()
				.toMultimap()
				.entrySet()
				.stream()
				.filter(x -> !x.getKey()
						.contains("OkHttp"))
				.filter(x -> !x.getKey()
						.contains(HttpHeaders.TRANSFER_ENCODING))
				.filter(x -> !x.getKey()
						.contains(HttpHeaders.CONTENT_ENCODING))
				.map(x -> x.getKey() + ": " + String.join(";", x.getValue()))
				.collect(Collectors.joining(CommonConst.RAW_HTTP_DELIMITER));
	}

	private String buildResponseFirstLine(Response response) {
		int code = response.code();
		return response.protocol()
				.toString()
				.toUpperCase() + StringUtils.SPACE + code + StringUtils.SPACE + HttpStatus.valueOf(code)
				.getReasonPhrase();
	}

	private String buildResponseBody(Response response) {
		String result = StringUtils.EMPTY;
		try {
			result = decodeResponseBody(response.body()
					.byteStream(), response.header(HttpHeaders.CONTENT_ENCODING));
		} catch (IOException e) {
			log.error("Exception has occurred during building raw response body", e);
		}
		return result;
	}

	private String decodeResponseBody(InputStream inputStream, String contentEncoding) {
		String result = StringUtils.EMPTY;
		if (contentEncoding != null) {
			try {
				CompressorInputStream compressorInputStream = CompressorStreamFactory.getSingleton()
						.createCompressorInputStream(contentEncoding, inputStream);
				result = inputStreamToString(compressorInputStream);
			} catch (Exception e) {
				log.error("Exception has occurred during decompressing raw response body with content-encoding [{}]", contentEncoding, e);
			}
		} else {
			result = inputStreamToString(inputStream);
		}
		return result;
	}

	private String inputStreamToString(InputStream inputStream) {
		String result = StringUtils.EMPTY;
		try {
			result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("Exception has occurred during converting input stream to string", e);
		}
		return result;
	}
}
