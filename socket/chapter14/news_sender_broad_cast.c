#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "../lib/common.h"

#define TTL 64
#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage: %s <Broadcast IP> <PORT> \n", argv[0]);
        exit(1);
    }

    int send_sock = socket(PF_INET, SOCK_DGRAM, 0);
    struct sockaddr_in mul_addr;
    memset(&mul_addr, 0, sizeof(mul_addr));
    mul_addr.sin_family = AF_INET;
    mul_addr.sin_addr.s_addr = inet_addr(argv[1]);
    mul_addr.sin_port = htons(atoi(argv[2]));

    int so_broadcast = 1;
    setsockopt(send_sock, SOL_SOCKET, SO_BROADCAST, (void *)&so_broadcast, sizeof(so_broadcast));

    FILE *fp;
    if ((fp = fopen("news.txt", "r")) == NULL)
        error_handle("open() error");

    char buf[BUF_SIZE];
    while (!feof(fp))
    {
        fgets(buf, BUF_SIZE, fp);
        sendto(send_sock, buf, strlen(buf), 0, (struct sockaddr *)&mul_addr, sizeof(mul_addr));
    }
    close(fp);
    close(send_sock);

    return 0;
}
