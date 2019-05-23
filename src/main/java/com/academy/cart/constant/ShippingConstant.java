package com.academy.cart.constant;

public enum ShippingConstant {

	
	ORDER_ID("orderId"),
	PROFILE("profile");

	private final String urlParamName;

	private ShippingConstant(String urlParamName) {
		this.urlParamName = urlParamName;
	}

	public String getUrlParamName() {
		return urlParamName;
	}
}