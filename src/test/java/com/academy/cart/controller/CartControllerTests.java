package com.academy.cart.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.academy.cart.service.CartAggregationService;
import com.academy.cart.service.CartService;
import com.academy.cart.vo.CartRequest;
import com.academy.cart.vo.InitiateRequest;
import com.academy.cart.vo.Sku;
import com.academy.cart.vo.UpdateQuantityRequest;
import com.academy.cart.vo.UpdateQuantityRequest.OrderQuantity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Sapient
 * Unit test for controller class.
 *
 */
public class CartControllerTests {

	@InjectMocks
	CartController cartController;

	@Mock
	CartService cartService;

	@Mock
	HttpServletResponse httpServletResponse;
	
	@InjectMocks
	ObjectMapper jsonMapper;
	
	MockMvc mockMvc;
	CartRequest cartRequest;
	String requestJson;
	
	@Mock
	CartAggregationService cartAggregationService;

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
		cartRequest = new CartRequest();
		cartRequest.setGiftAmount(210.0);
		cartRequest.setInventoryCheck(true);
		List<Sku> skus = new ArrayList<>();
		Sku sku = new Sku();
		sku.setId("1111");
		sku.setQuantity(1);
		skus.add(sku);
		cartRequest.setSkus(skus);
		ObjectMapper mapper = new ObjectMapper();
		try {
			requestJson = mapper.writeValueAsString(cartRequest);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getMiniCartDetailsTest() throws Exception {
		String cartId = "15969";

		mockMvc.perform(get("/api/cart/{cartId}/summary", cartId)).andExpect(status().isOk());
	}

	@Test
	public void getCartDetailsTest() throws Exception {

		mockMvc.perform(get("/api/cart")).andExpect(status().isOk());
	}

	@Test
	public void addCartTest() throws Exception {
		
		when(cartService.addCart(cartRequest, httpServletResponse)).thenReturn(requestJson);
		String response = cartController.addCart(cartRequest, httpServletResponse);
		assertNotNull(response);

	}

	@Test
	public void addCartIntegrationTest() throws Exception {

		mockMvc.perform(post("/api/cart/sku").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());

	}
	
	@Test
	public void updateCartIntegrationTest() throws Exception {

		mockMvc.perform(post("/api/cart/PUT/sku").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk());

	}
	
	@Test
	public void viewCart() throws Exception {
		String cartId = "550072034";

		mockMvc.perform(get("/api/cart/{cartId}?deliveryZipCode=77449&storeZipCode=72201", cartId)).andExpect(status().isOk());
	}

	
	@Test
	public void updateItemQuantityTest() throws Exception {
		
		String cartId = "550072034";
		UpdateQuantityRequest quantityRequest = new UpdateQuantityRequest();
		UpdateQuantityRequest.OrderQuantity itemQuantity = new OrderQuantity();
		itemQuantity.setOrderItemId("550271042");
		itemQuantity.setQuantity("4");
		quantityRequest.addOrderItem(itemQuantity);
		itemQuantity = new OrderQuantity();
		itemQuantity.setOrderItemId("550271041");
		itemQuantity.setQuantity("6");
		quantityRequest.addOrderItem(itemQuantity);
		String updateQuantityJson = jsonMapper.writeValueAsString(quantityRequest);
		
		mockMvc.perform(post("/api/cart/PUT/{cartId}/updateItemQuantity",cartId).contentType(MediaType.APPLICATION_JSON).content(updateQuantityJson))
				.andExpect(status().isOk());
	}
	
	@Test
	public void getAvailableShippingMethods() throws Exception {
		String cartId = "550072034";

		mockMvc.perform(get("/api/cart/{cartId}/getAvailableShippingMethods/?profile=77449&orderId="+cartId, cartId)).andExpect(status().isOk());
	}
	
	@Test
	public void initiateCheckout() throws Exception {
		
		String cartId = "550072034";
		InitiateRequest initiateRequest = new InitiateRequest();
		initiateRequest.setOrderId(cartId);
		String initiateRequestJson = jsonMapper.writeValueAsString(initiateRequest);
		
		mockMvc.perform(post("/api/cart/PUT/{cartId}/initiate",cartId).contentType(MediaType.APPLICATION_JSON).content(initiateRequestJson))
				.andExpect(status().isNoContent());
	}
	
	@Test
	public void getCart() throws Exception {
		String cartId = "550072034";

		mockMvc.perform(get("/api/carts/{cartId}", cartId)).andExpect(status().isOk());
	}
	

}
