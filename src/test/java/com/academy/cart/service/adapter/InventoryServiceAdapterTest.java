package com.academy.cart.service.adapter;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import com.academy.cart.feign.MockInventoryFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InventoryServiceAdapterTest {
	
	@InjectMocks
	InventoryServiceAdapter inventoryServiceAdapter;
	
	@InjectMocks
	MockInventoryFeignClient inventoryfeignClient;
	
	@InjectMocks 
	ObjectMapper jsonMapper;
	
	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {
		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");
		
		ReflectionTestUtils.setField(inventoryServiceAdapter, "inventoryFeignClient", inventoryfeignClient);
	}
	
	@Test
	public void getInventoryresponse() throws IOException {
		inventoryfeignClient.isException = false;
		String storeId = "033";
		String inventoryResponse = inventoryServiceAdapter.getInventory(null, storeId,true, null);
		assertNotNull(inventoryResponse);
	}
	
	@Test
	public void getInventoryresponse_forException() {
		inventoryfeignClient.isException = true;
		inventoryServiceAdapter.getInventory(null, null,true, null);
	}

}
