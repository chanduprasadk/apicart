package com.academy.cart.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.Sku;

/**
 * 
 * @author Sapient Unit test for UrlBuilderUtil
 *
 */
public class UrlBuilderUtilTest {

	static final Logger LOGGER = LoggerFactory
			.getLogger(UrlBuilderUtilTest.class);

	@InjectMocks
	UrlBuilderUtil urlBuilderUtil;

	Map<String, Integer> cartFieldsMap;

	@Mock
	CartRequest cartRequest;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		cartRequest = new CartRequest();
		cartRequest.setGiftAmount(210.0);
		cartRequest.setItemComment("Item comment");
		List<Sku> skus = new ArrayList<>();
		Sku sku = new Sku();
		sku.setId("1111");
		sku.setQuantity(2);
		cartRequest.setInventoryCheck(true);
		skus.add(sku);
		cartRequest.setSkus(skus);
		cartFieldsMap = new HashMap<>();
		cartFieldsMap.put("storeId", 10151);
		cartFieldsMap.put("catalogId", 10151);
		cartFieldsMap.put("langId", -1);
	}

	@Test
	public void buildUrlTest() {
		String response = urlBuilderUtil.buildUrl(cartRequest, cartFieldsMap);
		assertNotNull(response);
		assertTrue(response.contains("inventoryValidation"));

	}

	@Test
	public void buildUrlTestCartRequestNull() {
		cartRequest.setItemComment("itemComment");
		String response = urlBuilderUtil.buildUrl(cartRequest, cartFieldsMap);
		assertNotNull(response);
		assertTrue(response.contains("inventoryValidation"));

	}

	@Test
	public void buildUrlParamsTest() {
		urlBuilderUtil.buildUrlParams("profile", "orderId");
	}

	@Test
	public void buildUrlParamsTestProfileNull() {
		urlBuilderUtil.buildUrlParams(null, "orderId");
	}

	@Test
	public void buildUrlParamsTestOrderNull() {
		urlBuilderUtil.buildUrlParams("profile", null);
	}

	@Test
	public void buildURITest() {
		Map<String,Integer> queryParams = new HashMap<String,Integer>();
		queryParams.put("query", 123);
		urlBuilderUtil.buildUrl(cartRequest, queryParams);
	}

	@Test
	public void buildUrlTestWhenCartRequestIsNull() {
		cartRequest = new CartRequest();
		String response = urlBuilderUtil.buildUrl(cartRequest, cartFieldsMap);
		LOGGER.info(response);
		//assertTrue(response.contains("inventoryValidation"));
		
	}
	
	@Test
	public void buildUrlTestWhenSkuIsNull() {
		cartRequest.setSkus(Collections.emptyList());
		String response = urlBuilderUtil.buildUrl(cartRequest, cartFieldsMap);
		LOGGER.info(response);
		// assertTrue(response.contains("inventoryValidation"));

	}
}
