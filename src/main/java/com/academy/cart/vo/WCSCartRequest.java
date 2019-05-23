package com.academy.cart.vo;

import java.util.List;

/**
 * 
 * @author Sapient
 * This class keep WCS cart request info.
 *
 */
public class WCSCartRequest {

	String orderId;
	int xCalculateOrder;
	boolean xInventoryValidation;
	List<OrderItem> orderItem;
	int langId;
	int catalogId;
	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}
	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	/**
	 * @return the xCalculateOrder
	 */
	public int getxCalculateOrder() {
		return xCalculateOrder;
	}
	/**
	 * @param xCalculateOrder the xCalculateOrder to set
	 */
	public void setxCalculateOrder(int xCalculateOrder) {
		this.xCalculateOrder = xCalculateOrder;
	}
	/**
	 * @return the xInventoryValidation
	 */
	public boolean isxInventoryValidation() {
		return xInventoryValidation;
	}
	/**
	 * @param xInventoryValidation the xInventoryValidation to set
	 */
	public void setxInventoryValidation(boolean xInventoryValidation) {
		this.xInventoryValidation = xInventoryValidation;
	}
	/**
	 * @return the orderItem
	 */
	public List<OrderItem> getOrderItem() {
		return orderItem;
	}
	/**
	 * @param orderItem the orderItem to set
	 */
	public void setOrderItem(List<OrderItem> orderItem) {
		this.orderItem = orderItem;
	}
	/**
	 * @return the langId
	 */
	public int getLangId() {
		return langId;
	}
	/**
	 * @param langId the langId to set
	 */
	public void setLangId(int langId) {
		this.langId = langId;
	}
	/**
	 * @return the catalogId
	 */
	public int getCatalogId() {
		return catalogId;
	}
	/**
	 * @param catalogId the catalogId to set
	 */
	public void setCatalogId(int catalogId) {
		this.catalogId = catalogId;
	}
	
}
