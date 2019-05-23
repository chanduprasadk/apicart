/**
 * 
 */
package com.academy.cart.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author sboinp
 *
 *         This class keep info about order.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class OrderItem {

	private String orderItemId;
	private String productId;
	private String quantity;
	private String xitem_shipModeId;
	
	@JsonInclude(Include.NON_EMPTY)
	private String xitem_selectedStoreId;
	

	public String getXitem_selectedStoreId() {
		return xitem_selectedStoreId;
	}

	public void setXitem_selectedStoreId(String xitem_selectedStoreId) {
		this.xitem_selectedStoreId = xitem_selectedStoreId;
	}

	/**
	 * @return the orderItemId
	 */
	public String getOrderItemId() {
		return orderItemId;
	}

	/**
	 * @param orderItemId the orderItemId to set
	 */
	public void setOrderItemId(String orderItemId) {
		this.orderItemId = orderItemId;
	}
	
	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * @param productId
	 *            the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the xitem_shipModeId
	 */
	public String getXitem_shipModeId() {
		return xitem_shipModeId;
	}

	/**
	 * @param xitem_shipModeId the xitem_shipModeId to set
	 */
	public void setXitem_shipModeId(String xitem_shipModeId) {
		this.xitem_shipModeId = xitem_shipModeId;
	}

}
