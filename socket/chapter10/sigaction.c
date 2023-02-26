#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include "../lib/common.h"

int main(int argc, char *argv[])
{
    struct sigaction act;
    act.__sigaction_u.__sa_handler = timeout;
    sigemptyset(&act.sa_mask);
    act.sa_flags = 0;
    sigaction(SIGALRM, &act, 0);

    alarm(2);

    for (int i = 0; i < 3; i++)
    {
        puts("wait....");
        sleep(100);
    }

    return 0;
}
