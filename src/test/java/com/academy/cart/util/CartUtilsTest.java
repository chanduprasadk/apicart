package com.academy.cart.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
 * @author Sapient Unit test for CartUtils
 *
 */
public class CartUtilsTest {

	static final Logger LOGGER = LoggerFactory.getLogger(CartUtilsTest.class);

	@InjectMocks
	CartUtils cartUtils;

	@Mock
	Map<String, Integer> cartFieldsMap;

	@Mock
	CartRequest cartRequest;

	@Mock
	Object obj;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		cartRequest = new CartRequest();
		cartRequest.setGiftAmount(210.0);
		List<Sku> skus = new ArrayList<>();
		Sku sku = new Sku();
		sku.setId("1111");
		cartRequest.setInventoryCheck(true);
		skus.add(sku);
		cartRequest.setSkus(skus);
		cartFieldsMap = new HashMap<>();
		cartFieldsMap.put("langId", 1);
		cartFieldsMap.put("catalogId", 1);
	}

	@Test
	public void transformDtoTest() {
		String response = cartUtils.transformDto(cartRequest, cartFieldsMap);
		assertNotNull(response);
		JSONParser parser = null;
		try {
			parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(response);
			assertEquals(true,
					(boolean) jsonResponse.get("x_InventoryValidation"));
		} catch (ParseException e) {
			LOGGER.error("Exception", e);
		}

	}

	@Test
	public void transformDtoWhenMapIsNullTest() {
		cartRequest.setSkus(Collections.emptyList());
		String response = cartUtils.transformDto(cartRequest, null);
		assertNotNull(response);
		JSONParser parser = null;
		try {
			parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(response);
			assertEquals(true,
					(boolean) jsonResponse.get("x_InventoryValidation"));
		} catch (ParseException e) {
			LOGGER.error("Exception", e);
		}

	}

	@Test
	public void transformDtoWhenRequestIsNullTest() {
		String response = cartUtils.transformDto(null, cartFieldsMap);
		assertTrue("null".equalsIgnoreCase(response));
	}

	@Test
	public void transformDtoWhenSKUsIsNullTest() {
		cartRequest.setSkus(null);
		cartFieldsMap.put("lang", 1);
		cartFieldsMap.put("catalog", 1);
		String response = cartUtils.transformDto(cartRequest, cartFieldsMap);
		assertNotNull(response);
		JSONParser parser = null;
		try {
			parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(response);
			assertEquals(true,
					(boolean) jsonResponse.get("x_InventoryValidation"));
		} catch (ParseException e) {
			LOGGER.error("Exception", e);
		}
	}
	@Test
	public void transformDtoWhenSkuIsNullTest() {

		cartRequest.getSkus().add(null);
		String response = cartUtils.transformDto(cartRequest, Collections.emptyMap());
		assertNotNull(response);
		JSONParser parser = null;
		try {
			parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(response);
			assertEquals(true, (boolean) jsonResponse.get("x_InventoryValidation"));
		} catch (ParseException e) {
			LOGGER.error("Exception", e);
		}
	}
	
	@Test
	public void validateProfileIdForRegisteredUser() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals("620094", profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case1() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=G";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case2() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case3() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=U";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case4() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE:R";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case5() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094:620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForUnRegisteredUser_case6() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr; JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION?620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		String profileId = CartUtils.extractProfileIdFromCookies(cookies);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForNoCookies() {
		String profileId = CartUtils.extractProfileIdFromCookies(null);
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void validateProfileIdForEmptyCookies() {
		String profileId = CartUtils.extractProfileIdFromCookies("");
		LOGGER.debug("profile id {} ", profileId);
		assertEquals(null, profileId);
	}
	
	@Test
	public void testGetCookiesValue() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr;WC_StLocId=123456;JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		String cookieValue = CartUtils.getCookieValue(cookies, "WC_StLocId");
		LOGGER.debug("cookie vale {} ",cookieValue);
		assertEquals("123456", cookieValue);
	}
	
	@Test
	public void testGetCookiesValue_whenNotExist() {
		String cookies = "correlationId=AA-0kdtMIyK4ngsdnwzj9wwaEI7nSfnjYdr;JSESSIONID=0000mzfVzvHsbhtnM5MeflMjz4p:1chon6lgp; WC_PERSISTENT=J1Qmwsu7WwklparvP6C%2Furf9%2FmNcFIMScWWcu9T0Mvc%3D%3B2018-08-25+13%3A14%3A12.097_1533834386431-20_10151; WC_SESSION_ESTABLISHED=true; WC_AUTHENTICATION_620094=620094%2CmQ1J%2FDuFmHYQW2VaYUN3InE6jJZCUXhJI2Jx6mTjonQ%3D; WC_ACTIVEPOINTER=-1%2C10151; WC_USERACTIVITY_620094=620094%2C10151%2C0%2Cnull%2C1535220852098%2C1535235252098%2Cnull%2Cnull%2Cnull%2Cnull%2C482166349%2Cz5Fpy2nnrM1wWkFqjGvy0Ty%2Ffycn0Rp8mzrurxxrI9vaA4ggio8GhiD2jU0eEd80DivHlaqjqvzDKE8%2F8uEFOYl9QDepeYWcVBhDzBCd8cD50ZVPiNUXFuITYyGEkaE2UuLHzxi99vm2NyCBOjpDuB8eY3vbtN49YyeSo97qe1KkuXP34CPVf5FOP8LqdMprXr7uG5nCURQMv1mHcWfyMA0TQyJQ5epdd7gz5SBOEaLgcEKYd3GWbx7OtXKtveSn9ylpxmwFTGueOYD9s84JBw%3D%3D; USERTYPE=R";
		String cookieValue = CartUtils.getCookieValue(cookies, "WC_StLocId");
		LOGGER.debug("cookie vale {} ",cookieValue);
		assertEquals(null, cookieValue);
	}
	
	@Test
	public void testGetCookiesValue_whenNoCookes() {
		String cookies = null;
		String cookieValue = CartUtils.getCookieValue(cookies, "WC_StLocId");
		LOGGER.debug("cookie vale {} ",cookieValue);
		assertEquals(null, cookieValue);
	}


}
