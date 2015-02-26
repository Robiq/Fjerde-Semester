#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <inttypes.h>

//To get the struct-info
#include "Transfer.c"

int createSend(struct MIP_Frame* frame, char* msg, struct send *snd)
{	
	ssize_t tst;
	if(msg!=NULL)	tst = sizeof(frame) + strlen(msg);
	else	tst = sizeof(frame);

	printf("Tst: %d\n", (int) tst);

	if(tst > maxSize)	return 0;

	snd->frame = malloc(sizeof(struct MIP_Frame));

	/* Error handling!
	printf("Src: %x\n", frame->srcMIP[0]);
	printf("Dst: %x\n", frame->dstMIP[0]);
	printf("TTL etc: %d\n", frame->TRA_TTL_Payload[0]);
	printf("Sizeof frame: %d\n", (int)sizeof(frame));
	*/

	memcpy(snd->frame, frame, sizeof(struct MIP_Frame));
	if(msg != NULL){
		//MÅ FREE's! TODO
		snd->message=malloc(sizeof(msg));
		memcpy(snd->message, msg, (strlen(msg)+1));
		printf("Saved: %s\n", snd->message);
	}


	return 1;
}

int createEtherFrame(struct send *snd, uint8_t* iface_hwaddr, uint8_t* dst, struct ether_frame *frame)
{

	assert(frame);

	printf("Size 1:%d\n", (int) sizeof(frame));

	//Ethernet broadcast addr.
	memcpy(frame->dst_addr, dst, 6);

	printf("Size 2:%d\n", (int) sizeof(frame));

	//Ethernet source addr.
	memcpy(frame->src_addr, iface_hwaddr, 6);

	printf("Size 3:%d\n", (int) sizeof(frame));

	//Ethernet protocol field
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	printf("Size 4:%d\n", (int) sizeof(frame));

	//Fill in the message.
	memcpy(frame->contents, snd, sizeof(( sizeof(struct send) + sizeof(snd->message) + sizeof(snd->frame))));

	printf("Size 5:%d\n", (int) sizeof(frame));

	return 1;
}

int setARPReturn(char *src, char *dst, struct MIP_Frame* mipFrame){
	//TRA 000, Length 0
	//000 1111 0000 0000 0 = 7680
	mipFrame->TRA_TTL_Payload[0] = (uint16_t) ARPret;

	memcpy(mipFrame->dstMIP, dst, strlen(dst));
	memcpy(mipFrame->srcMIP, src, strlen(src));
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

int setTransport(char *src, char *dst, size_t payload, struct MIP_Frame* mipFrame){
	//TRA 100
	//100 1111 0000 0000 0 = 40448

	memcpy(mipFrame->srcMIP, src, strlen(src));
	memcpy(mipFrame->dstMIP, dst, strlen(dst));
	
	int calc = Transport + (payload/4);
	
	if(payload%4 != 0)	return 0;

	if(payload+4 > maxSize)	return 0;

	mipFrame->TRA_TTL_Payload[0] = calc;
	return 1;
}

int setTempTransp(char *src, size_t payload, struct MIP_Frame* mipFrame)
{
	//TRA 100 
	//100 1111 0000 0000 0 = 40448
	memset(mipFrame->dstMIP, 0, (sizeof(char)));

	memcpy(mipFrame->srcMIP, src, strlen(src));
	
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

	return 1;
}

int finalTransp(char* dst, struct MIP_Frame* mipFrame)
{
	memcpy(mipFrame->dstMIP, dst, strlen(dst));

	return 1;
}

int setRouting(char* src, char* dst, size_t payload, struct MIP_Frame* mipFrame){
	//Routing 010 1111 0000 0000 0 = 24064
	
	memcpy(mipFrame->srcMIP, src, strlen(src));
	memcpy(mipFrame->dstMIP, dst, strlen(dst));
	
	int calc = Routing + (payload/4);
	
	if(payload%4 != 0)	return 0;

	if(payload+4 > maxSize)	return 0;

	mipFrame->TRA_TTL_Payload[0] = calc;
	return 1;
}