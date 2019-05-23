package com.academy.cart.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.academy.cart.util.CartUtils;
import com.academy.cart.util.UrlBuilderUtil;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.InitiateRequest;
import com.academy.cart.vo.Sku;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.academy.cart.vo.UpdateShippingRequest;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorResolver;
import com.academy.integration.handler.HandlerResponseImpl;
import com.academy.integration.service.IntegrationServiceImpl;
import com.academy.integration.transformer.ResponseTransformerImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Sapient Unit test for CartServiceImpl
 *
 */
public class CartServiceImplTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CartServiceImplTest.class);

	@InjectMocks
	CartServiceImpl cartServiceImpl;

	@Mock
	IntegrationServiceImpl cartIntegrationServiceImpl;

	@Mock
	ResponseTransformerImpl cartResponseTransformerImpl;

	@Mock
	HandlerResponseImpl cartResponseHandlerImpl;

	@Mock
	CartUtils cartUtils;

	@Mock
	UrlBuilderUtil urlBuilderUtil;

	@Mock
	ErrorResolver errorResolver;

	@Mock
	Map<String, Integer> cartFieldsMap;

	@Mock
	HttpServletResponse httpServletResponse;

	@Mock
	UpdateShippingRequest updateShippingRequest;

	@Mock
	InitiateRequest initiateRequest;

	@Mock
	ObjectMapper jsonMapper;

	@Mock
	UpdateQuantityRequest updateQuantityRequest;

	JSONObject mockResponse;
	JSONObject addToCartResponse;
	JSONObject addToCartWithEmptyValues;
	JSONObject addToCartWithNullValues;

	private CartRequest cartRequest;
	// private InitiateRequest initiateRequest;

	private String request;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		Mockito.mock(MDC.class);
		MDC.put("x-userid", "12345");
		MDC.put("Set-Cookie", "Set-Cookie");
		mockResponse = new JSONObject();
		JSONParser parser = new JSONParser();

		cartRequest = new CartRequest();
		initiateRequest = new InitiateRequest();

		initiateRequest.setOrderId("ORD123");

		cartRequest.setGiftAmount(210.0);
		List<Sku> skus = new ArrayList<>();
		Sku sku = new Sku();
		sku.setId("1111");
		cartRequest.setInventoryCheck(true);
		cartRequest
				.setItemComment("com.ibm.commerce.exception.ECApplicationException:");
		skus.add(sku);
		cartRequest.setSkus(skus);

		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("minicart.json")
					.getFile());
			Object obj = parser.parse(new FileReader(file));
			mockResponse = (JSONObject) obj;

			File addToCartFile = new File(classLoader.getResource("addToCart.json").getFile());
			Object addToCartObject = parser.parse(new FileReader(addToCartFile));
			addToCartResponse = (JSONObject) addToCartObject;

			File addToCartLessDataFile = new File(classLoader.getResource("addToCartWithEmptyValues.json").getFile());
			Object addToCartLessDataObject = parser.parse(new FileReader(addToCartLessDataFile));
			addToCartWithEmptyValues = (JSONObject) addToCartLessDataObject;

			File addToCartWithNullValuesFile = new File(
					classLoader.getResource("addToCartWithNullValues.json").getFile());
			Object addToCartWithNullObject = parser.parse(new FileReader(addToCartWithNullValuesFile));
			addToCartWithNullValues = (JSONObject) addToCartWithNullObject;

			ObjectMapper mapper = new ObjectMapper();
			request = mapper.writeValueAsString(cartRequest);

		} catch (Exception e) {

			LOGGER.error("Exception", e);
		}
	}

	@After
	public void cleanUp() {

		MDC.remove("x-userid");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getCartDetailsTest() {


		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(mockResponse.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(mockResponse.toString());
		String response = cartServiceImpl.getCartDetails();


		try {
			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonResponse = (org.json.simple.JSONObject) parser
					.parse(response);

			assertEquals("7542008002", (String) jsonResponse.get("orderId"));
		} catch (Exception e) {
			LOGGER.error("Exception", e);

		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void getCartDetailsExceptionTest() {

		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
						Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleHTTPResponse((HashMap<String, String>) Mockito
								.anyMap())).thenReturn(mockResponse.toString());
		Mockito.when(
				cartResponseTransformerImpl.transform(Mockito.anyString(),
						Mockito.anyString())).thenReturn(null);
		cartServiceImpl.getCartDetails();

	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void getCartDetailsExceptionWhenTransformJsonIsNullAsStringTest() {


		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(mockResponse.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");
		String response = cartServiceImpl.getCartDetails();

		try {
			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonResponse = (org.json.simple.JSONObject) parser.parse(response);

			assertEquals("1042115", (String) jsonResponse.get("productId"));
		} catch (Exception e) {
			LOGGER.error("Exception", e);
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMiniCartDetailsTest() {


		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(mockResponse.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(mockResponse.toString());

		ResponseEntity<?> response = cartServiceImpl.getMiniCartDetails("1111");
		assertNotNull(response);
		assertTrue(response.getBody().toString().contains("7542008002"));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMiniCartDetailsWhenTransformIsNullTest() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenThrow(ASOException.class);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString())).thenReturn(null);


		ResponseEntity<String> response = cartServiceImpl.getMiniCartDetails("1111");
		assertNotNull(response);
		assertTrue(response.getStatusCode().compareTo(HttpStatus.OK) == 0);


		

	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Test(expected=BusinessException.class) public void
	 * getMiniCartDetailsWhenTransformIsNullTest() {
	 * 
	 * Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
	 * Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap())).thenReturn(null);
	 * Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String,
	 * String>) Mockito.anyMap())) .thenReturn(mockResponse.toString());
	 * Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(),
	 * Mockito.anyString())) .thenReturn(null);
	 * 
	 * 
	 * ResponseEntity<?> response = cartServiceImpl.getMiniCartDetails("1111");
	 * 
	 * System.out.println(response); try { JSONParser parser = new JSONParser();
	 * org.json.simple.JSONObject jsonResponse = (org.json.simple.JSONObject)
	 * parser.parse(response.getBody().toString());
	 * 
	 * assertEquals("USD", (String) jsonResponse.get("currency")); } catch
	 * (Exception e) { LOGGER.error("Exception", e);
	 * 
	 * } }
	 */

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Test(expected=BusinessException.class) public void
	 * getMiniCartDetailsWhenTransformIsNullAsStringTest() {
	 * 
	 * Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
	 * Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap())).thenReturn(null);
	 * Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String,
	 * String>) Mockito.anyMap())) .thenReturn(mockResponse.toString());
	 * Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(),
	 * Mockito.anyString())) .thenReturn("null");
	 * 
	 * 
	 * ResponseEntity<?> response = cartServiceImpl.getMiniCartDetails("1111");
	 * 
	 * try { JSONParser parser = new JSONParser(); org.json.simple.JSONObject
	 * jsonResponse = (org.json.simple.JSONObject)
	 * parser.parse(response.getBody().toString());
	 * 
	 * assertEquals("USD", (String) jsonResponse.get("currency")); } catch
	 * (Exception e) { LOGGER.error("Exception", e);
	 * 
	 * } }
	 */


	@SuppressWarnings("unchecked")
	@Test
	public void getMiniCartDetailsWhenTransformIsNullAsStringTest() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(mockResponse.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");

		ResponseEntity<String> response = cartServiceImpl.getMiniCartDetails("1111");
		assertNotNull(response);
		assertTrue(response.getStatusCode().compareTo(HttpStatus.OK) == 0);
	}


	@SuppressWarnings("unchecked")
	@Test
	public void addCartTest() {



		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(addToCartResponse.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(addToCartResponse.toString());
		Mockito.when(Mockito.mock(CartServiceImpl.class).getCartFieldsMap()).thenReturn(cartFieldsMap);
		Cookie cookie = new Cookie("Set-Cookie", "Set-Cookie");
		cookie.setMaxAge(1800);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		// assertTrue(responseStr.contains("1111"));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartTestWhenCartMapIsNull() {
		MDC.put("Set-Cookie", "Expires;Path;HttpOnly;Session;HostOnly;Secure;cookie=\"new=cookie");
		request = request.replace("com.ibm.commerce.exception.ECApplicationException:", "");
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(request);
		Mockito.when(Mockito.mock(CartServiceImpl.class).getCartFieldsMap()).thenReturn(null);
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("1111"));

	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenCartIsNull() {

		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
						Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleHTTPResponse((HashMap<String, String>) Mockito
								.anyMap())).thenReturn(request);
		Mockito.when(
				cartResponseTransformerImpl.transform(Mockito.anyString(),
						Mockito.anyString())).thenReturn(request);
		cartServiceImpl.addCart(null, null);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenSkuIsNull() {

		cartRequest.setSkus(null);
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
						Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleHTTPResponse((HashMap<String, String>) Mockito
								.anyMap())).thenReturn(request);
		Mockito.when(
				cartResponseTransformerImpl.transform(Mockito.anyString(),
						Mockito.anyString())).thenReturn(request);
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenSkuIsEmpty() {

		cartRequest.setSkus(Collections.EMPTY_LIST);
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(request);
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenTransformedJsonIsNull() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenTransformedJsonIsNullAsString() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenaggregatedResponseJSONIsNullAsReference() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn("null");
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenaggregatedResponseJSONIsNullAsString() {

		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
						Mockito.anyMap(), Mockito.eq(null), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseTransformerImpl.transform(Mockito.anyString(),
						Mockito.anyString())).thenReturn("null");
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
		;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test
	public void getAvailableShippingmethodsTest() {
		Mockito.when(
				cartIntegrationServiceImpl.invoke(Mockito.anyString(),
						Mockito.anyMap(), Mockito.anyString(), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleResponse((HashMap<String, Exchange>) Mockito
								.anyMap())).thenReturn(null);
		cartServiceImpl.getAvailableShippingmethods("profile", "orderId");

	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test(expected = BusinessException.class)
	public void getAvailableShippingmethodsTestWithNullValues() {
		Mockito.when(
				cartIntegrationServiceImpl.invoke(Mockito.anyString(),
						Mockito.anyMap(), Mockito.anyString(), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleResponse((HashMap<String, Exchange>) Mockito
								.anyMap())).thenReturn(null);
		cartServiceImpl.getAvailableShippingmethods(null, null);

	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Test(expected = BusinessException.class)
	public void getAvailableShippingmethodsTestWithNullString() {
		Mockito.when(
				cartIntegrationServiceImpl.invoke(Mockito.anyString(),
						Mockito.anyMap(), Mockito.anyString(), Mockito.anyMap()))
				.thenReturn(null);
		Mockito.when(
				cartResponseHandlerImpl
						.handleResponse((HashMap<String, Exchange>) Mockito
								.anyMap())).thenReturn(null);
		cartServiceImpl.getAvailableShippingmethods("", "");

	}

	@Test(expected = BusinessException.class)
	public void initiateCheckoutTestWithNull() {
		cartServiceImpl.initiateCheckout(null);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void initiateCheckoutTest() {
		Mockito.when(
				cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(),
						Mockito.anyMap(), Mockito.anyString(), Mockito.anyMap()))
				.thenReturn(null);

		cartServiceImpl.initiateCheckout(initiateRequest);

		try {
			jsonMapper.writeValueAsString(initiateRequest);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	@Test
	public void updateItemQuantityTest() {
		cartServiceImpl.updateItemQuantity(null);
	}

	@Test(expected = BusinessException.class)
	public void bopisUpdateShippingModeTest() {
		cartServiceImpl.bopisUpdateShippingMode(updateShippingRequest);

		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = BusinessException.class)
	public void addCartWhenaggregatedResponseParseException() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn("&");
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("null");
		cartServiceImpl.addCart(cartRequest, httpServletResponse);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartWhenCookieIsEmpty() throws JsonProcessingException {

		MDC.put("Set-Cookie", "");
		String request = "{\"addToCart\" : null}";
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(request);
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("addToCart"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartWhenProductNameAndQuantityAreNull() throws JsonProcessingException {

		MDC.put("Set-Cookie", "");
		String request = "{\"addToCart\":{\"items\" : [ {\"productName\" : null,\"quantity\" : null} ]}}";
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(request);
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("addToCart"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartWhenProductNameAndQuantityAreEmpty() throws JsonProcessingException {

		MDC.put("Set-Cookie", "");
		String request = "{\"addToCart\":{\"items\":[{\"productName\" : \"\",\"quantity\":\"\"} ],\"cartURL\":\"&amp;cartURL\",\"checkoutURL\":\"&amp;checkoutURL\",\"urlPrefix\":\"&amp;urlPrefix\",\"urlSuffix\":\"&amp;urlSuffix\"}}";
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(request);
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(request);
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("addToCart"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartWithSomeEmptyResponse() {

		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(addToCartWithEmptyValues.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(addToCartWithEmptyValues.toString());
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("addToCart"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addCartWithNullValues() {
		Mockito.when(cartIntegrationServiceImpl.invokeHTTP(Mockito.anyString(), Mockito.anyMap(), Mockito.eq(null),
				Mockito.anyMap())).thenReturn(null);
		Mockito.when(cartResponseHandlerImpl.handleHTTPResponse((HashMap<String, String>) Mockito.anyMap()))
				.thenReturn(addToCartWithNullValues.toString());
		Mockito.when(cartResponseTransformerImpl.transform(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(addToCartWithNullValues.toString());
		String responseStr = cartServiceImpl.addCart(cartRequest, httpServletResponse);
		assertNotNull(responseStr);
		assertTrue(responseStr.contains("addToCart"));

	}

}
