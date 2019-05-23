package com.academy.cart.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.academy.cart.constant.CommonConstant;
import com.academy.cart.util.UrlBuilderUtil;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.InitiateRequest;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.academy.cart.vo.UpdateShippingRequest;
import com.academy.common.aspect.perf.PerfLog;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.common.exception.util.ErrorResolver;
import com.academy.common.service.DomainService;
import com.academy.integration.handler.HandlerResponseImpl;
import com.academy.integration.service.IntegrationServiceImpl;
import com.academy.integration.transformer.ResponseTransformerImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Sapient
 * 
 * This class is to retrieves the response from third party system 
 * and transforms the response json with the only required fields
 */
@Service
public class CartServiceImpl extends DomainService implements CartService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CartServiceImpl.class);
	private static final String ENTRY_METHOD = "Entry method {}";

	private static final String EXIT_METHOD = "Exit method {}";

	private static final String ERR_DETAIL_NOT_FOUND = "ERR_DETAIL_NOT_FOUND";

	private static final String EXPIRES = "Expires";

	private static final String PATH = "Path";

	private static final String HTTP_ONLY = "HttpOnly";

	private static final String SET_COOKIE = "Set-Cookie";

	private static final String SESSION = "Session";

	private static final String HOST_ONLY = "HostOnly";
	
	private static final String SECURE = "Secure";

	private static final String CART_URL = "cartURL";
	
	private static final String QTY_ADDED = "totalQuantityAdded";
	
	private static final String CART_QTY = "totalCartQuantity";
	
	private static final String ITEMS = "items";
	
	private static final String QUANTITY = "quantity";
	
	private static final String URL_PREFIX = "urlPrefix"; 
	
	private static final String URL_SUFFIX = "urlSuffix";
	
	private static final String RESPONSE_ERROR_MESSAGE = "ErrorMessage";
	
	private static final String RESPONSE_EXCEPTION_MESSAGE = "exception";

	private static final String PRODUCT_NAME = "productName";
	
	private static final String VALUE = "value";
	
	private static final String DIFF = "diff";

	private static final Pattern PATTERN = Pattern.compile("amp;", Pattern.LITERAL);
	
	
	@Autowired
	private IntegrationServiceImpl cartIntegrationServiceImpl;

	@Autowired
	private ResponseTransformerImpl cartResponseTransformerImpl;
	
	@Autowired
	private HandlerResponseImpl cartResponseHandlerImpl;
	
	@Autowired
	private ErrorResolver errorResolver;

	@Autowired
	private UrlBuilderUtil urlBuilderUtil;

	@Value("#{${cartFieldsMap}}")
	private Map<String, Integer> cartFieldsMap;

	@Value("#{${cartWCSURLMap}}")
	private Map<String, String> cartWCSURLMap;

	@Value("#{${minicartNotFound}}")
	private String minicartNotFound;
	
	@Value("${enableJSONLogging}")
	private boolean enableJSONLogging;
	
	public Map<String, Integer> getCartFieldsMap() {
		return cartFieldsMap;
	}
	
	@Autowired
	ObjectMapper jsonMapper;
	
	
	/**
	 * 
	 * Retrieve cart details from third party system and transforms
	 * 
	 * @return String
	 */
	@Override
	public String getCartDetails(String transformationId)  {
		 
		HashMap<String, String> responseMap = null ;
		String finalResponseJSON = null;
		String transformedJSON = null;
		
		LOGGER.debug("Entry getCartDetails");
		
		responseMap  = cartIntegrationServiceImpl.invokeHTTP("cart", null,null,null);
		
		if(null != responseMap && null == responseMap.get("cart")) {
			LOGGER.error("finalResponseJSON is null, bencause of 204 empty response body, thowing 404");
			throw new BusinessException(ErrorCode.NOT_FOUND, errorResolver.getErrorMessage("ERR_RESPONSE_NOT_FOUND",""));
		}
		  
		finalResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);
		
		LOGGER.info("Final response JSON is {}" , finalResponseJSON);
		
		transformedJSON = cartResponseTransformerImpl.transform(transformationId, finalResponseJSON);
		
		LOGGER.debug("Trasnformed JSON is {}" , transformedJSON);
		return transformedJSON;
	}
		
	@Override
	public String getCartDetails() {
		HashMap<String, String> responseMap = null;
		String finalResponseJSON = null;
		String transformedJSON = null;
		LOGGER.info("Enter CartServiceImpl.getCartDetails()");
		responseMap = cartIntegrationServiceImpl.invokeHTTP("cart", null, null, null);
		finalResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);	
		if(enableJSONLogging) {
			LOGGER.debug("Final response JSON is {}", finalResponseJSON);
		}
		transformedJSON = cartResponseTransformerImpl.transform("cart", finalResponseJSON);		
		if(enableJSONLogging) {
			LOGGER.debug("Trasnformed JSON is {}", transformedJSON);
		}
		if (Objects.isNull(transformedJSON) || CommonConstant.IS_NULL.getName().equalsIgnoreCase(transformedJSON)) {
			LOGGER.error("Trasnformed JSON is {}{}", transformedJSON, "throwing not found exception");
			throw new BusinessException(ErrorCode.NOT_FOUND,
					errorResolver.getErrorMessage(ERR_DETAIL_NOT_FOUND, CommonConstant.BLANK.getName()));
		}
		LOGGER.info("Exit CartServiceImpl.getCartDetails()");   
	   return transformedJSON;
	}
	
	/**
	 * 
	 * Retrieve mini cart details from third party system and transforms
	 * 
	 * @return String
	 */
	@Override
	@PerfLog
	public ResponseEntity<String> getMiniCartDetails(String cartId) {

		LOGGER.debug(ENTRY_METHOD, "getMiniCartDetails");

		HashMap<String, String> responseMap = null;
		String finalResponseJSON = null;
		String transformedJSON = null;

		try {
			responseMap = cartIntegrationServiceImpl.invokeHTTP("minicart", null, null, null);
		} catch (ASOException asoException) {
			if (!Objects.isNull(asoException.getMessage())) {
				if (asoException.getMessage().contains(String.valueOf(ErrorCode.NOT_FOUND.getErrorCode()))) {
					LOGGER.error(asoException.getMessage());
					return new ResponseEntity<>(minicartNotFound, HttpStatus.OK);
				} else {
					throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE, asoException.getMessage());
				}
			}
		}
		
		finalResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);
		
		
		if(enableJSONLogging) {
			LOGGER.debug("Final response JSON is {}", finalResponseJSON);
		}
		if(!StringUtils.isBlank(finalResponseJSON)) {
			transformedJSON = cartResponseTransformerImpl.transform("minicart", finalResponseJSON);
		}
		if(enableJSONLogging) {
			LOGGER.debug("Trasnformed JSON is {}", transformedJSON);
		}

		if (Objects.isNull(transformedJSON) || CommonConstant.IS_NULL.getName().equalsIgnoreCase(transformedJSON)) {
			return new ResponseEntity<>(minicartNotFound, HttpStatus.OK);
		}

		LOGGER.debug(EXIT_METHOD, "getMiniCartDetails");
		return new ResponseEntity<>(transformedJSON, HttpStatus.OK);
	}


	@Override
	@PerfLog
	public String addCart(CartRequest cartRequest,HttpServletResponse httpServletResponse) {
		
		LOGGER.debug("Entry addCart()");

		if (Objects.isNull(cartRequest) || Objects.isNull(cartRequest.getSkus()) || cartRequest.getSkus().isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("ERR_BAD_REQUEST", ""));
		}

		String serviceName = "addToCart";
		HashMap<String, String> responseMap = null;
		String aggregatedResponseJSON = null;
		String transformedJSON = null;
		Map<String, String> mapParams = new HashMap<>();




		mapParams.put("queryParam", urlBuilderUtil.buildUrl(cartRequest, getCartFieldsMap()));


		// retrieve cart details
		responseMap = cartIntegrationServiceImpl.invokeHTTP(serviceName, mapParams, null, null);

		// validate the response json
		aggregatedResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);
		
		// just one line below must be remove when wcs will pass right json
		if (!Objects.isNull(aggregatedResponseJSON)
				&& !CommonConstant.NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
			aggregatedResponseJSON = aggregatedResponseJSON.replaceAll("/\\*", "").replaceAll("\\*/", "");
		}
		
		aggregatedResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);

		if(enableJSONLogging) {
			LOGGER.info("Aggregated  response JSON is {}", aggregatedResponseJSON);
		}

		if (Objects.isNull(aggregatedResponseJSON)
				|| CommonConstant.IS_NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
			throw new BusinessException(ErrorCode.NOT_FOUND,
					errorResolver.getErrorMessage(ERR_DETAIL_NOT_FOUND, CommonConstant.BLANK.getName()));
		}

		// replacing amp; to blank from urlSuffix.
		aggregatedResponseJSON = modifyJSON(aggregatedResponseJSON);

		// transform the json
		transformedJSON = cartResponseTransformerImpl.transform(serviceName, aggregatedResponseJSON);

		if(enableJSONLogging) {
			LOGGER.debug("transformed json is {} ", transformedJSON);
		}


		if (Objects.isNull(transformedJSON) || CommonConstant.NULL.getName().equalsIgnoreCase(transformedJSON)) {
			throw new BusinessException(ErrorCode.NOT_FOUND, errorResolver.getErrorMessage("ERR_DETAIL_NOT_FOUND", ""));
		}
		
		LOGGER.debug("exit addCart()");

		if (Objects.isNull(transformedJSON) || CommonConstant.IS_NULL.getName().equalsIgnoreCase(transformedJSON)) {
			throw new BusinessException(ErrorCode.NOT_FOUND,
					errorResolver.getErrorMessage(ERR_DETAIL_NOT_FOUND, CommonConstant.BLANK.getName()));
		}

		// adding cookie into response.
		//setCookieInResponse(httpServletResponse);

		// Shortening exception message.
		transformedJSON = filterResponse(transformedJSON);

		LOGGER.debug(EXIT_METHOD, "addCart");

		return transformedJSON;
	}

	/**
	 * This method filters response for exception and URLs.
	 * 
	 * @param transformedJSON
	 * @return String
	 */
	private String filterResponse(String transformedJSON) {

		if (transformedJSON.contains("com.ibm.commerce.exception.ECApplicationException:")) {
			transformedJSON = transformedJSON.replaceAll("com.ibm.commerce.exception.ECApplicationException:",
					CommonConstant.BLANK.getName());
		}

		return transformedJSON;
	}


	//UpdateShippingmode
	@Override
	public String bopisUpdateShippingMode(UpdateShippingRequest updateShippingRequest) {
		LOGGER.info("Entry Update Shipping Mode ()");

		if (Objects.isNull(updateShippingRequest) || Objects.isNull(updateShippingRequest.getOrderItem()) || updateShippingRequest.getOrderItem().isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("ERR_BAD_REQUEST", ""));
		}
		
		String serviceName = "bopisupdateshippingmode";
		HashMap<String, String> responseMap = null;
		String aggregatedResponseJSON = null;
		String transformedJSON = null;
		String requestBody = transformRequest(updateShippingRequest);
		
		LOGGER.debug("Update Shipping request sending to WCS with component name {} and request body {} ", serviceName ,  requestBody); 
		
		responseMap = cartIntegrationServiceImpl.invokeHTTP(serviceName, null, requestBody, null);
		aggregatedResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);
		if (!Objects.isNull(aggregatedResponseJSON)
				&& !CommonConstant.NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
			aggregatedResponseJSON = aggregatedResponseJSON.replaceAll("/\\*", "").replaceAll("\\*/", "");
		}
		
		LOGGER.info("Aggregated  response JSON is {}", aggregatedResponseJSON);
		transformedJSON = cartResponseTransformerImpl.transform(serviceName, aggregatedResponseJSON);
		LOGGER.debug("transformed json is {} ", transformedJSON);

		if (Objects.isNull(transformedJSON) || CommonConstant.NULL.getName().equalsIgnoreCase(transformedJSON)) {
			throw new BusinessException(ErrorCode.NOT_FOUND, errorResolver.getErrorMessage("ERR_DETAIL_NOT_FOUND", ""));


		}
		
		LOGGER.debug("Exit Update Shipping Mode ()");
		return transformedJSON;
	}

	//UpdateShippingmode
	private String transformRequest(UpdateShippingRequest updateShippingRequest) {
		ObjectMapper mapper = null;
		String requestBody = null;
		String transformedJSON=null;
		try {
			updateShippingRequest.setX_calculationUsage("-1,-2,-7");
			mapper = new ObjectMapper();
			requestBody = mapper.writeValueAsString(updateShippingRequest);
			transformedJSON = requestBody;
		} catch (JsonProcessingException e) {
			throw new BusinessException(ErrorCode.NOT_FOUND, errorResolver.getErrorMessage("ERR_JSON_TRANFORMATION_FAILURE", ""));
		}	
			
		if (transformedJSON.contains("@cartURL@")) {
			transformedJSON = transformedJSON.replaceFirst("@cartURL@", cartWCSURLMap.get("cartURL"));
		}
		
		if (transformedJSON.contains("amp;")) {
			transformedJSON = PATTERN.matcher(transformedJSON).replaceAll("");
		}
		
		return transformedJSON;
	}

	/**
	 * This method add cookies into response.
	 * 
	 * @param httpServletResponse
	 */
	private void setCookieInResponse(HttpServletResponse httpServletResponse) {
		String[] cookieString = Pattern.compile(CommonConstant.SEMICOLON.getName()).split(MDC.get(SET_COOKIE));
		for (String cookie : cookieString) {
			if (!CommonConstant.BLANK.getName().equals(cookie) && !(cookie.contains(EXPIRES) || cookie.contains(PATH)
					|| cookie.contains(HTTP_ONLY) || cookie.contains(HOST_ONLY) || cookie.contains(SESSION) || cookie.contains(SECURE))) {
				// split cookie and set into response.
				splitCookieAndSetIntoResponse(httpServletResponse, cookie);
			}
		}
	}

	/**
	 * This method split cookies and add those into response.
	 * 
	 * @param httpServletResponse
	 * @param cookieStr
	 */
	private void splitCookieAndSetIntoResponse(HttpServletResponse httpServletResponse, String cookieStr) {
		String[] cookiePair = cookieStr.split(CommonConstant.EQUALS.getName());
		if (cookiePair.length > 1) {
			Cookie cookie = new Cookie(cookiePair[0], cookiePair[1]);
			cookie.setPath("/");
			httpServletResponse.addCookie(cookie);
		}
	}

	/**
	 * This method transforms String to JSON and replace amp; to blank string from
	 * urlSuffix.
	 * 
	 * @param transformedJSON
	 * @return
	 */
	@PerfLog
	private String modifyJSON(String transformedJSON) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject = (JSONObject) parser.parse(transformedJSON);
		} catch (ParseException e) {
			LOGGER.error("Tranfromation fails modifyJSON()");
			throw new BusinessException(ErrorCode.WRONG_DATA,
					errorResolver.getErrorMessage("ERR_JSON_TRANFORMATION_FAILURE", CommonConstant.BLANK.getName()));
		}

		JSONObject rootNode = (JSONObject) jsonObject.get("addToCart");
		if (Objects.isNull(rootNode)) {
			return transformedJSON;
		} else {
			resolveCartURL(rootNode);
			resolveChecoutURL(rootNode);
			resolveQuantityAndPrdName(rootNode);
			resolveErrorMessage(rootNode);
			resolveExceptionMessage(rootNode);
			return jsonObject.toString();
		}
	}

	/**
	 * This method replaces amp; to blank in cartUrl.
	 * 
	 * @param rootNode
	 */
	private void resolveQuantityAndPrdName(JSONObject rootNode) {		
		Object qtyAdded = rootNode.get(QTY_ADDED);
		if (!Objects.isNull(qtyAdded) && !qtyAdded.toString().isEmpty()) {
			String qty = qtyAdded.toString();
			try {							
				rootNode.put(QTY_ADDED, ""+Double.valueOf(qty).intValue());
			}catch(Exception e) {				
				//do nothing		
			}			
		}
		Object cartQtyAdded = rootNode.get(CART_QTY);
		if (!Objects.isNull(cartQtyAdded) && !cartQtyAdded.toString().isEmpty()) {
			String cartQty = cartQtyAdded.toString();
			try {							
				rootNode.put(CART_QTY, ""+Double.valueOf(cartQty).intValue());
			}catch(Exception e) {				
				//do nothing		
			}			
		}
		try {
			if (rootNode.get(ITEMS) != null) {
				JSONArray itemJsonObj = (JSONArray) rootNode.get(ITEMS);
				if (itemJsonObj != null) {
					for (int i = 0; i < itemJsonObj.size(); i++) {
						JSONObject item = (JSONObject) itemJsonObj.get(i);
						// resolve quantity
						resolveItemQty(item);
						// resolve product name
						resolveProductName(item);
						//resolve diff
						resolveDiff(item);
					}
				}

			}
		}catch(Exception e) {				
			//do nothing		
		}	
	}


	/**
	 * This method replaces amp; to blank in cartUrl.
	 * 
	 * @param rootNode
	 */
	private void resolveCartURL(JSONObject rootNode) {
		
		Object cartURLObj = rootNode.get(CART_URL);
		if (!Objects.isNull(cartURLObj) && !cartURLObj.toString().isEmpty()) {
			String cartURL = cartURLObj.toString();
			if (cartURL.contains(CommonConstant.AMP.getName())) {
				cartURL = cartURL.replaceAll(CommonConstant.AMP.getName(), CommonConstant.BLANK.getName());
			}
			rootNode.put(CART_URL, cartURL);
		}
	}

	/**
	 * @param rootNode
	 * 
	 * In case of guest user PrefixURL is not null and suffixURL is null
	 * In case of registered user Prefix & suffix URL are not null
	 * 
	 *+--------------------------------------------+
	 *| 		    | Prefix		| Suffix   	   |
	 *+--------------------------------------------+
	 *| Guest		| &				| EMPTY_STRING |
	 *+--------------------------------------------+
	 *| Registered	| &amp;			| &			   |
	 *+--------------------------------------------+
	 */
	private void resolveChecoutURL(JSONObject rootNode) {
		String urlSuffix = "";
		String urlPrefix = "";
		if (!Objects.isNull(rootNode.get(URL_PREFIX))) {
			urlPrefix = rootNode.get(URL_PREFIX).toString();
		}
		if (!Objects.isNull(rootNode.get(URL_SUFFIX))) {
			urlSuffix = rootNode.get(URL_SUFFIX).toString();
		}

		if (!urlSuffix.isEmpty()) {
			// for registerd user
			urlSuffix = urlSuffix.replaceAll(CommonConstant.AMP.getName(), CommonConstant.BLANK.getName());
			try {
				int firstIndex = urlSuffix.indexOf('=');
				String url = urlSuffix.substring(firstIndex + 1, urlSuffix.length());
				url = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
				urlSuffix = urlSuffix.substring(0, firstIndex + 1).concat(url);

			} catch (UnsupportedEncodingException e) {
				throw new BusinessException(ErrorCode.WRONG_DATA, errorResolver
						.getErrorMessage("ERR_JSON_TRANFORMATION_FAILURE", CommonConstant.BLANK.getName()));
			}
			rootNode.put(URL_SUFFIX, urlSuffix);

		}
		if (!urlPrefix.isEmpty()) {
			urlPrefix = urlPrefix.replaceAll(CommonConstant.AMP.getName(), CommonConstant.BLANK.getName());
			rootNode.put(URL_PREFIX, urlPrefix);
		}
	}
	
	private void resolveErrorMessage(JSONObject rootNode) {

		Object cartErrorMessage = rootNode.get(RESPONSE_ERROR_MESSAGE);
		if (!Objects.isNull(cartErrorMessage) && !cartErrorMessage.toString().isEmpty()) {
			String cartErrorMessageStr = StringEscapeUtils.unescapeHtml4(cartErrorMessage.toString());
			rootNode.put(RESPONSE_ERROR_MESSAGE, cartErrorMessageStr);
		}
	}
	
	private void resolveExceptionMessage(JSONObject rootNode) {

		Object cartExceptionMessage = rootNode.get(RESPONSE_EXCEPTION_MESSAGE);
		if (!Objects.isNull(cartExceptionMessage) && !cartExceptionMessage.toString().isEmpty()) {
			String cartExceptionMessageStr = StringEscapeUtils.unescapeHtml4(cartExceptionMessage.toString());
			rootNode.put(RESPONSE_EXCEPTION_MESSAGE, cartExceptionMessageStr);
		}
	}
	

	private void resolveItemQty(JSONObject item) {
		Object itemQty = item.get(QUANTITY);
		if (!Objects.isNull(itemQty) && !itemQty.toString().isEmpty()) {
			String itemQtyStr = itemQty.toString();
			item.put(QUANTITY, "" + Double.valueOf(itemQtyStr).intValue());
		}
	}
	
	private void resolveProductName(JSONObject item) {
		Object productName = item.get(PRODUCT_NAME);
		if (!Objects.isNull(productName) && !productName.toString().isEmpty()) {
			String productNameStr = StringEscapeUtils.unescapeHtml4(productName.toString());
			item.put(PRODUCT_NAME, productNameStr);
		}
	}




	
	
	
	/**
	 * Gets the available shipping modes for the cart.
	 *
	 * @param profile
	 * @param orderId
	 * @return the string
	 */
		@Override
		
		@PerfLog
		public String getAvailableShippingmethods(final String profile,final String orderId) {		
			
			LOGGER.debug("get available Shipping methods ()");	
				if ( (null == profile || profile.isEmpty()) || (null == orderId ||  orderId.isEmpty())) {
				throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("ERR_BAD_REQUEST", ""));
			}
			
			String serviceName = "availableshippingmode";
			HashMap<String, Exchange> responseMap = null;
			String aggregatedResponseJSON = null;
			Map<String, String> mapParams = new HashMap<>();
			//TO DO mapparams transformedJSON
			mapParams.put("queryParam", "?profile="+profile+"&orderId="+orderId);
			responseMap = cartIntegrationServiceImpl.invoke(serviceName, mapParams, null, null);
			aggregatedResponseJSON = cartResponseHandlerImpl.handleResponse(responseMap);
			if (!Objects.isNull(aggregatedResponseJSON)
					&& !CommonConstant.NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
				aggregatedResponseJSON = aggregatedResponseJSON.replaceAll("/\\*", "").replaceAll("\\*/", "");
			}
			LOGGER.debug("exit availableshippingmode()");
			return aggregatedResponseJSON;
		}
		
		@Override
		@PerfLog
		public String updateItemQuantity(UpdateQuantityRequest quantityRequest) {
			HashMap<String, String> responseMap = null;
			String aggregatedResponseJSON = null;
			try {
				String quantityRequestJson = jsonMapper.writeValueAsString(quantityRequest);
				LOGGER.debug("update quantity request payload to WCS {} ", quantityRequestJson);
				responseMap = cartIntegrationServiceImpl.invokeHTTP("updateItemQuantity", null, quantityRequestJson, null);
				aggregatedResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);

				// just one line below must be remove when wcs will pass right json
				if (!Objects.isNull(aggregatedResponseJSON)
						&& !CommonConstant.NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
					aggregatedResponseJSON = aggregatedResponseJSON.replaceAll("/\\*", "").replaceAll("\\*/", "");
				}

				LOGGER.debug("aggregatedResponseJSON json is {} ", aggregatedResponseJSON);
				return aggregatedResponseJSON;
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, errorResolver.getErrorMessage("ERR_JSON_TRANFORMATION_FAILURE", ""));
			}
			
		}
		
		@Override
		@PerfLog
		public void initiateCheckout(InitiateRequest initiateRequest) {
			
			if (Objects.isNull(initiateRequest) ) {
				throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("ERR_BAD_REQUEST", ""));
				
			}
			HashMap<String, String> responseMap = null;
			String aggregatedResponseJSON = null;
			String requestBody=transformRequest(initiateRequest);
			responseMap=cartIntegrationServiceImpl.invokeHTTP("initiateCheckout", null,requestBody, null);
			aggregatedResponseJSON = cartResponseHandlerImpl.handleHTTPResponse(responseMap);
			if (!Objects.isNull(aggregatedResponseJSON)
					&& !CommonConstant.NULL.getName().equalsIgnoreCase(aggregatedResponseJSON)) {
				aggregatedResponseJSON = aggregatedResponseJSON.replaceAll("/\\*", "").replaceAll("\\*/", "");
			}
			else if(null == aggregatedResponseJSON || aggregatedResponseJSON.isEmpty())
			{
				throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("NOT_FOUND", ""));
			}
				
		}
		
		
	@PerfLog	
	private String transformRequest(InitiateRequest initiateRequest) {
		String requestBody = null;
		try {
			requestBody = jsonMapper.writeValueAsString(initiateRequest);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
					errorResolver.getErrorMessage("ERR_JSON_TRANFORMATION_FAILURE", ""));
		}

		return requestBody;
	} 

	

	private void resolveDiff(JSONObject item) {
		if (item.get(DIFF) != null) {
			JSONArray diffJsonObj = (JSONArray) item.get(DIFF);
			if (diffJsonObj != null) {
				for (int i = 0; i < diffJsonObj.size(); i++) {
					JSONObject diff = (JSONObject) diffJsonObj.get(i);
					// resolve value
					resolveValue(diff);
				}
			}
		}
	}

	private void resolveValue(JSONObject diff) {
		Object value = diff.get(VALUE);
		if (!Objects.isNull(value) && !value.toString().isEmpty()) {
			String valueStr = StringEscapeUtils.unescapeHtml4(value.toString());
			diff.put(VALUE, valueStr);
		}
	}

}

