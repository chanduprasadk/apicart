package com.academy.cart.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;

// TODO: Auto-generated Javadoc
/**
 * The Interface CartAggregationService.
 */
public interface CartAggregationService {
	
	/**
	 * View cart.
	 *
	 * @param storeId the store id
	 * @param queryParams the query params
	 * @return the string
	 */
	public String fetchCart(String storeId, Map<String, String> queryParams, HttpHeaders headers);

}
