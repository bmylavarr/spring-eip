package com.amb.splitteraggregator.product.service;

import org.springframework.messaging.Message;

import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE;
import com.amb.splitteraggregator.product.model.Composite;
import com.amb.splitteraggregator.product.model.ProductRequestBatch;

public interface ProductService {
	public Message<Composite> validateAndTransformRequest(Message<String> productRequestMessage);

	public Message<PRODUCTRESPONSE> buildGatewayProductResponse(Message<Composite> compositeMessageWithResponse);

	public Message<Composite> generateRawProducts(Message<Composite> compositeMessageWithRequest);

	public Message<Composite> processProductRequestBatches(ProductRequestBatch productRequestBatch);
}