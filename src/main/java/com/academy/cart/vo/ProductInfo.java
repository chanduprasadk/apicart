package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfo {

	private String id;
	private String name;
	private String shortDescription;
	private String longDescription;
	private boolean sellable;
	private boolean isSingleSkuProduct;
	private String partNumber;
	private String parentCategoryURL;
	private String fullImage;
	private String thumbnail;
	private String imageAltDescription;
	private List<ProductAttribute> productAttributes;
	private String catEntryId;
	private String disclaimerMessage;
	private String seoURL;
	private String isBuyNowEligible;
	private String isGiftCard;
	private String categoryName;
	private String manufacturer;
	private String parentCatalogEntryID;
	private boolean sofItem;
	private boolean bopis;
	private List<String> adBug;
	private String parentSkuId;
	private List<ProductMessage> productMessage;
	
	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ProductMessage {
		
		private String key;
		private String value;
	}
	
}

