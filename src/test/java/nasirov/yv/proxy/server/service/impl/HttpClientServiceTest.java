package nasirov.yv.proxy.server.service.impl;

import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;
import lombok.SneakyThrows;
import nasirov.yv.proxy.server.service.HttpClientServiceI;
import nasirov.yv.proxy.server.utils.HttpStatus;
import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class HttpClientServiceTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
			.dynamicPort()
			.dynamicHttpsPort());

	private HttpClientServiceI httpClientService = HttpClientService.getInstance();

	@Test
	public void shouldReturnOkResponse() {
		//given
		Request request = new Builder().url("http://localhost:" + wireMockRule.port() + "/foo")
				.method("GET", null)
				.addHeader(HttpHeaders.USER_AGENT, "Foo-Bar")
				.build();
		stubOk();
		//when
		Response result = httpClientService.executeRequest(request);
		//then
		assertEquals(HttpStatus.OK.value(), result.code());
		assertEquals("body", getBodyAsString(result));
	}

	@Test
	public void shouldReturnErrorResponse() {
		//given
		Request request = new Builder().url("http://localhost:" + wireMockRule.port() + "/foo")
				.method("GET", null)
				.addHeader(HttpHeaders.USER_AGENT, "Foo-Bar")
				.build();
		stubFail();
		//when
		Response result = httpClientService.executeRequest(request);
		//then
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.code());
		assertEquals("Exception has occurred during call " + request.urlString(), getBodyAsString(result));
	}

	private void stubOk() {
		WireMock.stubFor(WireMock.get("/foo")
				.withHeader(HttpHeaders.USER_AGENT, WireMock.equalTo("Foo-Bar"))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK.value())
						.withBody("body")));
	}

	private void stubFail() {
		WireMock.stubFor(WireMock.get("/foo")
				.withHeader(HttpHeaders.USER_AGENT, WireMock.equalTo("Foo-Bar"))
				.willReturn(WireMock.aResponse()
						.withFault(Fault.CONNECTION_RESET_BY_PEER)));
	}

	@SneakyThrows
	private String getBodyAsString(Response result) {
		return result.body()
				.string();
	}
}