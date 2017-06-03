package com.amb.splitteraggregator.product.service.impl;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.xml.sax.SAXException;

import com.amb.splitteraggregator.product.exception.ProductException;
import com.amb.splitteraggregator.product.helper.ProductServiceHelper;
import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST;
import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST.PRODUCTREQUESTDATA.PRODUCT;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE;
import com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.PRODUCTRESPONSEDATA;
import com.amb.splitteraggregator.product.model.Composite;
import com.amb.splitteraggregator.product.model.EligibleProduct;
import com.amb.splitteraggregator.product.model.ProductRequestBatch;
import com.amb.splitteraggregator.product.model.RawProduct;
import com.amb.splitteraggregator.product.service.ProductService;

@MessageEndpoint
public class ProductServiceImpl implements ProductService {
	
	final static Logger logger = Logger.getLogger(ProductServiceImpl.class);
	
	@Autowired
	ServletContext servletContext;
	
	@Autowired
	ProductServiceHelper productServiceHelper;
	
	@Override
	@Transformer(inputChannel="gatewayProductRequests", outputChannel="compositeMessagesWithRequestOrResponse")
	public Message<Composite> validateAndTransformRequest(Message<String> productRequestMessage) {
		logger.info("validateAndTransformRequest :: START");
		
		PRODUCTREQUEST productRequest = null;
		PRODUCTRESPONSE productResponse = null;
		Composite compositeWithRequestOrResponse = null;
		
		String productRequestStr = productRequestMessage.getPayload();
		logger.info("Payload retrieved :: " + productRequestStr);
		
		try {
			productRequest = processProductRequest(servletContext, productRequestStr);
			compositeWithRequestOrResponse = productServiceHelper.buildComposite(false, productRequest);
		} catch (Exception e) {
			productResponse = productServiceHelper.buildInvalidProductResponse(e);
			compositeWithRequestOrResponse = productServiceHelper.buildComposite(true, productResponse);
		}
		
		if (null != productRequest) {
			try{
			    UUID.fromString(productRequest.getPRODUCTREQUESTDATA().getUuid());
			} catch (IllegalArgumentException iae){
				productResponse = productServiceHelper.buildInvalidProductResponse(new ProductException("Invalid UUID in the Request - Retry with a programmatically generated UUID"));
				compositeWithRequestOrResponse = productServiceHelper.buildComposite(true, productResponse);
			}
		}
		
		Message<Composite> compositeMessageWithRequestOrResponse = MessageBuilder.withPayload(compositeWithRequestOrResponse).copyHeadersIfAbsent(productRequestMessage.getHeaders()).build();
		
		logger.info("validateAndTransformRequest :: END");
		return compositeMessageWithRequestOrResponse;
	}

	private PRODUCTREQUEST processProductRequest(ServletContext servletContext, String productRequestString) throws Exception {
		logger.info("processProductRequest :: START");
		PRODUCTREQUEST productRequest = null;
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		
		try{
			schema = schemaFactory.newSchema(new StreamSource(new File(servletContext.getRealPath("/WEB-INF/xsd/ProductRequest.xsd"))));
		} catch (SAXException se) {
			se.printStackTrace();
			throw se;
		}
		
		JAXBContext jaxbContext = null;
		try {
			jaxbContext = JAXBContext
			.newInstance("com.amb.splitteraggregator.product.jaxb.request");
		} catch (JAXBException jxbe) {
			jxbe.printStackTrace();
			throw jxbe;
		}
		
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException jxbe) {
			jxbe.printStackTrace();
			throw jxbe;
		}
		
		unmarshaller.setSchema(schema);
		
		StreamSource streamSource = new StreamSource(new StringReader(productRequestString));
		
		try {
			productRequest = (PRODUCTREQUEST) JAXBIntrospector
			.getValue(unmarshaller.unmarshal(streamSource, PRODUCTREQUEST.class));
		} catch (JAXBException jxbe) {
			jxbe.printStackTrace();
			throw jxbe;
		}
		
		logger.info("processProductRequest :: END");
		return productRequest;
	}
	
	@Override
	@ServiceActivator(inputChannel="downStreamProductRequests", outputChannel="compositeMessagesWithRawProductsAndRequestOrResponse")
	public Message<Composite> generateRawProducts(Message<Composite> compositeMessageWithRequest)  {
		logger.info("generateRawProducts :: START");
		
		Composite compositeWithRawProductsAndRequestOrResponse = null;
		PRODUCTRESPONSE productResponse = null;
		Message<Composite> compositeMessageWithRawProductsAndRequestOrResponse = null;
		
		PRODUCTREQUEST productRequest = compositeMessageWithRequest.getPayload().getProductRequest();
		Map<UUID, String> uuidStringIDMap = new HashMap<UUID, String>();
		
		List<PRODUCT> productsinRequest = productRequest.getPRODUCTREQUESTDATA().getPRODUCT();
		List<RawProduct> rawProductsList = new ArrayList<RawProduct>();
		RawProduct rawProduct = null; 
		
		if (productsinRequest.isEmpty()) {
			productResponse = productServiceHelper.buildInvalidProductResponse(productRequest.getPRODUCTREQUESTDATA().getUuid(), new ProductException("No Product in the request"));
			compositeWithRawProductsAndRequestOrResponse = productServiceHelper.buildComposite(true, productResponse);

			compositeMessageWithRawProductsAndRequestOrResponse = MessageBuilder.withPayload(compositeWithRawProductsAndRequestOrResponse).build();
			return compositeMessageWithRawProductsAndRequestOrResponse;
		}
		
		for(PRODUCT productInRequest : productsinRequest) {
			rawProduct = new RawProduct();
			rawProduct.setColor(productInRequest.getColor());
			rawProduct.setName(productInRequest.getName());
			rawProduct.setUnitCost(productInRequest.getUnitCost());
			rawProduct.setQuantity(productInRequest.getQuantity());
			
			rawProductsList.add(rawProduct);
		}
		
		compositeWithRawProductsAndRequestOrResponse = productServiceHelper.buildComposite(false, productRequest, rawProductsList);
		compositeMessageWithRawProductsAndRequestOrResponse = MessageBuilder.withPayload(compositeWithRawProductsAndRequestOrResponse).copyHeadersIfAbsent(compositeMessageWithRequest.getHeaders()).build();
		
		logger.info("generateRawProducts :: END");
		return compositeMessageWithRawProductsAndRequestOrResponse;
	}

	@Override
	@ServiceActivator(inputChannel = "downStreamProductRequestBatches", outputChannel = "compositeMessagesForAggregation")
	public Message<Composite> processProductRequestBatches(ProductRequestBatch productRequestBatch) {
		logger.info("processProductRequestBatches :: START");
		boolean isInvalidBatch = false;	
		
		Composite compositeForAggregation = null;
		Message<Composite> compositeMessageForAggregation = null;
		
		PRODUCTRESPONSE productResponse = new PRODUCTRESPONSE();
		PRODUCTRESPONSEDATA productResponseData = new PRODUCTRESPONSEDATA();
		com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.PRODUCTRESPONSEDATA.PRODUCT productInResponse = null;
		
		for(EligibleProduct eligibleProduct : productRequestBatch.getListOfEligibleProducts()) {
			if (eligibleProduct.getColor().equalsIgnoreCase("Floro")) {
				isInvalidBatch = true;
			}
		}

		if (isInvalidBatch) {
			productResponse = productServiceHelper.buildInvalidProductResponse(productRequestBatch.getBatchUUID().toString(), new ProductException("Product has Floro as colour - illegal"));
			compositeForAggregation = productServiceHelper.buildComposite(true, productResponse);

			compositeMessageForAggregation = MessageBuilder.withPayload(compositeForAggregation).build();
			return compositeMessageForAggregation;
		}
		
		for(EligibleProduct eligibleProductInList : productRequestBatch.getListOfEligibleProducts()) {
			productInResponse = new com.amb.splitteraggregator.product.jaxb.response.PRODUCTRESPONSE.PRODUCTRESPONSEDATA.PRODUCT();
			productInResponse.setColor(eligibleProductInList.getColor());
			productInResponse.setName(eligibleProductInList.getName());
			productInResponse.setTotalCost(eligibleProductInList.getQuantity().multiply(eligibleProductInList.getUnitCost()));
			
			productResponseData.getPRODUCT().add(productInResponse);
			productResponseData.setUuid(productRequestBatch.getProductRequest().getPRODUCTREQUESTDATA().getUuid());
		}
		
		productResponse.setPRODUCTRESPONSEDATA(productResponseData);
		
		compositeForAggregation = productServiceHelper.buildComposite(false, productResponse);
		compositeMessageForAggregation = MessageBuilder.withPayload(compositeForAggregation).build();
		
		logger.info("processProductRequestBatches :: END");
		
		return compositeMessageForAggregation;
	}

	@Override
	@Transformer(inputChannel="upstreamProductResponses", outputChannel="gatewayProductResponses")
	public Message<PRODUCTRESPONSE> buildGatewayProductResponse(Message<Composite> compositeMessageWithResponse) {
		logger.info("buildGatewayProductResponse :: START");
		
		PRODUCTRESPONSE productResponse = compositeMessageWithResponse.getPayload().getProductResponse();
		Message<PRODUCTRESPONSE> productResponseMessage;
		
		productResponseMessage = MessageBuilder.withPayload(productResponse).copyHeadersIfAbsent(compositeMessageWithResponse.getHeaders()).setHeader("http_statusCode", HttpStatus.OK).build();
		
		logger.info("buildGatewayProductResponse :: END");
		
		return productResponseMessage;
	}
}