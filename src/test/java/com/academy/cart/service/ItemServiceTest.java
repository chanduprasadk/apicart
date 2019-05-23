package com.academy.cart.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.academy.cart.data.TestData;
import com.academy.cart.domain.Cart;
import com.academy.cart.vo.ItemRequest;
import com.academy.cart.vo.UpdateCartRequest;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorResolver;
import com.academy.integration.handler.HandlerResponseImpl;
import com.academy.integration.service.IntegrationServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


public class ItemServiceTest {
	
	@InjectMocks
	ItemService itemService;
	
	@Mock
	IntegrationServiceImpl cartIntegrationServiceImpl;
	
	@Spy
	ObjectMapper jsonMapper;
	
	@Mock
	private HandlerResponseImpl handlerResponseImpl;
	
	@Mock
	private ErrorResolver errorResolver;
	
	private final Logger logger = LoggerFactory.getLogger(ItemServiceTest.class);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(itemService, "enableJSONLogging", true);
		ReflectionTestUtils.setField(itemService, "errorResolver", errorResolver);
		
		Mockito.when(errorResolver.getErrorMessage(Mockito.anyString(), Mockito.anyString())).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.name());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteItem() {
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		boolean flag = itemService.deleteItem("123", "123");
		assertTrue("Item Not Deleted", flag);
	}
	
	/*@SuppressWarnings("unchecked")
	@Test(expected = JsonProcessingException.class)
	public void testDeleteItemJsonParserFailure() throws JsonProcessingException {
		Mockito.when(jsonMapper.writeValueAsString(Mockito.anyObject())).thenThrow(new IOException("Invalid JSON"));
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		boolean flag = itemService.deleteItem("123", "123");
		assertTrue("Item Not Deleted", flag);
	}
*/
	@Test
	public void testUpdateItem() {
		ItemRequest itemRequest = new ItemRequest();
		itemRequest.setQuantity("2");
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(StringUtils.EMPTY, new HashMap<>(), StringUtils.EMPTY,
						new HashMap<>())).thenReturn(null);
		boolean flag = itemService.updateItem("123", "123", itemRequest);
		assertTrue("Item Not updated", flag);
	}
	
	@Test
	public void testUpdateOrderItem() throws IOException {
		
		UpdateCartRequest updateCartRequest = TestData.getUpdateCartRequest();
		itemService.updateOrderItem(updateCartRequest, "10151");
	}
	
	@Test
	public void testAddItems_Regular() throws IOException {
		InputStream inputStream = new ClassPathResource("test-data/add-items/request-regular-items.json").getInputStream();
		String cartRequest = IOUtils.toString(inputStream);
		
		String wcsRespone = IOUtils.toString(new ClassPathResource("test-data/add-items/wcs-response.json").getInputStream());
		
		HashMap<String, String> responseMap = new HashMap<>();
		responseMap.put("addItems", wcsRespone);
		
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP("addItems", null, null,
						null)).thenReturn(responseMap);
		
		Mockito.when(
				handlerResponseImpl.handleHTTPResponse(new HashMap<>())).thenReturn(wcsRespone);
		
		String addItemsResponse = itemService.addItems("00000", jsonMapper.readValue(cartRequest, Cart.class));
		JsonNode jsonResponse = jsonMapper.readValue(addItemsResponse, JsonNode.class);
		validateAdditemsResponse(jsonResponse);
	}
	
	@Test
	public void testAddItems_Bopus() throws IOException {
		InputStream inputStream = new ClassPathResource("test-data/add-items/request-bopus-items.json").getInputStream();
		String cartRequest = IOUtils.toString(inputStream);
		
		String wcsRespone = IOUtils.toString(new ClassPathResource("test-data/add-items/wcs-response.json").getInputStream());
		
		HashMap<String, String> responseMap = new HashMap<>();
		responseMap.put("addItems", wcsRespone);
		
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP("addItems", null, null,
						null)).thenReturn(responseMap);
		
		Mockito.when(
				handlerResponseImpl.handleHTTPResponse(new HashMap<>())).thenReturn(wcsRespone);
		
		String addItemsResponse = itemService.addItems("00000", jsonMapper.readValue(cartRequest, Cart.class));
		JsonNode jsonResponse = jsonMapper.readValue(addItemsResponse, JsonNode.class);
		validateAdditemsResponse(jsonResponse);
	}
	
	@Test
	public void testAddItems_Bundle() throws IOException {
		InputStream inputStream = new ClassPathResource("test-data/add-items/request-bundle-items.json").getInputStream();
		String cartRequest = IOUtils.toString(inputStream);
		
		String wcsRespone = IOUtils.toString(new ClassPathResource("test-data/add-items/wcs-response.json").getInputStream());
		
		HashMap<String, String> responseMap = new HashMap<>();
		responseMap.put("addItems", wcsRespone);
		
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP("addItems", null, null,
						null)).thenReturn(responseMap);
		
		Mockito.when(
				handlerResponseImpl.handleHTTPResponse(new HashMap<>())).thenReturn(wcsRespone);
		
		String addItemsResponse = itemService.addItems("00000", jsonMapper.readValue(cartRequest, Cart.class));
		JsonNode jsonResponse = jsonMapper.readValue(addItemsResponse, JsonNode.class);
		validateAdditemsResponse(jsonResponse);
	}
	
	@Test
	public void testAddItems_Gift() throws IOException {
		InputStream inputStream = new ClassPathResource("test-data/add-items/request-gift-items.json").getInputStream();
		String cartRequest = IOUtils.toString(inputStream);
		
		String wcsRespone = IOUtils.toString(new ClassPathResource("test-data/add-items/wcs-response.json").getInputStream());
		
		HashMap<String, String> responseMap = new HashMap<>();
		responseMap.put("addItems", wcsRespone);
		
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP("addItems", null, null,
						null)).thenReturn(responseMap);
		
		Mockito.when(
				handlerResponseImpl.handleHTTPResponse(new HashMap<>())).thenReturn(wcsRespone);
		
		String addItemsResponse = itemService.addItems("00000", jsonMapper.readValue(cartRequest, Cart.class));
		JsonNode jsonResponse = jsonMapper.readValue(addItemsResponse, JsonNode.class);
		validateAdditemsResponse(jsonResponse);
	}
	
	@Test(expected = BusinessException.class)
	public void testAddItems_emptyCart() {
		itemService.addItems(null, null);
	}
	

	@Test(expected = BusinessException.class)
	public void testAddItems_NoWcsResponse() throws IOException {
		InputStream inputStream = new ClassPathResource("test-data/add-items/request-gift-items.json").getInputStream();
		String cartRequest = IOUtils.toString(inputStream);
		
		String wcsRespone = IOUtils.toString(new ClassPathResource("test-data/add-items/wcs-response.json").getInputStream());
		
		HashMap<String, String> responseMap = new HashMap<>();
		responseMap.put("addItems", wcsRespone);
		
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP("addItems", null, null,
						null)).thenReturn(responseMap);
		
		Mockito.when(
				handlerResponseImpl.handleHTTPResponse(new HashMap<>())).thenReturn(null);
		
		itemService.addItems("00000", jsonMapper.readValue(cartRequest, Cart.class));
	}
	

	private void validateAdditemsResponse(JsonNode jsonResponse) {
		logger.debug("add items response {} ", jsonResponse);
		String orderId = jsonResponse.findPath("orderId").asText();
		assertNotNull(orderId);
		assertNotEquals(StringUtils.EMPTY, orderId);
		ArrayNode orderItemsArray = (ArrayNode) jsonResponse.findPath("orderItem");
		assertTrue(orderItemsArray.size() > 0);
		orderItemsArray.forEach(itemId -> {
			assertNotNull(itemId);
			assertNotEquals(StringUtils.EMPTY, itemId);
		});
		
	}

}
