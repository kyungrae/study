# 2. 설치와 설정

## 2.1 MySQL 서버 설치

/usr/local/mysql 디렉토리 구조

1. bin  
MySQL 서버와 클라이언트 프로그램, 유틸리티를 위한 디렉토리
2. data  
로그 파일과 데이터 파일들이 저장되는 디렉토리
3. include  
C++ 헤더 파일들이 저장되는 디렉토리
4. lib  
라이브러리 파일들이 저장된 디렉토리
5. share  
다양한 지원 파일들이 저장돼 있으며, 에러 메시지나 샘플 설정 파일(my.cnf)이 있는 디렉토리

## 2.2 MySQL 서버의 시작과 종료

### 2.2.3 서버 연결 테스트

1. `mysql -uroot -p --host=localhost --sock=/tmp/mysql.sock`  
unix 소켓 파일을 이용해 IPC 통신
2. `mysql -root -p`  
unix 소켓 파일을 이용해 IPC 통신을 이용하며 socket 파일은 서버 설정 파일에서 참조
3. `mysql -uroot -p --host=127.0.0.1 --port=3306`  
loopback TCP/IP 접속

```bash
# 명령어를 통해서 IPC와 TCP/IP 통신의 차이를 확인할 수 있다.
lsof -i:3306 
```

## 2.4 서버 설정

- 설정 파일
  - /etc/my.cnf
  - /etc/msyql/my.cnf
  - /user/etc/my.cnf
  - ~/.my.cnf
- 시스템 변수
  - 확인 `SHOW (GLOBAL) VARIABLES`
  - 수정 `SET (PERSIST) (GLOBAL) VARIABELS`
