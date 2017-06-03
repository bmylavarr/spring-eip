package com.amb.splitteraggregator.product.model;

import java.math.BigInteger;
import java.util.UUID;

/**
 * @author bharath.am
 *
 */
public class EligibleProduct {
	private UUID batchUUID;
	
	private String name;
	private String color;
	private BigInteger unitCost;
	private BigInteger quantity;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public BigInteger getUnitCost() {
		return unitCost;
	}
	public void setUnitCost(BigInteger unitCost) {
		this.unitCost = unitCost;
	}
	public BigInteger getQuantity() {
		return quantity;
	}
	public void setQuantity(BigInteger quantity) {
		this.quantity = quantity;
	}
	public UUID getBatchUUID() {
		return batchUUID;
	}
	public void setBatchUUID(UUID batchUUID) {
		this.batchUUID = batchUUID;
	}
	
}