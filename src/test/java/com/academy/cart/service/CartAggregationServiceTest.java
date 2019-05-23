package com.academy.cart.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import com.academy.cart.service.adapter.InventoryServiceAdapter;
import com.academy.cart.service.adapter.ProductServiceAdapter;
import com.academy.cart.service.adapter.ProfileServiceAdapter;
import com.academy.cart.service.adapter.PromotionalServiceAdapter;
import com.academy.cart.service.adapter.TaxAndShippingServiceAdapter;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.WCSIntegrationException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.integration.transformer.ResponseTransformerImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CartAggregationServiceTest {

	@Mock
	CartServiceImpl cartServiceImpl;

	@InjectMocks
	CartAggregationServiceImpl aggregationServiceImpl;

	@Mock
	ProductServiceAdapter productServiceAdapter;

	@Mock
	TaxAndShippingServiceAdapter taxAndShippingServiceAdapter;

	@Mock
	PromotionalServiceAdapter promotionalServiceAdapter;
	
	@Mock
	ProfileServiceAdapter profileServiceAdapter;
	
	@Mock
	InventoryServiceAdapter inventoryServiceAdapter;

	@InjectMocks
	ObjectMapper jsonMapper;

	@InjectMocks
	ResponseTransformerImpl responseTransformer;

	private String cartResponse;

	private String productResponse;

	private String taxResponse;

	private String promoResponse;

	private String shippingresponse;
	
	private String inventoryRequest;
	
	private String inventoryResponse;
	
	private Map<String, String> headersMap = new HashMap<>();
	
	private HttpHeaders headers = new HttpHeaders();

	@Before
	public void setup() throws FileNotFoundException, IOException, ParseException {

		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("Set-Cookie", "Set-Cookie");

		aggregationServiceImpl.responseTransformer = responseTransformer;

		setupDependencies();
		
		headersMap.put(HttpHeaders.COOKIE, MDC.get(HttpHeaders.COOKIE));
		headersMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/inventory-request.json").getInputStream();
		inventoryRequest = IOUtils.toString(inputStream);
	}
	
	private void setupDependencies() {
		Map<String, String> specMap = new HashMap<>();
		specMap.put("aggregatedCartView", "DisplayCartSpec.json");
		specMap.put("availableShippingMethods", "AvailableShippingMethodsSpec.json");
		ReflectionTestUtils.setField(responseTransformer, "specMap", specMap);
		ReflectionTestUtils.setField(aggregationServiceImpl, "jsonMapper", jsonMapper);
		ReflectionTestUtils.setField(aggregationServiceImpl, "enableJSONLogging", true);
		ReflectionTestUtils.setField(aggregationServiceImpl, "emptyCart", "\"{\"orders\" : []}\"");
		ReflectionTestUtils.setField(aggregationServiceImpl, "threadPool", Executors.newFixedThreadPool(10));
		
	}

	@Test
	public void getAggregatedCartDetails() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/inventory-response.json").getInputStream();
		inventoryResponse = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/tax-response.json").getInputStream();
		taxResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);
		
		
		Mockito.when(cartServiceImpl.getCartDetails(Mockito.anyString())).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(inventoryServiceAdapter.getInventory(inventoryRequest,"033",true,headersMap)).thenReturn(inventoryResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("550071032", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		String orderId = jsonNode.findPath("orderId").asText();
		double ordertotal = jsonNode.findPath("orderTotal").asDouble();
		double estimatedTax = jsonNode.findPath("totalEstimatedTax").asDouble();
		double totalAdjustment = jsonNode.findPath("totalAdjustment").asDouble();
		assertEquals("550071032", orderId);
		assertEquals(4.45, estimatedTax, 0);
		assertEquals(-6, totalAdjustment, 0);
		assertEquals(58.44, ordertotal, 0);
	}

	@Test
	public void viewCartWithoutTax() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);

		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		String orderId = jsonNode.findPath("orderId").asText();
		double ordertotal = jsonNode.findPath("orderTotal").asDouble();
		double estimatedTax = jsonNode.findPath("totalEstimatedTax").asDouble();
		double totalAdjustment = jsonNode.findPath("totalAdjustment").asDouble();
		assertEquals("550071032", orderId);
		assertEquals(0, estimatedTax, 0);
		assertEquals(-6, totalAdjustment, 0);
		assertEquals(53.99, ordertotal, 0);

	}

	@Test
	public void viewCartWithOutAdjustment() throws IOException {
		Map<String, String> queryParams = new HashMap<>();

		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response-without-adjustment.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);

		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		String orderId = jsonNode.findPath("orderId").asText();
		double ordertotal = jsonNode.findPath("orderTotal").asDouble();
		double estimatedTax = jsonNode.findPath("totalEstimatedTax").asDouble();
		double totalAdjustment = jsonNode.findPath("totalAdjustment").asDouble();
		assertEquals("550071032", orderId);
		assertEquals(0, estimatedTax, 0);
		assertEquals(0, totalAdjustment, 0);
		assertEquals(53.99, ordertotal, 0);
	}
	
	@Test
	public void checkForDefaultInventory() throws IOException {
		
		Map<String, String> queryParams = new HashMap<>();

		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response-without-adjustment.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/product-response-no-inventory.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("550071032", headersMap)).thenReturn(promoResponse);
		
		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		assertNotNull(viewCart);
	}
	
	@Test(expected = BusinessException.class)
	public void validateGetCartException() {
		cartResponse = "test1";
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		aggregationServiceImpl.fetchCart("10151", null, headers);
	}
	
	@Test(expected = Exception.class)
	public void validateGetCartTransformationException() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails(Mockito.anyString(), headersMap)).thenReturn(promoResponse);
		ReflectionTestUtils.setField(responseTransformer, "specMap", null);
		aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		setupDependencies();
	}
	
	@Test(expected = ASOException.class)
	public void validateGetTaxException() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn("test1");
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		aggregationServiceImpl.fetchCart("10151", queryParams, headers);
	}
	
	@Test(expected = BusinessException.class)
	@Ignore
	public void validateAvailableShippingmethodsException() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("test1");
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		aggregationServiceImpl.fetchCart("10151", queryParams, headers);
	}
	
	@Test(expected = ASOException.class)
	@Ignore
	public void validateProductsException() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn("test1");
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		aggregationServiceImpl.fetchCart("10151", queryParams, headers);
	}
	
	@Test
	public void checkPrecisonForNonNumeric() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/invalid-cart-response.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		String fetchCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		assertNotNull(fetchCart);
	}
	
	@Test
	@Ignore
	public void validateEmptyPromotions() throws IOException {
		Map<String, String> queryParams = new HashMap<>();

		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response-without-adjustment.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);

		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		String orderId = jsonNode.findPath("orderId").asText();
		double ordertotal = jsonNode.findPath("orderTotal").asDouble();
		double estimatedTax = jsonNode.findPath("totalEstimatedTax").asDouble();
		double totalAdjustment = jsonNode.findPath("totalAdjustment").asDouble();
		ArrayNode promotions = (ArrayNode) jsonNode.findPath("promotions");
		assertEquals("550071032", orderId);
		assertEquals(0, estimatedTax, 0);
		assertEquals(0, totalAdjustment, 0);
		assertEquals(53.99, ordertotal, 0);
		assertEquals(53.99, ordertotal, 0);
		assertEquals(0, promotions.size());
	}
	
	@Test
	public void validatePromotionsWhenNotApplied() throws IOException {
		Map<String, String> queryParams = new HashMap<>();

		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response-without-adjustment.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response-when-no-promotion-applied.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);
		
		Mockito.when(cartServiceImpl.getCartDetails("cartDetails")).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("550071032", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		String orderId = jsonNode.findPath("orderId").asText();
		double ordertotal = jsonNode.findPath("orderTotal").asDouble();
		double estimatedTax = jsonNode.findPath("totalEstimatedTax").asDouble();
		double totalAdjustment = jsonNode.findPath("totalAdjustment").asDouble();
		ArrayNode promotions = (ArrayNode) jsonNode.findPath("promotions");
		assertEquals("550071032", orderId);
		assertEquals(0, estimatedTax, 0);
		assertEquals(0, totalAdjustment, 0);
		assertEquals(53.99, ordertotal, 0);
		assertEquals(53.99, ordertotal, 0);
		assertEquals(0, promotions.size());
	}
	
	@Test
	public void validateSofItems() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/cart-response-sof-items.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/tax-response.json").getInputStream();
		taxResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);
		Mockito.when(cartServiceImpl.getCartDetails(Mockito.anyString())).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("550071032", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);

		boolean sofFlag = jsonNode.findPath("hasSOFItems").asBoolean();
		assertEquals(true, sofFlag);
	    ArrayNode storeArray = (ArrayNode) jsonNode.findPath("orderItems").findPath("storeAddress");
	    assertEquals(3, storeArray.size());	
	}
	
	@Test
	@Ignore
	public void validateBundleItems() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/bundle-inventory-request.json").getInputStream();
		inventoryRequest = IOUtils.toString(inputStream);
		
		inputStream = new ClassPathResource("test-data/view-cart/inventory-response.json").getInputStream();
		inventoryResponse = IOUtils.toString(inputStream);
		
		
		inputStream = new ClassPathResource("test-data/view-cart/cart-response-bundle-items.json").getInputStream();
		cartResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/bundle-product-response.json").getInputStream();
		productResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/tax-response.json").getInputStream();
		taxResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/promo-response.json").getInputStream();
		promoResponse = IOUtils.toString(inputStream);

		inputStream = new ClassPathResource("test-data/view-cart/shipping-method-response.json").getInputStream();
		shippingresponse = IOUtils.toString(inputStream);
		Mockito.when(cartServiceImpl.getCartDetails(Mockito.anyString())).thenReturn(cartResponse);
		Mockito.when(cartServiceImpl.getAvailableShippingmethods(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(shippingresponse);
		Mockito.when(productServiceAdapter.getProductDetails(Mockito.anyString())).thenReturn(productResponse);
		Mockito.when(inventoryServiceAdapter.getInventory(inventoryRequest,"10151",true,headersMap)).thenReturn(inventoryResponse);
		Mockito.when(taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap)).thenReturn(taxResponse);
		Mockito.when(promotionalServiceAdapter.getPromoDetails("550071032", headersMap)).thenReturn(promoResponse);

		String viewCart = aggregationServiceImpl.fetchCart("10151", queryParams, headers);
		JsonNode jsonNode = jsonMapper.readValue(viewCart, JsonNode.class);
		
	    ArrayNode orderItemArray = (ArrayNode) jsonNode.findPath("orderItems");
	    assertEquals(3, orderItemArray.size());	
	    orderItemArray.forEach(bundleItem -> {
	    	boolean bundleFlag = bundleItem.findPath("isBundleItem").asBoolean();
	    	assertEquals(true, bundleFlag);
	    });
	    ArrayNode bundleProductInfo = (ArrayNode) jsonNode.findPath("bundleProductInfo");
	    assertTrue((bundleProductInfo.size() > 0));
	    
	    bundleProductInfo.forEach(bundleItem -> {
	    	ArrayNode orderItemIds = (ArrayNode) bundleItem.findPath("bundleOrderItems");
	    	assertEquals(true, orderItemIds.size() == 3);
	    	assertEquals("5104001", bundleItem.findPath("skuId").asText());
	    	ArrayNode adjustmentArray = (ArrayNode) bundleItem.findPath("totalAdjustment");
	    	adjustmentArray.forEach(node -> {
	    		if(node.findPath("displayLevel").asText().equals("OrderItem")) {
	    			assertEquals(-26, node.findPath("amount").asDouble(), 0);
	    		}
	    		else if(node.findPath("displayLevel").asText().equals("Order")) {
	    			assertEquals(-17, node.findPath("amount").asDouble(), 0);
	    		}
	    	});
	    });
	}
	
	@Test
	public void testForEmptyCartWhenWCSRetruns_404() {
		WCSIntegrationException integrationException = new WCSIntegrationException(ErrorCode.NOT_FOUND.getErrorCode(), "");
		Mockito.when(cartServiceImpl.getCartDetails(Mockito.anyString())).thenThrow(integrationException);
		String response = aggregationServiceImpl.fetchCart("10151", null , headers);
		assertEquals("\"{\"orders\" : []}\"", response);
	}
	
	@Test(expected = WCSIntegrationException.class)
	public void testForEmptyCart_IntegrationException() {
		WCSIntegrationException integrationException = new WCSIntegrationException(ErrorCode.SERVICE_NOT_AVAILABLE.getErrorCode(), "Service not available");
		Mockito.when(cartServiceImpl.getCartDetails(Mockito.anyString())).thenThrow(integrationException);
		aggregationServiceImpl.fetchCart("10151", null, headers);
	}
	
	@Test
	public void testCart_forChangedZipCode_case1() {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("changedZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		aggregationServiceImpl.fetchDeliveryZipCode(queryParams, null, headers);
		assertTrue(queryParams.containsKey("deliveryZipCode"));
		assertEquals("77449", queryParams.get("deliveryZipCode"));
	}
	
	@Test
	public void testCart_forUserProfileZipCode_case2() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "77449");
		queryParams.put("storeZipCode", "72201");
		
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		headersMap.put(HttpHeaders.COOKIE,cookies);
		
		InputStream inputStream = new ClassPathResource("test-data/view-cart/user-address.json").getInputStream();
		String userAddress = IOUtils.toString(inputStream);
		
		Mockito.when(profileServiceAdapter.getAddressForUser("620094", headersMap)).thenReturn(userAddress);
		
		aggregationServiceImpl.fetchDeliveryZipCode(queryParams, headersMap, headers);
		assertTrue(queryParams.containsKey("deliveryZipCode"));
		assertEquals("75019", queryParams.get("deliveryZipCode"));
	}
	
	@Test
	public void testCart_forDeliveryZipCodeInQueryParam_case3() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("deliveryZipCode", "12345");
		queryParams.put("storeZipCode", "72201");
		
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=G";
		headersMap.put(HttpHeaders.COOKIE,cookies);

		aggregationServiceImpl.fetchDeliveryZipCode(queryParams, headersMap, headers);
		assertTrue(queryParams.containsKey("deliveryZipCode"));
		assertEquals("12345", queryParams.get("deliveryZipCode"));
	}
	
	@Test
	public void testCart_forDeliveryZipCodeInHeader_case4() throws IOException {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("storeZipCode", "72201");
		
		headers.add("zipCode", "900011");
		
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=G";
		headersMap.put(HttpHeaders.COOKIE,cookies);

		aggregationServiceImpl.fetchDeliveryZipCode(queryParams, headersMap, headers);
		assertTrue(queryParams.containsKey("deliveryZipCode"));
		assertEquals("900011", queryParams.get("deliveryZipCode"));
	}
	
}
