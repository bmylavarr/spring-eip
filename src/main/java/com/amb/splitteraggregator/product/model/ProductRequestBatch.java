package com.amb.splitteraggregator.product.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST;

public class ProductRequestBatch {
	private UUID batchUUID;
	private List<EligibleProduct> listOfEligibleProducts;
	private PRODUCTREQUEST productRequest;
	private Map<UUID, String> uuidRequestIDMap;
	
	public UUID getBatchUUID() {
		return batchUUID;
	}
	public void setBatchUUID(UUID batchUUID) {
		this.batchUUID = batchUUID;
	}
	public List<EligibleProduct> getListOfEligibleProducts() {
		return listOfEligibleProducts;
	}
	public void setListOfEligibleProducts(List<EligibleProduct> listOfEligibleProducts) {
		this.listOfEligibleProducts = listOfEligibleProducts;
	}
	public PRODUCTREQUEST getProductRequest() {
		return productRequest;
	}
	public void setProductRequest(PRODUCTREQUEST productRequest) {
		this.productRequest = productRequest;
	}
	public Map<UUID, String> getUuidRequestIDMap() {
		return uuidRequestIDMap;
	}
	public void setUuidRequestIDMap(Map<UUID, String> uuidRequestIDMap) {
		this.uuidRequestIDMap = uuidRequestIDMap;
	}
}
