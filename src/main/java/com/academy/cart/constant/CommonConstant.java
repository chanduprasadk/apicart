package com.academy.cart.constant;

/**
 * The Enum CommonConstant. /**
 * 
 * @author Sapient This constant file keeps special chars etc.
 *
 */
public enum CommonConstant {

	/** The equals. */
	EQUALS("="),

	/** The ampersand. */
	AMPERSAND("&"),

	/** The comma. */
	COMMA(","),

	/** The underscore. */
	UNDERSCORE("_"),

	/** The questionmark. */
	QUESTIONMARK("?"),

	/** The blank. */
	BLANK(""),
	NULL("null"),
	IS_NULL("null"),
	SEMICOLON(";"),
	AMP("amp;"),
	CALCULATEORDER("1"),
	CALCULATIONUSAGE("-1,-2,-7"),
	STORE_IDENTIFIRE("STOREIDENTIFIER"),
	BUNDLE_IDENTIFIRE("BundleId"),
	STORE_IDENTIFIRE_TYPE("String"),
	BUNDLE_IDENTIFIRE_TYPE("String"),
	CHANGED_ZIP_CODE("changedZipCode"),
	PROFILE_ZIP_CODE("zipCode"),
	HEADER_ZIP_CODE("zipCode"),
	DELIVERY_ZIP_CODE("deliveryZipCode"),
	STORE_ZIP_CODE("storeZipCode"),
	REGISTERED_USER_KEY("USERTYPE"),
	REGISTERED_USER_VALUE("R"),
	USER_DEFAULT_ADDRESS_IDENTIFIER("defaultAddress"),
	REGISTERED_USER_PROFILE_ID("WC_AUTHENTICATION"),
	WHILE_GLOVE_SHIPPING_TYPE("whiteGlove"),
	DROP_SHIP_SHIPPING_TYPE("dropShip"),
	SPECIAL_ORDER_SHIPPING_TYPE("specialOrder"),
	HAZMAT_SHIPPING_TYPE("hazmat"),
	HOT_MARKET_SHIPPING_MESSAGE("hotMarketShippingMessage"),
	ASSEMBLY_REQUIRED_MESSAGE("mayRequirehoursForAssembly"),
	QUANTITY_LIMIT("quantityLimit"),
	AMMUNITION_MESSAGE("AMMUNITION_MESSAGE"),
	IS_BOPIS_STORE("isBopisStore");
	

	private final String name;

	/**
	 * Instantiates a new common constant.
	 *
	 * @param name
	 *            the name
	 */
	private CommonConstant(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
