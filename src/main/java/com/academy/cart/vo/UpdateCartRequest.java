/**
 * 
 */
package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author Agil
 *
 */
@Data
public class UpdateCartRequest {
	
	@JsonProperty("orderItem")
	private List<OrderItem> orderItems;
	
	private String x_calculateOrder;
	
	private String x_calculationUsage;
	
	@JsonProperty("x_isCartUpdate")
	private String isCartUpdate = "true";

}
