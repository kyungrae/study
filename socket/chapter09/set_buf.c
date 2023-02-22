#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

int main(int argc, char *argv[])
{
    int send_buf = 1024 * 3, recv_buf = 1024 * 3;
    int sock = socket(PF_INET, SOCK_STREAM, 0);
    int state = setsockopt(sock, SOL_SOCKET, SO_RCVBUF, (void *)&recv_buf, sizeof(recv_buf));
    if (state)
        error_handling("setsockopt() error");

    state = setsockopt(sock, SOL_SOCKET, SO_SNDBUF, (void *)&send_buf, sizeof(send_buf));
    if (state)
        error_handling("setsockopt() error");

    socklen_t len = sizeof(send_buf);
    state = getsockopt(sock, SOL_SOCKET, SO_SNDBUF, (void *)&send_buf, &len);
    if (state)
        error_handling("getsockopt() error");

    len = sizeof(recv_buf);
    state = getsockopt(sock, SOL_SOCKET, SO_RCVBUF, (void *)&recv_buf, &len);
    if (state)
        error_handling("getsockopt() error");

    printf("Input buffer size: %d \n", recv_buf);
    printf("Input buffer size: %d \n", send_buf);
    return 0;
}
