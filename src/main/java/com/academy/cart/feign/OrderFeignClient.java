package com.academy.cart.feign;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

import com.academy.cart.feign.OrderFeignClient.OrderFeignClientFallback;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;

// TODO: Auto-generated Javadoc
/**
 * The Interface OrderFeignClient.
 */
@FeignClient(name = "order", fallback = OrderFeignClientFallback.class)
public interface OrderFeignClient {

	/**
	 * Gets the promo details.
	 *
	 * @param orderId the order id
	 * @param headerMap the header map
	 * @return the promo details
	 */
	@RequestLine("GET /api/orders/{orderId}/promocode")
	String getPromoDetails(@Param(value = "orderId") String orderId, @HeaderMap Map<String, String> headerMap);

	/**
	 * The Class OrderFeignClientFallback.
	 */
	@Component
	class OrderFeignClientFallback implements OrderFeignClient {

		/* (non-Javadoc)
		 * @see com.academy.cart.feign.OrderFeignClient#getPromoDetails(java.lang.String, java.util.Map)
		 */
		@Override
		public String getPromoDetails(String orderId, Map<String, String> headerMap) {
			//returning the default fallback response
			return null;
		}

	}
}
