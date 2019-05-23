package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryRequestWrapper {
	
	@JsonInclude(value = Include.NON_NULL)
	private OnlineInventoryRequest onlineskus;
	
	@JsonInclude(value = Include.NON_NULL)
	private PickUpInventoryRequest pickupskus;
	
	@JsonInclude(value = Include.NON_NULL)
	private List<BundleInventoryRequest> bundleskus;

}
