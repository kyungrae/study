#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

int main(int argc, char *agrv[])
{
    int sock_type;
    socklen_t optlen = sizeof(sock_type);
    int tcp_sock = socket(PF_INET, SOCK_STREAM, 0);
    int udp_sock = socket(PF_INET, SOCK_DGRAM, 0);
    printf("SOCK_STREAM: %d \n", SOCK_STREAM);
    printf("SOCK_STREAM: %d \n", SOCK_DGRAM);

    int state = getsockopt(tcp_sock, SOL_SOCKET, SO_TYPE, (void *)&sock_type, &optlen);
    if (state)
        error_handling("getsockopt() error!");
    printf("Socket type one: %d \n", sock_type);

    state = getsockopt(udp_sock, SOL_SOCKET, SO_TYPE, (void *)&sock_type, &optlen);
    if (state)
        error_handling("getsockopt() error!");
    printf("Socket type two: %d \n", sock_type);

    return 0;
}
