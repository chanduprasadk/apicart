package com.academy.cart.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateQuantityRequest {
	
	@JsonProperty("orderItem")
	private List<OrderQuantity> orderItems;
	
	// WCS required these fields to update the order total when quantity gets updated
	@JsonProperty("x_calculateOrder")
	private String calculateOrder = "1";
	@JsonProperty("x_calculationUsage")
	private String calculationUsage = "-1,-7";
	@JsonProperty("x_isCartUpdate")
	private String isCartUpdate = "true";
	
	private String x_isResetShipChargeCart="true";
	
	public void addOrderItem(OrderQuantity item) {
		orderItems = new ArrayList<>();
		orderItems.add(item);
	}

	@Data
	@NoArgsConstructor
	public static class OrderQuantity {
		
		private String orderItemId;
		private String quantity;
	}
	
}
