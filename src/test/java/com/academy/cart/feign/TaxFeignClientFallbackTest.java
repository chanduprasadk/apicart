package com.academy.cart.feign;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.academy.cart.feign.TaxAndShippingFeignClient.TaxFeignClientFallback;

public class TaxFeignClientFallbackTest {

	@InjectMocks
	TaxFeignClientFallback taxFeignClientFallback; 
	
	private Map<String, String> headerMap = new HashMap<>();

	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");
		
		headerMap.put(HttpHeaders.COOKIE, MDC.get(HttpHeaders.COOKIE));
		headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
	}
	
	@Test
	public void validateFallBack() {
		String taxResponse = taxFeignClientFallback.getTaxAndShipping("550071032", "77449", "72201", headerMap);
		assertEquals(null, taxResponse);
	}
	

}
