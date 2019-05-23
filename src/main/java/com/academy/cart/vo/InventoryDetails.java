package com.academy.cart.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryDetails {
	private List<InventorySku> store = new ArrayList<>();
	
	private List<InventorySku> online = new ArrayList<>();
	
	
	public static InventoryDetails populateDefaultInventory(String storeId, boolean isStorePickUp) {
		InventoryDetails inventoryDetails = new InventoryDetails();
		InventorySku inventorySku = new InventorySku();
		inventorySku.setAvailableQuantity("100");
		inventorySku.setInventoryStatus("AVAILABLE");
		inventoryDetails.getOnline().add(inventorySku);
		if(null != storeId && isStorePickUp) {
			inventorySku = new InventorySku();
			inventorySku.setAvailableQuantity("100");
			inventorySku.setInventoryStatus("AVAILABLE");
			inventoryDetails.getStore().add(inventorySku);
		}
		return inventoryDetails;
	}

}
