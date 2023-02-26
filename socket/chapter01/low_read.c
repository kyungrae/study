#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

void error_handle(char *message);

int main(void)
{
    int fd = open("data.txt", O_RDONLY);
    if (fd == -1)
        error_handle("open() error!");
    printf("file descriptor: %d \n", fd);

    char buf[100];
    if (read(fd, buf, sizeof(buf)) == -1)
        error_handle("read() error!");

    printf("file data: %s", buf);
    close(fd);
    return 0;
}

void error_handle(char *message)
{
    fputs(message, stderr);
    fputc('\n', stderr);
    exit(1);
}
