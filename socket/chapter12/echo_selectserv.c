#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/select.h>
#include "../lib/common.h"

#define BUF_SIZE 100

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Usage: %s <port>\r", argv[0]);
        exit(1);
    }

    int serv_sock = socket(PF_INET, SOCK_STREAM, 0);
    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(atoi(argv[1]));

    if (bind(serv_sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handle("bind() error");
    if (listen(serv_sock, 5) == -1)
        error_handle("listen() error");

    fd_set reads;
    FD_ZERO(&reads);
    FD_SET(serv_sock, &reads);
    int fd_max = serv_sock;

    while (1)
    {
        fd_set cpy_reads = reads;
        struct timeval timeout;
        timeout.tv_sec = 5;
        timeout.tv_usec = 5000;

        int fd_num;
        if ((fd_num = select(fd_max + 1, &cpy_reads, 0, 0, &timeout)) == -1)
            break;
        if (fd_num == 0)
        {
            printf("Time out\n");
            continue;
        }
        else
            printf("select() return %d \n", fd_num);

        for (int i = 0; i < fd_max + 1; i++)
        {
            if (FD_ISSET(i, &cpy_reads))
            {
                if (i == serv_sock)
                {
                    struct sockaddr_in clnt_addr;
                    socklen_t addr_size = sizeof(clnt_addr);
                    int clnt_sock = accept(serv_sock, (struct sockaddr *)&clnt_addr, &addr_size);
                    FD_SET(clnt_sock, &reads);
                    if (fd_max < clnt_sock)
                        fd_max = clnt_sock;
                    printf("Connected client: %d\n", clnt_sock);
                }
                else
                {
                    char buf[BUF_SIZE];
                    int str_len = read(i, buf, BUF_SIZE);
                    if (str_len == 0)
                    {
                        FD_CLR(i, &reads);
                        close(i);
                        printf("Closed client: %d\n", i);
                    }
                    else
                    {
                        write(i, buf, str_len);
                    }
                }
            }
        }
    }
    close(serv_sock);
    return 0;
}
