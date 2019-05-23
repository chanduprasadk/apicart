package com.academy.cart.feign;

import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;

import com.academy.cart.feign.ProfileFeignClient.ProfileFeignClientFallback;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;

@FeignClient(name = "profile", fallback = ProfileFeignClientFallback.class)
public interface ProfileFeignClient {

	@RequestLine(value = "GET /api/profile/{profileId}/address/")
	public String getAddressForUser(@Param(value = "profileId") String profileId,
			@HeaderMap Map<String, String> headerMap);

	@Component
	class ProfileFeignClientFallback implements ProfileFeignClient {

		@Override
		public String getAddressForUser(String profileId, Map<String, String> headerMap) {
			return null;
		}

	}
}