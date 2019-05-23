package com.academy.cart.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.InitiateRequest;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.academy.cart.vo.UpdateShippingRequest;

// TODO: Auto-generated Javadoc
/**
 * The Interface CartService.
 */
public interface CartService {
	
	/**
	 * Gets the cart details.
	 *
	 * @param transformationId the transformation id
	 * @return the cart details
	 */
	public String getCartDetails(String transformationId);
	public String getCartDetails();
	/**
	 * Gets the mini cart details.
	 *
	 * @param cartId the cart id
	 * @return the mini cart details
	 */
	


	public ResponseEntity<String> getMiniCartDetails(String cartId);


	/**
	 * Adds the cart.
	 *
	 * @param cartRequest the cart request
	 * @return the string
	 */
	public String addCart(CartRequest cartRequest,HttpServletResponse httpServletResponse);
	
	
	
	
	public String bopisUpdateShippingMode(UpdateShippingRequest updateShippingRequest);
	
	/**
	 * Gets the available shipping modes for the cart.
	 *
	 * @param profile
	 * @param orderId
	 * @return the string
	 */
	public String getAvailableShippingmethods(String profile,String orderId);
	
	/**
	 * Update item quantity.
	 *
	 * @param quantityRequest the quantity request
	 */
	public String updateItemQuantity(UpdateQuantityRequest quantityRequest);
	/**
	 * 
	 *
	 * @param 
	 */
	public void initiateCheckout(InitiateRequest initiateRequest);
	
	
	
	
}
