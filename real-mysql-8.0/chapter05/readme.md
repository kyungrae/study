# 5. 트랜잭션과 잠금

- 트랜잭션  
작업의 완전성을 보장한다.
논리적인 작업 셋을 모두 완벽하게 처리하거나, 처리 못할 경우에는 기존 상태로 복구해서 작업의 일부(Partial update)만 적용되지 않게 만들어주는 기능이다.
- 잠금  
동시성을 제어하기 위한 기능이다.

## 5.2 MySQL 엔진의 잠금

- 글로벌 락
- 테이블 락
- 네임드 락
- 메타데이터 락

## 5.3 InnoDB 스토리지 엔진 잠금

- 레코드 락  
레코드 자체만을 잠근다.
프라머리 키 또는 유니크 인덱스 조건을 이용한 변경에는 레코드 락을 사용한다.
- 갭 락  
레코드와 인접한 레코드 사이의 간격만을 잠가 레코드와 레코드 사이의 간격을 새로운 레코드가 생성되는 것을 제어한다.
- 넥스트 키 락
- 자동 증가 락

### 5.3.2 인덱스와 잠금

InnoDB 잠금은 변경해야 할 레코드를 찾기 위해 검색한 인덱스의 레코드를 모두 락을 건다.

### 5.3.3 레코드 수준의 잠금 확인 및 해제

- 상황

| Connection1                                                | Connection2                                                | Connection3                                                |
|------------------------------------------------------------|------------------------------------------------------------|------------------------------------------------------------|
| BEGIN;                                                     |                                                            |                                                            |
| UPDATE employees SET birth_date=now() WHERE emp_no=100001; |                                                            |                                                            |
|                                                            | UPDATE employees SET birth_date=now() WHERE emp_no=100001; |                                                            |
|                                                            |                                                            | UPDATE employees SET birth_date=now() WHERE emp_no=100001; |

- 프로세스 목록

| Id | User | Host | db | Command | Time | State | Info |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 5 | event\_scheduler | localhost | null | Daemon | 7933 | Waiting on empty queue | null |
| 8 | root | localhost | employees | Sleep | 12 |  | null |
| 9 | root | localhost | employees | Query | 7 | updating | update employees set birth\_date=now\(\) where emp\_no=100001 |
| 10 | root | localhost | employees | Query | 6 | updating | update employees set birth\_date=now\(\) where emp\_no=100001 |
| 17 | root | localhost:55994 | hama | Query | 0 | init | /\* ApplicationName=DataGrip 2023.1.2 \*/ SHOW PROCESSLIST |

- 잠금 대기 순서

| wating\_trx\_id | wating\_thread | waiting | blocking\_trx\_id | blocking\_thread | blocking\_query |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 48862 | 10 | update employees set birth\_date=now\(\) where emp\_no=100001 | 48860 | 8 | null |
| 48862 | 10 | update employees set birth\_date=now\(\) where emp\_no=100001 | 48861 | 9 | update employees set birth\_date=now\(\) where emp\_no=100001 |
| 48861 | 9 | update employees set birth\_date=now\(\) where emp\_no=100001 | 48860 | 8 | null |

- 스레드의 구체적인 잠금

| ENGINE | ENGINE\_LOCK\_ID | ENGINE\_TRANSACTION\_ID | THREAD\_ID | EVENT\_ID | OBJECT\_SCHEMA | OBJECT\_NAME | PARTITION\_NAME | SUBPARTITION\_NAME | INDEX\_NAME | OBJECT\_INSTANCE\_BEGIN | LOCK\_TYPE | LOCK\_MODE | LOCK\_STATUS | LOCK\_DATA |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| INNODB | 5771399856:1081:4423965112 | 48862 | 51 | 29 | employees | employees | null | null | null | 4423965112 | TABLE | IX | GRANTED | null |
| INNODB | 5771399856:19:746:25:4848677912 | 48862 | 51 | 29 | employees | employees | null | null | PRIMARY | 4848677912 | RECORD | X,REC\_NOT\_GAP | WAITING | 100001 |
| INNODB | 5771399064:1081:4423964088 | 48861 | 50 | 32 | employees | employees | null | null | null | 4423964088 | TABLE | IX | GRANTED | null |
| INNODB | 5771399064:19:746:25:4848673304 | 48861 | 50 | 32 | employees | employees | null | null | PRIMARY | 4848673304 | RECORD | X,REC\_NOT\_GAP | WAITING | 100001 |
| INNODB | 5771398272:1081:4423963064 | 48860 | 49 | 775 | employees | employees | null | null | null | 4423963064 | TABLE | IX | GRANTED | null |
| INNODB | 5771398272:19:746:25:4848668696 | 48860 | 49 | 775 | employees | employees | null | null | PRIMARY | 4848668696 | RECORD | X,REC\_NOT\_GAP | GRANTED | 100001 |

## 5.4 MySQL의 격리 수준

|                 | DIRTY READ | NON REPEATABLE READ | PHANTOM READ     |
|-----------------|------------|---------------------|------------------|
| READ UNCOMMITED | 발생        | 발생                 | 발생              |
| READ COMMITED   | 없음        | 발생                 | 발생              |
| REPEATBLE READ  | 없음        | 없음                 | 발생(InnoDB는 없음) |
| SERIAIZABLE     | 없음        | 없음                 | 없음              |

`SELECT ... FOR UPDATE` 쿼리는 SELECT 하는 레코드에 쓰기 잠금을 걸어야 하는데, 언두 레코드에는 잠금을 걸 수 없다.
