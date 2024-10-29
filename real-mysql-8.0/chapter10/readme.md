# 10. 실행 계획

옵티마이저가 항상 좋은 실행 계획을 만들어내는 것은 아니다.
옵티마이저의 실수를 사용자가 보완하기 위해 옵티마이저가 수립한 실행 계획을 확인하고 이해할 수 있어야 한다.

## 10.1 통계 정보

### 10.1.1 테이블 및 인덱스 통계 정보

테이블의 통계 정보를 mysql 데이터베이스의 innodb_index_stats 테이블과 innodb_table_stats 테이블에 관리한다.
테이블을 생성할 때 STATS_PERSISTENT 옵션을 지정해 통계 정보를 영구적으로 보관할 수 있다.

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

테이블의 통계 정보를 수집할 때 몇 개의 페이지를 샘플링할지 결정하는 옵션으로 innodb_stats_transient_sample_pages와 innodb_stats_persistent_sample_pages 시스템 변수가 제공된다.

- innodb_stats_transient_sample_pages  
자동으로 통계 정보 수집이 실행될 때 샘플링할 페이지 수(기본값 8)
- innodb_stats_persistent_sample_pages  
analyze table 명령으로 통계 정보 수집이 실행될 때 샘플링할 페이지 수(기본값 20)

### 10.1.2 히스토그램

#### 10.1.2.1 히스토그램 정보 수집 및 삭제

히스토그램 정보는 칼럼 단위로 관리되는데, 이는 자동으로 저장되지 않고 `ANALYZE TABLE ... UPDATE HISTOGRAM` 명령을 실행해 수동으로 수집 및 관리된다.
수집된 히스토그램 정보는 시스템 딕셔너리에 함께 저장되고, MySQL 서버가 시작될 때 딕셔너리의 히스토그램 정보를 information 데이터베이스의 column_statistics 테이블로 로드한다.

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

- Singleton: 칼럼값 개별로 레코드 건수를 관리하는 히스토그램으로, Value-Based 히스토그램 또는 도수 분포라고도 부른다.
- Equi-Height: 칼람값의 범위를 균등한 개수로 구분해서 관리하는 히스토그램으로, Height-Balanced 히스토그램이라고 불린다.

히스토그램은 버킷(Bucket) 단위로 구분되어 레코드 건수나 칼럼값의 범위가 관리되는데, 싱글톤 히스토그램은 칼럼이 가지는 값별로 버킷이 할당되며 높이 균형 히스토그램에서는 개수가 균등한 칼럼값의 범위별로 하나의 버킷이 할당된다.
싱글톤 히스토그램은 각 버킷이 칼럼의 값, 발생 빈도 비율 2개의 값을 가진다.
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
이때 조전절에 일치하는 레코드 건수를 예측하기 위해 옵티마이저는 실제 인덱스 B-Tree를 샘플링해서 살펴본다.
이 작업을 매뉴얼에서는 "인덱스 다이브(Index Dive)"라고 표현한다.

MySQL 8.0 서버에서는 인덱스된 칼럼을 검색 조건으로 사용하는 경우 그 칼럼의 히스토그램은 사용하지 않고 실제 인덱스 다이브를 통해 직접 수정한 정보를 활용한다.
실제 검색 조건의 대상 값에 대한 샘플링을 실행하기 때문에 히스토그램보다 정확한 결과를 기대할 수 있다.
하지만 인덱스 다이브 작업은 어느 정도의 비용이 필요하며, 때로는 (IN 절에 값이 많이 명시된 경우) 실행 계획 수립만으로도 상당한 인ㄷ게스 다이브를 실행하고 비용도 그만큼 커진다.

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
