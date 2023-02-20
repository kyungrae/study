#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

#define BUF_SIZE 1024
#define RLT_SIZE 4
#define OPSZ 4

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage : %s <IP> <port>\n", argv[0]);
        exit(1);
    }

    int sock = socket(PF_INET, SOCK_STREAM, 0);
    if (sock == -1)
        error_handling("socket() error");

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    serv_addr.sin_port = htons(argv[2]);

    if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handling("connect() error!");
    else
        puts("Connected........");

    fputs("Operand count: ", stdout);
    int opnd_cnt;
    scanf("%d", &opnd_cnt);

    char opmsg[BUF_SIZE];
    opmsg[0] = (char)opnd_cnt;

    for (int i = 0; i < opnd_cnt; i++)
    {
        printf("Operand %d: ", i + 1);
        scanf("%d", (int *)&opmsg[i * OPSZ + 1]);
    }

    fgetc(stdin);
    fputs("Operator: ", stdout);
    scanf("%c", &opmsg[opnd_cnt * OPSZ + 1]);
    write(sock, opmsg, opnd_cnt * OPSZ + 2);

    int result;
    read(sock, &result, RLT_SIZE);
    printf("Operation result: %d \n", result);
    close(sock);

    return 0;
}
