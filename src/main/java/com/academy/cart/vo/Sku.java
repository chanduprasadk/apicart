/**
 * 
 */
package com.academy.cart.vo;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author sboinp
 *
 *         This class contains sku level info.
 */

public class Sku {

	@NotBlank
	private String id;

	@Min(1)
	private long quantity;
	private String type;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the quantity
	 */
	public long getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
