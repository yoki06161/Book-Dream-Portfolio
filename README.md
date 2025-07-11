# Book-Dream (개인 포트폴리오용)

> **이 레포지토리는 원래 팀 프로젝트 ([원본 링크](https://github.com/yoki06161/Book-Dream))를**  
> **개인 포트폴리오 용도로 재구성**한 것입니다.

<br/>

## 개요

**Book-Dream**은 **중고 책 거래** 기능과 **실시간 채팅(WebSocket)** 기능을 제공하는 웹 애플리케이션입니다.

- **주요 목적**  
  - 책을 등록/판매/구매하는 **온라인 중고거래**  
  - 거래 상대방과 **실시간 채팅**(WebSocket)으로 소통



- **사용 기술 스택**
  
  [![stackticon](https://firebasestorage.googleapis.com/v0/b/stackticon-81399.appspot.com/o/images%2F1752196453573?alt=media&token=6b922f67-0896-4169-868c-a0c849058ae4)](https://github.com/msdio/stackticon)
  * **Backend**: Java, Spring Boot, Spring Data JPA, MySQL
  * **Frontend**: Thymeleaf, HTML, CSS, JavaScript
  * **빌드 및 협업**: Gradle, Git, GitHub
  * **실시간 통신**: WebSocket (SockJS, STOMP)
  * **크롤링**: Jsoup (알라딘 사이트 도서 정보 수집)
  * **로깅**: SLF4J, Logback
  
- **특징**  
  - 중고 책 등록 시, 알라딘에서 **도서 정보(제목, 정가, 이미지)** 등을 자동으로 크롤링  
  - **1대1 실시간 채팅**을 통해 메시지(텍스트/이미지) 교환, 읽음/미읽음 관리, 알림 기능 제공  
  - 판매 상태(판매중/예약중/거래완료)를 관리하는 **상태 업데이트** 로직

<br/>

### 개인 기여 내용

#### 1. 주요 담당 기능
- **중고거래 로직**: `TradeController`, `TradeService`를 중심으로 책 등록/수정/삭제 및 판매 상태 변경 기능을 **구현했습니다.** Jsoup을 이용한 외부 사이트 도서 정보 크롤링 기능을 **개발했습니다.**
- **실시간 채팅(WebSocket)**: `ChatController`, `ChatService` 및 STOMP, SockJS를 활용하여 1:1 실시간 채팅 기능을 **구현했습니다.** 메시지 읽음 처리, 이미지 전송, 안 읽음 메시지 알림 등의 기능을 포함합니다.

#### 2. 코드 품질 개선
- **안정성 및 유지보수성 향상**:
    - **생성자 주입**: `@Autowired` 필드 주입을 `@RequiredArgsConstructor`를 이용한 생성자 주입 방식으로 변경하여 의존성 결합도를 낮추고 순환 참조를 **방지했습니다.**
    - **체계적인 로깅**: `e.printStackTrace()`를 SLF4J 로거로 대체하여 예외 추적 시스템을 **구축했습니다.**
    - **방어적 프로그래밍**: 파일 저장 시 디렉토리 존재 여부를 미리 확인하고 생성하는 로직을 추가하여 `NoSuchFileException` 발생 가능성을 원천적으로 **차단했습니다.**

#### 3. 문제 해결 및 디버깅
- **Spring Security & Thymeleaf 렌더링 충돌 해결**: 익명 사용자의 CSRF 토큰 렌더링 과정에서 발생하던 `IllegalStateException`을 세션 생성 정책 변경(`SessionCreationPolicy.ALWAYS`)을 통해 **해결했습니다.**
- **통합 인증 로직 구현**: 일반 가입자와 소셜 로그인 사용자의 테이블이 이원화되어 발생하던 `DataNotFoundException`을 두 테이블을 모두 조회하는 통합 서비스 로직으로 **개선했습니다.**
- **실시간 메시지 배지 기능 구현**: 헤더에 전체 안 읽은 메시지 수를 표시하고, WebSocket과 REST API를 연동하여 실시간으로 업데이트하는 기능을 **구현했습니다.**
    
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
