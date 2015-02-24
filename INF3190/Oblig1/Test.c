#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include <assert.h>
#include <stdlib.h>
#include <unistd.h>

#include <arpa/inet.h>
#include <sys/socket.h>
#include <linux/if_packet.h>
#include <net/ethernet.h>

#include <net/if.h>

#include <sys/ioctl.h>
#include <bits/ioctls.h>
/*
void decodeBuf(const char* buf, char* msg, char* dst)
{	
	int i;
	for(i=0;i<strlen(buf);i++){
		if(buf[i] == '_' && buf[i+1] == '_'){
			dst[0]=buf[i+2];
			printf("True\n");
			break;
		}
		msg[i]=buf[i];
	}
	msg[i]='\0';
}
*/

#define ARPret 7680
#define ARPretByte 30
#define ARPretByte2 0

struct MIP_Frame
{
	uint16_t TRA_TTL_Payload[1];
	uint8_t srcMIP[1];
	uint8_t dstMIP[1];
} __attribute__((packed));

int test(struct MIP_Frame* frm)
{
	int x= (int) frm->TRA_TTL_Payload[0];
	printf("Nr: %d\n", x);
	if(x == ARPret)	return 1;
	return 0;
}

void main(){
	
	struct MIP_Frame* frm=malloc(sizeof(struct MIP_Frame));
	frm->TRA_TTL_Payload[0]=ARPret;
	//frm->TRA_TTL_Payload[0]=ARPretByte;
	//frm->TRA_TTL_Payload[1]=ARPretByte2
	


	int a = test(frm);

	printf("Result: %d\n", a);

	free(frm);
	/*
	const char* buf = "Dette_er_beskjeden__a";
	char msg [50];
	char dst [1];

	decodeBuf(buf, msg, dst);

	printf("DST: %c\n", dst[0]);
	printf("MSG: %s\n", msg);
	*/
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