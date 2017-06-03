package com.amb.splitteraggregator.cafe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amb.splitteraggregator.cafe.Cafe;
import com.amb.splitteraggregator.cafe.DrinkType;
import com.amb.splitteraggregator.cafe.Order;

/**
 * Handles requests for the application home page.
 */
@Controller
public class CafeController {
	
	@Autowired
	Cafe cafe;
	
	private static final Logger logger = LoggerFactory.getLogger(CafeController.class);
	
	@RequestMapping(value = "/order", method = RequestMethod.GET)
	public String order() {
		logger.info("Start :: order()");
		
		for (int i = 1; i <= 100; i++) {
			Order order = new Order(i);
			order.addItem(DrinkType.LATTE, 2, false);
			order.addItem(DrinkType.MOCHA, 3, true);
			cafe.placeOrder(order);
		}
		
		return "success";
	}
}
