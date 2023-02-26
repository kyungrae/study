#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "../lib/common.h"

#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage : %s <IP> <port> \n", argv[0]);
        exit(1);
    }

    int sock = socket(PF_INET, SOCK_DGRAM, 0);
    if (sock == -1)
        error_handle("UDP socket creation error");

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    serv_addr.sin_port = htons(atoi(argv[2]));

    char msg1[] = "Hi!";
    char msg2[] = "I'm another UDP host!";
    char msg3[] = "Nice to meet you";
    sendto(sock, msg1, sizeof(msg1), 0, (struct sockaddr *)&serv_addr, sizeof(serv_addr));
    sendto(sock, msg2, sizeof(msg2), 0, (struct sockaddr *)&serv_addr, sizeof(serv_addr));
    sendto(sock, msg3, sizeof(msg3), 0, (struct sockaddr *)&serv_addr, sizeof(serv_addr));
    close(sock);
    return 0;
}
