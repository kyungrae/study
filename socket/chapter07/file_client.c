#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>

#include "../lib/error_handle.h"

#define BUF_SIZE 30

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage: %s <IP> <port>\n", argv[0]);
        exit(1);
    }

    FILE *fp = fopen("receive.dat", "wb");
    int sock = socket(PF_INET, SOCK_STREAM, 0);

    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    serv_addr.sin_port = htons(atoi(argv[2]));

    if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
        error_handling("connect error()");

    int read_cnt;
    char buf[BUF_SIZE];
    while ((read_cnt = read(sock, buf, BUF_SIZE)) != 0)
        fwrite((void *)buf, 1, read_cnt, fp);

    puts("Received file data");
    write(sock, "Thank you", 10);
    fclose(fp);
    close(sock);

    return 0;
}
