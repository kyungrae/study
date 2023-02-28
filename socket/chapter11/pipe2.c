#include <stdio.h>
#include <unistd.h>
#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    int fds[2];
    pipe(fds);

    pid_t pid = fork();
    char buf[BUF_SIZE];
    if (pid == 0)
    {
        char str1[] = "Who are you?";
        write(fds[1], str1, sizeof(str1));

        sleep(1);

        read(fds[0], buf, sizeof(buf));
        printf("Child process output: %s \n", buf);
    }
    else
    {
        read(fds[0], buf, BUF_SIZE);
        printf("Parent process output: %s \n", buf);

        char str[] = "Thank you for your message";
        write(fds[1], str, sizeof(str));
        sleep(2);
    }

    return 0;
}
