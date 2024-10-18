# 9. 옵티마이저와 힌트

옵티마이저는 쿼리를 최적으로 실행하기 위해 각 테이블의 데이터가 어떤 분포로 저장돼 있는지 통계 정보를 참조하며, 기본 데이터를 비교해 최적의 실행 계획을 수립한다.
EXPLAIN 명령으로 쿼리의 실행 계획을 확인할 수 있는데, 실행 계획에 표시되는 내용을 이해하려면 옵티마이저가 실행하는 최적화에 대해 지식을 갖추고 있어야 한다.

## 9.1 개요

### 9.1.1 쿼리 실행 절차

쿼리가 실행되는 과정은 크게 세 단계로 나눌 수 있다.

1. 요청된 SQL 문장을 쪼개서 MySQL 서버가 이해할 수 있는 수준으로 분리한다.
2. SQL의 파싱 정보(파스 트리)를 확인하면서 어떤 테이블부터 읽고 어떤 인덱스를 이용해 테이블을 읽을지 선택한다.
3. 두 번째 단계에서 결정된 테이블의 읽기 순서나 선택된 인덱스를 이용해 스토리지 엔진으로부터 데이터를 가져온다.

첫 번째 단계를 **SQL 파싱**이라고 하며, **SQL 파서**라는 모듈로 처리한다.
SQL 문장이 문법적으로 잘못됐다면 이 단계에서 걸러진다. 또한 이 단계에서 **SQL 파스 트리**가 만들어진다.
MySQL 서버는 SQL 파스 트리를 이용해 쿼리를 실행한다.

두 번째 단계는 첫 번째 단계에서 만들어진 SQL 파스 트리를 참조하면서 다음과 같은 내용을 처리한다.

- 불필요한 조건 제거 및 복잡한 연산의 단순화
- 여러 테이블의 조인이 있는 경우 어떤 순서로 테이블을 읽을지 결정
- 각 테이블에 사용된 조건과 인덱스 통계 정보를 이용해 사용할 인덱스를 결정
- 가져온 레코드들은 임시 테이블에 넣고 다시 한번 가공해야 하는지 결정

두 번째 단계는 **최적화 및 실행 계획 수립** 단계이며 **옵티마이저**에서 처리한다.
두 번째 단계가 완료되면 쿼리의 **실행 계획**이 만들어진다.
세 번째 단계는 수립된 실행 계획대로 스토리지 엔진에 레코드를 읽오도록 요청하고, 스토리지 엔진으로부터 받은 레코드를 조인하거나 정렬하는 작업을 수행한다.

### 9.1.2 옵티마이저의 종류

현재는 대부분의 RDBMS가 비용 기반의 옵티마이저를 채택하고 있다.

#### 규칙 기반 최적화

옵티마이저에 내장된 우선순위에 따라 실행 계획을 수립하는 방식이다.

#### 비용 기반 최적화

쿼리를 처리하기 위한 여러 가지 방법을 만들고, 각 단위 작업의 비용 정보와 대상 테이블의 예측된 통계 정보를 이용해 실행 계획별 비용을 산출한다.
산출된 실행 방법별로 비용이 최소로 소요되는 처리 방식을 선택해 쿼리를 실행한다.

## 9.2 기본 데이터 처리

### 9.2.1 풀 테이블 스캔과 풀 인덱스 스캔

어떤 영역의 데이터가 앞으로 필요해지리라는 것을 예측해서 요청이 오기 전에 미리 디스크에서 읽어 InnoDB의 버퍼 풀에 가져다 두는 것을 의미한다.
즉, 풀 테이블 스캔이 실행되면 처음 몇 개의 데이터 페이지는 포그라운드 스레드가 페이지 읽기를 실행하지만 특정 시점부터는 읽기 작업을 백그라운드 스레드로 넘긴다.
리드 어헤드는 풀 테이블 스캔에서만 사용되는 것이 아니라 풀 인덱스 스캔에서도 동일하게 사용된다.

### 9.2.2 병렬 처리

MySQL 8.0 버전에서는 다음 예제와 같이 아무런 WHERE 조건 없이 단순히 테이블의 전체 건수를 가져오는 쿼리만 병렬로 처리할 수 있다.

```SQL
SET SESSION innodb_parallel_read_threads=1;
SELECT COUNT(*) FROM salaries;

SET SESSION innodb_parallel_read_threads=2;
SELECT COUNT(*) FROM salaries;
```

### 9.2.3 ORDER BY 처리

정렬을 처리하는 방법은 **인덱스**를 이용하는 방법과 쿼리가 실행될 때 **Filesort**라는 별도의 처리를 이용하는 방법으로 나눌 수 있다.

|  | 장점 | 단점 |
|---|---|---|
| Index | 인덱스를 순서대로 읽기만 하면 되므로 빠르다. | 데이터 갱신 작업 시 인덱스 수정 작업으로 느리다.<br>인덱스 때문에 디스크와 메모리 공간이 더 필요하다. |
| Filesort | 인덱스를 생성하지 않아도 된다. | 정렬할 레코드가 많을수록 쿼리의 응답 속도가 느리다. |

#### 9.2.3.1 소트 버퍼(Sort buffer)

정렬을 수행하기 위해 별도의 메모리 공간을 할당받아서 사용하는데, 이 메모리 공간을 **소트 버퍼**(sort buffer)라고 한다.
소트 버퍼는 정렬이 필요한 경우에만 할당되며, 버퍼의 크기는 정렬해야 할 레코드의 크기에 따라 가변적으로 증가하지만 최대 사용 가능한 소트 버퍼의 공간은 sort_buffer_size라는 시스템 변수로 설정할 수 있다.

정렬해야 할 레코드의 건수가 소트 버퍼로 할당된 공간보다 크면 정렬해야 할 레코드를 여러 조각으로 나눠서 처리한다.
소트 버퍼에서 정렬을 수행하고, 그 결과를 임시로 디스크에 기록해 둔다.
그리고 다음 레코드를 가져와서 다시 정렬해서 반복적으로 디스크에 임시 저장한다.
각 버퍼 크기만큼 정렬된 레코드를 다시 Merlti-merge(병합)하면서 정렬을 수행한다.

소트 버퍼를 크게 설정하면 빨라질 것을 기대할 수 있지만, 벤치마크 결과로는 큰 차이를 보이진 않았다.
일반적인 소트 버퍼 크기는 56KB에서 1MB 미만이 적절해 보인다.

#### 9.2.3.2 정렬 알고리즘

레코드를 정렬할 때 레코드 전체를 소트 버퍼에 담을지 또는 정렬 기준 칼럼만 소트 버퍼에 담을지에 따라 **싱글 패스**(Single-pass)와 **투 패스**(Two-pass) 2가지 정렬 방식으로 나눌 수 있다.
정렬을 수행하는 쿼리가 어떤 정렬 방식을 사용하는지는 다음과 같이 옵티마이저 트레이스 기능으로 확인할 수 있다.

```SQL
SET OPTIMIZER_TRACE="enabled=on",END_MARKERS_IN_JSON=on;
SET OPTIMIZER_TRACE_MAX_MEM_SIZE=1000000;
SELECT * FROM employees ORDER BY last_name LIMIT 100000,1;
SELECT * FROM INFORMATION_SCHEMA.OPTIMIZER_TRACE\G

...
  "filesort_priority_queue_optimization": {
    "limit": 100001
  } /* filesort_priority_queue_optimization */,
  "filesort_execution": [
  ] /* filesort_execution */,
  "filesort_summary": {
    "memory_available": 262144,
    "key_size": 32,
    "row_size": 169,
    "max_rows_per_buffer": 1551,
    "num_rows_estimate": 300030,
    "num_rows_found": 300024,
    "num_initial_chunks_spilled_to_disk": 82,
    "peak_memory_used": 262144,
    "sort_algorithm": "std::stable_sort",
    "sort_mode": "<fixed_sort_key, packed_additional_fields>"
  } /* filesort_summary */
...
```

출력된 내용에서 "filesort_summary" 섹션의 "sort_mode" 필드에서 정렬 방식을 확인할 수 있다.

- <sort_key, rowid>: 정렬 키와 레코드의 로우 아이디만 가져와서 정렬하는 방식
- <sort_key, additional_fields>: 정렬 키와 레코드 전체를 가져와서 정렬하는 방식으로, 레코드의 칼럼들은 고정 사이즈로 메모리 저장
- <sort_key, packed_additional_fields>: 정렬 키와 레코드 전체를 가져와서 정렬하는 방식으로, 레코드의 칼럼들은 가변 사이즈로 메모리 저장

첫 번째 방식을 투 패스 정렬 방식이라고 명명하고, 두 번째와 세 번째 방식을 싱글 패스 정렬 방식이라고 명명한다.

##### 9.2.3.2.1 싱글 패스 정렬 방식

소트 버퍼에 정렬 기준 칼럼을 포함해 SELECT 대상이 되는 칼럼 전부를 담아서 정렬을 수행하는 정렬 방식이다.

##### 9.2.3.2.2 투 패스 정렬 방식

정렬 대상 칼럼과 프라이머리 키 값만 소트 버퍼에 담아서 정렬을 수행하고, 정렬된 순서대로 다시 프라이머리 키로 테이블을 읽어서 SELECT할 칼럼을 가져오는 정렬 방식이다.

투 패스 방식은 테이블을 두 번 읽어야 하지만, 싱글 패스 정렬 방식은 더 많은 소트 버퍼 공간이 필요하다.
최신 버전에서는 일반적으로 싱글 패스 정렬 방식을 주로 사용하지만 다음의 경우는 투 패스 정렬 방식을 사용한다.

- 레코드의 크기가 max_length_for_sort_data 시스템 변수에 설정된 값보다 클 때
- BLOB이나 TEXT 타입의 칼럼이 SELECT 대상에 포함할 때

#### 9.2.3.3 정렬 처리 방법

쿼리에 ORDER BY가 사용되면 다음 3가지 처리 방법 중 하나로 정렬이 처리된다.
일반적으로 아래쪽에 있는 정렬 방법으로 갈수록 처리 속도는 떨어진다.

| 정렬 처리 방법 | 실행 계획의 Extra 칼럼 내용 |
|---|---|
| 인덱스를 이용한 정렬 | 별도 표기 없음 |
| 조인에서 드라이빙 테이블만 정렬 | "Using filesort" 메시지가 표시됨 |
| 임시 테이블을 이용한 정렬 | "Using temporary; Using filesort" 메시지가 표시됨 |

##### 9.2.3.3.1 인덱스를 이용한 정렬

ORDER BY에 명시된 칼럼이 드라이빙 테이블에 속하고, ORDER BY의 순서대로 생성된 인덱스가 있어야한다.
또한 WHERE 절에 첫 번째로 읽는 테이블의 칼럼에 대해 조건이 있다면 그 조건과 ORDER BY는 같은 인덱스를 사용할 수 있어야 한다.
생성된 인덱스는 B-Tree 계열의 인덱스이어야 하고 조인이 사용되는 경우 네스티드 루프(Nested-loop) 방식의 조인이어야 한다.

##### 9.2.3.3.2 조인에서 드라이빙 테이블만 정렬

조인의 결과로 레코드 건수가 늘어나고, 레코드 하나하나의 크기도 늘어난다.
조인을 실행하기 전 드라이빙 테이블의 레코드를 먼저 정렬한 다음 조인을 실행하는 것이 정렬의 차선책이다.
이 방법으로 정렬이 처리되려면 조인에서 드라이빙 테이블의 칼럼만으로 ORDER BY 절을 작성해야 한다.

##### 9.2.3.3.3 임시 테이블을 이용한 정렬

조인의 드라이빙 테이블 정렬 패턴 외에 정렬을 해야 한다면 조인의 결과를 임시 테이블에 저장하고, 그 결과를 다시 정렬하는 과정을 거친다.

##### 9.2.3.3.4 정렬 처리 방법의 성능 비교

- 스트리밍 방식  
서버 쪽에서 처리할 데이터가 얼마인지와 관계없이 조건에 일치하는 레코드가 검색될 때마다 바로 클라이언트에게 전송해 주는 방식을 의미한다.
스트리밍 방식으로 처리되는 쿼리에서 LIMIT처럼 결과 건수를 제한하는 조건들은 쿼리의 실행 시간을 줄여줄 수 있다.

- 버퍼링 방식  
ORDER BY나 GROUP BY 같은 처리는 결과가 스트리밍되는 것을 불가능하게 한다.
우선 WHERE 조건에 일치하는 모든 레코드를 가져온 후, 정렬하거나 그루핑해서 차례대로 보내야 하기 때문이다.

인덱스를 사용한 정렬 방식만 스트리밍 형태의 처리이며, 조인에서 드라이빙 테이블만 정렬 방식과 임시 테이블을 이용한 정렬 방식 모두 버퍼링된 후 정렬된다.

#### 9.2.3.4 정렬 상태 변수

```SQL
FLUSH STATUS;
SHOW STATUS LIKE 'SORT%';
```

### 9.2.4 GROUP BY 처리

#### 9.2.4.1 인덱스 스캔을 이용하는 GROUP BY

조인의 드라이빙 테이블에 속한 칼럼만 이용해 그루핑할 때 GROUP BY 칼럼으로 인덱스가 있다면 그 인덱스를 차례대로 읽으면서 그루핑 작업을 수행하고 그 결과로 조인을 처리한다.

#### 9.2.4.2 루스 인덱스 스캔을 이용하는 GROUP BY

인덱스의 레코드를 건너뛰면서 필요한 부분만 읽어서 가져오는 것을 의미한다.
실행 계획의 Extra 칼럼에 "Using index for group-by" 코멘트가 표시된다.

```SQL
-- INDEX (emp_no, first_date)
SELECT emp_no FROM salaries WHERE first_data='1985-03-01' GROUP BY emp_no;
```

#### 9.2.4.3 임시 테이블을 사용하는 GROUP BY

인덱스를 사용하지 못할 때는 임시 테이블을 만들어 처리한다.
GROUP BY가 필요한 경우 내부적으로 GROUP BY 절의 칼럼들로 구성된 유니크 인덱스를 가진 임시 테이블을 만들어서 중복 제거와 집합 함수 연산을 수행한다.
조인 결과를 한 건씩 가져와 임시 테이블에서 중복 체크를 하면서 INSERT 또는 UPDATE를 실행한다.

```SQL
SELECT e.last_name, AVG(s.salary) FROM employees e, salaries s WHERE s.emp_no = e.emp_no GROUP BY e.last_name;

CREATE TABLE ... (last_name varchar(16), salary INT, UNIQUE INDEX ux_lastname (last_name));
```

### 9.2.5 DISTINCT 처리

#### 9.2.5.1 SELECT DISTINCT

SELECT 되는 레코드 중에서 유니크한 레코드(튜플)만 가져오고자 하면 SELECT DISTINCT 형태의 쿼리 문장을 사용한다.
특정 컬럼만 유니크하게 조회하는 것이 아니다.

```SQL
SELECT DISTINCT emp_no FROM salaries;
SELECT emp_no FROM salaries GROUP BY emp_no;
```

#### 9.2.5.2 집합 함수와 함께 사용된 DISTINCT

집합 함수 내에서 사용된 DISTINCT는 그 집합 함수의 인자로 전달된 칼럼 값이 유니크한 것들을 가져온다.
아래 쿼리는 내부적으로 COUNT(DISTINCT s.salary)를 처리하기 위해 임시 테이블을 사용한다.
하지만 쿼리의 실행 계획에는 임시 테이블(Using temporary)을 사용한다는 메시지는 표시되지 않는다.

```SQL
-- s.salary & e.last_name 2개의 임시 테이블
SELECT COUNT(DISTINCT s.salary), COUNT(DISTINCT e.last_name) FROM employees e, salaries s 
WHERE e.emp_no = s.emp_no AND e.emp_no BETWEEN 100001 AND 100100;
```

인덱스된 칼럼에 대해 DISTINCT 처리를 수행할 때는 인덱스를 풀 스캔하거나 레인지 스캔하면서 임시 테이블 없이 최적화된 처리를 수행할 수 있다.

### 9.2.6 내부 임시 테이블 활용

MySQL 엔진이 스토리지 엔진으로부터 받아온 레코드를 정렬하거나 그루핑할 때는 내부적인 임시 테이블(Internal temporary table)을 사용한다.
임시 테이블은 처음에는 메모리에 생성됐다가 테이블의 크기가 커지면 디스크로 옮겨진다.

#### 9.2.6.1 메모리 임시 테이블과 디스크 임시 테이블

메모리는 TempTable이라는 스토리지 엔진을 사용하고, 디스크에 저장되는 임시 테이블은 InnoDB 스토리지 엔진(또는 TempTable 스토리지 엔진의 MMAP 파일 버전)을 사용하도록 개선됐다.

#### 9.2.6.2 임시 테이블이 필요한 경우

- ORDER BY와 GROUP BY에 명시된 칼럼이 다른 쿼리
- ORDER BY나 GROUP BY에 명시된 칼람이 조인의 순서상 첫 번째 테이블이 아닌 경우
- DISTINCT와 ORDER BY가 동시에 쿼리에 존재하는 경우 또는 DISTINCT가 인덱스로 처리되지 못하는 경우
- UNION이나 UNION DISTINCT가 사용된 쿼리(select_type 칼럼이 UNION RESULT인 경우)
- 쿼리의 실행 계획에서 select_type이 DERIVED인 경우

#### 9.2.6.3 임시 테이블이 디스크에 생성되는 경우

- UNION이나 UNION AL에서 SELECT되는 칼럼 중에서 길이가 512바이트 이상인 크기의 칼림이 있는 경우
- GROUP BY나 DISTINCT 칼럼에서 512바이트 이상인 크기의 칼림이 있는 경우
- 메모리 임시 테이블 크기가 (MEMORY 스토리지 엔진에서) tmp_table_size 또는 max_heap_table_size 시스템 변수보다 크거나 (TempTable 스토리지 엔진에서) temptable_max_ram 시스템 변수 값보다 큰 경우

#### 9.2.6.4 임시 테이블 관련 상태 변수

```SQL
FLUSH STATUS;
SHOW SESSION STATUS LIKE 'Created_tmp%';
```

## 9.3 고급 최적화

옵티마이저가 실행 계획을 수립할 때 통계 정보와 옵티마이저 옵션을 결합해서 최적의 실행 계획을 수립하게 된다.
옵티마이저 옵션은 크게 조인 관련된 옵티마이저 옵션과 옵티마이저 스위치로 구분할 수 있다.

### 9.3.1 옵티마이저 스위치 옵션

옵티마이저 스위치 옵션은 글로벌과 세션별로 설정할 수 있는 시스템 변수이다.
"SET_VAR" 옵티마이저 힌트를 이용해 현재 쿼리에만 설정할 수 있다.

```SQL
SET GLOBAL optimizer_switch='index_merge=on,index_merge_union=on,...';
SET SESSION optimizer_switch='index_merge=on,index_merge_union=on,...';

SELECT /** SET_VAR(optimizer_switch='condition_fanout_filter=off') */ ... FROM ...
```

#### 9.3.1.1 Multi-Range Read(mrr)

인덱스 또는 드라이빙 테이블에서 레코드를 읽을 때 검색된 레코드 수만큼 베이스(드리븐) 테이블을 읽어야 한다.
MRR 방식은 조건에 맞는 엔트리를 키 값에 따라 정렬해 보관하고 정렬된 키의 순서대로 베이스(드리븐) 테이블을 읽는다.
MRR 작업은 디스크 랜덤 엑세스를 줄이고 쿼리 결과가 ID로 정렬된다.

mrr 옵션은 MRR 최적화를 활성화할지 여부를 결정한다.
mrr_cost_based 옵션은 비용을 기반으로 MRR 최적화를 수행할지, 아니면 가능할 때마다 MRR을 적용할지를 결정하는 옵션이다.

#### 9.3.1.2 블록 네스티드 루프 조인(block_nested_loop)

**네스티드 루프 조인**(Nested Loop Join)은 드라이빙 테이블의 레코드를 한 건 읽어서 드리븐 테이블의 일치하는 레코드를 찾아서 조인을 수행한다.

```pseudocode
for(row1 IN driving_table) {
  for(row2 IN driven_table) {
    if(condition_matched) return (row1, row2)
  }
}
```

드리븐 테이블의 조인 조건이 인덱스를 이용할 수 없는 상황에서 네스트드 루프 조인 방식은 드라이빙 테이블에서 검색된 레코드 수만큼 드리븐 테이블을 풀 스캔한다.
**블록 네스티드 루프 조인**은 옵티마이저가 드라이빙 테이블에서 읽은 레코드를 조인 버퍼에 캐시한 후 드리븐 테이블과 조인 버퍼를 조인한다.
일반적으로 조인이 수행된 후 가져 오는 결과는 드라이빙 테이블의 순서에 의해 결정되지만, 조인 버퍼가 사용되는 조인에서는 결과의 정렬 순서가 흐트러질 수 있다.
"**Using Join buffer**"는 **조인 버퍼**를 사용한다는 것을 의미한다.

#### 9.3.1.3 인덱스 컨디션 푸시 다운(index_condition_pushdown)

인덱스를 범위 제한 조건으로 사용하지 못한다고 하더라도 인덱스에 포함된 칼럼 조건이 있다면 스토리지 엔진으로 전달해 인덱스에서 읽을 엔트리를 최대한 필터링한다.

```SQL
EXPLAIN SELECT* FROM employees WHERE last_name='Acton' AND first_name LIKE '%sal';
```

| id | select_type | table | type | key | key_len | Extra |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | employees | ref | ix_lastname_firstname | 66 | Using index condition |

#### 9.3.1.4 인덱스 확장(use_index_extensions)

InnoDB 스토리지 엔진을 사용하는 테이블에서 세컨더리 인덱스에 자동으로 추가된 프라이머리 키를 활용할지를 결정하는 옵션이다.
실행 계획의 key_len 칼럼은 이 쿼리가 인덱스를 구성하는 칼럼 중에서 어느 부분(칼럼)까지 사용했는지를 바이트 수로 보여주는데, from_date 칼럼(3바이트)과 dept_emp(16바이트)까지 사용했다는 것을 알 수 있다.

```SQL
EXPLAIN SELECT * FROM dept_emp WHERE from_date='1987-07-25' AND dept_no='d001';
```

| id | select_type | table | type | key | key_len | Extra |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | dept_emp | ref | ix_fromdate | 19 | Using index |

#### 9.3.1.5 인덱스 머지(index_merge)

쿼리에 사용된 각각의 조건이 서로 다른 인덱스를 사용할 수 있고 그 조건을 만족하는 레코드 건수가 많을 것으로 예상될 때 MySQL 서버는 인덱스 머지 실행 계획을 선택한다.
인덱스 머지 실행 계획에는 index_merge_intersection, index_merge_union, index_merge_sort_union이 있다.
index_merge 옵티마이저 옵션은 3개의 최적화 옵션을 한 번에 제어할 수 있는 옵션이다.

#### 9.3.1.6 인덱스 머지 - 교집합(index_merge_intersection)

인덱스 머지의 "**Using intersect**"는 WHERE 절에 사용된 2개 이상의 조건이 각각의 인덱스를 사용하되 AND 연산자로 연결된 경우 사용되는 최적화다.

```SQL
EXPLAIN SELECT * FROM employees WHERE first_name='Georgi' AND emp_no BETWEEN 10000 AND 20000;
```

| type | key | key_len | Extra |
|---|---|---|---|
| index_merge | ix_firstname,PRIMARY | 62,4 | Using intersect(ix_firstname,PRIMARY); Using where |

#### 9.3.1.7 인덱스 머지 - 합집합(index_merge_union)

인덱스 머지의 "**Using union**"는 WHERE 절에 사용된 2개 이상의 조건이 각각의 인덱스를 사용하되 OR 연산자로 연결된 경우 사용되는 최적화다.
첫 번째 인덱스의 검색 결과와 두 번째 인덱스 검색 결과를 병합할 때, 중복을 제거하기 위해 두 검색 결과의 프라이머리 키 값을 비교해 중복된 레코드를 제거한다.

```SQL
EXPLAIN SELECT * FROM employees WHERE first_name='Matt' OR hire_date = '1987-03-31';
```

| type | key | key_len | Extra |
|---|---|---|---|
| index_merge | ix_firstname,PRIMARY | 58,3 | Using union(ix_firstname,ix_hiredate); Using where |

#### 9.3.1.8 인덱스 머지 - 정렬 후 합집합(index_merge_sort_union)

인덱스 머지의 "**Using sort union**"는 인덱스 검색 결과를 병합하기 전에 정렬이 필요할 때 사용되는 최적화다.
ix_hiredate 인덱스 범위 검색은 ID로 정렬되어 있지 않아 중복을 제거하기 위해 정렬 작업이 추가로 필요하다.

```SQL
EXPLAIN SELECT * FROM employees WHERE first_name='Matt' OR hire_date BETWEEN '1987-03-01' AND '1987-03-31';;
```

| type | key | key_len | Extra |
|---|---|---|---|
| index_merge | ix_firstname,PRIMARY | 58,3 | Using sort union(ix_firstname,ix_hiredate); Using where |

#### 9.3.1.9 세미 조인(semijoin)

다른 테이블과 조인을 수행하지 않고 다른 테이블에서 조건에 일치하는 레코드가 있는지 없는지만 체크하는 쿼리를 **세미 조인**(Semi-Join)이라고 한다.
세미 조인 최적화 전략으로 Table Pull-out, Duplicate Weed-out, First Match, Loose Scan, Materialization가 있다.

#### 9.3.1.10 테이블 풀-아웃(Table Pull-out)

세미 조인의 서브쿼리에 사용된 테이블을 끄집어낸 후에 inner join 쿼리로 재작성하는 형태의 최적화다.
아래 쿼리 실행 계획에서 id 칼럼 값이 모두 1이라는 것은 조인으로 처리됐음을 의미한다.

```SQL
EXPLAIN SELECT * FROM employees e WHERE e.emp_no IN (
  SELECT de.emp_no FROM dept_emp de WHERE de.dept_no = 'd009'
);
```

| id | select_type | tabe | type | key | rows | Extra |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | de | ref | PRIMARY | 46012 | Using index |
| 1 | SIMPLE | e | eq_ref | PRIMARY | 1 | NULL |

#### 9.3.1.11 퍼스트 매치(firstmatch)

서브 쿼리가 드리븐 테이블이 되고 조건이 일치하는 entry가 여러개 일 때, 조건에 일치하는 첫 레코드만 반환해 최적화를 수행한다.

```SQL
EXPLAIN SELECT * FROM employees e WHERE e.first_name = 'Matt' AND e.emp_no IN (
  SELECT t.emp_no FROM titles t WHERE t.from_date BETWEEN '1995-01-01' AND '1995-01-30'
);
```

| id | select_type | tabe | type | key | rows | Extra |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | e | ref | ix_firstname | 233 | NULL |
| 1 | SIMPLE | t | ref | PRIMARY | 1 | Using where; Using index; FirstMatch(e) |

#### 9.3.1.12 루스 스캔(loosescan)

서브쿼리가 드라이빙 테이블이 되고 루스 인덱스 스캔을 사용할 수 있을 때 사용할 수 있는 최적화이다.

```SQL
EXPLAIN SELECT * FROM departments d WHERE d.dept_no IN (SELECT de.dept_no FROM dept_emp de);
```

| id | tabe | type | key | rows | Extra |
|---|---|---|---|---|---|
| 1 | de | ref | PRIMARY | 331143 |  Using index; LooseScan |
| 1 | d | eq_ref | PRIMARY | 1 | NULL |

#### 9.3.1.13 구체화(materialization)

서브 쿼리 결과를 index가 있는 임시 테이블을 만들어 조인을 수행한다.
Index된 칼럼은 조인 조건에 명시된 칼럼으로 중복을 제거하기 위해 사용된다.

```SQL
EXPLAIN SELECT * FROM employees e WHERE e.emp_no IN (
  SELECT de.emp_no FROM dept_emp de WHERE de.from_date='1995-01-01'
);
```

| id | select_type | tabe | type | key | ref |
|---|---|---|---|---|---|
| 1 | SIMPLE | \<subquery2> | ALL | NULL | NULL |
| 1 | SIMPLE | e | eq_ref | PRIMARY | \<subquery2>.emp_no |
| 2 | MATERIALIZED | de | ref | ix_fromdate | const |

#### 9.3.1.14 중복 제거(duplicateweedout)

조인 결과에서 임시 테이블을 이용해 중복을 제거한다.

```SQL
EXPLAIN SELECT * FROM employees e WHERE e.emp_no IN (
  SELECT s.emp_no FROM salaries s WHERE s.salary > 150000
);

SELECT e.* FROM employees e, salaries s
WHERE e.emp_no = s.emp_no AND s.salary > 150000
GROUP BY e.emp_no;
```

| id | select_type | tabe | type | key | Extra |
|---|---|---|---|---|---|
| 1 | SIMPLE | s | range | ix_salary | Using where; Using index; Start temporary |
| 1 | SIMPLE | e | eq_ref | PRIMARY | End temporary |

#### 9.3.1.15 컨디션 팬아웃(condition_fanout_filter)

옵티마이저가 조건에 만족하는 레코드 비율울 예측할 수 있게 실행 계획의 filtered 칼럼 값을 예측한다.

#### 9.3.1.16 파생 테이블 머지(derived_merge)

FROM 절에 사용된 서브쿼리를 외부 쿼리와 병합할 때 임시 테이블을 사용하지 않도록한다.

#### 9.3.1.17 인비저블 인덱스(use_invisible_index)

옵티마이저가 INVISIBLE 인덱스를 사용 가능하도록 만드는 설정이다.

#### 9.3.1.18 스킵 스캔(skip_scan)

복합 인덱스의 선행 칼럼이 조건절에 사용되지 않았더라도 후행 칼럼의 조건만으로도 인덱스를 이용해 검색 작업 범위를 줄인다.
선행 칼럼의 다양한 값을 가지는 경우 스킵 스캔 최적화는 비효율적이다.

#### 9.3.1.19 해시 조인(hash_join)

조인 조건의 칼럼이 인덱스가 없거나 조인 대상 테이블 중 일부의 레코드 건수가 적은 경우에 대해서만 해시 조인 알고리즘을 사용한다.
해시 조인은 빌드 단계(Build-phase)와 프로브 단계(Probe_phase)로 나누어 처리된다.
빌드 단계에서는 조인 대상 테이블 중에서 레코드 건수가 적어 해시 테이블로 만들기 용이한 테이블을 골라 메모리에 해시 테이블을 생성하는 작업을 수행한다.
프로브 단계는 나머지 테이블의 레코드를 읽어 해시 테이블의 일치하는 레코드를 찾는 과정을 의미한다.

```SQL
EXPLAIN FORMAT=TREE SELECT * FROM dept_emp de, employees e WHERE de.from_date = '1995-01-01' AND e.emp_no<109004\G
```

```plaintext
EXPLAIN: -> Inner hash join (no condition)  (cost=1.71e+6 rows=8.55e+6)
    -> Filter: (e.emp_no < 109004)  (cost=15297 rows=149960)
        -> Index range scan on e using PRIMARY over (emp_no < 109004)  (cost=15297 rows=149960)
    -> Hash
        -> Index lookup on de using ix_fromdate (from_date=DATE'1995-01-01')  (cost=59.4 rows=57)
```

#### 9.3.1.20 인덱스 정렬 선호(prefer_ordering_index)

옵티마이저는 ORDER BY 또는 GROUP BY를 인덱스를 사용해 처리 가능한 경우 쿼리의 실행 계획에서 인덱스 가중치를 높이 설정한다.
간혹 아래 쿼리를 실행할 때 ORDER BY 가중치를 너무 높게 측정해 ix_hiredate 인덱스를 사용하지 못하는 경우가 있다.
옵티마이저가 자주 실수하는 경우 prefer_ordering_index 옵션으로 off로 변경하면 된다.

```SQL
EXPLAIN SELECT * FROM employees WHERE hire_date BETWEEN '1985-01-01' AND '1985-02-01' ORDER BY emp_no;
```

### 9.3.2 조인 최적화 알고리즘

#### 9.3.2.1 Exhaustive 검색 알고리즘

#### 9.3.2.2 Greedy 검색 알고리즘

## 9.4 쿼리 힌트
