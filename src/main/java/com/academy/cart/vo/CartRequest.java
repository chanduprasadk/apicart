package com.academy.cart.vo;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Sapient. 
 * This class contains cart request level info.
 */
public class CartRequest {

	@Valid
	private List<Sku> skus;
	private double giftAmount;
	private boolean inventoryCheck;
	private List<Integer> calculationUsages;
	@JsonProperty
	private boolean isBundle;
	@JsonProperty
	private boolean isMultiSku;
	@JsonProperty
	private boolean isGCItem;
	private String itemComment;
	@JsonProperty
	private boolean isItemDetails;
	private String bundleId;
	private String selectedStoreId;
	@JsonProperty
	private boolean isPickUpInStore;

	/**
	 * @return the skus
	 */
	public List<Sku> getSkus() {
		return skus;
	}

	/**
	 * @param skus
	 *            the skus to set
	 */
	public void setSkus(List<Sku> skus) {
		this.skus = skus;
	}

	/**
	 * @return the giftAmount
	 */
	public double getGiftAmount() {
		return giftAmount;
	}

	/**
	 * @param giftAmount
	 *            the giftAmount to set
	 */
	public void setGiftAmount(double giftAmount) {
		this.giftAmount = giftAmount;
	}

	/**
	 * @return the inventoryCheck
	 */
	public boolean isInventoryCheck() {
		return inventoryCheck;
	}

	/**
	 * @param inventoryCheck
	 *            the inventoryCheck to set
	 */
	public void setInventoryCheck(boolean inventoryCheck) {
		this.inventoryCheck = inventoryCheck;
	}

	/**
	 * @return the calculationUsages
	 */
	public List<Integer> getCalculationUsages() {
		if (calculationUsages == null || calculationUsages.isEmpty()) {
			List<Integer> usages = new ArrayList<>();
			usages.add(-1);
			calculationUsages = usages;
		}
		return calculationUsages;
	}

	/**
	 * @param calculationUsages
	 *            the calculationUsages to set
	 */
	public void setCalculationUsages(List<Integer> calculationUsages) {
		this.calculationUsages = calculationUsages;
	}

	/**
	 * @return the isBundle
	 */
	public boolean isBundle() {
		return isBundle;
	}

	/**
	 * @param isBundle
	 *            the isBundle to set
	 */
	public void setBundle(boolean isBundle) {
		this.isBundle = isBundle;
	}

	/**
	 * 
	 * @return the isMultiSku
	 */
	public boolean isMultiSku() {
		return isMultiSku;
	}
	/**
	 * @param isMultiSku
	 *    the isMultiSku to set
	 */
	public void setMultiSku(boolean isMultiSku) {
		this.isMultiSku = isMultiSku;
	}

	/**
	 * @return the isGCItem
	 */
	public boolean isGCItem() {
		return isGCItem;
	}

	/**
	 * @param isGCItem
	 *            the isGCItem to set
	 */
	public void setGCItem(boolean isGCItem) {
		this.isGCItem = isGCItem;
	}

	/**
	 * @return the itemComment
	 */
	public String getItemComment() {
		return itemComment;
	}

	/**
	 * @param itemComment
	 *            the itemComment to set
	 */
	public void setItemComment(String itemComment) {
		this.itemComment = itemComment;
	}

	public boolean isItemDetails() {
		return isItemDetails;
	}

	public void setItemDetails(boolean isItemDetails) {
		this.isItemDetails = isItemDetails;
	}

	public String getBundleId() {
		return bundleId;
	}

	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}

	public String getSelectedStoreId() {
		selectedStoreId = StringUtils.stripStart(selectedStoreId, "0");
		if(null != selectedStoreId && selectedStoreId.isEmpty()) {
			selectedStoreId = "0";
        }
		return selectedStoreId;
	}

	public void setSelectedStoreId(String selectedStoreId) {
		this.selectedStoreId = selectedStoreId;
	}

	public boolean isPickUpInStore() {
		return isPickUpInStore;
	}

	public void setPickUpInStore(boolean isPickUpInStore) {
		this.isPickUpInStore = isPickUpInStore;
	}

}
