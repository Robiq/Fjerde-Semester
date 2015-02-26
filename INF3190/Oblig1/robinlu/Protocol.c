#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <inttypes.h>

//To get the struct-info
#include "Transfer.c"

int createEtherFrame(struct MIP_Frame *snd, size_t sndSize, uint8_t* iface_hwaddr, uint8_t* dst, struct ether_frame *frame)
{

	assert(frame);

	//printf("Size 1:%d\n", (int) sizeof(frame));

	//Ethernet broadcast addr.
	memcpy(frame->dst_addr, dst, 6);

	//printf("Size 2:%d\n", (int) sizeof(frame));

	//Ethernet source addr.
	memcpy(frame->src_addr, iface_hwaddr, 6);

	//printf("Size 3:%d\n", (int) sizeof(frame));

	//Ethernet protocol field
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	//printf("Size 4:%d\n", (int) sizeof(frame));

	//Fill in the frame.
	memcpy(frame->contents, snd, (sizeof(struct MIP_Frame)+sndSize));

	//printf("Size 5:%d\n", (int) sizeof(frame));

	return 1;
}

int setARPReturn(char *src, char *dst, struct MIP_Frame* mipFrame){
	//TRA 000, Length 0
	//000 1111 0000 0000 0 = 7680
	mipFrame->TRA_TTL_Payload[0] = (uint16_t) ARPret;
	
	
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	memcpy(mipFrame->srcMIP, src, strlen(src));

	//TODO rm printf("Adr cont: %p\n",  mipFrame->message);

	
	return 1;
}

int setARP(char *src, struct MIP_Frame* mipFrame){
	//TRA 001, Length 0, DST = 11111111
	//001 1111 0000 0000 0 = 15872
	mipFrame->TRA_TTL_Payload[0] = (uint16_t) ARP;
	uint8_t b = (uint8_t) 255;
	memset(mipFrame->dstMIP, (uint8_t)b, (sizeof(char)));
	memcpy(mipFrame->srcMIP, src, strlen(src));
	return 1;
}

int setTransport(char *src, char *dst, size_t payload, char msg[], struct MIP_Frame* mipFrame){
	//TRA 100
	//100 1111 0000 0000 0 = 40448

	memcpy(mipFrame->srcMIP, src, sizeof(char));
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	
	int calc = Transport + (payload/4);
	
	if(payload%4 != 0)	return 0;

	if(payload+4 > maxSize)	return 0;

	mipFrame->TRA_TTL_Payload[0] = calc;

	memcpy(mipFrame->message, msg, payload);

	return 1;
}

int setTempTransp(char *src, size_t payload, char msg[], struct MIP_Frame* mipFrame)
{
	//TRA 100 
	//100 1111 0000 0000 0 = 40448
	memset(mipFrame->dstMIP, 0, (sizeof(char)));

	memcpy(mipFrame->srcMIP, src, (sizeof(char)));
	
	int calc = Transport + (payload/4);
	
	if(payload%4 != 0){
		printf("Payload not a multiple of 4!\n");
		return 0;
	}

	if(payload+4 > maxSize){
		printf("Payload to big!\n");
		return 0;
	}

	mipFrame->TRA_TTL_Payload[0] = calc;

	memcpy(mipFrame->message, msg, payload);

	return 1;
}

int finalTransp(char* dst, struct MIP_Frame* mipFrame)
{
	memcpy(mipFrame->dstMIP, dst, sizeof(char));

	return 1;
}

int setRouting(char* src, char* dst, size_t payload, char msg[], struct MIP_Frame* mipFrame){
	//Routing 010 1111 0000 0000 0 = 24064
	
	memcpy(mipFrame->srcMIP, src, sizeof(char));
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	
	int calc = Routing + (payload/4);
	
	if(payload%4 != 0)	return 0;

	if(payload+4 > maxSize)	return 0;

	mipFrame->TRA_TTL_Payload[0] = calc;

	memcpy(mipFrame->message, msg, strlen(msg));

	return 1;
}