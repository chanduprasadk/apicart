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
public class WCSItemRequest {

	private String productId;

	private String quantity;

	@JsonProperty("xitem_shipModeId")
	private String shipModeId;
	
	@JsonProperty("xitem_isGCItem")
	private boolean giftItemFlag;
	
	@JsonProperty("xitem_giftAmount")
	private double giftAmount;
	
	private String comment;
	
	@JsonProperty("xitem_isPickupInStore")
	private boolean storePickupFlag; 
	
	private List<WCSItemAttribute> orderItemExtendAttribute;
	
}
