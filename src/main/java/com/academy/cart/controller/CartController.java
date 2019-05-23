package com.academy.cart.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.cart.service.CartAggregationService;
import com.academy.cart.service.CartService;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.InitiateRequest;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class CartController.
 *
 * @author dmuru1
 * 
 *         Controller class for cart service
 */

@RequestMapping("/api")
@RestController
public class CartController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);
	
	@Value("${defaultStore}")
	private String defaultStore;

	/** The cart service. */
	@Autowired
	private CartService cartService;
	
	/** The aggregation service. */
	@Autowired
	private CartAggregationService aggregationService;

	/**
	 * Gets the cart details.
	 *
	 * @return the cart details
	 */
	@RequestMapping(value = "/cart", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get Cart Details", notes = "This API is used to fetch the details of the cart and the cart items")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful insertion of new Product details to the table"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String getCartDetails() {

		LOGGER.debug("Entry getCartDetails");

		return cartService.getCartDetails("cart");
	}
	

	/**
	 * This method used to fetch the details of the minicart and the minicart items
	 * 
	 * @param cartId
	 * @return
	 */
	@RequestMapping(value = "/cart/{cartId}/summary", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get MiniCart Details", notes = "This API is used to fetch the details of the minicart and the minicart items")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful insertion of new Product details to the table"),
			@ApiResponse(code = 500, message = "Internal minicartr error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public ResponseEntity<String> getMiniCartDetails(
			@ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId) {

		LOGGER.debug("Entry getMiniCartDetails");

		return cartService.getMiniCartDetails(cartId);
	}
	
	
		/**
	 * This method is responsible to add new cart to order.
	 * 
	 * @param cartRequest
	 * @param httpServletResponse
	 * @return
	 */
	@RequestMapping(value = "/cart/sku", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "Add cart", notes = "This API is used to add cart")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful insertion of new cart details to the order"),
			@ApiResponse(code = 500, message = "Internal cart error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String addCart(@Validated @RequestBody CartRequest cartRequest, HttpServletResponse httpServletResponse) {

		LOGGER.debug("Entry addCart");

		return cartService.addCart(cartRequest, httpServletResponse);
	}

	/**
	 * This method is calling add to cart service internally, in future there will
	 * be different implementation.
	 * 
	 * @param cartRequest
	 * @param httpServletResponse
	 * @return
	 */
	@RequestMapping(value = "/cart/PUT/sku", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "update cart", notes = "This API is used to update cart")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful updation of cart details to the order"),
			@ApiResponse(code = 500, message = "Internal cart error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String updateCart(@Validated @RequestBody CartRequest cartRequest, HttpServletResponse httpServletResponse) {

		LOGGER.debug("Entry updateCart");

		return cartService.addCart(cartRequest, httpServletResponse);
	}
	

	/**
	 * View cart.
	 *
	 * @param storeId the store id
	 * @param deliveryZipCode the delivery zip code
	 * @param storeZipCode the store zip code
	 * @return the string
	 */
	@RequestMapping(value="/cart/{cartId}", method = RequestMethod.GET, produces = "application/json")

	@ApiOperation(value = "View Cart", notes = "This API is used to fetch the details of the cart , product")	
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful received the response from underlying service"),
			@ApiResponse(code = 500, message = "Internal minicartr error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String viewCart(@ApiParam(value = "queryParams", required = true) @RequestParam Map<String, String> queryParams, @RequestHeader HttpHeaders headers) {
		LOGGER.debug("Entry viewCart with query params {} and headers length {} ", queryParams , headers != null ? headers.getContentLength() : 0);
		if(null == queryParams) {
			queryParams = new HashMap<>();
		}
		return aggregationService.fetchCart(defaultStore , queryParams, headers);
	}
	
	/**
	 * <p>
	 * Method that would invoke the WCS layer and fetch the getavailableshippingmethods.
	 * </p>
	 * 
	 * @param profile
	 * @param orderId
	 * @return Json with the available shipping methods
	 */
	@RequestMapping(value= {"/cart/{cartId}/getAvailableShippingMethods/","/cart/{cartId}/shippingMethod"}, method = RequestMethod.GET, produces = "application/json")

	@ApiOperation(value = "get Available shipping methods", notes = "This API is used to get the available shipping methods for the particular item")	
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful ritreival of the availableshippingmethods for the user"),
			@ApiResponse(code = 500, message = "Internal shippingmethods error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String getAvailableShippingMethods( @ApiParam(value = "profile", required = true) @RequestParam("profile") String profile,@ApiParam(value = "orderId", required = true) @RequestParam("orderId") String orderId,@ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId) {

		LOGGER.debug("Entry getavailableshippingmethods");

		return cartService.getAvailableShippingmethods(profile, orderId);
		
		
	}
	
	@RequestMapping(value = {"/cart/PUT/{cartId}/updateItemQuantity","/cart/{cartId}"}, method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "update items quantity", notes = "This API is used to update the items quantity")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Successful updation of items quantity"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 404, message = "Items that you are looking for not available") })
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.OK)
	public String updateItemsQuantity(@ApiParam(value = "storeId", required = true) @RequestParam(defaultValue="10151", name="storeId") String storeId,
			                       @ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			                       @Validated @RequestBody UpdateQuantityRequest quantityRequest) {
		
		return cartService.updateItemQuantity(quantityRequest);
	}
	
	@RequestMapping(value = {"/cart/PUT/{cartId}/initiate","/cart/{cartId}/initiate"}, method = RequestMethod.POST)
	@ApiOperation(value = "initiate the checkout", notes = "This API is used to initiate the checkout")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Order successfully placed"),
			@ApiResponse(code = 400, message = "Bad Request. Input invalid"),
			@ApiResponse(code = 401, message = "Not authenticated. User session invalid"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 404, message = "request resource not found"),
			@ApiResponse(code = 500, message = "Internal server error") })
	@ApiParam(value = "storeId", required = false)
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void initiateCheckout(@RequestParam(defaultValue="10151", name="storeId") String storeId,  @ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			                     @Validated @RequestBody InitiateRequest initiateRequest) {
		
		cartService.initiateCheckout(initiateRequest);
	}	
	
	
	/**
	 * View cart.
	 *
	 * @param storeId the store id
	 * @param deliveryZipCode the delivery zip code
	 * @param storeZipCode the store zip code
	 * @return the string
	 */
	@RequestMapping(value="/carts/{cartId}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "View Cart", notes = "This API is used to fetch the details of the cart , product")	
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful received the response from underlying service"),
			@ApiResponse(code = 500, message = "Internal minicartr error"),
			@ApiResponse(code = 404, message = "Error while retrieving the data") })
	@ExceptionMetered
	@Timed
	public String getCart() {
		LOGGER.info("Enter CartController.getCart(");
		Map<String, String> queryParams = new HashMap<>();
		LOGGER.info("Exit CartController.getCart(");
		return aggregationService.fetchCart(defaultStore, queryParams , null);
	}
	
}


