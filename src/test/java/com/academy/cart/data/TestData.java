package com.academy.cart.data;

import java.io.File;
import java.io.IOException;

import com.academy.cart.vo.UpdateCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestData {

	private static ObjectMapper mapper = new ObjectMapper();
	private static ClassLoader classLoader = TestData.class.getClassLoader();

	public static UpdateCartRequest getUpdateCartRequest() throws IOException {

		File file = new File(classLoader.getResource("test-data/cart/add-update-cart.json").getFile());
		
		return mapper.readValue(file, UpdateCartRequest.class);
	}

}
