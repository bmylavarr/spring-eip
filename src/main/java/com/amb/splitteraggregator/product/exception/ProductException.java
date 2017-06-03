package com.amb.splitteraggregator.product.exception;

public class ProductException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String message;
	
	public ProductException(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}
}