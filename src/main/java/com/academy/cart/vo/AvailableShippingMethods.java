package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableShippingMethods {

	private String shippingType;
	
	private String shipmodeDes;
	
	private String estimatedFromDate;
	
	private String estimatedToDate;
	
	private String shipmodeId;
	
	@JsonInclude(value = Include.NON_NULL)
	private String estimatedTime;

}
