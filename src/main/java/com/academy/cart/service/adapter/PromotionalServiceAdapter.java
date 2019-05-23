package com.academy.cart.service.adapter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academy.cart.feign.OrderFeignClient;
import com.academy.integration.adapter.AbstractIntegrationAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class PromotionalServiceAdapter.
 */
@Service
public class PromotionalServiceAdapter extends AbstractIntegrationAdapter {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(PromotionalServiceAdapter.class);

	/** The order feign client. */
	@Autowired
	OrderFeignClient orderFeignClient;

	/**
	 * Gets the promo details.
	 *
	 * @param orderId the order id
	 * @return the promo details
	 */
	public String getPromoDetails(String orderId, Map<String, String> headerMap) {
		try {
		logger.debug("getPromoDetails invoked with orderId {} ", orderId);
		return orderFeignClient.getPromoDetails(orderId, headerMap);
		}
		catch(Exception e) {
			return null;
		}
	}

}
