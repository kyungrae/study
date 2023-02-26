#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>

int main(int argc, char *argv[])
{
    pid_t pid = fork();

    if (pid == 0)
    {
        sleep(3);
        return 24;
    }
    else
    {
        int status;
        while (!waitpid(-1, &status, WNOHANG))
        {
            sleep(5);
            puts("sleep 5 sec.");
        }

        if (WIFEXITED(status))
            printf("Child send %d \n", WEXITSTATUS(status));
    }

    return 0;
}
