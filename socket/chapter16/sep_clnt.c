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
    if (argc != 3)
    {
        printf("Usage : %s <IP> <port>\n", argv[0]);
        exit(1);
    }

    int serv_sock = socket(PF_INET, SOCK_STREAM, 0);
    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    serv_addr.sin_port = htons(atoi(argv[2]));

    if (connect(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handle("connect() error");

    FILE *readfp = fdopen(serv_sock, "r");
    FILE *writefp = fdopen(serv_sock, "w");

    while (1)
    {
        char buf[BUF_SIZE];
        if (fgets(buf, sizeof(buf), readfp) == NULL)
            break;
        fputs(buf, stdout);
        fflush(stdout);
    }

    fputs("FROM CLIENT: Thank youn", writefp);
    fflush(writefp);
    fclose(writefp);
    fclose(readfp);

    return 0;
}
