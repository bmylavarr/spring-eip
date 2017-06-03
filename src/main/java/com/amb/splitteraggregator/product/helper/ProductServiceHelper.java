package com.amb.splitteraggregator.product.helper;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.ERROR;
import com.amb.splitteraggregator.product.model.Composite;
import com.amb.splitteraggregator.product.model.RawProduct;

@Component
public class ProductServiceHelper {
	final static Logger logger = Logger.getLogger(ProductServiceHelper.class);

	public PRODUCTRESPONSE buildInvalidProductResponse(Exception e) {
		logger.info("buildInvalidProductResponse :: START");
		
		PRODUCTRESPONSE productResponse = new PRODUCTRESPONSE();
		ERROR error = new ERROR();

	    if (e instanceof JAXBException || e instanceof SAXException) {
			error.setErrorMsgCode("Ill-formed <PRODUCT_REQUEST> with reason :: " + e.getCause());
		} else {
			error.setErrorMsgCode(e.toString());
		}
		productResponse.setERROR(error);
		
		logger.info("buildInvalidProductResponse :: END");
		return productResponse;
	}
	
	public PRODUCTRESPONSE buildInvalidProductResponse(String uuid, Exception e) {
		logger.info("buildInvalidProductResponse :: START");
		
		PRODUCTRESPONSE productResponse = new PRODUCTRESPONSE();
		ERROR error = new ERROR();

		if (e instanceof JAXBException || e instanceof SAXException) {
			error.setErrorMsgCode("Ill-formed <PRODUCT_REQUEST> with reason :: " + e.getCause());
		} else {
			error.setErrorMsgCode(e.toString());
		}
		error.setUuid(uuid);
		productResponse.setERROR(error);
		
		logger.info("buildInvalidProductResponse :: END");
		return productResponse;
	}
	
	public Composite buildComposite(boolean isError, Object... jaxbObjects) {
		logger.info("buildComposite :: START");
		
		Composite composite = null;
		
		if (isError) {
			composite = new Composite(isError, (PRODUCTRESPONSE) jaxbObjects[0]);
		} else {
			if (jaxbObjects.length > 1){
				composite = new Composite(isError, (PRODUCTREQUEST) jaxbObjects[0], (List<RawProduct>) jaxbObjects[1]);
			} else {
				if (jaxbObjects[0] instanceof PRODUCTREQUEST) {
					composite = new Composite(isError, (PRODUCTREQUEST) jaxbObjects[0]);
				} else if (jaxbObjects[0] instanceof PRODUCTRESPONSE) {
					composite = new Composite(isError, (PRODUCTRESPONSE) jaxbObjects[0]);
				} else { 
					composite = new Composite(isError, (List<RawProduct>) jaxbObjects[0]);
				}
			}
		}
		
		logger.info("buildComposite :: END");
		return composite;
	}
}
