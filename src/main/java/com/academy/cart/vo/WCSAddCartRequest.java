package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class WCSAddCartRequest {
	
	@JsonProperty("orderId")
	private String cartId;
	
	@JsonProperty("orderItem")
	private List<WCSItemRequest> items;
	
	@JsonProperty("x_inventoryValidation")
	private boolean inventoryCheck;
	
	private String comment;
	
	@JsonProperty("x_calculateOrder")
	private String calculateOrder;
	
	private boolean itemDetails;
	
	@JsonProperty("x_isBundle")
	private boolean bundleFlag;

}
