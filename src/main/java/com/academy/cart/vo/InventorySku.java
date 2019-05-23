package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventorySku {
	
	private String skuId;
	private int requestedQuantity;
	
	@JsonInclude(value = Include.NON_NULL)
	private String availableQuantity;
	
	@JsonInclude(value = Include.NON_NULL)
	private String inventoryStatus;
	
	@JsonInclude(value = Include.NON_NULL)
	private String availabilityDate;
	
	@JsonInclude(value = Include.NON_NULL)
	private String storeId;
	

}
