#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "../lib/common.h"

#define BUF_SIZE 100

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        printf("Usage: %s <port>\n", argv[0]);
        exit(1);
    }

    struct sigaction act;
    act.__sigaction_u.__sa_handler = read_childproc;
    sigemptyset(&act.sa_mask);
    act.sa_flags = 0;
    int state = sigaction(SIGCHLD, &act, 0);

    int fds[2];
    pipe(fds);
    pid_t pid = fork();
    if (pid == 0)
    {
        FILE *fp = fopen("echomsg.txt", "w+");

        char msgbuf[BUF_SIZE];
        for (int i = 0; i < 10; i++)
        {
            int len = read(fds[0], msgbuf, BUF_SIZE);
            fwrite((void *)msgbuf, 1, len, fp);
        }
        fclose(fp);
        return 0;
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

    while (1)
    {
        struct sockaddr_in clnt_addr;
        socklen_t addr_size = sizeof(clnt_addr);
        int clnt_sock = accept(serv_sock, (struct sockaddr *)&clnt_addr, &addr_size);
        if (clnt_sock == -1)
            continue;
        else
            puts("new client connected...");

        pid = fork();
        if (pid == 0)
        {
            close(serv_sock);
            int str_len;
            char buf[BUF_SIZE];
            while ((str_len = read(clnt_sock, buf, BUF_SIZE)) != 0)
            {
                write(clnt_sock, buf, str_len);
                write(fds[1], buf, str_len);
            }

            close(clnt_sock);
            puts("client disconnected...");
            return 0;
        }
        else
            close(clnt_sock);
    }

    close(serv_sock);
    return 0;
}
