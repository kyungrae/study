#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

int main(int argc, char *argv[])
{
    pid_t pid = fork();

    if (pid == 0)
        return 3;
    else
    {
        printf("Child PID: %d \n", pid);
        pid = fork();
        if (pid == 0)
            exit(7);
        else
        {
            printf("Child PID: %d \n", pid);

            sleep(1);
            int status;
            wait(&status);
            if (WIFEXITED(status))
                printf("Child send one: %d \n", WEXITSTATUS(status));

            wait(&status);
            if (WIFEXITED(status))
                printf("Child send two: %d \n", WEXITSTATUS(status));
        }
    }
    return 0;
}
