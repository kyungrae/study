#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "../lib/common.h"

#define BUF_SIZE 1024
#define OPSZ 4

int calculate(int opnum, int opnds[], char operator);

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Usage : %s <port>\n", argv[0]);
        exit(1);
    }

    int serv_sock = socket(PF_INET, SOCK_STREAM, 0);
    if (serv_sock == -1)
        error_handle("socket() error");

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = PF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(argv[1]));

    if (bind(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handle("bind() error");
    if (listen(serv_sock, 5) == -1)
        error_handle("listen() errror");

    for (int i = 0; i < 5; i++)
    {
        struct sockaddr_in clnt_addr;
        int clnt_addr_sz = sizeof(clnt_addr);
        int clnt_sock = accept(serv_sock, (struct sockaddr *)&clnt_addr, &clnt_addr_sz);

        int opnd_cnt = 0;
        read(clnt_sock, &opnd_cnt, 1);

        int recv_len = 0, recv_cnt = 0;
        char opinfo[BUF_SIZE];
        while ((opnd_cnt * OPSZ + 1) > recv_len)
        {
            recv_cnt = read(clnt_sock, &opinfo[recv_len], BUF_SIZE - 1);
            recv_len += recv_cnt;
        }
        int result = calculate(opnd_cnt, (int *)opinfo, opinfo[recv_len - 1]);
        write(clnt_sock, (char *)&result, sizeof(result));
        close(clnt_sock);
    }
    close(serv_sock);

    return 0;
}

int calculate(int opnum, int opnds[], char op)
{
    int result = opnds[0], i = 0;
    switch (op)
    {
    case '+':
        for (i = 0; i < opnum; i++)
            result += opnds[i];
        break;
    case '-':
        for (i = 0; i < opnum; i++)
            result -= opnds[i];
        break;
    case '*':
        for (i = 0; i < opnum; i++)
            result *= opnds[i];
        break;
    }
    return result;
}
