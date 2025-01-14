package com.bookdream.sbb.trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class TradeCrawling {

    private static final String URL = "https://www.aladin.co.kr/search/wsearchresult.aspx?SearchTarget=Book&SearchWord=";

    public List<Trade> searchBooks(String keyword) {
        List<Trade> trades = new ArrayList<>();
        try {
            String searchUrl = URL + keyword;
            Document doc = getHtml(searchUrl);
            Elements books = doc.select("div.ss_book_box");

            for (Element book : books) {
                String title = book.select("a.bo3").text(); // 책 제목
                String details = "Unknown"; // 기본값 설정
                
                Elements detailElements = book.select("li:contains(지은이)");
                if (detailElements.size() > 0) {
                    details = detailElements.get(0).text(); // 저자, 옮긴이, 출판사 정보
                }
                String priceText = "0"; // 기본값 설정
                Elements priceElements = book.select("li span.ss_p2 b span");
                if (priceElements.size() > 0) {
                    priceText = priceElements.get(0).text(); // 가격 정보
                }
                
                String imgUrl = book.select("div.flipcover_in img.front_cover").attr("src"); // 이미지 URL

                Trade trade = new Trade();
                trade.setTitle(title);
                trade.setInfo(details);
                trade.setPrice(0); // 판매가 초기화
                trade.setOriginalPrice(parsePrice(priceText)); // 정가 설정
                trade.setImage(imgUrl);
                trade.setIntro("");
                trades.add(trade);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trades;
    }

    private Document getHtml(String url) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            return executor.submit(() -> Jsoup.connect(url).get()).get();
        } catch (Exception e) {
            throw new IOException("Failed to fetch HTML from " + url, e);
        } finally {
            executor.shutdown();
        }
    }

    private int parsePrice(String priceText) {
        try {
            return Integer.parseInt(priceText.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

