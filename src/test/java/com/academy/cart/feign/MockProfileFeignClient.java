package com.academy.cart.feign;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.util.ErrorCode;

public class MockProfileFeignClient implements ProfileFeignClient {
	
	public boolean isException = false;

	@Override
	public String getAddressForUser(String profileId, Map<String, String> headerMap) {
		if(isException) {
			throw new ASOException("internal server error");
		}
		try {
			InputStream inputStream = new ClassPathResource("test-data/view-cart/user-address.json").getInputStream();
			return IOUtils.toString(inputStream);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

}
