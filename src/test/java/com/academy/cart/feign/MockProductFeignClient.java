package com.academy.cart.feign;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorCode;

@Component
public class MockProductFeignClient implements ProductFeignClient {
	
	public boolean isException = false;

	@Override
	public String getProductDetails(String productId, Map<String, String> headerMap) {
		if(isException) {
			throw new ASOException("internal server error");
		}
		try {
			InputStream inputStream = new ClassPathResource("test-data/view-cart/product-response.json").getInputStream();
			return IOUtils.toString(inputStream);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

}
