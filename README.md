[공통] 배치서버 설정 및 정보
======================

# 1. 설정
## 1.1. properties
    1. spring.profiles.active=local : 브랜치명과 같아야함. 꼭 확인 할 것.
    2. jwt.status=off 는 jwt 미적용. on은 적용(개발 편의를 위해 기능 설정한 것이기 때문에
     local에서만 적용할 것)

# 2. controller
    1. ActionController.java
    1.1 /requestAction.json
    1.2 
    2. SendController.java
    2.1 /api/send/crm.json

# 3. 개발 참고 문서
    1. 배치관련
    1.1 https://jojoldu.tistory.com/336?category=902551
    1.2 https://github.com/jojoldu/spring-batch-in-action
    2. 로그 관련
    2.1 https://perfectacle.github.io/2018/07/22/spring-boot-2-log/

