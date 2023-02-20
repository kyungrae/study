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
        printf("Usage: %s <port>\n", argv[0]);
        exit(1);
    }

    FILE *fp = fopen("../file_server.c", "rb");
    int serv_sock = socket(PF_INET, SOCK_STREAM, 0);

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(argv[1]));

    if (bind(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handling("bind() error");

    if (listen(serv_sock, 5) == -1)
        error_handling("listen() errror");

    struct sockaddr_in clnt_addr;
    socklen_t clnt_addr_sz = sizeof(clnt_addr);
    int clnt_sock = accept(serv_sock, (struct sockaddr *)&clnt_addr, &clnt_addr_sz);

    char buf[BUF_SIZE];
    while (1)
    {
        int read_cnt = fread((void *)buf, 1, BUF_SIZE, fp);
        if (read_cnt < BUF_SIZE)
        {
            write(clnt_sock, buf, read_cnt);
            break;
        }
        write(clnt_sock, buf, BUF_SIZE);
    }

    shutdown(clnt_sock, SHUT_WR);
    read(clnt_sock, buf, BUF_SIZE);
    printf("Message from client: %s \n", buf);

    fclose(fp);
    close(clnt_sock);
    close(serv_sock);

    return 0;
}
