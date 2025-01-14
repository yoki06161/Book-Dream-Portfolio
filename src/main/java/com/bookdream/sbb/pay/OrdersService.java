package com.bookdream.sbb.pay;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookdream.sbb.basket.Basket;

@Service
public class OrdersService {
	@Autowired
	private OrdersRepository ordersRepository;

	public void saveOrdersItems(List<Map<String, Object>> sessionData) {
	    List<Orders> ordersToSave = sessionData.stream()
	            .map(data -> {
	                Orders order = new Orders();
	                // 타입 변환 시 NumberFormatException을 피하기 위해 parseInt 사용
	                order.setBook_id(Integer.parseInt(data.get("book_id").toString()));
	                order.setPay_id((String) data.get("pay_id"));
	                order.setCount(Integer.parseInt(data.get("count").toString()));
	                order.setCount_price(data.get("count_price").toString());
	                return order;
	            })
	            .collect(Collectors.toList());

	    ordersRepository.saveAll(ordersToSave);
	}

	public List<Orders> getOrdersById(String pay_id) {
		return ordersRepository.findByPayId(pay_id);
	}

	public void deleteOrdersByPayId(String pay_id) {
		ordersRepository.deleteAllByPayId(pay_id);
	}
}