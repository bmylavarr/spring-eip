package com.amb.splitteraggregator.product.integration.router;

import org.apache.log4j.Logger;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;
import org.springframework.messaging.Message;

import com.amb.splitteraggregator.product.model.Composite;

@MessageEndpoint
public class ValidatedRequestRouter {
	final static Logger logger = Logger.getLogger(ValidatedRequestRouter.class);
	
	@Router(inputChannel="compositeMessagesWithRequestOrResponse")
	public String routeValidatedRequest(Message<Composite> compositeMessageWithRequestOrResponse) {
		logger.info("routeValidatedRequest :: START");
		String targetRoute = null;
		if (compositeMessageWithRequestOrResponse.getPayload().isError()) {
			targetRoute = "upstreamProductResponses";
		} else {
			targetRoute = "downStreamProductRequests";
		}
		
		logger.info("routeValidatedRequest :: END");
		return targetRoute;
	}
}
