package com.academy.cart.vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoDetails {

	@JsonProperty("getpromocode")
	private PromoCode promoCode;

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PromoCode {

		private String orderId;
		private String resourceId;
		private String resourceName;
		private Channel channel;
		private List<PromotionCode> promotionCode = new ArrayList<>();
	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PromotionCode {

		private List<AssociatedPromotion> associatedPromotion = new ArrayList<>();
		private String code;

	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AssociatedPromotion {

		private String promotionId;
		private String description;

	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Channel {

		private Description description;
		private Object userData;
		private ChannelIdentifer channelIdentifer;

	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ChannelIdentifer {

		private String channelName;
		private String uniqueID;

	}

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Description {

		private String value;
		private String language;

	}

}
