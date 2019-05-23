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

import com.academy.cart.feign.MockProfileFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ProfileServiceAdapterTest {


	@InjectMocks
	ProfileServiceAdapter profileServiceAdapter;
	
	@InjectMocks
	MockProfileFeignClient mockProfileFeignClient;
	
	@InjectMocks 
	ObjectMapper jsonMapper;
	
	private Map<String, String> headersMap = new HashMap<>();
	
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");
		
		ReflectionTestUtils.setField(profileServiceAdapter, "profileFeignClient", mockProfileFeignClient);
	}
	
	@Test
	public void getUserAddress() throws IOException {
		mockProfileFeignClient.isException = false;
		String userAddress = profileServiceAdapter.getAddressForUser("12345", headersMap);
		assertNotNull(userAddress);
		ArrayNode userAddressArray = (ArrayNode) jsonMapper.readValue(userAddress, JsonNode.class);
		assertTrue(userAddressArray.size() > 0);
	}
	
	@Test
	public void getUserAddress_forException() {
		mockProfileFeignClient.isException = true;
		String userAddress = profileServiceAdapter.getAddressForUser("12345", headersMap);
		assertEquals(null, userAddress);
	}
}
