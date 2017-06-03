package com.amb.splitteraggregator.product.model;

import java.util.ArrayList;
import java.util.List;

import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE;

public class Composite {
	private PRODUCTREQUEST productRequest;
	private PRODUCTRESPONSE productResponse;
	private List<RawProduct> listOfRawProducts;
	private boolean isError;
	
	public Composite(boolean isError, PRODUCTREQUEST productRequest) {
		this.isError = isError;
		this.productRequest = productRequest;
	}
	
	public Composite(boolean isError, PRODUCTRESPONSE productResponse) {
		this.isError = isError;
		this.productResponse = productResponse;
	}
	
	public Composite(boolean isError, List<RawProduct> listOfRawProducts) {
		this.isError = isError;
		this.listOfRawProducts = new ArrayList<RawProduct>(listOfRawProducts);
	}
	
	public Composite(boolean isError, PRODUCTREQUEST productRequest, List<RawProduct> listOfRawProducts) {
		this.isError = isError;
		this.productRequest = productRequest;
		this.listOfRawProducts = new ArrayList<RawProduct>(listOfRawProducts);
	}
	
	public PRODUCTREQUEST getProductRequest() {
		return productRequest;
	}

	public PRODUCTRESPONSE getProductResponse() {
		return productResponse;
	}
	
	public boolean isError() {
		return isError;
	}
	
	public List<RawProduct> getListOfRawProducts() {
		return this.listOfRawProducts;
	}
}
