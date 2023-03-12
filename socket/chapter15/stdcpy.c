#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
    FILE *fp1 = fopen("origin.txt", "rb");
    FILE *fp2 = fopen("copy_std.txt", "wb");

    int len;
    char buf[3];
    while (fgets(buf, sizeof(buf), fp1) != NULL)
        fputs(buf, fp2);

    fclose(fp1);
    fclose(fp2);

    return 0;
}
