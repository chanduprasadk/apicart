package com.academy.cart.vo;




import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;



//UpdateShippingmode
@Data


public class UpdateShippingRequest {

	private List<OrderItems> orderItem;	
    private String x_calculationUsage;	
	private boolean shipAsComplete;
	
	@JsonProperty("x_updateShipMode")
	private String updateShipMode = "true";

	
	@Data
	public static class OrderItems {
        private String orderItemId;    
		private String shipModeId;	    
		private List<OrderItemExtendAttributes> orderItemExtendAttribute;
	}	
	
	@Data
	public static class OrderItemExtendAttributes {
        private String attributeName;    
		private String attributeValue;	    
		private String attributeType;
	}
	    
		
	//}
		
	    
}