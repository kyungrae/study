# 3. 사용자 및 권한

## 3.1 사용자 식별

`'아이디'@'host'`

## 3.2 사용자 계정 관리

```sql
CREATE USER 'user'@'%' IDENTIFIED WITH 'mysql_native_password' BY 'password';
```

### 3.2.2.1 identified with

- native pluggable authentication  
비밀번호에 대한 hash(SHA-1) 저장하고 클라이언트가 보낸 비밀번호 hash 값이 일치하는지 단순 비교
- caching sha-2 pluggable authentication  
SHA-2 알고리즘을 이용해 비밀번호 hash를 생성한다. rainbow table & brute force 취약점을 해결하기 위해 salt & key streching 적용했다.
많은 컨넥션을 연결하는 경우 hash 계산하기 위해 CPU 부하가 발생할 수 있다.
해당 취약점을 보안하기 위해 hash 결과값을 메모리에 caching 해 CPU 리소스 소모를 줄일 수 있다. SSL/TLS 또는 RSA 키페어 방식 필요하다.

## 3.3 권한

```sql
GRANT privilege_list ON db.table TO 'user'@'host';
```

## 3.4 역할

```sql
## 역할 생성
CREATE ROLE role_emp_read, role_emp_write;

## 역할에 권한 생성
GRANT SELECT ON employees.* TO role_emp_read;
GRANT INSERT,UPDATE,DELETE ON employees.* TO role_emp_write;

## 계정 생성
CREATE USER reader@'127.0.0.1' IDENTIFIED BY 'qwerty';
CREATE USER writer@'127.0.0.1' IDENTIFIED BY 'qwerty';

## 권한 부여
GRANT role_emp_read TO reader@'127.0.0.1';
GRANT role_emp_write TO writer@'127.0.0.1';

## 명령을 통해 MySQL 로그인 했을 때, 할당된 역할을 자동으로 활성화할 수 있다.
SET GLOBAL activate_all_roles_on_loin=ON;
```
