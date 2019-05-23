package com.academy.cart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.academy.cart.service.CartService;
import com.academy.cart.vo.UpdateShippingRequest;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author alearani
 *
 *         Controller class for updateshippingmode service
 * 
 */
@RequestMapping("/api")
@RestController
public class UpdateShippingModeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateShippingModeController.class);

	@Autowired
	private CartService cartService;

	//UpdateShippingmode
	@RequestMapping(value= {"/cart/PUT/updateshippingmode","/cart/{cartId}/shippingMode"}, method = RequestMethod.POST, produces = "application/json")

	@ApiOperation(value = "Add cart shipping mode update", notes = "This API is used to add cart")	
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful updation of the shipping mode details to the cart item"),
			@ApiResponse(code = 500, message = "Internal cart error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String updateShippingMode( @RequestBody UpdateShippingRequest updateShippingRequest) {
		
		LOGGER.debug("Updating the Shipping Mode");
		return cartService.bopisUpdateShippingMode(updateShippingRequest);
	}

}
