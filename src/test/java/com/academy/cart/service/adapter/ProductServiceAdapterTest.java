package com.academy.cart.service.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import com.academy.cart.feign.MockProductFeignClient;
import com.academy.common.exception.ASOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductServiceAdapterTest {
	
	@InjectMocks
	ProductServiceAdapter productAdapter;
	
	@InjectMocks
	MockProductFeignClient productfeignClient;
	
	@InjectMocks 
	ObjectMapper jsonMapper;
	
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");
		
		ReflectionTestUtils.setField(productAdapter, "productFeignClient", productfeignClient);
	}
	
	@Test
	public void getProductresponse() throws IOException {
		productfeignClient.isException = false;
		String productid = "39878";
		String productDetails = productAdapter.getProductDetails(productid);
		assertNotNull(productDetails);
		JsonNode jsonNode = jsonMapper.readValue(productDetails, JsonNode.class);
		String productIdFromResponse = jsonNode.findPath("id").asText();
		assertEquals(productIdFromResponse, productid);
	}
	
	@Test(expected = ASOException.class)
	@Ignore
	public void getProductresponse_forException() {
		productfeignClient.isException = true;
		productAdapter.getProductDetails(null);
	}

}
