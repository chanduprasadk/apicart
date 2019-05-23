package com.academy.cart.service.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.springframework.test.util.ReflectionTestUtils;

import com.academy.cart.feign.MockPromoCodeFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PromotionalServiceAdapterTest {

	@InjectMocks
	PromotionalServiceAdapter promotionalServiceAdapter;

	@InjectMocks
	MockPromoCodeFeignClient promoCodeFeignClient;

	@InjectMocks
	ObjectMapper jsonMapper;
	
	private Map<String, String> headersMap = new HashMap<>();

	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");

		ReflectionTestUtils.setField(promotionalServiceAdapter, "orderFeignClient", promoCodeFeignClient);
	}

	@Test
	public void getPromoresponse() throws IOException {
		promoCodeFeignClient.isException = false;
		String orderId = "550071032";
		String promoResponse = promotionalServiceAdapter.getPromoDetails(orderId,headersMap);
		assertNotNull(promoResponse);
		JsonNode jsonNode = jsonMapper.readValue(promoResponse, JsonNode.class);
		String orderIdfromResponse = jsonNode.findPath("orderId").asText();
		assertEquals(orderId, orderIdfromResponse);
	}
	
	public void getProductresponse_forException() {
		promoCodeFeignClient.isException = true;
		String promoDetails = promotionalServiceAdapter.getPromoDetails(null, null);
		assertTrue(null == promoDetails);
	}

}
