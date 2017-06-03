package com.amb.splitteraggregator.product.integration.router;

import org.apache.log4j.Logger;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;
import org.springframework.messaging.Message;

import com.amb.splitteraggregator.product.model.Composite;

@MessageEndpoint
public class RawProductRouter {
	final static Logger logger = Logger.getLogger(RawProductRouter.class);
	
	@Router(inputChannel="compositeMessagesWithRawProductsAndRequestOrResponse")
	public String routeRawProducts(Message<Composite> compositeWithRawProductsAndRequestOrResponse) {
		logger.info("routeRawProducts :: START");
		
		String targetRoute = null;
		if (compositeWithRawProductsAndRequestOrResponse.getPayload().isError()) {
			targetRoute = "upstreamProductResponses";
		} else {
			targetRoute = "downStreamRawProductsAndRequest";
		}
		
		logger.info("routeRawProducts :: END");
		return targetRoute;
	}
}
