package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetails {

	private ProductInfo productinfo;

	private InventoryDetails inventory = new InventoryDetails();

}
