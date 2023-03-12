#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
    int fd1 = open("origin.txt", O_RDONLY);
    int fd2 = open("copy_cpy.txt", O_WRONLY | O_CREAT | O_TRUNC);

    int len;
    char buf[3];
    while ((len = read(fd1, buf, sizeof(buf))) > 0)
        write(fd2, buf, len);

    close(fd1);
    close(fd2);

    return 0;
}
