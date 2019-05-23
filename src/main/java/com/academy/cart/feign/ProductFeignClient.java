package com.academy.cart.feign;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

import com.academy.cart.feign.ProductFeignClient.ProductInfoFeignClientFallback;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.util.ErrorCode;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;
/**
 * The Interface ProductFeignClient.
 */
@FeignClient(name = "product-info" , fallback = ProductInfoFeignClientFallback.class)
public interface ProductFeignClient {

	/**
	 * Gets the product details.
	 *
	 * @param productId the product id
	 * @param headerMap the header map
	 * @return the product details
	 */
	@RequestLine("GET /api/productinfo?productIds={productIds}&summary=true&dbFallback=true")
	String getProductDetails(@Param(value = "productIds") String productIds, @HeaderMap Map<String, String> headerMap);

	/**
	 * The Class ProductInfoFeignClientFallback.
	 */
	@Component
	class ProductInfoFeignClientFallback implements ProductFeignClient {

		/* (non-Javadoc)
		 * @see com.academy.cart.feign.ProductFeignClient#getProductDetails(java.lang.String, java.util.Map)
		 */
		@Override
		public String getProductDetails(String productIds, Map<String, String> headerMap) {
			throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE, "Product service is not available");
		}

	}
}
