package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemAdjustment {
	
	private String amount = "0" ;
	private String displayLevel;
	private String usage;
	private String code;
	private String currency;
	private String description;
	private static String hideMessage = "Free Shipping on orders over $10000";

	public String getDescription() {
		if (hideMessage.equalsIgnoreCase(description)) {
			return "";
		}
		return description;
	}

}
