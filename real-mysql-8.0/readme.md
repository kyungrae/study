# Real MySQL 8.0

## 도커 컨테이너

```bash
docker run \
 --name mysql \
 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
 -p 3306:3306 \
 -d \
 mysql:8.0.39
```

## [2. 설치와 설정](./chapter02/readme.md)

## [3. 사용자 및 권한](./chapter03/readme.md)

## [4. 아키텍처](./chapter04/readme.md)

## [5. 트랜잭션과 잠금](./chapter05/readme.md)

## [6. 데이터 압축](./chapter06/readme.md)

## [7. 데이터 암호화](./chapter07/readme.md)

## [8. 인덱스](./chapter08/readme.md)
