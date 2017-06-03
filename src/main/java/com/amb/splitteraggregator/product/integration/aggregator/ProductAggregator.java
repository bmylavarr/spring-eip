package com.amb.splitteraggregator.product.integration.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.amb.splitteraggregator.product.helper.ProductServiceHelper;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.ERROR;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.PRODUCTRESPONSEDATA;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.PRODUCTRESPONSEDATA.PRODUCT;
import com.amb.splitteraggregator.product.model.Composite;

@MessageEndpoint 
public class ProductAggregator {
	final static Logger logger = Logger.getLogger(ProductAggregator.class);
	
	@Autowired
	ProductServiceHelper productServiceHelper;
	
	@Aggregator(inputChannel="compositeMessagesForAggregation", outputChannel="upstreamProductResponses")
	public Message<Composite> generateAggregatedResponse(List<Message<Composite>> listOfCompositeMessagesForAggregation) {
		logger.info("generateAggregatedResponse :: START");
		
		PRODUCTRESPONSE productResponse = new PRODUCTRESPONSE();
		PRODUCTRESPONSEDATA productResponseData = new PRODUCTRESPONSEDATA();
		ERROR error = new ERROR();
		
		PRODUCTRESPONSE productResponseInComposite = null;
		List<PRODUCT> listOfProductsInComposite = null;
		
		List<PRODUCT> listOfProductsToReturn = new ArrayList<PRODUCT>();
		StringBuilder errorsToReturn = new StringBuilder();
		
		String uuid = null;
		
		for(Message<Composite> compositeMessage : listOfCompositeMessagesForAggregation) {
			productResponseInComposite = compositeMessage.getPayload().getProductResponse();
			if (null != productResponseInComposite.getPRODUCTRESPONSEDATA()) {
				uuid = productResponseInComposite.getPRODUCTRESPONSEDATA().getUuid();
				listOfProductsInComposite = productResponseInComposite.getPRODUCTRESPONSEDATA().getPRODUCT();
				listOfProductsToReturn.addAll(listOfProductsInComposite);
			} else if (null != productResponseInComposite.getERROR()) {
				errorsToReturn.append(productResponseInComposite.getERROR().getErrorMsgCode());
				errorsToReturn.append(",");
			}
		}
		
		if (null != listOfProductsToReturn && !listOfProductsToReturn.isEmpty()) {
			productResponseData.getPRODUCT().addAll(listOfProductsToReturn);
			productResponseData.setUuid(uuid);
			productResponse.setPRODUCTRESPONSEDATA(productResponseData);
		}
		
		if (errorsToReturn.length() != 0) {
			error.setErrorMsgCode(errorsToReturn.toString());
			error.setUuid(uuid);
			productResponse.setERROR(error);
		}
		
		Composite compositeWithAggregatedResponse = productServiceHelper.buildComposite(false, productResponse);
		Message<Composite> compositeMessageWithAggregatedResponse = MessageBuilder.withPayload(compositeWithAggregatedResponse).build();
		
		logger.info("generateAggregatedResponse :: END");
		return compositeMessageWithAggregatedResponse;
	}
	
	@CorrelationStrategy
	public UUID correlateByUUID(Message<Composite> compositeMessageForAggregation) {
		logger.info("correlateByUUID :: START");
		logger.info("Correlation by UUID done");
		logger.info("correlateByUUID :: END");
		UUID correlationUUID = null;
		
		if (null != compositeMessageForAggregation.getPayload().getProductResponse().getPRODUCTRESPONSEDATA()) {
			correlationUUID = UUID.fromString(compositeMessageForAggregation.getPayload().getProductResponse().getPRODUCTRESPONSEDATA().getUuid());
		} else if (null != compositeMessageForAggregation.getPayload().getProductResponse().getERROR()) {
			correlationUUID = UUID.fromString(compositeMessageForAggregation.getPayload().getProductResponse().getERROR().getUuid());
		}
		return correlationUUID;
	}
}
