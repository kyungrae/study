# TCP/IP 소켓프로그래밍

## TCP

- TCP 데이터는 경계가 없어서 stream이라고 표현
- unreliable 채널(IP)에서 reliable 하게 데이터를 전송
  - ack 수신 못하면(packet/ack 유실 or timeout) packet 재전송
  - sliding window(윈도우 사이즈 만큼 packet을 동시에 보낼 수 있음)
- congestion control
  - congestion window(cwnd)
  - additive increase & multiplicative decrease(AIMD)
  - slow start & ssthresh
- 3 way handshake
  - A --- (SYN) --> B
  - A <-- (SYN/ACK) --- B
  - A --- (ACK) --> B
- 4 way handshake
  - A --- (FIN) --> B
  - A <-- (ACK) --- B
  - A <-- (FIN) --- B
  - A --- (ACK) --> B
- half close(shutdown)
  - 송수신 모두 가능한 상태에서 송신 또는 수신만 가능한 상태
- time wait 상태
  - FIN ack 유실로 상대방 호스트가 connection 종료 못하는 문제 예방

## UDP

- unreliable 데이터 전송
- multicast
  - multicast address range 가입되어 있는 호스트에게 데이터 전송
  - multicast를 지원하지 않는 라우터에서는 tunneling 기술 필요
- broadcast
  - 같은 네트워크 환경에 있는 호스트에게 데이터 전송
  - Directed broadcast(x.x.x.255) vs Local broadcast(255.255.255.255)

## Linux Kernel API

- 자식 프로세스 생성 (fork)
  - multi-process 환경에서 socket release를 위해 parent & chlid process 두 곳 모두에서 socket file descriptor close 필요
- 자식 프로세스 종료 확인 (wait, waitpid)
- kernel signal에 대한 handler 등록 (signal, sigaction)
- IPC (pipe)
- IO multiplexing (select)
- standard streams & socket
  - 입출력 버퍼를 통해 system call 감소로 성능 향상
  - 파일 입출력(FILE)으로 입출력 스트림 분리
  - File descriptor 복사(dup)을 이용해 half close 구현
- 고성능 IO multiplexing
  - epoll, kqueue, /dev/poll, IOCP
  - 관심있는 file descriptor를 운영체제에게 전달하고 변경 사항만 전달 받음
    - 불필요한 select call(system call) 감소
