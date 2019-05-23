package com.academy.cart.vo;

import java.util.ArrayList;
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
public class CartDetails {
	
	private int recordSetTotal;
	private String totalSalesTaxCurrency;
	@JsonInclude(value = Include.NON_NULL)
    private String totalAdjustment;
    private String totalShippingTax = "0";
    private String totalProductPrice = "0";
    private String grandTotalCurrency;
	private String orderId;
	private String totalShippingTaxCurrency;
	private int recordSetStartNumber;
	private String totalShippingChargeCurrency;
	private String grandTotal = "0";
	private String orderStatus;
	private String totalProductPriceCurrency;
	private String totalAdjustmentCurrency;
	private List<CartItem> orderItem;
	private PromoDetails promoDetails;
	private int langId;
	private int catalogId;
	private String totalEstimatedTax = "0";
	private String totalEstimatedShippingCharge = "0";
	private boolean hasSOFItems;
	private boolean hasAgeRestrictedItems;
	private List<CartItem> bundleProductDetails;
	private String storeId;
	@JsonInclude(value = Include.NON_NULL)
	private String shipToStoreCharge;
	private String standardShipToStoreCharge = "0";
	private String zipCode;
	private List<ItemAdjustment> adjustment = new ArrayList<>();
	
	@JsonInclude(value = Include.NON_NULL)
	private String employeeDiscount;
	
	private boolean taxCallSuccess;
	private boolean inventoryCallSuccess;
	@JsonProperty("isSTSStoreDisabled")
	private String isSTSStoreDisabled;

}
