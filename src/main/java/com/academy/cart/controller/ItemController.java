package com.academy.cart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.cart.domain.Cart;
import com.academy.cart.service.ItemService;
import com.academy.cart.vo.ItemRequest;
import com.academy.cart.vo.UpdateCartRequest;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/***
 * 
 * @author Yogi
 */
	
@RestController
@RequestMapping("/api")
public class ItemController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);
	
	@Autowired
	private ItemService itemService;
	
	@RequestMapping(value = "/cart/DELETE/{cartId}/items/{itemId}", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Item", notes = "This API is used to delete the item")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful deletion of item"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 404, message = "Items that you are looking for not available") })
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteItem(@ApiParam(value = "storeId", required = true) @RequestParam(defaultValue="10151", name="storeId") String storeId,
			                       @ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			                       @ApiParam(value = "itemId", required = true) @PathVariable("itemId") String itemId) {
		LOGGER.info("Entering ItemController deleteItem cart id {} and item id {} and store id {}", cartId, itemId, storeId);
		itemService.deleteItem(cartId, itemId);
		LOGGER.info("Exiting ItemController deleteItem");
	}
	
	@RequestMapping(value = "/cart/PUT/{cartId}/items/{itemId}", method = RequestMethod.POST)
	@ApiOperation(value = "Update Item", notes = "This API is used to update the item")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully updated the item"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 404, message = "Items that you are looking for not available") })
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.OK)
	public void updateItem(@ApiParam(value = "storeId", required = true) @RequestParam(defaultValue="10151", name="storeId") String storeId,
			                       @ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			                       @ApiParam(value = "itemId", required = true) @PathVariable("itemId") String itemId, 
			                       @Validated @RequestBody ItemRequest itemRequest) {
		LOGGER.info("Entering ItemController updateItem cart id {} and item id {} and store id {}", cartId, itemId, storeId);
		itemService.updateItem(cartId, itemId, itemRequest);
		LOGGER.info("Exiting ItemController updateItem");
	}
	
	
	@RequestMapping(value = "/cart/PUT/{cartId}/updateOrderItem", method = RequestMethod.POST)
	@ApiOperation(value = "update items quantity or add new item", notes = "This API is used to update the items quantity")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Successful updation of items quantity"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 404, message = "Items that you are looking for not available") })
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.OK)
	public void addItemOrUpdateQuantity(
			@RequestParam(defaultValue = "10151") String storeId,
			@ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			@Validated @RequestBody UpdateCartRequest updateCartRequest) {

		itemService.updateOrderItem(updateCartRequest, storeId);
	}
	

	@RequestMapping(value = "/cart/{cartId}/items", method = RequestMethod.POST)
	@ApiOperation(value = "Add Items", notes = "This API is used to add the item(s) in the cart")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully added the item(s)"),
			@ApiResponse(code = 500, message = "Internal server error"),
			@ApiResponse(code = 400, message = "Request is invalid"),
	        @ApiResponse(code = 404, message = "Cart does not exist") })
	@ExceptionMetered
	@Timed
	@ResponseStatus(value = HttpStatus.CREATED)
	public String addItems(@ApiParam(value = "storeId", required = true) @RequestParam(defaultValue="10151", name="storeId") String storeId,
			                       @ApiParam(value = "cartId", required = true) @PathVariable("cartId") String cartId,
			                       @Validated @RequestBody Cart cart) {
		LOGGER.info("Entering ItemController addItems cart id {} and store id {}", cartId, storeId);
		return itemService.addItems(cartId, cart);
	}

}
