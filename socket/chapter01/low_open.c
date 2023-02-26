#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

void error_handle(char *message);

int main(void)
{
    int fd = open("data.txt", O_CREAT | O_WRONLY | O_TRUNC);
    if (fd == -1)
        error_handle("open() error!");
    printf("file descriptor: %d \n", fd);

    char buf[] = "Let's go!\n";
    if (write(fd, buf, sizeof(buf)) == -1)
        error_handle("write() error!");

    close(fd);
    return 0;
}

void error_handle(char *message)
{
    fputs(message, stderr);
    fputc('\n', stderr);
    exit(1);
}
