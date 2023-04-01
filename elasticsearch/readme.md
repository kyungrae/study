# 엘라스틱서치 실무 가이드

## 1. 검색 시스템 이해하기

### 에제 파일 다운로드

```bash
## Download snapshot
wget https://github.com/javacafe-project/elastic-book-snapshot/raw/master/book_backup.zip --no-check-certificate

## Unzip
unzip book_backup.zip -d ./backup
```

### elastic search 실행

```bash
## Run elasticsearch cluster & kibana
docker-compose up -d

## Copy security certificates from container to local machine
docker cp elastic-search-es01-1:/usr/share/elasticsearch/config/certs/es01/es01.crt .
```

### snaptshot 활성화 & 복구

```bash
## Register snapshot repository
curl -XPUT -k --cacert es01.crt -u elastic:hamakim https://localhost:9200/_snapshot/javacafe \
  -H 'Content-Type: application/json' \
  -d '{ 
    "type": "fs",
    "settings": {
      "location": "/usr/share/elasticsearch/backup/search_example",
      "compress": true
    }
  }'

curl -XPUT -k --cacert es01.crt -u elastic:hamakim https://localhost:9200/_snapshot/apache-web-log \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "fs",
    "settings": {
      "location": "/usr/share/elasticsearch/backup/agg_example",
      "compress": true
    }
  }'

## Restore snapsot
curl -XPOST -k --cacert es01.crt -u elastic:hamakim https://localhost:9200/_snapshot/javacafe/movie-search/_restore\
  -H 'Content-Type: application/json' \
  -d '{
    "indices": "movie_search"
  }'

curl -XPOST -k --cacert es01.crt -u elastic:hamakim https://localhost:9200/_snapshot/apache-web-log/default/_restore\
  -H 'Content-Type: application/json' \
  -d '{
    "indices": "apache-web-log"
  }'

curl -XPOST -k --cacert es01.crt -u elastic:hamakim https://localhost:9200/_snapshot/apache-web-log/applied-mapping/_restore\
  -H 'Content-Type: application/json' \
  -d '{
    "indices": "apache-web-log-applied-mapping"
  }'
```

## 2. 엘라스틱서치 살펴보기

### 기본 용어

- index
  - 데이터 저장 공간
  - 데이터 저장 시 여러 개의 파티션으로 나누어 저장되는데, 파티션을 샤드로 구성되며 primary shard와 replica shard로 구성된다.
- document
  - 데이터가 저장되는 최소 단위
- field
  - document를 구성하기 위한 속성
  - 하나의 필드는 목적에 따라 다수의 데이터 타입을 가질 수 있음
- mapping
  - 아직 잘 모르겠음

### 노드의 종류

- master node
  - 클러스터 관리(노드 추가/제거)
- data node
  - 데이터 저장/검색/통계 작업
- coordinating node
  - 사용자 요청 load balancing
  - 클러스터 관련 요청은 마스터 노드에 전달, 데이터 관련 요청은 데이터 노드에 전달
- ingest node
  - document의 전처리 작업

## 3. 데이터 모델링

역인덱스

- 모든 문서가 가지는 단어의 고유 단어 목록
- 해당 단어가 어떤 문서에 속해 있는지에 대한 정보
- 전체 문서에 각 단어가 몇 개 들어있는지에 대한 정보
- 하나의 문서에 단어가 몇 번씩 출현했는지에 대한 빈도

분석기의 구조

- Character filte  
  문장을 특정한 규칙에 의해 수정한다.
  - Html strip filter
- Tokenizer filter  
  수정한 문장을 개별 토큰으로 분리한다.
  - standard tokenizer
  - whitespace tokenizer
- Token filter  
  개별 토큰을 특정한 규칙에 의해 변경한다.  
  - lowercase filter
  - Synonum filter
