## TCP

- TCP 데이터는 경계가 없어서 stream이라고 표현
- unreliable 채널에서 reliable 하게 데이터를 전송
- congestion control(sliding window)
- 3 way handshake, 4 way handshake
- 우아한 socket close를 위한 half close(shutdown)
- FIN ack 유실로 상대방 호스트가 connection 정상 종료 못하는 문제를 예방하기 위한 time wait 상태

## UDP

- unreliable 데이터 전송
- IPv4 multicast address range multicast
  - multicast를 지원하지 않는 라우터에서는 tunneling 기술 필요
  - multicast 그룹 가입
- Directed broadcast vs Local broadcast
  - x.x.x.255 vs 255.255.255.255

## Linux Kernel API

- 자식 프로세스 생성 (fork)
- 자식 프로세스 종료 확인 (wait, waitpid)
- kernel signal에 대한 handler 등록 (signal, sigaction)
- IPC (pipe)
- IO multiplexing (select)
