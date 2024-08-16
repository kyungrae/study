# 5. 트랜잭션과 잠금

## 5.1 트랜잭션

트랜잭션은 하나의 논리적인 작업 셋에 하나의 쿼리가 있든 두 개 이상의 쿼리가 있든 관계없이 논리적인 작업 셋 자체가 100% 적용되거나(COMMIT) 아무것도 적용되지 않아야 함을 보장해 주는 것이다.
트랜잭션 기능 덕분에 개발자는 Partial update에 대한 예외 처리를 고민하지 않아도 된다.

### 주의 사항

데이터베스의 커넥션을 가지고 있는 범위와 트랜잭션이 활성화돼 있는 프로그램의 범위를 최소화해야 한다.
커넥션 리소스는 유한한 리소스이기 때문에 반환을 빨리하는 것이 좋다.
MVCC 모델 특성상 트랜잭션을 긴 시간 동안 활성화하면 오버헤드가 증가한다.

1. validation과 같은 불필요한 연산은 트랜잭션 외부에서 실행한다.
2. 외부 IO 연산은 latency가 길고 이기종 트랜잭션 문제에 대해 고민해야기 때문에 트랜잭션에서 제거한다.
3. 필요한 최소 연산으로 트랜잭션을 가볍게 유지한다.

## 5.2 MySQL 엔진의 잠금

### 글로벌 락

MySQL에서 제공하는 잠금 가운데 가장 범위가 크다.
글로벌 락을 획득하면 다른 세션에서 SELECT를 제외한 DDL 또는 DML 요청은 글로벌 락이 해제될 때까지 대기 상태로 남는다.

### 테이블 락

테이블락은 개별 테이블 단위로 설정되는 잠금이며, 명시적 또는 묵시적으로 특정 테이블의 락을 획득할 수 있다.

### 네임드 락

`GET_LOCK()` 함수를 이용해 임의의 문자열에 대해 잠금을 설정할 수 있다.
한번에 많은 레코드를 변경한는 쿼리는 자주 데드락의 원인이 된다.
동일 데이터를 변경하는 프로그램끼리 분류해서 네임드 락을 걸고 쿼리를 실행하면 문제를 해결할 수 있다.

### 메타데이터 락

데이터베이스 객체(테이블, 뷰)의 이름이나 구조를 변경하는 겨우에 획득하는 잠금이다.

## 5.3 InnoDB 스토리지 엔진 잠금

### 레코드 락  

레코드 자체만을 잠근다.
InnoDB 스토리지 엔진은 레코드 자체가 아니라 인덱스의 레코드를 잠근다.
프라머리 키 또는 유니크 인덱스 조건을 이용한 변경에는 레코드 락을 사용한다.

### 갭 락

레코드와 인접한 레코드 사이의 간격만을 잠가 레코드와 레코드 사이의 간격에 새로운 레코드가 생성되는 것을 제어한다.

### 자동 증가 락

자동 증가하는 숫자 값을 채번하기 위해 AUTO_INCREMENT 락이라 하는 테이블 수준의 잠금을 사용한다.
트랜잭션과 관계 없이 INSERT나 REPLACE 요청에서 AUTO_INCREMENT 값을 가져오는 순간만 락을 걸었다가 즉시 해제된다.
AUTO_INCREMENT 락은 테이블에 하나만 존재하기 때문에 두 개의 INSERT 쿼리가 동시에 실행되는 경우 하나의 쿼리가 락을 얻으면 나머지 쿼리는 락을 기다려야 한다.

### 인덱스와 잠금

InnoDB 잠금은 변경해야 할 레코드를 찾기 위해 검색한 인덱스의 레코드를 모두 락을 건다.

### 레코드 수준의 잠금 확인 및 해제

- 상황

| Connection1                                     | Connection2                                     | Connection3                                     |
|-------------------------------------------------|-------------------------------------------------|-------------------------------------------------|
| BEGIN;                                          |                                                 |                                                 |
| UPDATE employees SET date=now() WHERE emp_no=1; |                                                 |                                                 |
|                                                 | UPDATE employees SET date=now() WHERE emp_no=1; |                                                 |
|                                                 |                                                 | UPDATE employees SET date=now() WHERE emp_no=1; |

- 프로세스 목록

| Id | User | Host | db | Command | Time | State | Info |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 5 | event\_scheduler | localhost | null | Daemon | 7933 | Waiting on empty queue | null |
| 8 | root | localhost | employees | Sleep | 12 |  | null |
| 9 | root | localhost | employees | Query | 7 | updating | update employees set date=now\(\) where emp\_no=1 |
| 10 | root | localhost | employees | Query | 6 | updating | update employees set date=now\(\) where emp\_no=1 |
| 17 | root | localhost:55994 | hama | Query | 0 | init | /\* ApplicationName=DataGrip 2023.1.2 \*/ SHOW PROCESSLIST |

- 잠금 대기 순서

| wating\_trx\_id | wating\_thread | waiting | blocking\_trx\_id | blocking\_thread | blocking\_query |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 48862 | 10 | update employees set date=now\(\) where emp\_no=1 | 48860 | 8 | null |
| 48862 | 10 | update employees set date=now\(\) where emp\_no=1 | 48861 | 9 | update employees set date=now\(\) where emp\_no=1 |
| 48861 | 9 | update employees set date=now\(\) where emp\_no=1 | 48860 | 8 | null |

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

트랜잭션의 격리 수준이란 여러 트랜잭션이 동시에 처리될 때 특정 트랜잭션이 다른 트랜잭션에서 변경하거나 조회한 데이터를 볼 수 있게 허용할지 말지를 결정하는 것이다.

|                 | DIRTY READ | NON REPEATABLE READ | PHANTOM READ     |
|-----------------|------------|---------------------|------------------|
| READ UNCOMMITED | 발생        | 발생                 | 발생              |
| READ COMMITED   | 없음        | 발생                 | 발생              |
| REPEATBLE READ  | 없음        | 없음                 | 발생(InnoDB는 없음) |
| SERIAIZABLE     | 없음        | 없음                 | 없음              |

`SELECT ... FOR UPDATE` 쿼리는 SELECT 하는 레코드에 쓰기 잠금을 걸어야 하는데, 언두 레코드에는 잠금을 걸 수 없다.
