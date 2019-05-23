package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class WCSItemAttribute {
	
	private String attributeName;
	
	private String attributeValue;
	
	private String attributeType;
}
