package com.academy.cart.feign;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

import com.academy.cart.feign.TaxAndShippingFeignClient.TaxFeignClientFallback;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;

/**
 * The Interface TaxAndShippingFeignClient.
 */
@FeignClient(name = "tax", fallback = TaxFeignClientFallback.class)
public interface TaxAndShippingFeignClient {

	/**
	 * Gets the tax and shipping.
	 *
	 * @param storeId         the store id
	 * @param orderId         the order id
	 * @param deliveryZipCode the delivery zip code
	 * @param storeZipCode    the store zip code
	 * @param headerMap       the header map
	 * @return the tax and shipping
	 */
	@RequestLine(value = "GET /api/taxes/order/{orderId}/tax?orderId={orderId}&deliveryZipCode={deliveryZipCode}&storeZipCode={storeZipCode}")
	String getTaxAndShipping(@Param(value = "orderId") String orderId,
			@Param(value = "deliveryZipCode") String deliveryZipCode,
			@Param(value = "storeZipCode") String storeZipCode, @HeaderMap Map<String, String> headerMap);

	/**
	 * The Class TaxFeignClientFallback.
	 */
	@Component
	class TaxFeignClientFallback implements TaxAndShippingFeignClient {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.academy.cart.feign.TaxAndShippingFeignClient#getTaxAndShipping(java.lang.
		 * String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
		 */
		@Override
		public String getTaxAndShipping(String orderId, String deliveryZipCode, String storeZipCode,
				Map<String, String> headerMap) {
			return null;
		}

	}
}
