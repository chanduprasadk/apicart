package com.academy.cart.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.academy.cart.constant.CommonConstant;
import com.academy.cart.service.adapter.InventoryServiceAdapter;
import com.academy.cart.service.adapter.ProductServiceAdapter;
import com.academy.cart.service.adapter.ProfileServiceAdapter;
import com.academy.cart.service.adapter.PromotionalServiceAdapter;
import com.academy.cart.service.adapter.TaxAndShippingServiceAdapter;
import com.academy.cart.util.CartUtils;
import com.academy.cart.vo.AvailableShippingMethods;
import com.academy.cart.vo.BundleInventoryRequest;
import com.academy.cart.vo.CartDetails;
import com.academy.cart.vo.CartItem;
import com.academy.cart.vo.CartItem.BundleItemProperties;
import com.academy.cart.vo.InventoryDetails;
import com.academy.cart.vo.InventoryRequestWrapper;
import com.academy.cart.vo.InventoryResponseWrapper;
import com.academy.cart.vo.InventorySku;
import com.academy.cart.vo.ItemAdjustment;
import com.academy.cart.vo.OnlineInventoryRequest;
import com.academy.cart.vo.PickUpInventoryRequest;
import com.academy.cart.vo.ProductAttribute;
import com.academy.cart.vo.ProductInfo;
import com.academy.cart.vo.ProductInfo.ProductMessage;
import com.academy.cart.vo.ProductInfoWrapper;
import com.academy.cart.vo.PromoDetails;
import com.academy.cart.vo.ShippingAndTaxVO;
import com.academy.cart.vo.WCSItemAttribute;
import com.academy.common.aspect.perf.PerfLog;
import com.academy.common.exception.ASOException;
import com.academy.common.exception.BusinessException;
import com.academy.common.exception.WCSIntegrationException;
import com.academy.common.exception.util.ErrorCode;
import com.academy.integration.transformer.ResponseTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class CartAggregationServiceImpl.
 */
@Service
public class CartAggregationServiceImpl implements CartAggregationService {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(CartAggregationServiceImpl.class);

	/** The product service adaper. */
	@Autowired
	private ProductServiceAdapter productServiceAdaper;

	/** The tax and shipping service adapter. */
	@Autowired
	private TaxAndShippingServiceAdapter taxAndShippingServiceAdapter;

	/** The promotional service adapter. */
	@Autowired
	private PromotionalServiceAdapter promotionalServiceAdapter;
	
	@Autowired
	private ProfileServiceAdapter profileServiceAdapter;
	
	@Autowired
	private InventoryServiceAdapter inventoryServiceAdapter;
	
	/** The cart service. */
	@Autowired
	private CartService cartService;

	/** The json mapper. */
	@Autowired
	private ObjectMapper jsonMapper;

	/** The response transformer. */
	@Autowired
	ResponseTransformer responseTransformer;

	private static final String PRODUCT_ATTRIBUTE = "Defining";

	private static final String ASO_TWO_PLACE_DECIMAL_FORMATTER = "0.00";

	private static final String ITEM_DISCOUNT_FILTER_LEVEL = "OrderItem";
	
	private static final String ORDER_DISCOUNT_FILTER_LEVEL = "Order";
	
	private static final String SHIPPING_TYPE_STS = "STS";
	
	private static final String SHIPPING_TYPE_PICK_UP = "PICKUPINSTORE";
	
    private static final String BUNDLE_INVENTORY_SOURCE_ONLINE = "online";
	
	private static final String BUNDLE_INVENTORY_SOURCE_STORE = "pickup";
	
	private static final String INVENTORY_STATUS_AVAILABLE = "AVAILABLE";
	
	private static final int STORE_ID_SIZE_LIMIT = 3;
	
	private static final String STORE_ID_PAD_CHAR = "0";
	
	private static final String NUMBER_DEFAULT_VALUE = "0.00"; 
	
	public static final String AVAILABLE = "AVAILABLE";
	
	public static final String LIMITED_STOCK = "LIMITED_STOCK";

	public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
	
	public static final String BOPIS_STORE_EXCLUDE = "Y";
	
	public static final String STS_STORE_DISABLED = "Y";

	@Value("${enableJSONLogging}")
	private boolean enableJSONLogging;
	
	@Value("${emptyCart}")
	private String emptyCart;
	
	@Value("${cart.aggregator.thread.pool.size}")
	private int threadPoolSize;
	
	private static final String SHIP_MODE_CODE_PICKUP_IN_STORE = "PickupInStore";
	private static final String PHYSICAL_STORE_ID = "STOREIDENTIFIER"; 
	private static final String SHIP_MODE_CODE_SHIP_TO_STORE = "Ship To Store";
	private static final String STOREID_COOKIE_NAME = "WC_StLocId";
	
	private static final String[] excludeProperties = new String []{"productDetails", "unitPrice", "orderItemInventoryStatus", "orderItemId", "usableShippingChargePolicy", "productId", "totalAdjustment"};
	
	private ExecutorService threadPool;

	@PostConstruct
	public void postConstruct() {
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.academy.cart.service.CartAggregationService#viewCart(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	
	@PerfLog
	public String fetchCart(String storeId, Map<String, String> queryParams, HttpHeaders headers) {
		
		CartDetails cartDetails = null;
		try {
			String cartDetailsJson = cartService.getCartDetails("cartDetails");
			logger.debug("cart response received from cart service {} ", cartDetailsJson);
			cartDetails = jsonMapper.readValue(cartDetailsJson, CartDetails.class);
			if(null == cartDetails) {
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Error while fetching cart details");
			}
			if(cartDetails.getOrderItem() != null) {
				String availableShippingmethodsResponse = cartService.getAvailableShippingmethods("getshipppingmodeswithestimate",
						cartDetails.getOrderId());
				aggregateAvailableShippingMethods(availableShippingmethodsResponse, cartDetails);
				updateStoreIdForBopus(cartDetails);
				invokeServicesInParallel(storeId, cartDetails, queryParams, headers);
			}
			updateCartDetails(cartDetails);
			Optional.ofNullable(cartDetails.getOrderItem()).ifPresent(items -> Collections.reverse(items));
			Optional.ofNullable(cartDetails.getBundleProductDetails()).ifPresent(items -> Collections.reverse(items));
			String aggregatedjson = jsonMapper.writeValueAsString(cartDetails);
			logJsonData("aggregatedjson {} ", aggregatedjson);
			String finalCartJson = responseTransformer.transform("aggregatedCartView", aggregatedjson);
			logJsonData("final get cart aggregated json {}  ", finalCartJson);
			return finalCartJson;

		} catch (WCSIntegrationException e) {
			// handle WCS 404 exception and return the empty cart for UI
			logger.debug("HTTP error code received from Integration service {} ",e.getHttpStatus());
			if (HttpStatus.NOT_FOUND.value() == e.getHttpStatus()) {
				logger.debug("cart not found , returning empty cart {} ", emptyCart);
				return emptyCart;
			} else {
				throw e;
			}
		} catch (BusinessException businessException) {
			return emptyCart;
		}catch(ASOException asoEx) {
			logger.error(asoEx.getMessage(), asoEx);
			throw asoEx;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(ErrorCode.SERVICE_NOT_AVAILABLE, e.getMessage());
		}
	}

	private void invokeServicesInParallel(String storeId, final CartDetails cartDetails,
			Map<String, String> queryParams, HttpHeaders headers) {
		final Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaders.COOKIE, MDC.get(HttpHeaders.COOKIE));
		headersMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		
		Map<String, List<CartItem>> productWithItemMap = new HashMap<>();
		cartDetails.getOrderItem().parallelStream().forEach(cartItem -> {
			promotionsAdjustment(cartItem);
			if(productWithItemMap.containsKey(cartItem.getProductId())) {
				productWithItemMap.get(cartItem.getProductId()).add(cartItem);
			} else {
				List<CartItem> itemList = new ArrayList<>();
				itemList.add(cartItem);
				productWithItemMap.put(cartItem.getProductId(), itemList);
			}
		});
		Map<String, CartItem> bundleMap = new HashMap<>();
		fillBundleProductsInfo(cartDetails, bundleMap);
		
		CompletableFuture<Void> callAndPopulateProductinfo = CompletableFuture.supplyAsync(() -> {
			fetchProductDetails(cartDetails, productWithItemMap, bundleMap);
			return null;
		}, threadPool);
		
		CompletableFuture<Void> callAndPopulateInventoryinfo = CompletableFuture.supplyAsync(() -> {
			Map<String, List<CartItem>> itemMapFilterByBundleId = cartDetails.getOrderItem().stream().filter(item -> item.getBundleId() != null).collect(Collectors.groupingBy(CartItem::getBundleId));
			String inventoryRequest = buildInventoryRequest(cartDetails, itemMapFilterByBundleId);
			logJsonData("inventoryRequest sending to inventory api {} ", inventoryRequest);
			boolean isbopisStore = false;
			if(null != queryParams.get(CommonConstant.IS_BOPIS_STORE.getName())) {
				isbopisStore = queryParams.get(CommonConstant.IS_BOPIS_STORE.getName()).equals("1") ? true : false;
			}
			String inventoryResponse = inventoryServiceAdapter.getInventory(inventoryRequest, storeId,isbopisStore, headersMap);
			logJsonData("inventory response received from api {} ", inventoryResponse);
			if(null != inventoryResponse) {
				BinaryOperator<List<CartItem>> mergeFunction = (a, b) -> {
					a.addAll(b);
					return a;
				};
				Map<String, List<CartItem>> partNumberOrderItemMap = cartDetails.getOrderItem().stream()
						.filter(item -> item.getPartNumber() != null)
						.collect(Collectors.toMap(CartItem::getPartNumber, item -> {
							List<CartItem> list = new ArrayList<>();
							list.add(item);
							return list;
						}, mergeFunction));
				processInventoryResponse(inventoryResponse, productWithItemMap, cartDetails.getStoreId(), partNumberOrderItemMap, itemMapFilterByBundleId, bundleMap);
			    cartDetails.setInventoryCallSuccess(true);
			}
			return null;
		}, threadPool);
		

		CompletableFuture<Void> taxResponse = CompletableFuture.supplyAsync(() -> {
			fetchDeliveryZipCode(queryParams, headersMap, headers);
			logger.debug("query params map received after zip code processing {} ", queryParams);
			if(queryParams.containsKey(CommonConstant.DELIVERY_ZIP_CODE.getName()) && queryParams.containsKey(CommonConstant.STORE_ZIP_CODE.getName())) {
				String deliveryZipCode = queryParams.get(CommonConstant.DELIVERY_ZIP_CODE.getName());
				String storeZipCode = queryParams.get(CommonConstant.STORE_ZIP_CODE.getName());
				if((null != deliveryZipCode && !deliveryZipCode.isEmpty()) || (null != storeZipCode && !storeZipCode.isEmpty())) {
					queryParams.put("storeId", storeId);
					queryParams.put("orderId", cartDetails.getOrderId());
					cartDetails.setZipCode(deliveryZipCode);
					String taxAndShippingChargesResponse = taxAndShippingServiceAdapter.getTaxAndShippingCharges(queryParams, headersMap);
					updateShippingAndTax(cartDetails, taxAndShippingChargesResponse);
				}
			}
			return null;
		}, threadPool);
		
		CompletableFuture<Void> promoResponse = CompletableFuture.supplyAsync(() -> {
			String promoDetailsResponse = promotionalServiceAdapter.getPromoDetails(cartDetails.getOrderId(), headersMap);
			fetchPromoDetails(cartDetails, promoDetailsResponse);
			return null;
		}, threadPool);

		CompletableFuture<Void> allServiceResponses = CompletableFuture.allOf(callAndPopulateProductinfo, callAndPopulateInventoryinfo, taxResponse,
				promoResponse);

		try {
			allServiceResponses.get();
			// The following condition will check if the store is a bopis eligible
			// items available will be made to OOS even if the inventory has come from OMS
			if (queryParams.containsKey(CommonConstant.IS_BOPIS_STORE.getName())
					&& queryParams.get(CommonConstant.IS_BOPIS_STORE.getName()) != null
					&& queryParams.get(CommonConstant.IS_BOPIS_STORE.getName()).equals("0")) {
				cartDetails.getOrderItem().parallelStream().forEach(item -> {
					if (item.getProductDetails() != null && item.getProductDetails().getInventory() != null
							&& CollectionUtils.isNotEmpty(item.getProductDetails().getInventory().getStore())) {
						item.getProductDetails().getInventory().getStore().parallelStream().forEach(store -> {
							if (store != null && store.getInventoryStatus() != null
									&& (store.getInventoryStatus().equalsIgnoreCase(AVAILABLE)
											|| store.getInventoryStatus().equalsIgnoreCase(LIMITED_STOCK)
											)) {
								store.setInventoryStatus(OUT_OF_STOCK);
								store.setAvailableQuantity("0");
							}
						});
					}
				});
			}
			else {
				cartDetails.getOrderItem().parallelStream().forEach(item -> {
					if (item.getProductDetails() != null && item.getProductDetails().getInventory() != null
							&& CollectionUtils.isNotEmpty(item.getProductDetails().getInventory().getStore())) {
						item.getProductDetails().getInventory().getStore().parallelStream().forEach(store -> {
							if (store != null && store.getInventoryStatus() != null
									&& ((store.getInventoryStatus().equalsIgnoreCase(AVAILABLE)
											|| store.getInventoryStatus().equalsIgnoreCase(LIMITED_STOCK))
											&& (item.getIsBopisStoreExclude() != null && item.getIsBopisStoreExclude().equalsIgnoreCase(BOPIS_STORE_EXCLUDE)))) {
								store.setInventoryStatus(OUT_OF_STOCK);
								store.setAvailableQuantity("0");
							}
						});
					}
				});
			}
			
			
			if(cartDetails.getIsSTSStoreDisabled() != null && cartDetails.getIsSTSStoreDisabled().equalsIgnoreCase(STS_STORE_DISABLED)) {
				cartDetails.getOrderItem().parallelStream()
					.forEach(item -> {
					if (item.getProductDetails() != null 
								&& (validateAvailableShippingMethods(item, SHIPPING_TYPE_STS))
								&& item.getProductDetails().getInventory() != null
								&& CollectionUtils.isNotEmpty(item.getProductDetails().getInventory().getOnline()) 
								) {
							item.getProductDetails().getInventory().getOnline().parallelStream().forEach(online -> {
								if (online != null && online.getInventoryStatus() != null
										&& (online.getInventoryStatus().equalsIgnoreCase(AVAILABLE)
												|| online.getInventoryStatus().equalsIgnoreCase(LIMITED_STOCK))) {
									online.setInventoryStatus(OUT_OF_STOCK);
									online.setAvailableQuantity("0");
								}
							});
						
						}
				});
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ASOException(ErrorCode.SERVICE_NOT_AVAILABLE,
					"Some Error occurred while processing request, please try after some time");
		}
	}
	/*
	 * LOGIC to extract the deliveryZipCode for Tax Service Call
	 * Priority 1 : check the query param changedZipCode and if exist then it will be deliveryZipCode for tax service
	 * Priority 2 :  check the default address in user profile for registered user and if exist , it will be deliveryZipCode for tax service
	 * Priority 3 :  check the query param deliveryZipCode and if exist then it will be deliveryZipCode for tax service
	 * Priority 4 :  find the zipCode in request header and if exist it will be deliveryZipCode for tax service 
	 */
	protected void fetchDeliveryZipCode(Map<String, String> queryParams, Map<String, String> headersMap , HttpHeaders headers) {
		boolean found = false;
		if(queryParams.containsKey(CommonConstant.CHANGED_ZIP_CODE.getName())) {
			String changedZipCode = queryParams.get(CommonConstant.CHANGED_ZIP_CODE.getName());
			logger.debug("changedZipCode received from query params {} ", changedZipCode);
			queryParams.put(CommonConstant.DELIVERY_ZIP_CODE.getName(), changedZipCode);
			found = true;
		}
		if(!found) {
			String cookies = headersMap.get(HttpHeaders.COOKIE);
			logger.debug("cookie extracted from MDC header {} ", cookies);
			String profileId = CartUtils.extractProfileIdFromCookies(cookies);
			logger.debug("profile id received from cookie {} ", profileId);
			if(null != profileId) {
				String userAddress = profileServiceAdapter.getAddressForUser(profileId, headersMap);
				String defaultZipCode = getDefaultZipCode(userAddress);
				logger.debug("zip code received from user default address {} ", defaultZipCode);
				if(null != defaultZipCode) {
					found = true;
				    queryParams.put(CommonConstant.DELIVERY_ZIP_CODE.getName(), defaultZipCode);
				}
			}
			if(!found && !queryParams.containsKey(CommonConstant.DELIVERY_ZIP_CODE.getName())){
				String headerZipCode = headers.getFirst(CommonConstant.HEADER_ZIP_CODE.getName());
				logger.debug("zip code received from header {} ", headerZipCode);
				if(headerZipCode != null) {
					queryParams.put(CommonConstant.DELIVERY_ZIP_CODE.getName(), headerZipCode);
				}
			}
		}
	}
	
	private String getDefaultZipCode(String userAddress) {
        String userAddressZipCode = null;
		if (null != userAddress) {
			try {
				JsonNode addressNode = jsonMapper.readValue(userAddress, JsonNode.class);
				if (addressNode instanceof ArrayNode) {
					ArrayNode addressArray = (ArrayNode) addressNode;
					userAddressZipCode = findZipCode(addressArray);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
		return userAddressZipCode;
	}
	
	private String findZipCode(final ArrayNode addressArray) {
		if (addressArray.size() > 0) {
			for (JsonNode address : addressArray) {
				boolean isDefaultAddress = address.findPath(CommonConstant.USER_DEFAULT_ADDRESS_IDENTIFIER.getName()).asBoolean();
				if (isDefaultAddress) {
					return address.findPath(CommonConstant.PROFILE_ZIP_CODE.getName()).asText();
				}
			}
		}
		return null;
	}

	/**
	 * Fetch product details.
	 *
	 * @param cartVO the cart VO
	 */
	private void fetchProductDetails(final CartDetails cartDetails, final Map<String, List<CartItem>> productWithItemMap,
			final Map<String, CartItem> bundleMap) {
		String bundleProductIds = null;
		if (!bundleMap.isEmpty()) {
			bundleItemAdjustment(bundleMap);
			bundleProductIds = bundleMap.keySet().stream().collect(Collectors.joining(CommonConstant.COMMA.getName()));
		}
		String commaSeperatedProductIds = productWithItemMap.keySet().stream()
				.collect(Collectors.joining(CommonConstant.COMMA.getName()));
		boolean isbundle = false;
		if (bundleProductIds != null) {
			commaSeperatedProductIds = StringUtils.joinWith(CommonConstant.COMMA.getName(), commaSeperatedProductIds,
					bundleProductIds);
			bundleMap.forEach((key,val) ->{
				List<CartItem> items = new ArrayList<>();
				items.add(val);
				productWithItemMap.put(key, items);
				
			});
			isbundle = true;
		}
		logger.debug("productIds received from Cart {} ", commaSeperatedProductIds);
		if (null != commaSeperatedProductIds && !productWithItemMap.isEmpty()) {
			//product API call
			String productsResponse = productServiceAdaper.getProductDetails(commaSeperatedProductIds);
			//String productsResponse = null;
			logJsonData("product response recived from productinfo service {}", productsResponse);
			if(!StringUtils.isBlank(productsResponse)) {
				populateProductInfo(productWithItemMap, productsResponse);
				if (isbundle) {
					cartDetails.setBundleProductDetails(new ArrayList<>(bundleMap.values()));
				}
			}
			
		}
	}
	
	private void populateProductInfo(final Map<String, List<CartItem>> productWithItemMap, String productsResponse) {
		if (null != productsResponse) {
			try {
				Optional.ofNullable(jsonMapper.readValue(productsResponse, ProductInfoWrapper.class))
						.ifPresent(productInfoWrapper -> {
							Optional.ofNullable(productInfoWrapper.getProductinfo()).ifPresent(products -> {
								products.stream().forEach(productInfo -> {
									if (productWithItemMap.containsKey(productInfo.getId())) {
										List<CartItem> cartItems = productWithItemMap.get(productInfo.getId());
										cartItems.stream().forEach(cartItem -> {
											filterProductAttributes(productInfo);
											cartItem.getProductDetails().setProductinfo(productInfo);
										});
									}
								});
							});
						});

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				//throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
	}
	
	private String buildInventoryRequest(final CartDetails cartDetails, final Map<String, List<CartItem>> itemMapFilterByBundleId) {
		InventoryRequestWrapper inventoryWrapper = new InventoryRequestWrapper();
		Optional.ofNullable(cartDetails.getOrderItem()).ifPresent(items -> {
			List<InventorySku> onlineSkus = new ArrayList<>();
			List<InventorySku> pickupSkus = new ArrayList<>();
			items.stream().forEach(item -> {
				if(!item.isBundleItem()) {
					populateInventorySku(item, onlineSkus, false);
					Optional.ofNullable(item.getAvailableShippingMethods()).ifPresent(shippingMthds -> {
						if(cartDetails.getStoreId() != null && !cartDetails.getStoreId().isEmpty()
								&& validateAvailableShippingMethods(item, SHIPPING_TYPE_PICK_UP)) {
							
							populateInventorySku(item, pickupSkus, true);
						}
					});
				}
			});
			if(!onlineSkus.isEmpty()) {
				OnlineInventoryRequest onlineInventory = new OnlineInventoryRequest();
				onlineInventory.setSkus(onlineSkus);
				inventoryWrapper.setOnlineskus(onlineInventory);
			}
			
			if(!pickupSkus.isEmpty()) {
				PickUpInventoryRequest pickUpInventoryRequest = new PickUpInventoryRequest();
				pickUpInventoryRequest.setStoreId(cartDetails.getStoreId());
				pickUpInventoryRequest.setSkus(pickupSkus);
				inventoryWrapper.setPickupskus(pickUpInventoryRequest);
			}
			buildBundleInventoryRequest(inventoryWrapper, itemMapFilterByBundleId, cartDetails.getStoreId());
		});
		
		return CartUtils.getJSONString(inventoryWrapper);
	}
	
	private void buildBundleInventoryRequest(final InventoryRequestWrapper inventoryWrapper, final Map<String, List<CartItem>> itemMapFilterByBundleId, String pickupStoreId) {
		if(null != itemMapFilterByBundleId && !itemMapFilterByBundleId.isEmpty()) {
			List<BundleInventoryRequest> bundleInventoryList = new ArrayList<>();
			itemMapFilterByBundleId.forEach((bundleId, bundleItems) -> {
				BundleInventoryRequest bundleInventoryRequest = new BundleInventoryRequest();
				bundleInventoryRequest.setBundleId(bundleId);
				
				AtomicInteger bundleBopusCount = new AtomicInteger(0);
				List<InventorySku> bundleOnlineInventories = bundleItems.stream().map(item -> {
					return populateBundleInventorySku(item, false, bundleBopusCount);
				}).collect(Collectors.toList());
				bundleInventoryRequest.setInventorySource(BUNDLE_INVENTORY_SOURCE_ONLINE);
				bundleInventoryRequest.setSkus(bundleOnlineInventories);
				bundleInventoryList.add(bundleInventoryRequest);
				
				logger.debug("bundleBopus count from available shipping methods {}", bundleBopusCount.get());
				if(bundleBopusCount.get() == bundleItems.size()) {
					bundleInventoryRequest = new BundleInventoryRequest();
					bundleInventoryRequest.setInventorySource(BUNDLE_INVENTORY_SOURCE_STORE);
					bundleInventoryRequest.setStoreId(pickupStoreId);
					List<InventorySku> bundleStoreInventories = bundleItems.stream().map(item -> {
						return populateBundleInventorySku(item, true, null);
					}).collect(Collectors.toList());
					bundleInventoryRequest.setSkus(bundleStoreInventories);
					bundleInventoryList.add(bundleInventoryRequest);
				}
			});
			if(!bundleInventoryList.isEmpty()) {
				inventoryWrapper.setBundleskus(bundleInventoryList);
			}
		}
	}
	
	private void populateInventorySku(final CartItem item, final List<InventorySku> inventorySkus, boolean isPickUp) {
		InventorySku inventorySku = new InventorySku();
		if(isPickUp) {
			inventorySku.setSkuId(item.getPartNumber());
		}
		else {
			inventorySku.setSkuId(item.getProductId());
		}
		
		if(isPickUp && inventorySkus.stream().anyMatch(sku -> sku.getSkuId().equals(item.getPartNumber()))) {
			inventorySkus.stream().filter(sku -> sku.getSkuId().equals(item.getPartNumber())).findFirst().ifPresent(inv -> {
				if(item.getShipModeCode().equalsIgnoreCase(SHIPPING_TYPE_PICK_UP)) {
					inv.setRequestedQuantity((int) item.getQuantity());
				}
			});
		} else {
			inventorySku.setRequestedQuantity((int) item.getQuantity());
			inventorySkus.add(inventorySku);
		}
	}
	
	private InventorySku populateBundleInventorySku(final CartItem item, boolean isPickUp, final AtomicInteger bundleBopusCount) {
		final InventorySku inventorySku = new InventorySku();
		inventorySku.setRequestedQuantity((int)(item.getQuantity()));
		if(isPickUp) {
			inventorySku.setSkuId(item.getPartNumber());	
		}
		else {
			inventorySku.setSkuId(item.getProductId());
			if(validateAvailableShippingMethods(item, SHIPPING_TYPE_PICK_UP)) {
				bundleBopusCount.incrementAndGet();
			}
		}
		return inventorySku;
	}
	
	private void padStoreIdWithZeros(final CartDetails cartDetails) {
		String storeId = cartDetails.getStoreId();
		if(null != storeId && !storeId.isEmpty() && storeId.length() < 3) {
			storeId = StringUtils.leftPad(storeId, STORE_ID_SIZE_LIMIT, STORE_ID_PAD_CHAR);
		}
		cartDetails.setStoreId(storeId);
	}
	
	
	private void processInventoryResponse(String inventoryResponse, final Map<String, List<CartItem>> productWithItemMap,
			String storeId, final Map<String, List<CartItem>> partNumberOrderItemMap,
			final Map<String, List<CartItem>> itemMapFilterByBundleId, final Map<String, CartItem> bundleMap) {
		try {
			Optional.ofNullable(jsonMapper.readValue(inventoryResponse, InventoryResponseWrapper.class))
					.ifPresent(inventory -> {
						setOnlineInventory(inventory, productWithItemMap);
						setStoreInventory(inventory, partNumberOrderItemMap, storeId);
						setBundleInventory(inventory, itemMapFilterByBundleId, bundleMap, storeId);
					});
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			//throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	private void setOnlineInventory(InventoryResponseWrapper inventory, final Map<String, List<CartItem>> productWithItemMap) {
		 Optional.ofNullable(inventory.getOnlineskus()).ifPresent(onlineInventory -> {
			  Optional.ofNullable(onlineInventory.getSkus()).ifPresent(onlineSkus -> {
				  onlineSkus.stream().forEach(onlineSku -> {
					  if(productWithItemMap.containsKey(onlineSku.getSkuId())) {
						  List<CartItem> cartItems = productWithItemMap.get(onlineSku.getSkuId());
						  cartItems.stream().forEach(cartItem -> {
							  if(cartItem.getQuantity() == onlineSku.getRequestedQuantity()) {
								  InventoryDetails inventoryDetails = cartItem.getProductDetails().getInventory();
								  inventoryDetails.getOnline().add(onlineSku);
							  }
						  });
					  }
				  });
			  });
		  });
	}
	
	private void setStoreInventory(InventoryResponseWrapper inventory,
			final Map<String, List<CartItem>> partNumberOrderItemMap, String storeId) {
		Optional.ofNullable(inventory.getPickupskus()).ifPresent(pickUpInventory -> {
			pickUpInventory.stream().forEach(pickUpSkus -> {
				Optional.ofNullable(pickUpSkus).ifPresent(skus -> {
					if (null != storeId && storeId.equals(pickUpSkus.getStoreId())) {
						skus.getSkus().stream().forEach(pickUpSku -> {
							if (partNumberOrderItemMap.containsKey(pickUpSku.getSkuId())) {
								List<CartItem> cartItems = partNumberOrderItemMap.get(pickUpSku.getSkuId());
								cartItems.stream().forEach(cartItem -> {
									if (cartItem.getQuantity() == pickUpSku.getRequestedQuantity()) {
										InventoryDetails inventoryDetails = cartItem.getProductDetails().getInventory();
										pickUpSku.setStoreId(pickUpSkus.getStoreId());
										inventoryDetails.getStore().add(pickUpSku);
									} else {
										InventoryDetails inventoryDetails = cartItem.getProductDetails().getInventory();
										InventorySku inventorySku = new InventorySku();
										inventorySku.setStoreId(pickUpSkus.getStoreId());
										inventorySku.setAvailableQuantity("0");
										inventorySku.setInventoryStatus(OUT_OF_STOCK);
										inventoryDetails.getStore().add(inventorySku);
									}
								});
							}
						});
					}
				});
			});

		});
	}
	
	private void setBundleInventory(final InventoryResponseWrapper inventory, final Map<String, List<CartItem>> itemMapFilterByBundleId, final Map<String, CartItem> bundleMap, String storeId) {
		Optional.ofNullable(inventory.getBundleskus()).ifPresent(pickUpInventory -> {
			  pickUpInventory.stream().forEach(bundleSkus -> {
				  Optional.ofNullable(bundleSkus).ifPresent(skus -> {
					  if(itemMapFilterByBundleId.containsKey(skus.getBundleId())) {
						  List<CartItem> bundleItems = itemMapFilterByBundleId.get(skus.getBundleId());
						  Optional.ofNullable(skus.getSkus()).ifPresent(skusInventory -> {
							  populateBundleItemsInventory(skus, bundleItems, skusInventory, storeId);
							  aggregateBundleInfoInventory(skus, bundleMap, storeId);
						  });
					  }
				  });
			  });
			 
		  });
	}
	
	private void populateBundleItemsInventory(final BundleInventoryRequest skus, final List<CartItem> bundleItems,
			final List<InventorySku> skusInventory, String storeId) {
		if (BUNDLE_INVENTORY_SOURCE_ONLINE.equals(skus.getInventorySource())) {
			Map<String, CartItem> onlineSkuMap = bundleItems.stream()
					.collect(Collectors.toMap(CartItem::getProductId, Function.identity()));
			skusInventory.stream().forEach(inv -> {
				if (onlineSkuMap.containsKey(inv.getSkuId())) {
					CartItem cartItem = onlineSkuMap.get(inv.getSkuId());
					cartItem.getProductDetails().getInventory().getOnline().add(inv);
				}
			});
		}
		else if (BUNDLE_INVENTORY_SOURCE_STORE.equals(skus.getInventorySource()) && null != storeId && storeId.equals(skus.getStoreId())) {
			Map<String, CartItem> storeSkuMap = bundleItems.stream()
					.collect(Collectors.toMap(CartItem::getPartNumber, Function.identity()));
			skusInventory.stream().forEach(inv -> {
				if (storeSkuMap.containsKey(inv.getSkuId())) {
					CartItem cartItem = storeSkuMap.get(inv.getSkuId());
					cartItem.getProductDetails().getInventory().getStore().add(inv);
				}
			});
		}

	}
	
	private void aggregateBundleInfoInventory(BundleInventoryRequest bundleInventory,
			final Map<String, CartItem> bundleMap, String storeId) {
		Optional.ofNullable(bundleInventory).ifPresent(inv -> {
			List<InventorySku> bundleInventories = inv.getSkus();
			CartItem cartItem = bundleMap.get(bundleInventory.getBundleId());
			if (BUNDLE_INVENTORY_SOURCE_ONLINE.equals(inv.getInventorySource()) && bundleInventories != null
					&& !bundleInventories.isEmpty()) {
				Optional.ofNullable(getBundleInventory(bundleInventories)).ifPresent(bundleOnlineInventory -> cartItem
						.getProductDetails().getInventory().getOnline().add(bundleOnlineInventory));
			}

			else if (BUNDLE_INVENTORY_SOURCE_STORE.equals(inv.getInventorySource()) && bundleInventories != null
					&& !bundleInventories.isEmpty() && null != storeId && storeId.equals(inv.getStoreId())) {
				Optional.ofNullable(getBundleInventory(bundleInventories))
						.ifPresent(bundleStoreInventory -> cartItem.getProductDetails().getInventory().getStore()
								.add(bundleStoreInventory));
			}
		});
	}
	
	private InventorySku getBundleInventory(final List<InventorySku> bundleInventories) {
		Optional<InventorySku> outOfStockInv = bundleInventories.stream()
				.filter(inv -> !INVENTORY_STATUS_AVAILABLE.equals(inv.getInventoryStatus())).findFirst();
		
		if(outOfStockInv.isPresent()) {
			return outOfStockInv.get();
		}
		Optional<InventorySku> availableInventory = bundleInventories.stream()
				.filter(inv -> INVENTORY_STATUS_AVAILABLE.equals(inv.getInventoryStatus()))
				.min(Comparator.comparing(sku->Double.valueOf(sku.getAvailableQuantity())));
		if(availableInventory.isPresent()) {
			return availableInventory.get();
		}
		return null;
	}
	
	

	/**
	 * Update shipping and tax.
	 *
	 * @param cartDetails the cart VO
	 * @param storeId     the store id
	 * @param queryParams the query params
	 */
	private void updateShippingAndTax(final CartDetails cartDetails, String taxResponse) {
		Optional.ofNullable(taxResponse).ifPresent(taxDetails -> {
			try {
				ShippingAndTaxVO shippingAndTaxVO = jsonMapper.readValue(taxDetails, ShippingAndTaxVO.class);
				Optional.ofNullable(shippingAndTaxVO).ifPresent(response -> {
					cartDetails.setTotalEstimatedShippingCharge(response.getTotalShippingCharge());
					cartDetails.setTotalEstimatedTax(response.getTotalTax());
					cartDetails.setGrandTotal(response.getOrderGrandTotal());
					cartDetails.setShipToStoreCharge(response.getShipToStoreCharge());
					cartDetails.setTotalAdjustment(response.getTotalAdjustment());
					cartDetails.setTotalProductPrice(response.getTotalProductPrice());
					cartDetails.setEmployeeDiscount(response.getTotalEmployeeDiscount());
					cartDetails.setTaxCallSuccess(true);
				});
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		});

	}

	/**
	 * Fetch promo details.
	 *
	 * @param cartDetails the cart VO
	 */
	private void fetchPromoDetails(final CartDetails cartDetails, String promoDetails) {
		logJsonData("Promo response {}", promoDetails);
		Optional.ofNullable(promoDetails).ifPresent(promotions -> {
			try {
				cartDetails.setPromoDetails(jsonMapper.readValue(promotions, PromoDetails.class));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		});
	}

	/**
	 * Aggregate available shipping methods.
	 *
	 * @param orderId the order id
	 */
	@PerfLog
	private void aggregateAvailableShippingMethods(String availableShippingMthdsRes, final CartDetails cartDetails) {
		logJsonData("Available shipping method response JSON is {}", availableShippingMthdsRes);
		try {
			if (availableShippingMthdsRes != null) {
				String transformResponse = responseTransformer.transform("availableShippingMethods", availableShippingMthdsRes);
				logJsonData("Available shipping method transformResponse JSON is {}", transformResponse);
				if(null != transformResponse) {
					Map<String, List<AvailableShippingMethods>> shippingMethodMap = jsonMapper.readValue(transformResponse, new TypeReference<Map<String, List<AvailableShippingMethods>>>(){});
					Optional.ofNullable(shippingMethodMap).ifPresent(map -> {
						cartDetails.getOrderItem().parallelStream().forEach(item -> {
							if(shippingMethodMap.containsKey(item.getOrderItemId())) {
								Optional.ofNullable(shippingMethodMap.get(item.getOrderItemId())).ifPresent(itemShippingMethodsList -> {
									itemShippingMethodsList.forEach(shippingMethod -> {
										String shipEstimatedFromDate = shippingMethod.getEstimatedFromDate();
										String shipEstimatedToDate = shippingMethod.getEstimatedToDate();
												if (shippingMethod != null
														&& StringUtils.isNotEmpty(shipEstimatedFromDate)
														&& StringUtils.isNotEmpty(shipEstimatedToDate)
														&& shipEstimatedFromDate.equals(shipEstimatedToDate)) {
													if(shipEstimatedToDate.length() == 10){
														try {
															LocalDate toDate = LocalDate.parse(shipEstimatedToDate).plusDays(1);
															shipEstimatedToDate = toDate.toString();
															shippingMethod.setEstimatedToDate(shipEstimatedToDate);
														} catch (DateTimeParseException pE) {
															logger.error("Error parsing shipping toDate: {}",shipEstimatedToDate);
														}
														
													}
										}
									});
									item.setAvailableShippingMethods(itemShippingMethodsList);
								});
							}
							else {
								logger.debug("No Shipping method found for order item id {}", item.getOrderItemId());
							}
						});
					});
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private void updateCartDetails(final CartDetails cartDetails) {
		List<CartItem> orderItems = cartDetails.getOrderItem();
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		Optional.ofNullable(orderItems).ifPresent(items -> {
			items.stream().forEach(order -> {
				Optional.ofNullable(order).ifPresent(item -> {
					double quantity = item.getQuantity();
					atomicInteger.addAndGet((int)quantity);
					setAmountsPrecision(item);
					if(validateAvailableShippingMethods(item, SHIPPING_TYPE_STS)) {
						item.setSOFItem(true);
					}
					populateDefaultInventory(cartDetails, item);
					setProductMessage(item);
				});
			});
		});
		cartDetails.setRecordSetTotal(atomicInteger.get());
		populateDefaultInventory(cartDetails);
		setAmountsPrecision(cartDetails);
	}
	
	private void populateDefaultInventory(final CartDetails cartDetails, final CartItem item) {
		if(!cartDetails.isInventoryCallSuccess()) {
			boolean isStorePickup = validateAvailableShippingMethods(item, SHIPPING_TYPE_PICK_UP);
			item.getProductDetails().setInventory(InventoryDetails.populateDefaultInventory(cartDetails.getStoreId(), isStorePickup));
		}
	}
	
	private void populateDefaultInventory(final CartDetails cartDetails) {
		if(!cartDetails.isInventoryCallSuccess()) {
			Optional.ofNullable(cartDetails.getBundleProductDetails()).ifPresent(bundleInfo -> {
				bundleInfo.stream().forEach(bundleItem -> {
					boolean isStorePickup = validateAvailableShippingMethods(bundleItem, SHIPPING_TYPE_PICK_UP);
					bundleItem.getProductDetails().setInventory(InventoryDetails.populateDefaultInventory(cartDetails.getStoreId(), isStorePickup));
				});
			});
		}
	}

	private void filterProductAttributes(final ProductInfo skuInfo) {
		Optional.ofNullable(skuInfo).ifPresent(skuinfo -> {
			List<ProductAttribute> skuAttributes = skuinfo.getProductAttributes();
			if (skuAttributes != null && !skuAttributes.isEmpty()) {
				if (skuAttributes.stream()
						.anyMatch(att -> "SPECIALORDER".equalsIgnoreCase(att.getName()) && att.getValues() != null
								&& att.getValues().get(0) != null
								&& "Y".equalsIgnoreCase(att.getValues().get(0).getValue()))) {
					skuInfo.setSofItem(true);
				}
				if (skuAttributes.stream()
						.anyMatch(att -> "bopisFlag".equalsIgnoreCase(att.getName()) && att.getValues() != null
								&& att.getValues().get(0) != null
								&& "Y".equalsIgnoreCase(att.getValues().get(0).getValue()))) {
					skuInfo.setBopis(true);
				}
				List<ProductAttribute> filteredAttributes = skuAttributes.stream()
						.filter(x -> PRODUCT_ATTRIBUTE.equalsIgnoreCase(x.getUsage())).collect(Collectors.toList());
				skuInfo.setProductAttributes(filteredAttributes);
			}
		});

	}

	private void promotionsAdjustment(CartItem orderItem) {
		// Promotion Adjustment value comes from WCS in negative so adding in the
		// priceAfterDiscount
		if (Optional.ofNullable(orderItem.getTotalAdjustment()).isPresent()) {
			List<ItemAdjustment> filteredItemDiscountList = orderItem.getTotalAdjustment().stream()
					.filter(Objects::nonNull)
					.filter(itemDiscount -> ITEM_DISCOUNT_FILTER_LEVEL.equals(itemDiscount.getDisplayLevel()))
					.collect(Collectors.toList());
			Optional.ofNullable(filteredItemDiscountList).ifPresent(discount -> {
				double sum = discount.stream().filter(Objects::nonNull).mapToDouble(adj -> Double.valueOf(adj.getAmount())).sum();
				double priceAfterDiscount = Double.valueOf(orderItem.getOrderItemPrice()) + sum;
				orderItem.setOrderItemDiscountedPrice(String.valueOf(priceAfterDiscount));
			});
		} else {
			orderItem.setOrderItemDiscountedPrice(orderItem.getOrderItemPrice());
		}

	}

	private void setAmountsPrecision(CartDetails cartDetails) {
		cartDetails.setTotalShippingTax(setPrecision(cartDetails.getTotalShippingTax()));
		cartDetails.setGrandTotal(setPrecision(cartDetails.getGrandTotal()));
		cartDetails.setTotalEstimatedTax(setPrecision(cartDetails.getTotalEstimatedTax()));
		cartDetails.setTotalAdjustment(setPrecision(cartDetails.getTotalAdjustment()));
		cartDetails.setTotalProductPrice(setPrecision(cartDetails.getTotalProductPrice()));
		cartDetails.setShipToStoreCharge(setPrecision(cartDetails.getShipToStoreCharge()));
		cartDetails.setEmployeeDiscount(setPrecision(cartDetails.getEmployeeDiscount()));
		cartDetails.setTotalEstimatedShippingCharge(setPrecision(cartDetails.getTotalEstimatedShippingCharge()));
		
		if(NUMBER_DEFAULT_VALUE.equals(cartDetails.getShipToStoreCharge())) {
			cartDetails.setShipToStoreCharge(null);
		}
		
		if(NUMBER_DEFAULT_VALUE.equals(cartDetails.getTotalAdjustment())) {
			cartDetails.setTotalAdjustment(null);
		}
		
		if(NUMBER_DEFAULT_VALUE.equals(cartDetails.getEmployeeDiscount())) {
			cartDetails.setEmployeeDiscount(null);
		}
		
	}
	
	private void setAmountsPrecision(CartItem item) {
		item.setUnitPrice(setPrecision(item.getUnitPrice()));
		item.setOrderItemPrice(setPrecision(item.getOrderItemPrice()));
		item.setOrderItemDiscountedPrice(setPrecision(item.getOrderItemDiscountedPrice()));
		Optional.ofNullable(item.getTotalAdjustment()).ifPresent(adjustments -> 
			adjustments.stream().filter(Objects::nonNull).forEach(adjustment -> 
				adjustment.setAmount(setPrecision(adjustment.getAmount()))
			));
	}
	
	private String setPrecision(String value) {
		if (NumberUtils.isCreatable(value)) {
			DecimalFormat doubleDecimalFormat = new DecimalFormat(ASO_TWO_PLACE_DECIMAL_FORMATTER);
			return doubleDecimalFormat.format(Double.parseDouble(value));
		}
		return value;
	}
	
	
	private void fillBundleProductsInfo(final CartDetails cartDetails, final Map<String, CartItem> bundleMap) {
		cartDetails.getOrderItem().stream().forEach(item -> {
			final List<WCSItemAttribute> orderItemExtendAttributes = item.getOrderItemExtendAttribute();
			Optional.ofNullable(orderItemExtendAttributes).ifPresent(allAttributes -> {
				List<WCSItemAttribute> bundlesAttributes = allAttributes
						.stream().filter(Objects::nonNull).filter(bundleAttrib -> CommonConstant.BUNDLE_IDENTIFIRE
								.getName().equalsIgnoreCase(bundleAttrib.getAttributeName()))
						.collect(Collectors.toList());

				if (null != bundlesAttributes && !bundlesAttributes.isEmpty()) {
					item.setBundleItem(true);
					item.setBundleInfo(bundlesAttributes);
					populateBundleItemDetails(bundlesAttributes, bundleMap, item);
				}
			});
		});
	}
	
	private void populateBundleItemDetails(final List<WCSItemAttribute> bundlesAttributes, final Map<String, CartItem> bundleMap, final CartItem item) {
		bundlesAttributes.stream().findFirst().ifPresent(attrib -> {
			String bundleId = attrib.getAttributeValue();
			item.setBundleId(bundleId);
			BundleItemProperties bundleItemProperties = null;
			if(bundleMap.containsKey(bundleId)) {
				CartItem existingBundleItem = bundleMap.get(bundleId);
				bundleItemProperties = new BundleItemProperties();
				bundleItemProperties.setOrderItemId(item.getOrderItemId());
				existingBundleItem.getBundleOrderItems().add(bundleItemProperties);
				existingBundleItem.setOrderItemPrice(setPrecision(String.valueOf((Double.valueOf(existingBundleItem.getOrderItemPrice()) + Double.valueOf(item.getOrderItemPrice())))));
				existingBundleItem.setOrderItemDiscountedPrice(setPrecision(String.valueOf(Double.valueOf(existingBundleItem.getOrderItemDiscountedPrice()) + Double.valueOf(item.getOrderItemDiscountedPrice()))));
				if(null != item.getTotalAdjustment()) {
					if(existingBundleItem.getTotalAdjustment() == null) {
						existingBundleItem.setTotalAdjustment(new ArrayList<>());
					}
					Optional.ofNullable(item.getTotalAdjustment()).ifPresent(adjustments -> {
						adjustments.stream().forEach(adjustment -> {
							ItemAdjustment itmAdj = new ItemAdjustment();
							BeanUtils.copyProperties(adjustment, itmAdj);
							existingBundleItem.getTotalAdjustment().add(itmAdj);
						});
					});
				}
				
			}
			else {
				CartItem bundleItem = new CartItem();
				BeanUtils.copyProperties(item, bundleItem, excludeProperties);
				List<ItemAdjustment> bundleadjustmentList = new ArrayList<>();
				Optional.ofNullable(item.getTotalAdjustment()).ifPresent(adjustments -> {
					adjustments.stream().forEach(adjustment -> {
						ItemAdjustment itmAdj = new ItemAdjustment();
						BeanUtils.copyProperties(adjustment, itmAdj);
						bundleadjustmentList.add(itmAdj);
					});
				});
				bundleItem.setTotalAdjustment(bundleadjustmentList);
				bundleItem.setProductId(bundleId);
				bundleItemProperties = new BundleItemProperties();
				bundleItemProperties.setOrderItemId(item.getOrderItemId());
				bundleItem.getBundleOrderItems().add(bundleItemProperties);
				bundleMap.put(bundleId, bundleItem);
			}
		});
		logger.debug("bundle items map before product deatils {} ", bundleMap);
	}
	
	private void bundleItemAdjustment(final Map<String, CartItem> bundleMap) {
		bundleMap.values().stream().forEach(item -> {
			Optional.ofNullable(item.getTotalAdjustment()).ifPresent(adjustment -> {
				double itemlevelAdjustment = adjustment.stream().filter(Objects::nonNull)
						.filter(itemDiscount -> ITEM_DISCOUNT_FILTER_LEVEL.equals(itemDiscount.getDisplayLevel()))
						.collect(Collectors.toList()).stream().filter(Objects::nonNull)
						.mapToDouble(adj -> Double.valueOf(adj.getAmount())).sum();

				double orderlevelAdjustment = adjustment.stream().filter(Objects::nonNull)
						.filter(itemDiscount -> ORDER_DISCOUNT_FILTER_LEVEL.equals(itemDiscount.getDisplayLevel()))
						.collect(Collectors.toList()).stream().filter(Objects::nonNull)
						.mapToDouble(adj -> Double.valueOf(adj.getAmount())).sum();
				
				adjustment.clear();
				
				ItemAdjustment adj = new ItemAdjustment();
				adj.setAmount(setPrecision(String.valueOf(orderlevelAdjustment)));
				adj.setDisplayLevel(ORDER_DISCOUNT_FILTER_LEVEL);
				adjustment.add(adj);

				adj = new ItemAdjustment();
				adj.setAmount(setPrecision(String.valueOf(itemlevelAdjustment)));
				adj.setDisplayLevel(ITEM_DISCOUNT_FILTER_LEVEL);
				adjustment.add(adj);

				item.setTotalAdjustment(adjustment);

			});
		});
	}
	
	private void updateStoreIdForBopus(final CartDetails cartDetails) {
		cartDetails.getOrderItem().stream()
				.filter(oI -> oI.getOrderItemExtendAttribute() != null
						&& (SHIP_MODE_CODE_PICKUP_IN_STORE.equals(oI.getShipModeCode())
								|| SHIP_MODE_CODE_SHIP_TO_STORE.equals(oI.getShipModeCode())))
				.findFirst()
				.ifPresent(orderItem -> orderItem.getOrderItemExtendAttribute().stream()
						.filter(oiea -> PHYSICAL_STORE_ID.equals(oiea.getAttributeName())).findFirst()
						.ifPresent(oieaObj -> {
							logger.debug("store id received from order {} ", oieaObj.getAttributeValue());
							cartDetails.setStoreId(oieaObj.getAttributeValue());
							
						}));
		if (null == cartDetails.getStoreId()) {
			String storeIdFromCookies = CartUtils.getCookieValue(MDC.get(HttpHeaders.COOKIE), STOREID_COOKIE_NAME);
			logger.debug("store id received from cookies {} ", storeIdFromCookies);
			cartDetails.setStoreId(storeIdFromCookies);
		}
		padStoreIdWithZeros(cartDetails);
	}
	
	private boolean validateAvailableShippingMethods(final CartItem item, String shippingType) {
		List<AvailableShippingMethods> availableShippingMethods = item.getAvailableShippingMethods();
		if(null != availableShippingMethods && !availableShippingMethods.isEmpty()) {
			long count = item.getAvailableShippingMethods().stream()
					.filter(shipping -> shipping != null && shippingType.equals(shipping.getShippingType())).count();
			return count > 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		return false;
	}
	
	private void logJsonData(final String key , final String value) {
		if(enableJSONLogging) {
			logger.debug(key , value);
		}
	}
	
	private void setProductMessage(final CartItem item) {
		Optional.ofNullable(item.getProductDetails().getProductinfo()).ifPresent(productinfo -> {
			List<ProductMessage> productMessageList = new ArrayList<>();
			String isExist = "Y";
			if(isExist.equalsIgnoreCase(item.getIsSpecialOrder())) {
				populateProductMessage(CommonConstant.SPECIAL_ORDER_SHIPPING_TYPE.getName(), productMessageList);
			}else {
				if(isExist.equalsIgnoreCase(item.getIsDropShip())) {
					populateProductMessage(CommonConstant.DROP_SHIP_SHIPPING_TYPE.getName(), productMessageList);
				}
				if(isExist.equalsIgnoreCase(item.getIsHazmat())) {
					populateProductMessage(CommonConstant.HAZMAT_SHIPPING_TYPE.getName(), productMessageList);
					populateProductMessage(CommonConstant.AMMUNITION_MESSAGE.getName(), productMessageList);
				}
				if(isExist.equalsIgnoreCase(item.getIsWhileGlove())) {
					populateProductMessage(CommonConstant.WHILE_GLOVE_SHIPPING_TYPE.getName(), productMessageList);
				}
				if(isExist.equalsIgnoreCase(item.getIsHotMarketEnabled())) {
					populateProductMessage(CommonConstant.HOT_MARKET_SHIPPING_MESSAGE.getName(), productMessageList);
				}
				if(isExist.equalsIgnoreCase(item.getIsAssemblyRequired())) {
					populateProductMessage(CommonConstant.ASSEMBLY_REQUIRED_MESSAGE.getName(), productMessageList);
				}
			}
			if(StringUtils.isNotEmpty(item.getQuantityLimit())) {
				populateProductMessage(CommonConstant.QUANTITY_LIMIT.getName(), productMessageList);
			}
			productinfo.setProductMessage(productMessageList);
		});
	}
	
	private void populateProductMessage(String key, final List<ProductMessage> productMessageList) {
		ProductMessage productMessage = new ProductMessage();
		productMessage.setKey(key);
		productMessage.setValue(key);
		productMessageList.add(productMessage);
	}

}
