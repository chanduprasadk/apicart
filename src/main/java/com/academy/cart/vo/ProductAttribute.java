package com.academy.cart.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAttribute {

	private String storeDisplay;
	private String groupName;
	private String uniqueID;
	private String sequence;
	private String name;
	private String usage;
	private String identifier;
	private List<AttributeValue> values;

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AttributeValue {

		private String value;
		private String uniqueID;
		private String identifier;
		private String sequence;
	}

}
