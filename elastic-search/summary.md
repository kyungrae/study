# 엘라스틱서치 실무 가이드

## 1. 검색 시스템 이해하기

### 에제 파일 다운로드

```bash
wget https://github.com/javacafe-project/elastic-book-snapshot/raw/master/book_backup.zip --no-check-certificate

unzip book_backup.zip -d ./backup
```

### elastic search 실행 & snapshot 데이터 활성화

```bash
## elastic search 실행 & 스냅샷 실행
docker-compose up -d

docker cp elastic-search-es01-1:/usr/share/elasticsearch/config/certs/es01/es01.crt .

curl -XPUT --cacert es01.crt -u elastic https://localhost:9200/_snapshot/javacafe -d '{
    "type": "fs",
    "settings": "{
        "location": "/usr/share/elasticsearch/backup/search_example",
        "compress": true
    }
}'

curl -XPUT --cacert es01.crt -u elastic https://localhost:9200/_snapshot/apache-web-log -d '{
    "type": "fs",
    "settings": "{
        "location": "/usr/share/elasticsearch/backup/agg_example",
        "compress": true
    }
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
