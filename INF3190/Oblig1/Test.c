#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

void decodeBuf(const char* buf, char* msg, char* dst)
{	
	int i;
	for(i=0;i<strlen(buf);i++){
		if(buf[i] == '_' && buf[i+1] == '_'){
			dst[0]=buf[i+2];
			dst[1]='\0';
			printf("True\n");
			break;
		}
		msg[i]=buf[i];
	}
	msg[i]='\0';
}

void main(){

	const char* buf = "Dette_er_beskjeden__a";
	char msg [50];
	char dst [2];

	decodeBuf(buf, msg, dst);

	printf("DST: %s\n", dst);
	printf("MSG: %s\n", msg);

	/*
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
	*/
}