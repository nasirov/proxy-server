package nasirov.yv.proxy.server.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import lombok.SneakyThrows;
import nasirov.yv.proxy.server.service.ProxyAuthenticationServiceI;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nasirov.yv
 */
public class ProxyAuthenticationServiceTest {

	private ProxyAuthenticationServiceI proxyAuthenticationService;

	@Before
	@SneakyThrows
	public void setUp() {
		SystemLambda.withEnvironmentVariable("PROXY_CREDENTIALS", "dGVzdDp0ZXN0")
				.execute(() -> proxyAuthenticationService = ProxyAuthenticationService.getInstance());
	}

	@Test
	public void shouldReturnTrue() {
		//given
		String rawHttp = "GET http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic dGVzdDp0ZXN0\r\n";
		//when
		boolean result = proxyAuthenticationService.isAuthenticated(rawHttp);
		//then
		assertTrue(result);
	}

	@Test
	public void shouldReturnFalseInvalidCredentials() {
		//given
		String rawHttp = "GET http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\nProxy-Authorization: Basic foo\r\n";
		//when
		boolean result = proxyAuthenticationService.isAuthenticated(rawHttp);
		//then
		assertFalse(result);
	}

	@Test
	public void shouldReturnFalseNoHeader() {
		//given
		String rawHttp = "GET http://foo.bar HTTP/1.1\r\nUser-Agent: Foo-bar\r\n";
		//when
		boolean result = proxyAuthenticationService.isAuthenticated(rawHttp);
		//then
		assertFalse(result);
	}
}