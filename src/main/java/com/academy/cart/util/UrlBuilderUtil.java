package com.academy.cart.util;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.academy.cart.constant.CartRequestConstant;
import com.academy.cart.constant.CommonConstant;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.Sku;
import com.academy.common.aspect.perf.PerfLog;

/**
 * 
 * @author Sapient 
 * 
 * This class contains utility methods to build WCS get URL.
 *
 */
@Component
public class UrlBuilderUtil {

	private static final String TRUE = "true";

	/**
	 * This method builds URL.
	 * 
	 * @param cartRequest
	 * @param cartFieldsMap
	 * @return String
	 */
	@PerfLog
	public String buildUrl(CartRequest cartRequest, Map<String, Integer> cartFieldsMap) {

		StringBuilder url = new StringBuilder(CommonConstant.QUESTIONMARK.getName());
		url.append(buildUrlFromObject(CartRequestConstant.STORE_ID.getUrlParamName(),
				cartFieldsMap.get(CartRequestConstant.STORE_ID.getUrlParamName())));
		url.append(buildUrlFromObject(CartRequestConstant.INVENTORY_VALIDATION.getUrlParamName(),
				cartRequest.isInventoryCheck()));

		url.append(buildUrlFromObject(CartRequestConstant.CATALOG_ID.getUrlParamName(),
				cartFieldsMap.get(CartRequestConstant.CATALOG_ID.getUrlParamName())));

		if (!Objects.isNull(cartRequest.isGCItem())) {
			url.append(buildUrlFromObject(CartRequestConstant.GC_ITEM.getUrlParamName(), cartRequest.isGCItem()));
		}
		if (!Objects.isNull(cartRequest.isBundle())) {
			url.append(buildUrlFromObject(CartRequestConstant.BUNDLE.getUrlParamName(), cartRequest.isBundle()));
		}
		
		if (!Objects.isNull(cartRequest.getBundleId())) {
			url.append(buildUrlFromObject(CartRequestConstant.BUNDLE_ID.getUrlParamName(), cartRequest.getBundleId()));
		}
		
		if (!Objects.isNull(cartRequest.getSelectedStoreId())) {
			url.append(buildUrlFromObject(CartRequestConstant.SOF_IDENTIFIER.getUrlParamName(), cartRequest.getSelectedStoreId()));
		}


		if (!Objects.isNull(cartRequest.getItemComment())) {
			url.append(buildUrlFromObject(CartRequestConstant.ITEM_COMMENT.getUrlParamName(),
					cartRequest.getItemComment()));
		}

		url.append(buildUrlFromObject(CartRequestConstant.GIFT_AMOUNT.getUrlParamName(), cartRequest.getGiftAmount()));
		if (!Objects.isNull(cartRequest.getSkus())) {
			List<String> cartEntryIds = cartRequest.getSkus().stream().map(Sku::getId).collect(Collectors.toList());
			List<String> quantities = cartRequest.getSkus().stream().map(sku -> String.valueOf(sku.getQuantity()))
					.collect(Collectors.toList());
			url.append(buildUrlFromList(CartRequestConstant.CART_ENRTY_ID.getUrlParamName(), cartEntryIds));
			url.append(buildUrlFromList(CartRequestConstant.QUANTITY.getUrlParamName(), quantities));
		}
		url.append(buildUrlFromObject(CartRequestConstant.PICK_UP_STORE.getUrlParamName(), cartRequest.isPickUpInStore()));
		url.append(buildUrlFromObject(CartRequestConstant.ITEM_DETAILS.getUrlParamName(), TRUE));
		url.append(buildUrlFromObject(CartRequestConstant.CALCULATE_ORDER.getUrlParamName(), 1));
		url.append(appendCalculationUsagesIntoUrl(cartRequest));
		url.deleteCharAt(url.length() - 1);
		return url.toString();
	}

	/**
	 * This method appends calculation usage into String URL.
	 * 
	 * @param cartRequest
	 * @return StringBuilder
	 */
	private StringBuilder appendCalculationUsagesIntoUrl(CartRequest cartRequest) {
		StringBuilder url = new StringBuilder();
		url.append(CartRequestConstant.CALCULATION_USAGE.getUrlParamName());
		url.append(CommonConstant.EQUALS.getName());
		url.append(cartRequest.getCalculationUsages().stream().map(String::valueOf)
				.collect(Collectors.joining(CommonConstant.COMMA.getName())));
		url.append(CommonConstant.AMPERSAND.getName());
		return url;
	}

	/**
	 * This method builds URL from List e.g catEntry_0= 111 & catEntry_1=111
	 * 
	 * @param paramName
	 * @param params
	 * @return StringBuilder
	 */
	private StringBuilder buildUrlFromList(String paramName, List<String> params) {

		StringBuilder builder = new StringBuilder();
		if (!Objects.isNull(params) && !params.isEmpty()) {
			int counter = 1;
			for (String paramVal : params) {
				builder.append(paramName);
				builder.append(CommonConstant.UNDERSCORE.getName());
				builder.append(counter++);
				buildPartialUrl(builder, paramVal);
			}
		}
		return builder;
	}

	/**
	 * This method builds URL from object e.g inventory=true
	 * 
	 * @param paramName
	 * @param paramValue
	 * @return StringBuilder
	 */
	private StringBuilder buildUrlFromObject(String paramName, Object paramValue) {
		StringBuilder builder = new StringBuilder();
		if (!Objects.isNull(paramName)) {
			builder.append(paramName);
			String param = "";
			if (!Objects.isNull(paramValue)) {
				if (!(paramValue instanceof String)) {
					param = String.valueOf(paramValue);
					buildPartialUrl(builder, param);
				} else {
					buildPartialUrl(builder, paramValue.toString());
				}

			} else {
				builder.append(CommonConstant.EQUALS.getName());
				builder.append(CommonConstant.AMPERSAND.getName());
			}
		}
		return builder;
	}

	/**
	 * This method append value against key into URL.
	 * 
	 * @param builder
	 * @param paramVal
	 */
	private void buildPartialUrl(StringBuilder builder, String paramVal) {
		builder.append(CommonConstant.EQUALS.getName());
		if (!Objects.isNull(paramVal) && !paramVal.isEmpty()) {
			builder.append(paramVal);
		} else {
			builder.append(CommonConstant.BLANK.getName());
		}

		builder.append(CommonConstant.AMPERSAND.getName());
	}
	
	public static URI buildURI(String url, Map<String,String> queryParams) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		Optional.ofNullable(queryParams).ifPresent(paramMap -> {
			paramMap.forEach((k, v) -> {
				builder.queryParam(k, v);
			});	
		});
		return builder.build().toUri();
	}
	

	/**
	 * This method append value against key into URL for getavailableshippingmethodsmethod.
	 * 
	 * @param builder
	 * @param paramVal
	 */
	public  StringBuilder buildUrlParams(String profile,String orderId) {

		StringBuilder url = new StringBuilder(CommonConstant.QUESTIONMARK.getName());
		
		if (!Objects.isNull(profile)) {
			url.append(buildUrlFromObject(CartRequestConstant.PROFILE.getUrlParamName(), profile));
		}
		if (!Objects.isNull(orderId)) {
			url.append(buildUrlFromObject(CartRequestConstant.ORDER_ID.getUrlParamName(), orderId));
		}

				return url;
	}
}
