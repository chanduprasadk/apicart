package com.academy.cart.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class Cart {

	private String cartId;
	private List<Item> items;
	private boolean inventoryCheck = true;
	private String comment;
	private String calculateOrder = "-1";
	private boolean itemDetails = true;
	
}
