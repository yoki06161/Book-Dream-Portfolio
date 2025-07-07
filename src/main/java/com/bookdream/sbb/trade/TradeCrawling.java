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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TradeCrawling {

    private static final Logger logger = LoggerFactory.getLogger(TradeCrawling.class);
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
                if (!detailElements.isEmpty()) {
                    details = detailElements.get(0).text(); // 저자, 옮긴이, 출판사 정보
                }
                String priceText = "0"; // 기본값 설정
                Element priceListItem = book.select("li:contains(원 →)").first();
                if (priceListItem != null) {
                    // <li> 요소의 전체 텍스트를 가져옵니다. (예: "8,000원 → 7,200원...")
                    String fullPriceText = priceListItem.text();

                    // "→" 기호 앞부분이 정가이므로, 이를 기준으로 텍스트를 분리합니다.
                    priceText = fullPriceText.split("→")[0].trim();
                } else {
                    // 만약 할인 정보(→)가 없는 책이라면, 첫 번째로 나오는 가격을 정가로 간주합니다.
                    Element regularPriceItem = book.select("li:contains(원)").first();
                    if(regularPriceItem != null) {
                        priceText = regularPriceItem.text().split("원")[0].trim();
                    }
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
            logger.error("알라딘 도서 정보 크롤링 중 오류 발생. Keyword: {}", keyword, e);
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

