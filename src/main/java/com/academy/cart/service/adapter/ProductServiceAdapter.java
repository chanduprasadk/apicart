package com.academy.cart.service.adapter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.academy.cart.feign.ProductFeignClient;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.integration.adapter.AbstractIntegrationAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductServiceAdapter.
 */
@Service
public class ProductServiceAdapter extends AbstractIntegrationAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceAdapter.class);

	/** The product feign client. */
	@Autowired
	private ProductFeignClient productFeignClient;
	
	/**
	 * Gets the product details.
	 *
	 * @param productIds the product ids
	 * @return the product details
	 */
	public String getProductDetails(String productIds) {
		try {
			logger.debug("product ids sending to feign client {} ", productIds);
			return productFeignClient.getProductDetails(productIds, getHeadersMap());
		}
		catch(Exception e) {
			logger.error(e.getMessage(), e);
			//return null;
			throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE, "Product service is not available");
		}
	}
	

}
