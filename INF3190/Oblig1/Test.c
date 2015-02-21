#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

void main(){
	time_t start;
	start = time (NULL);
	printf(ctime(&start));
	printf("\n");
	time_t now;
	now = time(NULL);

	printf(ctime(&now));
	printf("\n");

	double diff = difftime((now), (start));

	printf("THe difference is: %f \n", diff);

	return 0;
}