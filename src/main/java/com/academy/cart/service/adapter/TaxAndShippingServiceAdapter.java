package com.academy.cart.service.adapter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.cart.feign.TaxAndShippingFeignClient;
import com.academy.integration.adapter.AbstractIntegrationAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class TaxAndShippingServiceAdapter.
 */
@Service
public class TaxAndShippingServiceAdapter extends AbstractIntegrationAdapter {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(TaxAndShippingServiceAdapter.class);

	/** The tax and shipping feign client. */
	@Autowired
	private TaxAndShippingFeignClient taxAndShippingFeignClient;
	
	/**
	 * Gets the tax and shipping charges.
	 *
	 * @param queryParams the query params
	 * @return the tax and shipping charges
	 */
	public String getTaxAndShippingCharges(Map<String,String> queryParams, Map<String, String> headerMap) {
		logger.debug("getTaxAndShippingCharges invoked with query params {} ", queryParams);
		try {
			return taxAndShippingFeignClient.getTaxAndShipping( 
                    queryParams.get("orderId"),
                    queryParams.get("deliveryZipCode"),
                    queryParams.get("storeZipCode"),
                    getHeadersMap());	
		}
		catch(Exception e) {
			// if tax service is not available return no response and api will send the default response
			return null;
		}
		
	}
	
	
}
