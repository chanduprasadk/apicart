package com.academy.cart.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.academy.cart.constant.CommonConstant;
import com.academy.cart.domain.BopisItem;
import com.academy.cart.domain.BundleItem;
import com.academy.cart.domain.Cart;
import com.academy.cart.domain.GiftItem;
import com.academy.cart.domain.Item;
import com.academy.cart.util.CartUtils;
import com.academy.cart.vo.ItemRequest;
import com.academy.cart.vo.UpdateCartRequest;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.academy.cart.vo.UpdateQuantityRequest.OrderQuantity;
import com.academy.cart.vo.WCSAddCartRequest;
import com.academy.cart.vo.WCSItemAttribute;
import com.academy.cart.vo.WCSItemRequest;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.common.exception.util.ErrorResolver;
import com.academy.common.service.DomainService;
import com.academy.integration.handler.HandlerResponseImpl;
import com.academy.integration.service.IntegrationServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

/***
 * 
 * @author Yogi
 */

@Service
public class ItemService extends DomainService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);
	
	@Autowired
	protected ObjectMapper jsonMapper;
	
	@Autowired
	private IntegrationServiceImpl cartIntegrationServiceImpl;
	
	@Autowired
	private HandlerResponseImpl handlerResponseImpl;
	

	@Value("${enableJSONLogging}")
	private boolean enableJSONLogging;
	
	@Autowired
	private ErrorResolver errorResolver;
	
	public boolean deleteItem(final String cartId, final String itemId) {
		boolean flag = false;
		LOGGER.info("Enter ItemService.deleteItem");
		final UpdateQuantityRequest request = this.createUpdateQuantityRequest(cartId, itemId, "0");
		try {
			final String requestJson = jsonMapper.writeValueAsString(request);
			LOGGER.debug("UpdateQuantityRequest structure {} " , requestJson);
			this.callUpdateService(requestJson);
			flag = true;
		} catch (JsonProcessingException e) {
			LOGGER.error("ItemService.deleteItem exception will converting to JSON {} " , e.getMessage());
		}
		LOGGER.info("Exit ItemService.deleteItem");
		return flag;
	}
	
	public boolean updateItem(final String cartId, final String itemId, final ItemRequest itemRequest) {
		boolean flag = false;
		LOGGER.info("Enter ItemService.updateItem");
		final UpdateQuantityRequest request = this.createUpdateQuantityRequest(cartId, itemId, itemRequest.getQuantity());
		try {
			String quantityRequestJson = jsonMapper.writeValueAsString(request);
			LOGGER.debug("UpdateQuantityRequest structure {} " , quantityRequestJson);
			this.callUpdateService(quantityRequestJson);
			flag = true;
		} catch (JsonProcessingException e) {
			LOGGER.error("ItemService.updateItem exception will converting to JSON {} " , e.getMessage());
		}
		LOGGER.info("Exit ItemService.updateItem");
		return flag;
	}
	
	private UpdateQuantityRequest createUpdateQuantityRequest(final String cartId, final String itemId, final String qty) {
		final UpdateQuantityRequest request = new UpdateQuantityRequest();
		final OrderQuantity item = new OrderQuantity();
		item.setOrderItemId(itemId);
		item.setQuantity(qty);
		request.addOrderItem(item);
		return request;
	}
	
	@HystrixCommand(fallbackMethod = "fallbackService", commandProperties = {
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "5") })
	private void callUpdateService(final String request) {
		LOGGER.info("Enter ItemService.callUpdateService");
		cartIntegrationServiceImpl.invokeHTTP("updateItemQuantity", null, request, null);
		LOGGER.info("Exit ItemService.callUpdateService");
	}
	
	void fallbackUpdateService(final String request) {
		LOGGER.info("Enter ItemService.fallbackUpdateService");
		throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE,"Internal server error. Additional details will be contained on the server logs.");
	}

	/** 
	 * Update the item with new values or add new item in the order.
	 * @param updateCartRequest
	 */
	
	public void updateOrderItem(UpdateCartRequest updateCartRequest, String storeId) {

		LOGGER.info("Enter ItemService.updateOrderItem");

		updateCartRequest.setX_calculateOrder(CommonConstant.CALCULATEORDER.getName());
		updateCartRequest.setX_calculationUsage(CommonConstant.CALCULATIONUSAGE.getName());
		
		Map<String, String> params = new HashMap<>();
		
		params.put("1", storeId);
		
		String updateCartRequestJson = CartUtils.getJSONString(updateCartRequest);
		LOGGER.debug("updateCartRequest structure {} " , updateCartRequestJson);
		
		cartIntegrationServiceImpl.invokeHTTP("updateOrderItem", params, updateCartRequestJson, null);
		
		LOGGER.info("Exit ItemService.updateOrderItem");
	}
	
	public void fallbackUpdateOrder(final UpdateCartRequest request, String storeId, Throwable exception) {
		LOGGER.error("Enter ItemService.fallbackUpdateOrder {}", ExceptionUtils.getFullStackTrace(exception));
		throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE,
				"Internal server error. Additional details will be contained on the server logs.");
	}
	
	public String addItems(String cartId, Cart cart) {
		LOGGER.info("Enter ItemService.addItems with card id {} ", cartId);
		if (Objects.isNull(cart) ) {
			throw new BusinessException(ErrorCode.BAD_REQUEST, errorResolver.getErrorMessage("ERR_BAD_REQUEST", ""));
			
		}
		WCSAddCartRequest wcsCartRequest = new WCSAddCartRequest();
		populateWCSAddToCartRequest(cart, wcsCartRequest);
		String addItemsRequest = CartUtils.getJSONString(wcsCartRequest);
		if(enableJSONLogging) {
			LOGGER.debug("addItemsRequest sending to WCS  {} ", addItemsRequest);
		}
		HashMap<String, String> responseMap = cartIntegrationServiceImpl.invokeHTTP("addItems", null, addItemsRequest, null);
		String addItemsResponse = handlerResponseImpl.handleHTTPResponse(responseMap);
		if(enableJSONLogging) {
			LOGGER.debug("addItemsResponse received from WCS  {} ", addItemsResponse);
		}
		
		if(null == addItemsResponse || addItemsResponse.isEmpty()) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, errorResolver.getErrorMessage("ERR_INTERNAL_SERVER_ERROR", ""));
		}
		
		return addItemsResponse;
	}
	
	private void populateWCSAddToCartRequest(final Cart cart, final WCSAddCartRequest wcsCartRequest) {
		wcsCartRequest.setCalculateOrder(cart.getCalculateOrder());
		wcsCartRequest.setComment(cart.getComment());
		wcsCartRequest.setInventoryCheck(cart.isInventoryCheck());
		wcsCartRequest.setItemDetails(cart.isItemDetails());

		List<WCSItemRequest> wcsItems = new ArrayList<>();
        List<Item> items = cart.getItems();	
        Optional.ofNullable(items).ifPresent(itemList -> {
        	itemList.stream().forEach(item -> {
        		WCSItemRequest wcsItemRequest = new WCSItemRequest();
        		wcsItemRequest.setProductId(item.getProductId());
        		wcsItemRequest.setQuantity(item.getQuantity());
        		wcsItemRequest.setShipModeId(item.getShipModeId());
        		List<WCSItemAttribute> wcsItemAttributes = new ArrayList<>();
        		populateBundleItems(item, wcsCartRequest, wcsItemAttributes, wcsItemRequest);
        		populateBopusItems(item, wcsItemAttributes, wcsItemRequest);
        		populateGiftItems(item, wcsItemRequest);
        		wcsItems.add(wcsItemRequest);
        	});
        });
        wcsCartRequest.setItems(wcsItems);
	}
	
	private void populateBundleItems(final Item item, final WCSAddCartRequest wcsCartRequest, final List<WCSItemAttribute> wcsItemAttributes, final WCSItemRequest wcsItemRequest) {
		if (item.isBundle()) {
			wcsCartRequest.setBundleFlag(item.isBundle());
			List<BundleItem> bundleList = item.getBundleInfo();
			Optional.ofNullable(bundleList).ifPresent(bundleAttributes -> {
				bundleAttributes.stream().forEach(bundleAttrib -> {
					WCSItemAttribute wcsItemAttribute = new WCSItemAttribute();
					wcsItemAttribute.setAttributeType(CommonConstant.BUNDLE_IDENTIFIRE_TYPE.getName());
					wcsItemAttribute.setAttributeName(bundleAttrib.getAttributeKey() != null ? bundleAttrib.getAttributeKey() : CommonConstant.BUNDLE_IDENTIFIRE.getName());
					wcsItemAttribute.setAttributeValue(bundleAttrib.getAttributeValue());
					wcsItemAttributes.add(wcsItemAttribute);
				});
			});
			wcsItemRequest.setOrderItemExtendAttribute(wcsItemAttributes);
		}
	}
	
	private void populateBopusItems(final Item item, final List<WCSItemAttribute> wcsItemAttributes, final WCSItemRequest wcsItemRequest) {
		if(item.isPickInStore()) {
			List<BopisItem> bopisList = item.getStoreInfo();
			Optional.ofNullable(bopisList).ifPresent(bopisAttributes -> {
				bopisAttributes.stream().forEach(bopisAttrib -> {
					WCSItemAttribute wcsItemAttribute = new WCSItemAttribute();
					wcsItemAttribute.setAttributeType(CommonConstant.STORE_IDENTIFIRE_TYPE.getName());
					wcsItemAttribute.setAttributeName(bopisAttrib.getAttributeKey() != null ? bopisAttrib.getAttributeKey() : CommonConstant.STORE_IDENTIFIRE.getName());
					wcsItemAttribute.setAttributeValue(bopisAttrib.getAttributeValue());
					wcsItemAttributes.add(wcsItemAttribute);
				});
			});
			wcsItemRequest.setOrderItemExtendAttribute(wcsItemAttributes);
			wcsItemRequest.setStorePickupFlag(item.isPickInStore());
		}
	}
	
	private void populateGiftItems(final Item item, final WCSItemRequest wcsItemRequest) {
		if(item.isGiftItem()) {
			GiftItem giftItemInfo = item.getGiftItemInfo();
			Optional.ofNullable(giftItemInfo).ifPresent(giftItem -> {
				wcsItemRequest.setGiftItemFlag(item.isGiftItem());
				wcsItemRequest.setGiftAmount(giftItem.getGiftAmount());
				wcsItemRequest.setComment(giftItem.getComment());
			});
		}
	}

}
