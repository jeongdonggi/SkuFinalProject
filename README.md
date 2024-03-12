# JWT 인증과 권한을 이용한 웹기반 채팅
서경대학교 컴퓨터공학과 졸업작품

## 제작 목적
JWT를 이용하여 보안을 강화한 웹 메신저를 구현한다.

## 작품 설명
JWT 인증을 통한 게시판과 채팅 기능을 사용할 수 있는 사이트로 JWT를 가지고 HTTP 통신 시에 서버에서 토큰 인증을 받게 되고 인증에 성공하게 되면 사이트에 접근이 가능하게 된다. 또한 인증 후 자신의 권한에 따라 접근할 수 있는 기능이 달라진다.

## 작품 효과
JWT를 사용함으로써 데이터베이스와의 통신을 최소화 시켜 부하를 줄일 수 있으며, 사용자 인증 정보의 보안을 강화한다. 인증 정보에 있는 권한에 따라서 사용자의 사이트 기능 사용 범위가 달라지며, 권한이 높은 사용자는 권한이 낮은 사용자를 관리하는 방법을 통하여 보안을 강화한다.

## JWT를 사용한 이유
Session은 Token과 다르게 아이디나 비밀번호가 탈취당하더라도 Session Storage에서 값을 삭제하는 것으로 JWT보다 보안 부분에서 조금 더 좋다고 생각한다. 하지만 그럼에도 JWT를 사용하는 이유는 JWT의 확장성이 매력적으로 다가왔기 때문이다. JWT는 SecretKey만 있다면 서버의 확장이 용이하기 때문에 이번 프로젝트를 통해서 확장성이 용이하고 보안성이 뛰어난 사이트를 구현해보고 싶었다.

## JWT란
Json Web Token으로 당사자 간에 정보를 JSON 개체로 안전하게 전송하기 위한 토큰이다.

JWT에선 Signature가 중요하다.

### Signature
![image](https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/d4501719-79ae-4497-b4de-3be39971691e)
JWT는 Header, Payload, Signature로 구성되어 있다.

여기서 Signature는 Header, Payload, SecretKey로 구성되어 있다.

이 Signature는
<img width="1021" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/f1e54ee7-0869-4fb6-b96a-267398c7e0c1">
사진과 같이 JWT를 가지고 있는 사용자가 http 요청 시 JWT를 함께 보내게 되면, 서버에서는 고유 SecretKey를 이용하여 사용자가 보낸 Header, Payload를 이용해서 Signature를 생성하게 된다.

생성된 Signature와 사용자의 Signature를 비교하여 동일하면 인증 성공하게 된다.

### Rotating Refresh Token
<img width="1020" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/6d8eb026-785e-4002-97e2-9e3786ed2ef5">
위의 사진을 이해하기 전에 알아야 할 부분: AccessToken과 RefreshToken

- AccessToken: 인증 시 사용하는 Token 
- RefreshToken: AccessToken을 발급받을 때 사용하는 Token

그렇다면 왜 AccessToken과 RefreshToken을 같이 사용하는 것일까?
- accessToken을 탈취 당하게 된다면 누구나 인증이 가능하게 되기 때문에 accessToken의 유효기한을 줄이고 대신 refreshToken(JWT)를 이용해서 사용자가 로그인을 다시하는 수를 줄인다.
- 여기서 RefreshToken을 탈취당하게 되면 AccessToken을 누구나 재발급 받을 수 있기 때문에 RefreshToken을 사용할 때마다 재발급 받는 방법을 사용한다.

이와 같이 refreshToken을 재발급 받는 방식이 Rotating Refresh Token 이다.

## 개발 환경
![Jquery](https://img.shields.io/badge/Jquery-0769AD?style=flat&logo=Jquery)
![AJAX](https://img.shields.io/badge/AJAX-61DAFB?style=flat&logo=AJAX)
![Socket.io](https://img.shields.io/badge/Socket.io-010101?style=flat&logo=Socket.io)

![SpringBoot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat&logo=SpringBoot)
![SpringSecurity](https://img.shields.io/badge/SpringSecurity-6DB33F?style=flat&logo=SpringSecurity)
![SpringJPA](https://img.shields.io/badge/SpringJPA-6DB33F?style=flat&logo=Spring)
![WebSocket](https://img.shields.io/badge/WebSocket-6DB33F?style=flat&logo=SpringBoot)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo)
![STOMP](https://img.shields.io/badge/STOMP-000000?style=flat&logo=SpringBoot)
![QueryDSL](https://img.shields.io/badge/QueryDSL-6DB33F?style=flat&logo=SpringBoot)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=Thymeleaf)

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=MySQL)

## 시연 영상
<https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/5eb49227-72c9-49fd-888e-e08650ad0d25>

## 간략한 시스템 구조
<img width="773" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/f6d47cea-a346-4009-96fb-9a5ce69d1674">

## DataBase
<img width="551" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/9367f446-5ea3-4508-a939-e62f11d3218c">

## 주요 기능
<img width="1019" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/2f8ffaec-e0cf-4667-aa80-3271b196eff1">

## 권한
ADMIN, MANAGER, USER, SEMIUSER, GUEST로 나뉘어져 있다.
creator는 채팅방이나 게시글, 댓글을 만들거나 작성한 사용자이다.
### front

<img width="686" alt="image" src="https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/44ec2c35-1566-4b90-9a34-f29804eab5e4">

## 기능 설명

### 로그인
1. 회원 가입
2. 로그인
3. 비밀번호 찾기

#### 회원 가입
이름, 이메일, 비밀번호를 @vaild와 정규식을 이용하여 값을 받아준다.

비밀번호는 bcrypt를 이용하여 복호화가 불가능한 비밀번호를 만들어 주었다.

#### 로그인
Security의 기본 login을 사용하지 못하기 때문에 AbsctractAuthenticationProcessingFilter를 확장하여 구현한 CustomFIlter를 만들어 로그인을 구현해주었다.

DB에 저장된 값
![image](https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/d2d7248d-dc1e-48ba-bcb0-2657833ffbae)
Cookie에 저장된 값
![image](https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/97f05567-c11e-450a-b1e0-413a3f35b078)

#### 비밀번호 찾기
비밀번호가 복호화가 불가능하기 때문에 로그인한 이메일로 비밀번호를 랜덤으로 초기화하여 보내주는 방식을 채택하였다.

### JWT 인증
1. 인증이 없어도 접근 가능한 정적 리소스 및 홈 화면, 로그인, 회원가입, 비밀번호 찾기를 지정해준다.
2. 인증이 필요한 경우
    - accessToken과 refreshToken이 둘 다 있는 경우: accessToken의 사용 가능 여부에 따라 인증을 해준다.
    - accessToken만 있는 경우: accessToken의 사용 가능 여부에 따라 인증을 해준다.
    - refreshToken만 있는 경우: refreshToken의 사용 가능 여부에 따라 accessToken과 refreshToken을 재발급 해줄지 말지를 결정한다.
    - 둘 다 없는 경우: 인증을 거부한다

### 사용자 관리
#### ADMIN
![image](https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/95cd2d76-219f-4afe-a76c-e8b52e991c5c)
1. 사용자 권한 설정
2. 회원 삭제

#### MANAGER
![image](https://github.com/jeongdonggi/SkuFinalProject/assets/100845304/7845df8b-aa02-43c9-80a6-1d9fb3e4f67f)
1. 사용자 권한 설정
2. ADMIN 관리 X

#### 내 정보
1. 비밀번호 변경
2. 이름 변경
3. 회원 탈최
4. 어드민 변경
    - 미리 지정해둔 어드민키와 동일한 키를 입력하면 어드민으로 변경 가능

### 권한 설정
앞서 설명하였듯이 ADMIN, MANAGER, USER, SEMIUSER, GUEST로 나뉘어져 있다.

GUEST는 게시판 및 채팅 기능을 사용하지 못한다.

#### 게시판 및 파일, 댓글

1. SEMIUSER 이상: 
   - 게시글 및 댓글, 대댓글 저장
   - 게시글 및 댓글, 대댓글 수정 및 삭제(수정 및 삭제는 글쓴이만 가능)
   - 게시글 및 댓글, 대댓글 읽기
 - 게시글 페이징 및 서치(서치: 제목, 내용, 제목 + 내용)
2. USER 이상:
   - 파일 읽기 및 저장 가능(SEMIUSER는 불가능)
3. MANAGER, ADMIN:
   - 자신의 권한 미만 사용자가 자신의 게시글을 보지 못하도록 할 수 있다.

#### 채팅

1. 공개 채팅과 비공개 채팅
    - 공개 채팅: 말 그대로 공개 채팅으로 방을 만들면 누구나 들어올 수 있다.
    - 비공개 채팅: 방을 만들기 전 어떤 사람이 들어올 지 지정하고 방을 만든다.
2. 사용자 방출
    - ADMIN, MANAGER, ROOM CREATER가 방출 가능(방을 만든 사람 = ROOM CREATER)
    - 방출 시 방출된 사람한테 알림이 감(공개 채팅은 다시 들어올 수 있지만 비공개 채팅은 들어올 수 없음)

## 성과

### 현재 프로젝트 성과

AccessToken과 RefreshToken이 명확하게 구분되어 Rotating RefreshToken 방식을 잘 수행하고 있다.

사용자의 권한에 따른 기능 접근이 잘 수행되고 있다.

### 아쉬운 점
1. 프로젝트를 진행하면서 Rotating RefreshToken 방식에서 더 나아가질 못하였다.
2. 권한이 딱 정해져 있기 때문에 유연한 권한 부여가 안되어 있다.
3. 프로젝트 배포를 해보지 못하였다.
4. 채팅에 있는 기능이 부실하다.

### 향후 개선 방안
1. header에 JWT 넣어 보내기
2. 배포
