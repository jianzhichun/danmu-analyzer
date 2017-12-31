FROM frolvlad/alpine-oraclejdk8
LABEL author="jianzhichun"
LABEL email="zzchun12826@gmail.com"

WORKDIR /app
COPY . .
RUN chmod +x ./mvnw

CMD ./mvnw clean spring-boot:run