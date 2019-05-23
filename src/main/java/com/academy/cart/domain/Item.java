package com.academy.cart.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class Item {
	
	private String productId;
	
	private String quantity;
	
	private String shipModeId;
	
	@JsonProperty
	private boolean isGiftItem; 
	
	private GiftItem giftItemInfo;
	
	@JsonProperty
	private boolean isPickInStore; 
	
	private List<BopisItem> storeInfo;
	
	@JsonProperty
	private boolean isBundle;
	
	private List<BundleItem> bundleInfo;
	

}
