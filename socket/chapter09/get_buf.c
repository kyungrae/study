#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

int main(int argc, char *argv[])
{
    int send_buf;
    int sock = socket(PF_INET, SOCK_STREAM, 0);
    socklen_t len = sizeof(send_buf);
    int state = getsockopt(sock, SOL_SOCKET, SO_SNDBUF, (void *)&send_buf, &len);
    if (state)
        error_handling("getsockopt() error");

    int recv_buf;
    len = sizeof(recv_buf);
    state = getsockopt(sock, SOL_SOCKET, SO_RCVBUF, (void *)&recv_buf, &len);
    if (state)
        error_handling("getsockopt() error");

    printf("Input buffer size: %d \n", send_buf);
    printf("Output buffer size: %d \n", recv_buf);

    return 0;
}
