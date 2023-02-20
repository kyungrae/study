#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Usage : %s <port> \n", argv[0]);
        exit(1);
    }

    int serv_sock = socket(PF_INET, SOCK_DGRAM, 0);
    if (serv_sock == -1)
        error_handling("UDP socket creation error");

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(argv[1]));

    if (bind(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handling("bind() error");

    for (int i = 0; i < 3; i++)
    {
        sleep(5);
        struct sockaddr_in clnt_addr;
        socklen_t clnt_addr_size = sizeof(clnt_addr);
        char message[BUF_SIZE];
        int str_len = recvfrom(serv_sock, message, BUF_SIZE, 0, (struct sockadrr *)&clnt_addr, &clnt_addr_size);
        printf("Message: %d: %s \n", i + 1, message);
    }

    close(serv_sock);
    return 0;
}
