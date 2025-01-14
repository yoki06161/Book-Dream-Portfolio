package com.bookdream.sbb.basket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class BasketService {
    @Autowired
    private BasketRepository basketRepository;

    @Transactional
    public void saveBasketItems(List<Map<String, Object>> sessionData, String email) {
        List<Basket> basketsToSave = sessionData.stream()
                .map(data -> {
                    Integer bookId = Integer.valueOf(data.get("book_id").toString());
                    Optional<Basket> existingBasket = basketRepository.findByBookIdAndEmail(bookId, email);
                    if (existingBasket.isEmpty()) {
                        Basket basket = new Basket();
                        basket.setEmail(email);
                        basket.setBook_id(bookId);
                        basket.setCount(Integer.valueOf(data.get("count").toString()));
                        basket.setCount_price(data.get("count_price").toString());
                        return basket;
                    } else {
                        return null; // 중복된 항목은 제외
                    }
                })
                .filter(basket -> basket != null) // null 값을 제외
                .collect(Collectors.toList());

        if (!basketsToSave.isEmpty()) {
            basketRepository.saveAll(basketsToSave);
        }
    }
    
    @Transactional
    public void deleteBasketItems(Integer book_id, String email) {
        basketRepository.deleteByBookIdAndEmail(book_id, email);
    }

//	public List<Basket> getItemsByEmail(String email) {
//		return basketRepository.getItemsByEmail(email);
//	}
}