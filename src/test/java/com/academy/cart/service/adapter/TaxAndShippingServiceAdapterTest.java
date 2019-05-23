package com.academy.cart.service.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import com.academy.cart.feign.MockTaxFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaxAndShippingServiceAdapterTest {


	@InjectMocks
	TaxAndShippingServiceAdapter taxAndShippingServiceAdapter;
	
	@InjectMocks
	MockTaxFeignClient mockTaxFeignClient;
	
	@InjectMocks 
	ObjectMapper jsonMapper;
	
	private Map<String, String> headersMap = new HashMap<>();
	
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");
		
		ReflectionTestUtils.setField(taxAndShippingServiceAdapter, "taxAndShippingFeignClient", mockTaxFeignClient);
	}
	
	@Test
	public void getTaxResponse() throws IOException {
		mockTaxFeignClient.isException = false;
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		queryParams.put("storeId", "10151");
		queryParams.put("orderId", "550071032");
		String taxResponse = taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams,headersMap);
		assertNotNull(taxResponse);
		JsonNode jsonNode = jsonMapper.readValue(taxResponse, JsonNode.class);
		double totalShippingCharge = jsonNode.findPath("totalShippingCharge").asDouble();
		double orderGrandTotal = jsonNode.findPath("orderGrandTotal").asDouble();
		double totalTax = jsonNode.findPath("totalTax").asDouble();
		assertEquals(0, totalShippingCharge, 0);
		assertEquals(4.45, totalTax, 0);
		assertEquals(58.44, orderGrandTotal, 0);
	}
	
	@Test
	public void getTaxResponse_forException() {
		mockTaxFeignClient.isException = true;
		String taxResponse = taxAndShippingServiceAdapter.getTaxAndShippingCharges(null, headersMap);
		assertEquals(null, taxResponse);
	}
}
