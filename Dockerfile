# 1. 베이스 이미지 (우리의 뼈대가 될 자바 17 환경)
FROM eclipse-temurin:17-jdk

# 2. 도커 상자 안에서 우리가 작업할 기본 폴더 위치 지정
WORKDIR /app

# 3. 1단계에서 만든 jar 파일을 도커 상자 안의 app.jar라는 이름으로 복사
# (만약 Maven을 쓰신다면 build/libs/ 대신 target/*.jar 로 수정해야 합니다)
COPY build/libs/*.jar app.jar

# 4. 컨테이너가 8080 포트를 사용한다는 것을 명시 (문서화 용도)
EXPOSE 8080

# 5. 도커 상자가 켜질 때 실행할 마법의 주문
ENTRYPOINT ["java", "-jar", "app.jar"]