package com.academy.cart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.academy.cart.domain.Cart;
import com.academy.cart.domain.Item;
import com.academy.cart.service.ItemService;
import com.academy.cart.vo.ItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class ItemControllerTest {
	
	@InjectMocks
	ItemController itemController;
	
	@Mock
	ItemService itemService;
	
	MockMvc mockMvc;
	
	@InjectMocks
	ObjectMapper jsonMapper;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
	}

	@Test
	public void testDeleteItem() throws Exception {
		String cartId = "15969";
		String itemId = "1234";
		mockMvc.perform(post("/api/cart/DELETE/{cartId}/items/{itemId}", cartId,itemId)).andExpect(status().isOk());
	}

	@Test
	public void testUpdateItem() throws Exception {
		String cartId = "15969";
		String itemId = "1234";
		ItemRequest itemRequest = new ItemRequest();
		itemRequest.setQuantity("2");
		Gson gson = new Gson();
		String json = gson.toJson(itemRequest);
		mockMvc.perform(post("/api/cart/PUT/{cartId}/items/{itemId}", cartId,itemId).contentType("application/json").content(json)).andExpect(status().isOk());
	}
	
	@Test
	public void testUpdateItemMissingBody() throws Exception {
		String cartId = "15969";
		String itemId = "1234";
		mockMvc.perform(post("/api/cart/PUT/{cartId}/items/{itemId}", cartId,itemId)).andExpect(status().is(400));
	}
	
	@Test
	public void testAddItems() throws Exception {
	   Cart cart = new Cart();
	   String cartId = "123344";
	   List<Item> items = new ArrayList<>();
	   Item item = new Item();
	   item.setQuantity("1");item.setProductId("32578");
	   items.add(item);
	   cart.setItems(items);
	   String itemsJson = jsonMapper.writeValueAsString(cart);
	   mockMvc.perform(post("/api/cart/{cartId}/items", cartId).content(itemsJson).contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isCreated());
	}

}
