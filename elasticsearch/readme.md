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
docker cp elasticsearch-es01:/usr/share/elasticsearch/config/certs/es01/es01.crt .
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
  - 문서의 필드와 필드의 속성을 정의하고 그에 따른 색인 방법을 정의하는 프로세스다.

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

- Character filter
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

```mermaid
flowchart LR
  Text((text)) -->  CharacterFilter[Character Filter]
  CharacterFilter --가공된 text--> TokenizerFilter[Tokenizer Filter]
  subgraph filter
   direction TB
   TokenFilter[Token Filter]
   Dictionary[Synonym Dictionary]
  end
  TokenizerFilter --Terms--> TokenFilter[Token Filter]
  TokenFilter --가공된 Terms--> Index((Index))
```

## 4. 데이터 검색

### 검색 API

색인 시점에 Analyzer를 통해 분석된 term을 term, 출현빈도, 문서번호와 같이 역색인 구조로 만들어 내부적으로 저장한다.
검색 시점에는 keyword 타입과 같은 분석이 불가능한 데이터와 Text 타입과 같은 분석이 가능한 데이터를 구분해서 분석이 가능할 경우 분서기를 이용해 분석을 수행한다.

### QueryDSL 구조

```JSON
{
  "size":,
  "from":,
  "timeout":,
  "_source": {},
  "query": {
    "match": {},
    "term": {},
    "bool": {
      "must":[],
      "must_not":[],
      "should":[],
      "filter":[]
    }
  },
  "aggs": {},
  "sort":{}
}
```

### 쿼리 컨텍스트

- "query":{"match":{}}
- 문서가 쿼리와 얼마나 유사한지를 스코얼 계산한다.
- 질의가 요청될 때마다 엘라스틱서치에서 내부의 루씬을 이용해 계산을 수행한다(이때 결과가 캐싱되지 않느다).
- 일반적으로 전문 섬색에 많이 사용한다.
- 캐싱도지 않고 디스크 연산을 수행하기 때문에 상대적으로 느리다.

### 필터 컨텍스트

- "query": {"bool":{"filter":{}}}
- 쿼리의 조건과 문서가 일치하는지(Yes/No)를 구분한다.
- 별도로 스코어를 계산하지 않고 단순 매칭 여부를 검사한다.
- 자주 사용되는 필터의 결과는 엘라스틱서치가 내부적으로 캐싱한다.
- 기본적으로 메모리 연산을 수행하기 떄문에 상대적으로 빠르다.

### 페이징

엘라스틱서치는 **keyset pagination**이 불가능해서 페이지 번호가 높아질수록 비용이 커지니 주의하자.

### 부가적인 검색 API

1. 클러스터 설정 확인 API  
   `GET _cluster/settings`
2. 인덱스의 노드와 샤드 정보 조회 API  
   `POST {index}/_search_shard`
3. 하나 이상의 쿼리 실행  
   `POST _msearch`  
4. 검색된 문서 개수 조회 API  
   `POST {index}/_count`
5. score 계산 설명 API  
   `POST {index}/_explain/{id}`
6. 쿼리 실행 과정에서 샤드별로 얼마나 많은 시간이 소요  
   `POST {index}/_search`
