package com.academy.cart.constant;

/**
 * The Enum CartRequestConstant.
 */
/**
 * 
 * @author Sapient
 * 
 *         This file contains constants which get used while creating get wcs
 *         get URL.
 *
 */

public enum CartRequestConstant {
	
	/** The store id. */
	STORE_ID("storeId"),

	/** The catalog id. */
	CATALOG_ID("catalogId"),

	/** The lang id. */
	LANG_ID("langId"),

	/** The order id. */
	ORDER_ID("orderId"),

	/** The calculation usage. */
	CALCULATION_USAGE("calculationUsage"),

	/** The inventory validation. */
	INVENTORY_VALIDATION("inventoryValidation"),

	/** The bundle. */
	BUNDLE("isBundle"),

	/** The gc item. */
	GC_ITEM("isGCItem_1"),

	/** The cart enrty id. */
	MULTISKU("isMultiSku"),
	CART_ENRTY_ID("catEntryId"),

	/** The quantity. */
	QUANTITY("quantity"),

	/** The item comment. */
	ITEM_COMMENT("itemComment"),

	/** The gift amount. */
	ITEM_DETAILS("itemDetails"),
	
	CALCULATE_ORDER("calculateOrder"),

	GIFT_AMOUNT("giftAmount_1"),

	/** The profile. */
	PROFILE("profile"),
	
	BUNDLE_ID("bundleId"),
	
	SOF_IDENTIFIER("selectedStoreId"),
	
	PICK_UP_STORE("isPickUpInStore");

	/** The url param name. */
	private final String urlParamName;

	/**
	 * Instantiates a new cart request constant.
	 *
	 * @param urlParamName
	 *            the url param name Constructor
	 * @param urlParamName
	 */
	private CartRequestConstant(String urlParamName) {
		this.urlParamName = urlParamName;
	}

	/**
	 * Gets the url param name.
	 *
	 * @return the url param name This method returns name of respective constant.
	 * 
	 * @return urlParamName
	 */
	public String getUrlParamName() {
		return urlParamName;
	}
}