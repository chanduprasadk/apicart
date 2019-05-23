package com.academy.cart.service.adapter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.cart.feign.InventoryFeignClient;
import com.academy.integration.adapter.AbstractIntegrationAdapter;

@Service
public class InventoryServiceAdapter extends AbstractIntegrationAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(InventoryServiceAdapter.class);

	/** The product feign client. */
	@Autowired
	private InventoryFeignClient inventoryFeignClient;
	
	public String getInventory(String requestBody, String storeId ,boolean isbopisStore,  Map<String,String> headerMap) {
		try {
			logger.debug("calling inventory service with store id {} ", storeId);
			return inventoryFeignClient.getInventory(requestBody, storeId,isbopisStore, getHeadersMap());
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
