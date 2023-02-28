#include <stdio.h>
#include <unistd.h>
#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    int fds1[2], fds2[2];
    pipe(fds1), pipe(fds2);

    pid_t pid = fork();
    char buf[BUF_SIZE];
    if (pid == 0)
    {
        char str1[] = "Who are you?";
        write(fds1[1], str1, sizeof(str1));

        read(fds2[0], buf, BUF_SIZE);
        printf("Child process output: %s \n", buf);
    }
    else
    {
        read(fds1[0], buf, BUF_SIZE);
        printf("Parent process output: %s \n", buf);

        char str2[] = "Thank you for your message";
        write(fds2[1], str2, sizeof(str2));
    }

    return 0;
}
