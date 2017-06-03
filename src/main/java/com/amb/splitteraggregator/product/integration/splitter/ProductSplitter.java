package com.amb.splitteraggregator.product.integration.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.messaging.Message;

import com.amb.splitteraggregator.product.jaxb.request.PRODUCTREQUEST;
import com.amb.splitteraggregator.product.model.Composite;
import com.amb.splitteraggregator.product.model.EligibleProduct;
import com.amb.splitteraggregator.product.model.ProductRequestBatch;
import com.amb.splitteraggregator.product.model.RawProduct;

@MessageEndpoint
public class ProductSplitter {

	final static Logger logger = Logger.getLogger(ProductSplitter.class);

	final int batchSize = 2;

	@Splitter(inputChannel = "downStreamRawProductsAndRequest", outputChannel = "downStreamProductRequestBatches")
	public List<ProductRequestBatch> splitIntoProductBatches(Message<Composite> compositeMessageWithRawProducts) {
		logger.info("splitIntoProductBatches :: START");
		
		List<RawProduct> listOfRawProducts = new ArrayList<RawProduct>(
				compositeMessageWithRawProducts.getPayload().getListOfRawProducts());
		PRODUCTREQUEST productRequest = compositeMessageWithRawProducts.getPayload().getProductRequest();
		String batchUUID = productRequest.getPRODUCTREQUESTDATA().getUuid();

		List<ProductRequestBatch> listOfProductBatches = new ArrayList<ProductRequestBatch>();
		List<EligibleProduct> listOfEligibleProducts = new ArrayList<EligibleProduct>();
		ProductRequestBatch productRequestBatch = null;
		EligibleProduct eligibleProduct = null;

		for (RawProduct rawProduct : listOfRawProducts) {
			if (listOfEligibleProducts.size() < this.batchSize) {
				eligibleProduct = new EligibleProduct();
				eligibleProduct.setBatchUUID(UUID.fromString(batchUUID));
				eligibleProduct.setColor(rawProduct.getColor());
				eligibleProduct.setName(rawProduct.getName());
				eligibleProduct.setQuantity(rawProduct.getQuantity());
				eligibleProduct.setUnitCost(rawProduct.getUnitCost());

				listOfEligibleProducts.add(eligibleProduct);
				continue;
			}

			productRequestBatch = new ProductRequestBatch();
			productRequestBatch.setBatchUUID(UUID.fromString(batchUUID));
			productRequestBatch.setListOfEligibleProducts(listOfEligibleProducts);
			productRequestBatch.setProductRequest(productRequest);
			listOfProductBatches.add(productRequestBatch);

			listOfEligibleProducts = new ArrayList<EligibleProduct>();

			eligibleProduct = new EligibleProduct();
			eligibleProduct.setBatchUUID(UUID.fromString(batchUUID));
			eligibleProduct.setColor(rawProduct.getColor());
			eligibleProduct.setName(rawProduct.getName());
			eligibleProduct.setQuantity(rawProduct.getQuantity());
			eligibleProduct.setUnitCost(rawProduct.getUnitCost());

			listOfEligibleProducts.add(eligibleProduct);
		}

		productRequestBatch = new ProductRequestBatch();
		productRequestBatch.setBatchUUID(UUID.fromString(batchUUID));
		productRequestBatch.setListOfEligibleProducts(listOfEligibleProducts);
		productRequestBatch.setProductRequest(productRequest);
		listOfProductBatches.add(productRequestBatch);

		logger.info("splitIntoProductBatches :: END");
		return listOfProductBatches;
	}
}