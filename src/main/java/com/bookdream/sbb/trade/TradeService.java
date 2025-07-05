package com.bookdream.sbb.trade;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookdream.sbb.DataNotFoundException;
import com.bookdream.sbb.user.SiteUser;
import com.bookdream.sbb.user.UserRepository;

@Service
@RequiredArgsConstructor
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    public Page<Trade> getList(int page, String kw) {
        Pageable pageable = PageRequest.of(page, 10);
        if (!kw.isEmpty()) {
            return tradeRepository.findAllByKeyword(kw, pageable);
        } else {
            return tradeRepository.findAllByOrderByPostdateDesc(pageable);
        }
    }

    public Trade getTradeById(int idx) {
        Optional<Trade> trade = tradeRepository.findById(idx);
        return trade.orElse(null);
    }

    public String getUsername(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get().getUsername();
        } else {
            throw new DataNotFoundException("siteuser not found!!");
        }
    }
    
    @Transactional
    public void createTrade(Trade trade) {
        if (trade.getPostdate() == null) {
            trade.setPostdate(LocalDateTime.now());
        }
        try {
            tradeRepository.save(trade);
            System.out.println("Trade saved successfully: " + trade);
        } catch (Exception e) {
            System.out.println("Error while saving trade: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void updateTrade(int idx, Trade updatedTrade) {
        Optional<Trade> optionalTrade = tradeRepository.findById(idx);
        if (optionalTrade.isPresent()) {
            Trade trade = optionalTrade.get();
            trade.setTitle(updatedTrade.getTitle());
            trade.setPrice(updatedTrade.getPrice());
            trade.setInfo(updatedTrade.getInfo());
            trade.setIntro(updatedTrade.getIntro());
            trade.setImage(updatedTrade.getImage());
            trade.setGrade(updatedTrade.getGrade());
            trade.setOriginalPrice(updatedTrade.getOriginalPrice());
            try {
                tradeRepository.save(trade);
            } catch (Exception e) {
                logger.error("Trade 업데이트 중 오류 발생: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

    @Transactional
    public void deleteTrade(int idx) {
        try {
            tradeRepository.deleteById(idx);
            System.out.println("Trade deleted successfully: " + idx);
        } catch (Exception e) {
            System.out.println("Error while deleting trade: " + e.getMessage());
            throw e;
        }
    }
    
    public Trade getTradeByTitle(String title) {
        return tradeRepository.findByTitle(title);
    }
}