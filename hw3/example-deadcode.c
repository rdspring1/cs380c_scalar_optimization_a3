#include <stdio.h>
#define WriteLine() printf("\n");
#define WriteLong(x) printf(" %lld", x);
#define ReadLong(a) if (fscanf(stdin, "%lld", &a) != 1) a = 0;
#define long long long

void main()
{
    long a, b, c, d, x, y, output;

    a = 3; 
    b = 5;
    d = 4;
    x = 100; 

    if (a > b)
    {
	c = a + b;
    	d = 2;
    }

    y = 10;
    y = 15;
    y = 20;
    c = y + 4;
    output = b * d + c;
    WriteLong(output);
    WriteLine();
}
