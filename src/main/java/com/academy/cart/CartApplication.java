package com.academy.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

/**
 * @author dmuru1
 * 
 *         Cart service Main class Cart service Main class
 */
@SpringBootApplication
@ComponentScan("com.academy")
@EnableDiscoveryClient
@EnableAutoConfiguration
@EnableFeignClients("com.academy.cart.feign")
@EnableHystrix
@EnableHystrixDashboard
public class CartApplication {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(CartApplication.class, args);
	}

	/**
	 * Gets the template.
	 *
	 * @return the template
	 */
	@Bean
	public RestTemplate getTemplate() {
		return new RestTemplate();
	}

	/**
	 * This method registers error message properties file into context.
	 * 
	 * @return messageSource
	 */
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("errorMessages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	/**
	 * This method sets message source into validation factory.
	 * 
	 * @return localValidatorFactoryBean
	 */
	@Bean
	public Validator getValidator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.setValidationMessageSource(messageSource());
		return localValidatorFactoryBean;
	}

}
