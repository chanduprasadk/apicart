package com.academy.cart.feign;

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

import com.academy.cart.feign.InventoryFeignClient.InventoryFeignClientFallback;

public class InventoryFeignClientFallbackTest {

	@InjectMocks
	InventoryFeignClientFallback feignClientFallback; 
	
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
		feignClientFallback.getInventory(null, null,false, headerMap);
	}
	

}
