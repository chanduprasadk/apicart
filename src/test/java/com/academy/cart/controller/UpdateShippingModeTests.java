package com.academy.cart.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.academy.cart.service.CartService;
import com.academy.cart.vo.UpdateShippingRequest;
import com.academy.cart.vo.UpdateShippingRequest.OrderItemExtendAttributes;
import com.academy.cart.vo.UpdateShippingRequest.OrderItems;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateShippingModeTests {

	@InjectMocks
	UpdateShippingModeController updateShippingModeController;

	@Mock
	CartService cartService;

	private MockMvc mockMvc;
	private UpdateShippingRequest updateShippingRequest;
	private String requestJson;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(updateShippingModeController).build();
		updateShippingRequest = new UpdateShippingRequest();
		updateShippingRequest.setShipAsComplete(true);
		updateShippingRequest.setX_calculationUsage("-1,-2,-3,-4,-5,-6,-7");
		List<OrderItems> orderItem = new ArrayList<>();
		OrderItems orderItems = new OrderItems();
		orderItems.setOrderItemId("12345");
		List<OrderItemExtendAttributes> orderItemExtendAttribute = new ArrayList<>();
		OrderItemExtendAttributes orderItemExtendAttributes=new OrderItemExtendAttributes();
		orderItemExtendAttributes.setAttributeName("PHYSICAL_STORE_ID");
		orderItemExtendAttributes.setAttributeType("STRING");
		orderItemExtendAttributes.setAttributeValue("12009");
		orderItemExtendAttribute.add(orderItemExtendAttributes);
		orderItems.setShipModeId("shipmode12345");
		orderItem.add(orderItems);
		updateShippingRequest.setOrderItem(orderItem);
		ObjectMapper mapper = new ObjectMapper();
		try {
			requestJson = mapper.writeValueAsString(updateShippingRequest);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	

	@Test
	public void updateShippingModeTest() throws Exception {
		updateShippingRequest = new UpdateShippingRequest();
		updateShippingRequest.setShipAsComplete(true);
		updateShippingRequest.setX_calculationUsage("-1,-2,-3,-4,-5,-6,-7");
		List<OrderItems> orderItem = new ArrayList<>();
		OrderItems orderItems = new OrderItems();
		orderItems.setOrderItemId("12345");
		List<OrderItemExtendAttributes> orderItemExtendAttribute = new ArrayList<>();
		OrderItemExtendAttributes orderItemExtendAttributes=new OrderItemExtendAttributes();
		orderItemExtendAttributes.setAttributeName("PHYSICAL_STORE_ID");
		orderItemExtendAttributes.setAttributeType("STRING");
		orderItemExtendAttributes.setAttributeValue("12009");
		orderItemExtendAttribute.add(orderItemExtendAttributes);
		orderItems.setShipModeId("shipmode12345");
		orderItem.add(orderItems);
		updateShippingRequest.setOrderItem(orderItem);
		when(cartService.bopisUpdateShippingMode(updateShippingRequest)).thenReturn(requestJson);
		String response = updateShippingModeController.updateShippingMode(updateShippingRequest);
		assertNotNull(response);

	}
	
		
}
