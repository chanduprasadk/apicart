package com.academy.cart.service.adapter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.cart.feign.ProfileFeignClient;
import com.academy.integration.adapter.AbstractIntegrationAdapter;


/**
 * The Class ProfileServiceAdapter.
 */
@Service
public class ProfileServiceAdapter extends AbstractIntegrationAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(ProfileServiceAdapter.class);


	/** The profile feign client. */
	@Autowired
	private ProfileFeignClient profileFeignClient;
	
	/**
	 * Gets the address for user.
	 *
	 * @param profileId the profile id
	 * @param headerMap the header map
	 * @return the address for user
	 */
	public String getAddressForUser(String profileId , Map<String,String> headerMap) {
		try {
			return profileFeignClient.getAddressForUser(profileId, getHeadersMap());
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
