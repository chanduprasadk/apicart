package com.academy.cart.feign;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

import com.academy.cart.feign.InventoryFeignClient.InventoryFeignClientFallback;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;

@FeignClient(name = "inventory", fallback = InventoryFeignClientFallback.class)
public interface InventoryFeignClient {

	@RequestLine("POST /api/inventory/store/{storeId}?isBopisStore={isBopisStore}")
	String getInventory(String requestPayload, @Param(value = "storeId") String storeId,@Param(value = "isBopisStore") boolean isBopisStore, @HeaderMap Map<String, String> headerMap);

	@Component
	class InventoryFeignClientFallback implements InventoryFeignClient {

		private static final Logger logger = LoggerFactory.getLogger(InventoryFeignClientFallback.class);
		
		@Override
		public String getInventory(String requestPayload,String storeId,boolean isBopisStore, Map<String, String> headerMap) {
			logger.error("InventoryFeignClient fallback error for store id {} ", storeId);
			return null;
		}

	}
}
