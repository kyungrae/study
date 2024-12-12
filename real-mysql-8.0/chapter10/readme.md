# 10. 실행 계획

옵티마이저가 항상 좋은 실행 계획을 만들어내는 것은 아니다.
옵티마이저의 실수를 사용자가 보완하기 위해 옵티마이저가 수립한 실행 계획을 확인하고 이해할 수 있어야 한다.

## 10.1 통계 정보

### 10.1.1 테이블 및 인덱스 통계 정보

테이블의 통계 정보를 mysql.innodb_index_stats과 mysql.innodb_table_stats 테이블에 관리한다.
테이블을 생성할 때 STATS_PERSISTENT 옵션을 지정해 통계 정보를 테이블에 보관할 수 있다.

```SQL
-- innodb_index_stats와 innodb_table_stats에 employees 테이블의 통계 데이터를 저장
ALTER TABLE employees STATS_PERSISTENT = 1;
```

```sql
SELECT * FROM mysql.innodb_table_stats;
```

| table_name | n_rows | clustered_index_size | sum_of_other_index_sizes |
|---|---|---|---|
| salaries | 2630388 | 5672 | 4713 |

- n_rows: 테이블의 전체 레코드 건수
- clustered_index_size: 클러스터 인덱스의 페이지 수
- sum_of_other_index_sizes: 테이블의 세컨더리 인덱스의 페이지 수의 합

```sql
SELECT * FROM mysql.innodb_index_stats;
```

| index_name | stat_name | stat_value | sample_size | stat_description |
|---|---|---|---|---|
| PRIMARY | n_diff_pfx01 | 279347 | 20 | emp_no |
| PRIMARY | n_diff_pfx02 | 2630144 | 20 | emp_no,from_date |
| PRIMARY | n_leaf_pages | 5632 | NULL | Number of leaf pages in the index |
| PRIMARY | size | 5672 | NULL | Number of pages in the index |
| ix_salary | n_diff_pfx01 | 105357 | 20 | salary |
| ix_salary | n_diff_pfx02 | 2613016 | 20 | salary,emp_no |
| ix_salary | n_diff_pfx03 | 2495232 | 20 | salary,emp_no,from_date |
| ix_salary | n_leaf_pages | 4104 | NULL | Number of leaf pages in the index |
| ix_salary | size | 4713 | NULL | Number of pages in the index |

- stat_name='n_diff_pfx%': 인덱스가 가진 유니크한 값의 개수
- stat_name='n_leaf_pages': 인덱스의 리프 노드 페이지 개수
- stat_name='size': 인덱스 트리의 전체 페이지 개수

테이블의 통계 정보를 수집할 때 몇 개의 페이지를 샘플링할지 결정하는 옵션으로 innodb_stats_transient_sample_pages(자동으로 통계 정보 수집이 실행될 때 샘플링할 페이지 수)와 innodb_stats_persistent_sample_pages(analyze table 명령으로 통계 정보 수집이 실행될 때 샘플링할 페이지 수) 시스템 변수가 제공된다.

### 10.1.2 히스토그램

#### 10.1.2.1 히스토그램 정보 수집 및 삭제

히스토그램 정보는 칼럼 단위로 관리되는데, 이는 자동으로 저장되지 않고 `ANALYZE TABLE ... UPDATE HISTOGRAM` 명령을 실행해 수동으로 수집 및 관리된다.
수집된 히스토그램 정보는 시스템 딕셔너리에 함께 저장되고, MySQL 서버가 시작될 때 딕셔너리의 히스토그램 정보를 information.column_statistics 테이블로 로드한다.

```SQL
ANALYZE TABLE employees UPDATE HISTOGRAM ON gender, hire_date;
SELECT * FROM information_schema.COLUMN_STATISTICS WHERE TABLE_NAME='employees'\G
ANALYZE TABLE employees DROP HISTOGRAM ON gender, hire_date;
```

```json
// column: gender
{
  "buckets": [
    [1, 0.5975228946036536],
    [2, 1.0]
  ],
  "data-type": "enum",
  "null-values": 0.0,
  "collation-id": 45,
  "last-updated": "2024-10-29 14:15:29.443422",
  "sampling-rate": 0.34799360805340723,
  "histogram-type": "singleton",
  "number-of-buckets-specified": 100
}
// column: hire_date
{
  "buckets": [
    ["1985-01-01", "1985-02-28", 0.009912241331563518, 31],
    ["1985-03-01", "1985-03-28", 0.019996371241131027, 28],
    ["1985-03-29", "1985-04-25", 0.029870415110915878, 28],
    ~
    ["1998-08-18", "2000-01-28", 1.0, 501]
  ],
  "data-type": "date",
  "null-values": 0.0,
  "collation-id": 8,
  "last-updated": "2024-10-29 14:15:29.444408",
  "sampling-rate": 0.34799360805340723,
  "histogram-type": "equi-height",
  "number-of-buckets-specified": 100
}
```

히스토그램 종류

- Singleton  
칼럼값 개별로 레코드 건수를 관리하는 히스토그램으로, Value-Based 히스토그램 또는 도수 분포라고도 부른다.
싱글톤 히스토그램은 각 버킷이 칼럼의 값, 발생 빈도 비율 2개의 값을 가진다.

- Equi-Height  
칼람값의 범위를 균등한 개수로 구분해서 관리하는 히스토그램으로, Height-Balanced 히스토그램이라고 불린다.
높이 균형 히스토그램은 각 버킷이 범위 시작 값, 마지막 값, 발생 빈도율, 버킷에 포함된 유니크한 값의 개수 4개의 값을 가진다.

히스토그램의 모든 레코드 건수 비율은 누적으로 표시된다.

#### 10.1.2.2 히스토그램 용도

히스토그램 정보가 없으면 옵티마이저는 데이터가 균등하게 분포돼 있을 것으로 예측한다.
하지만 히스토그램이 있으면 특정 범위의 데이터가 많고 적음을 식별할 수 있다.

```SQL
EXPLAIN SELECT * FROM employees where first_name='Zita' AND birth_date BETWEEN '1950-01-01' AND '1960-01-01';
```

| id | select_type | table | type | key | rows | filtered |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | employees | ref | ix_firstname | 224 | 11.11 |

```SQL
ANALYZE TABLE employees UPDATE HISTOGRAM ON first_name, birth_date;
EXPLAIN SELECT * FROM employees where first_name='Zita' AND birth_date BETWEEN '1950-01-01' AND '1960-01-01';
```

| id | select_type | table | type | key | rows | filtered |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | employees | ref | ix_firstname | 224 | 60.82 |

#### 10.1.2.3 인덱스와 히스토그램

쿼리의 실행 계획을 수립할 때 사용 가능한 인덱스들로부터 조건절에 일치하는 레코드 건수를 대략 파악하고 최종적으로 가장 나은 실행 계획을 선택한다.
이때 조건절에 일치하는 레코드 건수를 예측하기 위해 옵티마이저는 실제 인덱스 B-Tree를 샘플링해서 살펴본다.
이 작업을 매뉴얼에서는 "인덱스 다이브(Index Dive)"라고 표현한다.

MySQL 8.0 서버에서는 인덱스된 칼럼을 검색 조건으로 사용하는 경우 그 칼럼의 히스토그램은 사용하지 않고 실제 인덱스 다이브를 통해 직접 수정한 정보를 활용한다.
실제 검색 조건의 대상 값에 대한 샘플링을 실행하기 때문에 히스토그램보다 정확한 결과를 기대할 수 있다.
하지만 인덱스 다이브 작업은 어느 정도의 비용이 필요하며, 때로는 (IN 절에 값이 많이 명시된 경우) 실행 계획 수립만으로도 상당한 인덱스 다이브를 실행하고 비용도 그만큼 커진다.

### 10.1.3 코스트 모델

전체 쿼리의 비용을 계산하는 데 필요한 단위 작업들의 비용을 코스트 모델이라고 한다.
서버의 코스트 모델은 mysql 데이터베이스의 server_cost 테이블과 engine_cost 테이블에 저장된다.

```SQL
SELECT * FROM mysql.engine_cost;
SELECT * FROM mysql.server_cost;
```

| table | cost_name | 설명 |
|---|---|---|
| engine_cost | io_block_read_cost | 디스크 데이터 페이지 읽기 |
| engine_cost | memory_block_read_cost | 메모리 데이터 페이지 읽기 |
| server_cost | disk_temptable_create_cost | 디스크 임시 테이블 생성 |
| server_cost | disk_temptable_row_cost | 디스크 임시 테이블의 레코드 읽기 |
| server_cost | key_compare_cost | 인덱스 키 비교 |
| server_cost | memory_temptable_create_cost | 메모리 임시 테이블 생성 |
| server_cost | memory_temptable_row_cost | 메모리 임시 테이블의 레코드 읽기 |
| server_cost | row_evaluate_cost | 레코드 비교 |

## 10.2 실행 계획 확인

### 10.2.1 실행 계획 출력 포맷

FORMAT 옵션을 사용해 실행 계획의 표시 방법을 JSON, TREE, 단순 테이블 형태로 선택할 수 있다.

```SQL
EXPLAIN [FORMAT={TREE|JSON}] SELECT * FROM employees e WHERE first_name = 'ABC';
```

### 10.2.2 쿼리의 실행 시간 확인

EXPLAIN ANALYZE 기능으로 쿼리의 실행 계획과 단계별 소요된 시간 정보를 확인할 수 있다.

```SQL
EXPLAIN ANALYZE 
SELECT e.emp_no, avg(s.salary) 
FROM employees e INNER JOIN salaries s ON s.emp_no = e.emp_no 
AND s.salary > 50000 AND s.from_date <= '1990-01-01' AND s.to_date > '1990-01-01'
WHERE e.first_name = 'Matt' 
GROUP BY e.emp_no\G
```

```text
-> Group aggregate: avg(s.salary)  (cost=483 rows=122) (actual time=0.205..5.11 rows=48 loops=1)
    -> Nested loop inner join  (cost=471 rows=122) (actual time=0.123..5.06 rows=48 loops=1)
        -> Covering index lookup on e using ix_firstname (first_name='Matt')  (cost=24 rows=233) (actual time=0.0817..0.185 rows=233 loops=1)
        -> Filter: ((s.salary > 50000) and (s.from_date <= DATE'1990-01-01') and (s.to_date > DATE'1990-01-01'))  (cost=0.978 rows=0.523) (actual time=0.0193..0.0206 rows=0.206 loops=233)
            -> Index lookup on s using PRIMARY (emp_no=e.emp_no)  (cost=0.978 rows=9.42) (actual time=0.016..0.019 rows=9.53 loops=233)
```

TREE 포맷의 실행 계획에서 들여쓰기는 호출 순서를 의미하며, 실제 실행 순서는 다음 기준으로 읽으면 된다.

- 들여쓰기가 같은 레벨에서는 상단에 위치한 라인이 먼저 실행
- 들여쓰기가 다른 레벨에서는 가장 안쪽에 위치한 라인이 먼저 실행

## 10.3 실행 계획 분석

### 10.3.1 id 칼럼

실행 계획에서 가장 왼쪽에 표시되는 id 칼럼은 단위 select 쿼리별로 부여되는 식별자 값이다.
하나의 SELECT 문장 안에서 여러 개의 테이블을 조인하면 테이블의 개수만큼 실행 계획 레코드가 출력되지만 같은 id값이 부여된다.
실행 계획의 id 칼럼이 테이블의 접근 순서를 의미하지 않는다.

### 10.3.2 select_type 칼럼

각 단위 SELECT 쿼리가 어떤 타입의 쿼리인지 표시된다.

#### 10.3.2.1 SIMPLE

UNION이나 서브쿼리를 사용하지 않는 단순한 SELECT 쿼리인 경우 해당 쿼리 문장의 select_type은 SIMPLE로 표시된다(쿼리에서 조인이 포함된 경우에도 마찬가지다).
쿼리 문장이 아무리 복잡하더라도 실행 계획에서 select_type이 SIMPLE인 단위 커리는 하나만 존재한다.

#### 10.3.2.2 PRIMARY

UNION이나 서브쿼리를 가지는 SELECT 쿼리의 실행 계획에서 가장 바깥쪽(Outer)에 있는 단위 쿼리는 select_type이 PRIMARY로 표시된다.

#### 10.3.2.3 UNION

UNION으로 결합하는 단위 SELECT 쿼리 가운데 첫 번째를 제외한 두 번째 이후 단위 SELECT 쿼리의 select_type은 UNION으로 표시된다.
UNION의 첫 번째 단위 SELECT는 select_type이 UNION이 아니라 UNION되는 쿼리 결과들을 모아서 저장하는 파생 테이블(DERIVED)이 select_type으로 표시된다.

| id | select_type | table | type | key | ref | rows | Extra |
|---|---|---|---|---|---|---|---|
| 1 | PRIMARY | \<derived2> | ALL | NULL | NULL | 30 | NULL |
| 2 | DERIVED | e1 | index | ix_hiredate | NULL | 300252 | NULL |
| 3 | UNION | e2 | index | ix_hiredate | NULL | 300252 | NULL |
| 4 | UNION | e3 | index | ix_hiredate | NULL | 300252 | NULL |

#### 10.3.2.4 DEPEDENT UNION

DEPENDENT UNION 또한 UNION select_type과 같이 UNION이나 UNION ALL로 집합을 결합하는 쿼리에서 표시된다.
그리고 DEPENDENT는 union이나 union all로 결합된 단위 쿼리가 외부 쿼리에 의해 영향을 받는 것을 의미한다.
내부 쿼리가 외부의 값을 참조해서 처리될 때 select_type에 DEPENDENT 키워드가 표시된다.

#### 10.3.2.5 UNION RESULT

UNION RESULT는 UNION 결과를 담아두는 임시 테이블을 의미한다.
UNION RESULT는 실제 쿼리에서 단위 쿼리가 아니기 떄문에 별도의 id 값은 부여되지 않는다.

| id | select_type | table | type | key | rows | Extra |
|---|---|---|---|---|---|---|
| 1 | PRIMARY | salaries | range | ix_salary | 191348 | Using where; Using index |
| 2 | UNION | dept_emp | range | ix_fromdate | 5325 | Using where; Using index |
| NULL | UNION RESULT | \<union1,2> | ALL | NULL | NULL | Using temporay |

#### 10.3.2.6 SUBQUERY

select_type의 SUBQUERY는 FROM 절 이외에서 사용되는 서브쿼리를 의미한다.

| id | select_type | table | type | key | rows | Extra |
|---|---|---|---|---|---|---|
| 1 | PRIMARY | e | const | PRIMARY | 1 | NULL |
| 2 | SUBQUERY | dm | index | PRIMARY | 24 | Using index |
| 2 | SUBQUERY | de | ref | PRIMARY | 41392 | Using index |

#### 10.3.2.7 DEPENDENT SUBQUERY

서브쿼리가 바깥쪽(Outer) SELECT 쿼리에서 정의된 칼럼을 사용하는 경우, select_type에 DEPENDENT SUBQUERY라고 표시된다.

#### 10.3.2.8 DERIVED

서브쿼리가 FROM 절에 사용된 경우 항상 select_type이 DERIVED인 실행 계획을 만든다.
DERIVED는 단위 SELECT 쿼리의 실행 결과로 메모리나 디스크에 임시 테이블을 생성하는 것을 의미한다.

#### 10.3.2.9 DEPENDENT DERIVED

래터럴 조인(LATERAL JOIN) 기능이 추가되면서 FROM 절의 서브쿼리에서도 외부 칼럼을 참조할 수 있게 됐다.

#### 10.3.2.10 UNCACHEABLE SUBQUERY & UNCACHEABLE UNION

하나의 쿼리 문장에 서브쿼리가 하나만 있더라도 실제 그 서브쿼리가 한 번만 실행되는 것은 아니다.
그런데 조건이 똑같은 서브쿼리가 실행될 때는 다시 실행하지 않고 이전의 실행 결과를 그대로 할 수 있게 서브쿼리의 결과를 내부적인 캐시 공간에 담아둔다.
select_type이 SUBQUERY인 경우와 UNCACHEABLE SUBQUERY는 이 캐시를 사용할 수 있느냐 없느냐의 차이가 있다.
서브쿼리에 포함된 요소에 의해 캐시 자체가 불가능할 수 있는데, 그럴 경우 select_type이 UNCACHEABLE SUBQUERY로 표시된다.

#### 10.3.2.12 MATERIALIZED

서브쿼리의 내용을 임시 테이블로 구체화한 후, 임시 테이블과 employees 테이블을 조인하는 형태로 최적화되어 처리한다.

| id | select_type | table | type | key | rows | Extra |
|---|---|---|---|---|---|---|
| 1 | SIMPLE | \<subquery2> | ALL | NULL | NULL | NULL |
| 1 | SIMPLE | e | eq_ref | PRIMARY | 1 | NULL |
| 2 | MATERIALIZED | salaries | range | ix_salary | 1 | Using where; Using index |

### 10.3.3 table 칼럼

실행 계획은 단위 SELECT 쿼리 기준이 아니라 테이블 기준으로 표시된다.
테이블 칼럼에 \<derived N> 또는 \<union M,N>과 같이 "<>"로 둘러싸인 이름이 명시되는 경우가 많은데, 이 테이블은 임시 테이블을 의미한다.
또한, "<>" 안에 표시되는 숫자는 단위 SELECT 쿼리의 id 값을 지칭한다.

### 10.3.4 partitions 칼럼

파티션이 여러 개인 테이블에서 불필요한 파티션을 빼고 쿼리를 수행하기 위해 접근해야 할 것으로 판단되는 테이블만 골라내는 과정을 파티셔닝 프루닝(partitioning pruning)이라고 한다.

```SQL
CREATE TABLE employees_2 (
  emp_no int NOT NULL,
  birth_date DATE NOT NULL,
  first_name VARCHAR(14) NOT NULL,
  last_name VARCHAR(16) NOT NULL,
  gender ENUM('M', 'F') NOT NULL,
  hire_date DATE NOT NULL,
  PRIMARY KEY (emp_no, hire_date)
) PARTITION BY RANGE COLUMNS(hire_date)
(PARTITION p1985_1990 VALUES LESS THAN ('1990-01-01'),
 PARTITION p1990_1995 VALUES LESS THAN ('1995-01-01'),
 PARTITION p1995_2000 VALUES LESS THAN ('2000-01-01'),
 PARTITION p2000_2005 VALUES LESS THAN ('2005-01-01'));

EXPLAIN SELECT * FROM employees_2 WHERE hire_date BETWEEN '1999-11-15' AND '2000-01-15';
```

| id | select_type | table | partitions | type | rows |
|---|---|---|---|---|---|
| 1 | SIMPLE | employees_2 | p1995_2000,p2000_2005 | ALL | 21743 |

type 칼럼의 값이 ALL이다. 물리적으로 개별 테이블처럼 별도의 저장 공간을 가지기 때문에 employees_2 테이블의 모든 파티션이 아니라 p1995_2000 파티션과 p2000_2005 파티션만 풀 스캔을 실행하게 된다.

### 10.3.5 type 칼럼

각 테이블의 접근 방법으로 해석하면 된다.

#### 10.3.5.1 system

레코드가 1건만 존재하는 테이블 또는 한 건도 존재하지 않는 테이블을 참조하는 형태의 접근 방법을 system이라고 한다.
MyISAM이나 MEMORY 테이블에서만 사용되는 접근 방법이다.

#### 10.3.5.2 const

프라이머리 키나 유니크 칼럼을 이용하는 WHERE 조건절을 가지고 있으며, 반드시 1건을 반환하는 쿼리의 처리 방식을 const라고한다.
실행 계획의 type 칼럼이 const인 실행 계획은 MySQL의 옵티마이저가 쿼리를 최적화하는 단계에서 쿼리를 먼저 실행해서 통째로 상수화한다.

#### 10.3.5.3 eq_ref

조인에서 처음 읽은 테이블의 칼럼값을 그 다음 읽어야 할 테이블의 프라이머리 키나 유니크 키 칼럼의 검색 조건에 사용할 때를 가르켜 eq_ref라고 한다.

#### 10.3.5.4 ref

인덱스의 종류와 관계없이 동등(equal) 조건으로 검색할 때는 ref 접근 방법이 사용된다.

#### 10.3.5.5 fulltext

전문 검색 인덱스(Full-text Search)를 사용해 레코드를 읽는 접근 방법을 의미한다.
쿼리에서 전문 인덱스를 사용하는 조건과 일반 인덱스를 사용하는 조건을 함께 사용하면 일반 인덱스의 접근 방법이 const, eq_ref, ref가 아니면 일반적으로 전문 인덱스를 사용한다.

#### 10.3.5.6 ref_or_null

ref 접근 방법과 같은데, NULL 비교가 추가된 형태다.

#### 10.3.5.7 unique_subquery

IN (subquery) 형태의 조건에서 subquery의 반환 값에는 중복이 없으므로 별도의 중복 제거 작업이 필요 없음

#### 10.3.5.8 index_subquery

IN (subquery) 형태의 조건에서 subquery의 반환 값에 중복된 값이 있을 수 있지만 인덱스를 이용해 중복된 값을 제거할 수 있음

#### 10.3.5.9 range

인덱스를 하나의 값이 아니라 범위로 검색하는 경우를 의미하는데, 주로 "<, >, IS NULL, BETWEEN, IN, LIKE" 등의 연산자를 이용해 인덱스를 검색할 때 사용된다.

#### 10.3.5.10 index_merge

2개 이상의 인덱스를 이용해 각각의 검색 결과를 만들어낸 후, 그 결과를 병합해서 처리하는 방식이다.

- 여러 인덱스를 읽어야 하므로 일반적으로 range 접근 방법보다 효율성이 떨어진다.
- 전문 검색 인덱스를 사용하는 쿼리에서는 index_merge가 적용되지 않는다.
- index_merge 접근 방법으로 처리된 결과는 항상 2개 이상의 집합이 되기 때문에 그 두 집합의 교집합이나 합집합, 또는 중복 제거와 같은 부가적인 작업이 더 필요하다.

#### 10.3.5.11 index

인덱스를 처음부터 끝까지 읽는 인덱스 풀 스캔을 의미한다.

#### 10.3.5.11 ALL

풀 테이블 스캔을 의미하는 접근 방법이다.

### 10.3.6 possible_keys 칼럼

옵티마이저가 최적의 실행 계획을 만들기 위해 후보로 선정했던 접근 방법에서 사용되는 인덱스의 목록이다.

### 10.3.7 key 칼럼

최종 선택된 실행 계획에서 사용하는 인덱스를 의미한다.

### 10.3.8 key_len 칼럼

인덱스의 각 레코드에서 몇 바이트 까지 사용했는지 알려주는 값이다.
key_len 필드의 값이 실제 데이터 타입의 길이보다 길게 표시되는 경우가 있다.
MySQL에서는 NOT NULL이 아닌 칼럼에서는 칼럼의 값이 NULL인지 아닌지를 저장하기 위해 1바이트를 추가로 사용한다.

### 10.3.9 ref 칼럼

접근 방법(type)이 ref면 참조 조건(Equal 비교 조건)으로 어떤 값이 제공됐는지 보여준다.
상수값을 지정했다면 ref 칼럼의 값은 const로 표시되고, 다른 테이블의 칼럼값이면 그 테이블명과 칼럼명이 표시된다.
콜레이션 변환이나 값 자체의 연산을 거쳐서 참조되는 경우 func라고 표시된다.

### 10.3.10 rows 칼럼

실행 계획의 효율성 판단을 위해 예측헀던 레코드 건수를 보여준다.
스토리지 엔진별로 가지고 있는 통계 정보를 참조해 옵티마이저가 산출해 낸 예상값이라서 정확하지 않다.
rows 칼럼에 표시되는 값은 반환하는 레코드의 예측치가 아니라 쿼리를 처리하기 위해 얼마나 많은 레코드를 읽고 체크해야 하는지를 의미한다.

### 10.3.11 filtered 칼럼

rows 칼럼 값은 인덱스를 사용하는 조건에만 일치하는 레코드 건수를 예측한 것이다.
하지만 인덱스를 사용하지 못하는 조건에 일치하는 레코드 건수를 파악하는 것도 중요하다.
filtered 칼럼의 값은 필터링되어 버려지는 레코드의 비율이 아니라 필터링되고 남은 레코드의 비율을 의미한다.

### 10.3.12 Extra 칼럼

주로 내부적인 처리 알고리즘에 대해 조금 더 깊이 있는 내용을 보여준다.

#### 10.3.12.21 Using filesort

조회된 레코드를 정렬용 메모리 버퍼에 복사해 퀵 소트 또는 힙 소트 알고리즘을 이용해 정렬을 수행하게 된다는 의미이다.

#### 10.3.12.22 Using index(커버링 인덱스)

데이터 파일을 전혀 읽지 않고 인덱스만 읽어서 쿼리를 모두 처리할 수 있을 때 표시된다.

#### 10.3.12.26 Using join buffer

드리븐 테이블에 검색을 위한 적적한 인덱스가 없다면 블록 네스티드 루프 조인이나 해시 조인을 사용한다.

#### 10.3.12.29 Using temporary

쿼리를 처리하는 동안 중간 결과를 담아 두기 위해 임시 테이블을 사용할 때 표시된다.
실행 계획의 Extra 칼럼에 Using temporary가 표시되지는 않지만 실제로 내부적으로 임시 테이블 사용할 때도 많다.

- FROM 절에 사용된 서브쿼리(derived table)
- UNION 또는 인덱스를 사용하지 못하는 DISTINCT 키워드를 포함하는 쿼리
- 인덱스를 사용하지 못하는 정렬 작업(Using file sort)

#### 10.3.12.30 Using where

MySQL 엔진 레이어에서 별도의 가공을 해서 필터링 작업을 처리하는 경우에만 Extra 칼럼에 Using where 코멘트가 표시된다.
