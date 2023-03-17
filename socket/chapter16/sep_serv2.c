#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "../lib/common.h"
#define BUF_SIZE 1024

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Usage: %s <port>\n", argv[0]);
        exit(1);
    }

    struct sockaddr_in serv_addr;
    int serv_sock = socket(PF_INET, SOCK_STREAM, 0);
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(argv[1]));

    if (bind(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handle("bind error()");
    if (listen(serv_sock, 5) == -1)
        error_handle("listen() error");

    struct sockaddr_in clnt_addr;
    socklen_t clnt_addr_size = sizeof(clnt_addr);
    int clnt_sock = accept(serv_sock, (struct sockaddr *)&clnt_addr, &clnt_addr_size);

    FILE *readfp = fdopen(clnt_sock, "r");
    FILE *writefp = fdopen(dup(clnt_sock), "w");

    fputs("FROM SERVER: Hi~ client? \n", writefp);
    fputs("I love all of the world \n", writefp);
    fputs("You are awesome! \n", writefp);
    fflush(writefp);

    shutdown(fileno(writefp), SHUT_WR);
    fclose(writefp);

    char buf[BUF_SIZE];
    fgets(buf, BUF_SIZE, readfp);
    fputs(buf, stdout);
    fclose(readfp);

    return 0;
}
