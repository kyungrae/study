#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

void error_handle(char *message)
{
    fputs(message, stderr);
    fputc('\n', stderr);
    exit(1);
}

void timeout(int sig)
{
    if (sig == SIGALRM)
        puts("Time out!");
    alarm(2);
}

void read_childproc(int sig)
{
    pid_t pid;
    int status;
    pid = waitpid(-1, &status, WNOHANG);
    printf("removed process id: %d \n", pid);
}
