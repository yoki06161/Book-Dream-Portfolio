package com.bookdream.sbb.trade;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final TradeCrawling tradeCrawling;


    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Trade> paging = tradeService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "trade/list";
    }

    @GetMapping("/detail/{idx}")
    public String detailTrade(@PathVariable("idx") int idx, Model model, Principal principal) {
        Trade trade = tradeService.getTradeById(idx);
        model.addAttribute("trade", trade);

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
        }
        
        String authorEmail = trade.getId();
        String Writer = tradeService.getUsername(authorEmail);
        model.addAttribute("Writer", Writer);
        return "trade/detail";
    }

    @PostMapping("/updateStatus/{idx}")
    public String updateStatus(@PathVariable("idx") int idx, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        try {
            Trade trade = tradeService.getTradeById(idx);
            trade.setStatus(status);
            tradeService.updateTrade(idx, trade);
            redirectAttributes.addFlashAttribute("successMsg", "상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            logger.error("상태 업데이트 중 오류 발생. idx: {}, status: {}", idx, status, e);
            redirectAttributes.addFlashAttribute("errorMsg", "상태 변경에 실패했습니다.");
        }
        return "redirect:/trade/detail/" + idx;
    }

    @GetMapping("/create")
    public String createTradeForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        String username = principal.getName();
        model.addAttribute("username", username);
        model.addAttribute("trade", new Trade());
        return "trade/create";
    }

    @PostMapping("/create")
    public String createTrade(@ModelAttribute Trade trade, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            String username = principal.getName();
            trade.setId(username);

            if (trade.getImage() != null && !trade.getImage().isEmpty()) {
                String imageUrl = trade.getImage();
                String fileName = UUID.randomUUID().toString() + ".jpg";

                // 이미지 저장 폴더가 없으면 생성하는 로직
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String filePath = uploadDir + fileName;

                try (InputStream in = new URL(imageUrl).openStream()) {
                    Files.copy(in, Paths.get(filePath));
                    trade.setImage(fileName);
                } catch (IOException e) {
                    logger.error("이미지 파일 저장 실패. URL: {}, Path: {}", imageUrl, filePath, e);
                    redirectAttributes.addFlashAttribute("errorMsg", "이미지 저장에 실패했습니다. 설정된 파일 경로를 확인해주세요.");
                    return "redirect:/trade/create";
                }
            }

            tradeService.createTrade(trade);
            redirectAttributes.addFlashAttribute("successMsg", "상품 등록에 성공했습니다.");
            return "redirect:/trade/list";
        } catch (DataAccessException e) { // DB 관련 예외를 구체적으로 명시
                logger.error("데이터베이스 저장 실패. 사용자: {}", principal.getName(), e);
                redirectAttributes.addFlashAttribute("errorMsg", "상품을 등록하는 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
                return "redirect:/trade/create";
        } catch (Exception e) {
            logger.error("상품 등록 중 알 수 없는 오류 발생. 사용자: {}, 오류: {}", principal.getName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", "상품 등록 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            return "redirect:/trade/create";
        }
    }

    @GetMapping("/edit/{idx}")
    public String editTradeForm(@PathVariable("idx") int idx, Model model) {
        Trade trade = tradeService.getTradeById(idx);
        model.addAttribute("trade", trade);
        return "trade/edit";
    }

    @PostMapping("/edit/{idx}")
    public String updateTrade(@PathVariable("idx") int idx, @ModelAttribute Trade updatedTrade, @RequestParam("useNewImage") boolean useNewImage, RedirectAttributes redirectAttributes) {
        try {
            Trade existingTrade = tradeService.getTradeById(idx);

            if (useNewImage) {
                // 크롤링된 이미지 URL을 로컬에 저장
                if (updatedTrade.getImage() != null && !updatedTrade.getImage().isEmpty()) {
                    String imageUrl = updatedTrade.getImage();
                    String fileName = UUID.randomUUID().toString() + ".jpg";
                    String filePath = uploadDir + fileName;

                    // 기존 이미지 삭제
                    if (existingTrade.getImage() != null && !existingTrade.getImage().isEmpty()) {
                        File existingFile = new File(uploadDir + existingTrade.getImage());
                        if (existingFile.exists()) {
                            if (!existingFile.delete()) {
                                logger.warn("기존 이미지 파일을 삭제하지 못했습니다: {}", existingFile.getPath());
                            }
                        }
                    }

                    // 이미지 다운로드 및 저장
                    try (InputStream in = new URL(imageUrl).openStream()) {
                        Files.copy(in, Paths.get(filePath));
                    }

                    updatedTrade.setImage(fileName); // 로컬 파일 이름으로 설정
                }
            } else {
                updatedTrade.setImage(existingTrade.getImage());
            }

            // 기존 데이터 유지
            if (updatedTrade.getOriginalPrice() == 0) {
                updatedTrade.setOriginalPrice(existingTrade.getOriginalPrice()); // 기존 정가 유지
            }

            tradeService.updateTrade(idx, updatedTrade);
            redirectAttributes.addFlashAttribute("successMsg", "상품 수정에 성공했습니다.");
            return "redirect:/trade/detail/" + idx;
        } catch (Exception e) {
            logger.error("상품 수정 중 오류 발생. idx: {}", idx, e);
            redirectAttributes.addFlashAttribute("errorMsg", "상품 수정에 실패했습니다.");
            return "redirect:/trade/edit/" + idx;
        }
    }

    @GetMapping("/delete/{idx}")
    public String deleteTrade(@PathVariable("idx") int idx, RedirectAttributes redirectAttributes) {
        try {
            Trade trade = tradeService.getTradeById(idx);
            if (trade.getImage() != null && !trade.getImage().isEmpty()) {
                File imageFile = new File(uploadDir + trade.getImage());
                if (imageFile.exists()) {
                    if (!imageFile.delete()) {
                        logger.warn("이미지 파일을 삭제하지 못했습니다: {}", imageFile.getPath());
                    }
                }
            }
            tradeService.deleteTrade(idx);
            redirectAttributes.addFlashAttribute("successMsg", "상품 삭제에 성공했습니다.");
        } catch (Exception e) {
            logger.error("상품 삭제 중 오류 발생. idx: {}", idx, e);
            redirectAttributes.addFlashAttribute("errorMsg", "상품 삭제에 실패했습니다.");
        }
        return "redirect:/trade/list";
    }

    @GetMapping("/book")
    @ResponseBody
    public ResponseEntity<Trade> getBookInfo(@RequestParam(name = "title") String title) {
        List<Trade> trades = tradeCrawling.searchBooks(title);

        if (!trades.isEmpty()) {
            Trade trade = trades.get(0);
            return ResponseEntity.ok(trade);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
