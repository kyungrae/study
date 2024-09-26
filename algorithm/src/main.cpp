#include <iostream>
#include <vector>
#include <string>
#include <algorithm>
#include <cstring>
#include <cmath>

using namespace std;

int cache[101];
const int MOD = 1000000007;

int tiling(int n)
{
    if (n <= 1)
        return 1;

    int &ret = cache[n];
    if (ret != -1)
        return ret;

    return ret = (tiling(n - 1) + tiling(n - 2)) % MOD;
}

int asyncTiling(int n)
{
    int ret;
    if (n % 2)
        ret = tiling(n) - tiling(n / 2);
    else
        ret = tiling(n) - tiling(n / 2) - tiling((n - 2) / 2);

    while (ret < 0)
        ret = (ret + MOD) % MOD;
    return ret;
}

int main()
{
    int C;
    cin >> C;
    while (C--)
    {
        int n;
        cin >> n;
        memset(cache, -1, sizeof(cache));
        cout << asyncTiling(n) << endl;
    }

    return 0;
}
