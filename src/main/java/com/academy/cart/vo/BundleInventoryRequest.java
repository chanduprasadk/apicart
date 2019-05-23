package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleInventoryRequest {
	
	private List<InventorySku> skus;	

	@JsonProperty("skuId")
	private String bundleId;
	
	private String inventorySource;
	
	@JsonInclude(value = Include.NON_NULL)
	private String storeId;

}
