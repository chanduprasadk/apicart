package com.academy.cart.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.academy.cart.constant.CommonConstant;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.OrderItem;
import com.academy.cart.vo.Sku;
import com.academy.cart.vo.WCSCartRequest;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.common.exception.util.ErrorResolver;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains utility methods.
 * @author Sapient
 *
 */
@Component
public class CartUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CartUtils.class);
	
	/**
	 * This method transform DTO into String.
	 * @param cartRequest
	 * @param cartFieldsMap
	 * @return String
	 */
	public String transformDto(CartRequest cartRequest, Map<String, Integer> cartFieldsMap) {
		WCSCartRequest wCSCartRequest = null;
		if (!Objects.isNull(cartRequest)) {
			wCSCartRequest = new WCSCartRequest();
			wCSCartRequest.setxInventoryValidation(cartRequest.isInventoryCheck());
			wCSCartRequest.setOrderItem(setWcsOrderItems(cartRequest));
			setWcsStaticFields(wCSCartRequest,cartFieldsMap);
		}
		return transformDtoToString(wCSCartRequest);
	}

	/**
	 * This method sets OrderItem of WCSCartRequest from Sku.
	 * 
	 * @param wCSCartRequest
	 * @return List
	 */
	private List<OrderItem> setWcsOrderItems(CartRequest wCSCartRequest) {
		List<OrderItem> orderItems = new ArrayList<>();
		if (!Objects.isNull(wCSCartRequest.getSkus()) && !wCSCartRequest.getSkus().isEmpty()) {
			for (Sku sku : wCSCartRequest.getSkus()) {
				if (!Objects.isNull(sku)) {
					OrderItem orderItem = new OrderItem();
					orderItem.setProductId(sku.getId());
					orderItem.setQuantity(""+sku.getQuantity());
					orderItems.add(orderItem);
				}
			}
		}
		return orderItems;
	}

	/**
	 * This method takes map from properties file and set those value into
	 * respective properties of WCSCartRequest
	 * 
	 * @param wCSCartRequest
	 */
	private void setWcsStaticFields(WCSCartRequest wCSCartRequest, Map<String, Integer> cartFieldsMap) {
		if (!Objects.isNull(cartFieldsMap) && !cartFieldsMap.entrySet().isEmpty()) {
			for (Map.Entry<String, Integer> cartFieldsEntySet : cartFieldsMap.entrySet()) {
				String key = cartFieldsEntySet.getKey();
				if (key.equalsIgnoreCase("langId")) {
					wCSCartRequest.setLangId(cartFieldsEntySet.getValue());
				} else if (key.equalsIgnoreCase("catalogId")) {
					wCSCartRequest.setCatalogId(cartFieldsEntySet.getValue());
				}
			}
		}
	}

	/**
	 * This method transforms WCSCartRequest into String.
	 * 
	 * @param wCSCartRequest
	 * @return String
	 */
	private String transformDtoToString(WCSCartRequest wCSCartRequest) {
		String requestBodyAsString = null;
		try {
			ObjectMapper mapperObj = new ObjectMapper();
			requestBodyAsString = mapperObj.writeValueAsString(wCSCartRequest);
			LOGGER.debug("Before replacing requestBodyAsString is: {}", requestBodyAsString);
			requestBodyAsString = requestBodyAsString.replaceAll("x", "x_");
			LOGGER.debug("After replacing requestBodyAsString is: {}", requestBodyAsString);

		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.WRONG_DATA,
					new ErrorResolver().getErrorMessage("ERR_DTO_TRANFORMATION_FAILURE", ""));
		}
		return requestBodyAsString;
	}
	
	/**
	 * Converts the object into corresponding JSON representation.
	 * 
	 * @param obj
	 * @return
	 */
	public static String getJSONString(Object obj) {
		try {
			ObjectMapper mapperObj = new ObjectMapper();
			mapperObj.setSerializationInclusion(Include.NON_NULL);
			return mapperObj.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	public static String extractProfileIdFromCookies(String rawCookies) {
		if(null != rawCookies && !rawCookies.isEmpty()) {
			Map<String, String> cookieMap = new HashMap<>();
			Optional.ofNullable(rawCookies.split(CommonConstant.SEMICOLON.getName())).
			ifPresent(cookies -> {
				for(String cookie : cookies) {
					if(cookie.contains(CommonConstant.REGISTERED_USER_KEY.getName())) {
						cookieMap.put(CommonConstant.REGISTERED_USER_KEY.getName(), cookie);
					}
					else if(cookie.contains(CommonConstant.REGISTERED_USER_PROFILE_ID.getName())) {
						cookieMap.put(CommonConstant.REGISTERED_USER_PROFILE_ID.getName(), cookie);
					}
					if(cookieMap.size() == 2) {
						break;
					}
				}
			});
			return authenticateUserAndGetProfileId(cookieMap);
		}
		return null;
	}
	
	private static String authenticateUserAndGetProfileId(final Map<String, String> cookieMap) {
		LOGGER.debug("filtered cookie map size {} and contains {} ",cookieMap.size(), cookieMap);
		String registerUserKey = cookieMap.get(CommonConstant.REGISTERED_USER_KEY.getName());
		if(registerUserKey != null && !registerUserKey.isEmpty()) {
			String[] userCookie = registerUserKey.split(CommonConstant.EQUALS.getName());
			if(userCookie != null && userCookie.length > 1 && CommonConstant.REGISTERED_USER_VALUE.getName().equals(userCookie[1])) {
				String authenticationKey = cookieMap.get(CommonConstant.REGISTERED_USER_PROFILE_ID.getName());
				return getProfileIdFromCookieTokens(authenticationKey);
			}
		}
		return null;
	}
	
	private static String getProfileIdFromCookieTokens(final String authenticationKey) {
		if(authenticationKey != null && !authenticationKey.isEmpty()) {
			String[] authenticationTokens = authenticationKey.split(CommonConstant.EQUALS.getName());
			if(authenticationTokens != null && authenticationTokens.length > 1) {
				String[] userProfileTokens = authenticationTokens[0].split(CommonConstant.UNDERSCORE.getName());
				if(userProfileTokens != null && userProfileTokens.length >= 3) {
					return userProfileTokens[2];
				}
			}
		}
		return null;
	}
	
	public static String getCookieValue(String rawCookies, String cookieName) {
		if (null != rawCookies && !rawCookies.isEmpty()) {
			String[] cookies = rawCookies.split(CommonConstant.SEMICOLON.getName());
			return checkAndExtractCookie(cookies, cookieName);
		}

		return null;
	}
	
	private static String checkAndExtractCookie(String[] cookies, String cookieName) {
		if (null != cookies && cookies.length > 0) {
			for (String cookie : cookies) {
				String[] cookiePair = cookie.split(CommonConstant.EQUALS.getName());
				if (null != cookiePair 
						&& StringUtils.isNotBlank(cookiePair[0])
						&& cookiePair[0].trim().equals(cookieName) 
						&& cookiePair.length > 1) {
					return cookiePair[1];
				}
			}
		}
		return null;
	}
	

}
