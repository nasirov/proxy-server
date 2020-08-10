package nasirov.yv.proxy.server.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Response.Builder;
import com.squareup.okhttp.ResponseBody;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import lombok.SneakyThrows;
import nasirov.yv.proxy.server.exception.RawHttpParseException;
import nasirov.yv.proxy.server.parser.RawHttpParserI;
import nasirov.yv.proxy.server.utils.HttpStatus;
import okio.Buffer;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by nasirov.yv
 */
@PrepareForTest({Response.class, ResponseBody.class})
@RunWith(PowerMockRunner.class)
public class RawHttpParserTest {

	private RawHttpParserI rawHttpParser = RawHttpParser.getInstance();

	@Test
	public void shouldParseFromRawHttp() {
		//given
		String rawHttp = "POST http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic base64encodedCredentials\r\nContent-Type: "
				+ "text/plain\r\n\r\nbody";
		//when
		Request result = rawHttpParser.parseFromRawHttp(rawHttp);
		//then
		assertEquals("http://foo.bar/", result.urlString());
		assertEquals("POST", result.method());
		assertEquals(2,
				result.headers()
						.size());
		assertEquals("Foo-bar", result.header(HttpHeaders.USER_AGENT));
		assertEquals("text/plain", result.header(HttpHeaders.CONTENT_TYPE));
		RequestBody body = result.body();
		assertEquals("body", getActualBody(body));
		assertEquals("text/plain; charset=utf-8",
				body.contentType()
						.toString());
	}

	@Test(expected = RawHttpParseException.class)
	public void shouldParseFromRawHttpWithBodyNoContentType() {
		//given
		String rawHttp = "POST http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic base64encodedCredentials\r\n\r\nbody";
		//when
		rawHttpParser.parseFromRawHttp(rawHttp);
	}

	@Test(expected = RawHttpParseException.class)
	public void shouldParseFromRawHttpWithContentTypeNoBody() {
		//given
		String rawHttp = "POST http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic base64encodedCredentials\r\nContent-Type: "
				+ "text/plain\r\n\r\n";
		//when
		rawHttpParser.parseFromRawHttp(rawHttp);
	}

	@Test(expected = RawHttpParseException.class)
	public void shouldFailOnNullMethod() {
		//given
		String rawHttp = "http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic base64encodedCredentials";
		//when
		rawHttpParser.parseFromRawHttp(rawHttp);
	}

	@Test(expected = RawHttpParseException.class)
	public void shouldFailOnNullUrl() {
		//given
		String rawHttp = "GET HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic base64encodedCredentials";
		//when
		rawHttpParser.parseFromRawHttp(rawHttp);
	}

	@Test
	public void shouldParseToRawHttpNoEncoding() {
		//given
		Response response = new Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.addHeader("OkHttp", "foo")
				.addHeader(HttpHeaders.TRANSFER_ENCODING, "chuncked")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body(ResponseBody.create(MediaType.parse("tex/plain"), "body"))
				.request(new Request.Builder().url("http://foo.bar")
						.method("GET", null)
						.build())
				.build();
		//when
		String result = rawHttpParser.parseToRawHttp(response);
		//then
		assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain,text/plain\r\n\r\nbody", result);
	}

	@Test
	public void shouldParseToRawHttpWithEncoding() {
		//given
		Response response = new Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.addHeader("OkHttp", "foo")
				.addHeader(HttpHeaders.TRANSFER_ENCODING, "chuncked")
				.addHeader(HttpHeaders.CONTENT_ENCODING, "gz")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body(ResponseBody.create(MediaType.parse("tex/plain"), getGzipBody()))
				.request(new Request.Builder().url("http://foo.bar")
						.method("GET", null)
						.build())
				.build();
		//when
		String result = rawHttpParser.parseToRawHttp(response);
		//then
		assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain,text/plain\r\n\r\nbody", result);
	}

	@Test
	public void shouldParseToRawHttpWithEmptyBodyDecompressFail() {
		//given
		Response response = new Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.addHeader("OkHttp", "foo")
				.addHeader(HttpHeaders.TRANSFER_ENCODING, "chuncked")
				.addHeader(HttpHeaders.CONTENT_ENCODING, "gz")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body(ResponseBody.create(MediaType.parse("tex/plain"), "not gzip"))
				.request(new Request.Builder().url("http://foo.bar")
						.method("GET", null)
						.build())
				.build();
		//when
		String result = rawHttpParser.parseToRawHttp(response);
		//then
		assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain,text/plain\r\n\r\n", result);
	}

	@Test
	public void shouldParseToRawHttpWithEmptyBodyBuildFail() {
		//given
		Response response = new Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.addHeader("OkHttp", "foo")
				.addHeader(HttpHeaders.TRANSFER_ENCODING, "chuncked")
				.addHeader(HttpHeaders.CONTENT_ENCODING, "gz")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body(ResponseBody.create(MediaType.parse("tex/plain"), "not gzip"))
				.request(new Request.Builder().url("http://foo.bar")
						.method("GET", null)
						.build())
				.build();
		Response spy = PowerMockito.spy(response);
		doThrow(new RuntimeException("some exception during build")).when(spy)
				.body();
		//when
		String result = rawHttpParser.parseToRawHttp(spy);
		//then
		assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain,text/plain\r\n\r\n", result);
	}

	@Test
	@SneakyThrows
	public void shouldParseToRawHttpWithEmptyBodyInputStreamToStringFail() {
		//given
		ResponseBody body = PowerMockito.mock(ResponseBody.class);
		Response response = new Builder().code(HttpStatus.OK.value())
				.protocol(Protocol.HTTP_1_1)
				.addHeader("OkHttp", "foo")
				.addHeader(HttpHeaders.TRANSFER_ENCODING, "chuncked")
				.addHeader(HttpHeaders.CONTENT_ENCODING, "gz")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.addHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
				.body(body)
				.request(new Request.Builder().url("http://foo.bar")
						.method("GET", null)
						.build())
				.build();
		doReturn(null).when(body)
				.byteStream();
		//when
		String result = rawHttpParser.parseToRawHttp(response);
		//then
		assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain,text/plain\r\n\r\n", result);
	}

	@SneakyThrows
	private String getActualBody(RequestBody body) {
		Buffer buffer = new Buffer();
		body.writeTo(buffer);
		return new String(buffer.readByteArray());
	}

	private byte[] getGzipBody() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
			gzipOutputStream.write("body".getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}