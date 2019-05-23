package com.academy.cart.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {
	
	private String unitPrice = "0";
	private String orderItemInventoryStatus;
	private String zipCode;
	private double quantity;
	private String shipModeCode;
	private String shipModeId;
	private String orderItemId;
	private List<UsableShippingChargePolicy> usableShippingChargePolicy;
	private String currency;
	private String orderItemPrice = "0";
	private String orderItemDiscountedPrice = "0";
	private String correlationGroup;
	private String partNumber;
	private boolean freeGift;
	@JsonProperty("xitem_isPersonalAddressesAllowedForShipping")
	private boolean isPersonalAddressesAllowedForShipping;
	private String productId;
	private String offerID;
	private ProductDetails productDetails = new ProductDetails();
	private List<AvailableShippingMethods> availableShippingMethods;
	private List<ItemAdjustment> totalAdjustment;
	private String city;
	private String postalCode;
	private String state;
	private String stateOrProvinceName;
	private List<String> storeAddress = new ArrayList<>();
	@JsonProperty("isBundleItem")
	private boolean isBundleItem;
	List<WCSItemAttribute> orderItemExtendAttribute;
	List<WCSItemAttribute> bundleInfo = new ArrayList<>();
	
	private List<BundleItemProperties> bundleOrderItems = new ArrayList<>();
	
	@JsonProperty("isSOFItem")
	private boolean isSOFItem;
	
	private String freeGiftParentId;
	
	private String bundleId;
	
	@JsonProperty("isWhileGlove")
	private String isWhileGlove;
	
	@JsonProperty("isDropShip")
	private String isDropShip;
	
	@JsonProperty("isSpecialOrder")
	private String isSpecialOrder;
	
	@JsonProperty("isHazmat")
	private String isHazmat;
	
	@JsonProperty("isHotMarketEnabled")
	private String isHotMarketEnabled;
	
	@JsonProperty("isAssemblyRequired")
	private String isAssemblyRequired;
	
	@JsonProperty("quantityLimit")
	private String quantityLimit;
	
	@JsonProperty("isGCItem")
	private String isGCItem;
	
	@JsonProperty("isBulkGCItem")
	private String isBulkGCItem;
	
	@JsonProperty("giftCartMaxQty")
	private String giftCartMaxQty;
	
	@JsonProperty("bulkGiftCartMinQty")
	private String bulkGiftCartMinQty;
	
	@JsonProperty("isBopisStoreExclude")
	private String isBopisStoreExclude;
	
	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BundleItemProperties {
		
		private String orderItemId;
	}
	
	
	

}
