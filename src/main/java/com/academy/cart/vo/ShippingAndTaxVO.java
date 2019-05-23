package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingAndTaxVO {

	private String totalShippingCharge;
	private String orderGrandTotal;
	private String totalTax;
	private String shipToStoreCharge;
	private String totalAdjustment;
	private String totalProductPrice;
	private String totalEmployeeDiscount;

}
