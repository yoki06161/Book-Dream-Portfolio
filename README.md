# Book-Dream (개인 포트폴리오용)

> **이 레포지토리는 원래 팀 프로젝트 ([원본 링크](https://github.com/yoki06161/Book-Dream))를**  
> **개인 포트폴리오 용도로 재구성**한 것입니다.

<br/>

## 개요

**Book-Dream**은 **중고 책 거래** 기능과 **실시간 채팅(WebSocket)** 기능을 제공하는 웹 애플리케이션입니다.

- **주요 목적**  
  1. 책을 등록/판매/구매하는 **온라인 중고거래**  
  2. 거래 상대방과 **실시간 채팅**(WebSocket)으로 소통

- **사용 기술 스택**  
  - **Backend**: Java, Spring Boot, Spring Data JPA, MySQL  
  - **Frontend**: Thymeleaf, HTML, CSS, JavaScript  
  - **실시간 통신**: WebSocket (SockJS, STOMP)  
  - **크롤링**: Jsoup (알라딘 사이트 도서 정보 수집)
  - **로깅**: SLF4J, Logback
  - **빌드 및 협업**: Gradle, Git, GitHub  

- **특징**  
  - 중고 책 등록 시, 알라딘에서 **도서 정보(제목, 정가, 이미지)** 등을 자동으로 크롤링  
  - **1대1 실시간 채팅**을 통해 메시지(텍스트/이미지) 교환, 읽음/미읽음 관리, 알림 기능 제공  
  - 판매 상태(판매중/예약중/거래완료)를 관리하는 **상태 업데이트** 로직

<br/>

## 개인 기여 내용
### 1. 팀 프로젝트 담당 기능

팀 프로젝트 당시 아래 기능들의 설계와 구현을 주도적으로 담당했습니다.

1. **중고거래 로직**  
   - `TradeController` / `TradeService` / `Trade` 엔티티를 이용해  
     - **책 등록/수정/삭제**, 상태 변경(판매중/예약중/거래완료) 기능 구현  
   - 알라딘 사이트 **크롤링(`TradeCrawling`)**으로 도서 정보(제목/정가/이미지) 자동 입력

2. **실시간 채팅(WebSocket)**  
   - `ChatController`, `ChatService`를 작성하여  
     - 메시지 전송/수신, 이미지를 포함한 **실시간 채팅**, **읽음 처리**, **새 메시지 알림** 등 구현  
   - `WebSocketConfig`로 SockJS & STOMP Endpoint 설정  
   - `ChatRoom`, `Chat` 엔티티 설계: **unreadCount**(미읽은 메시지 수), **나가기(LEAVE) 이벤트** 처리 등

### 2. 개인 포트폴리오를 위한 코드 리팩토링

이 프로젝트를 개인 포트폴리오로 재구성하면서, 코드의 안정성과 유지보수성을 높이기 위해 다음과 같은 리팩토링 작업을 진행했습니다.

-   **의존성 주입 방식 개선**: `@Autowired`를 사용한 필드 주입 방식에서 **생성자 주입 방식**(`@RequiredArgsConstructor`)으로 변경하여, 의존성 결합도를 낮추고 테스트 용이성을 확보했습니다.
-   **안정적인 예외 처리**: 모든 `e.printStackTrace()` 호출을 **SLF4J 로거(`logger.error`)**로 대체하여, 운영 환경에서의 체계적인 예외 추적 및 관리가 가능하도록 개선했습니다.
-   **코드 안정성 강화**: `File.delete()`의 반환값을 확인하여 파일 삭제 실패 시 경고 로그를 남기고, `NullPointerException` 발생 가능성이 있는 코드를 방어적으로 수정하여 안정성을 높였습니다.
-   **가독성 및 유지보수성 향상**: 중복되던 `DateUtils` 클래스를 공용 유틸리티로 통합하고, `.size() > 0` 대신 `!isEmpty()`를 사용하는 등 코드 컨벤션을 적용하여 가독성을 높였습니다.

<br/>

## 실행 방법

1. **레포지토리 클론**  
   - 원격 저장소에서 프로젝트를 클론 받은 뒤, 프로젝트 디렉토리로 이동합니다.

2. **MySQL DB 준비**  
   - MySQL에서 DB를 생성: 예) `bookdream`  
   - `src/main/resources/application.yml`(또는 `application.properties`) 파일에서  
     - `spring.datasource.url=jdbc:mysql://localhost:3306/bookdream`  
     - `spring.datasource.username=...`  
     - `spring.datasource.password=...`  
     를 환경에 맞게 수정

3. **Gradle(또는 Maven) 빌드 & 실행**  
   - 사용 중인 빌드 툴(Gradle/Maven)에 맞춰 빌드 및 서버 실행을 진행하세요.

4. **브라우저 접속**  
   - `http://localhost:8080/trade/list` 에서 책 목록을 확인  
   - (회원 기능이 필요한 경우) `/user/login` 또는 `/user/signup`을 통해 로그인 후 이용  
   - 로컬 PC 환경에 따라 포트나 경로를 수정해야 할 수도 있음

5. **이미지 저장 경로 설정(선택)**  
   - 소스 상의 `uploadDir` 변수가 `C:/Users/TJ/git/Book-Dream/src/main/resources/static/image/` 로 되어 있습니다.  
   - 실제 로컬 환경(Windows/Mac/Linux)에 맞게 수정해 주세요.

<br/>

## 주요 기능 소개

### 1) 중고 책 거래

- **책 등록**  
  - `/trade/create` 페이지에서 책 제목/정보/이미지 URL/정가를 **크롤링**으로 불러올 수 있음  
  - 판매 가격, 등급(최상/상/중) 입력 후 DB 저장 & 이미지 다운로드

- **책 목록** (`/trade/list`)
  - 검색어(`kw`)로 필터링, 페이징 처리  
  - 목록에서 제목, 가격, 등급, 이미지 확인

- **책 상세** (`/trade/detail/{idx}`)
  - 작성자는 **수정/삭제** 가능  
  - 구매자는 **채팅하기** 버튼으로 거래 메시지 전송 (상태가 ‘거래완료’면 버튼 비활성)

### 2) 실시간 채팅(WebSocket)

- **채팅방 생성**
  - `/trade/chat/create` → `ChatRoom` 생성 or 기존 채팅방 재활용 → `/trade/chat/start?chatRoomId=...`
- **실시간 메시지 전송**
  - `chat.html` 에서 SockJS & STOMP로 서버에 연결  
  - `ChatController`가 메시지 수신 → DB 저장(`Chat` 엔티티) → `/topic/public`에 브로드캐스트
- **읽음 처리 & 알림**
  - 채팅방 진입 시, 미읽은 메시지 수(`unreadCount`) 감소  
  - 채팅방 나가기 시, “상대방이 나갔습니다” 메시지 표시  
  - 이미지 메시지(`IMAGE`) 전송 가능

<br/>

## 화면 예시

- **중고 서적 목록**
![프로젝트 리스트](https://github.com/user-attachments/assets/403c36d5-f502-4c1a-b5cb-f47447bafbcf)
  - 검색, 페이지네이션, 판매 상태 표시

- **책 등록**
![프로젝트 상품 등록](https://github.com/user-attachments/assets/46a505f3-b932-4388-91eb-c682622d2021)
  - 책 등록

- **책 상세 보기**
![프로젝트 상세보기](https://github.com/user-attachments/assets/b7c9ebd7-b3f3-4bcc-a58c-9645d7bab1a7)
  - “채팅하기” 버튼, 상태 변경(작성자), 등급/가격 표시  

- **실시간 채팅**
![프로젝트 채팅](https://github.com/user-attachments/assets/17e84c83-84fe-4aca-8d91-18edfdcf6819)
  - 메시지 송수신, 이미지 미리보기, 읽음 처리, 메시지 시간 표시

<br/>
