//Alle recieve & send instruksjoner + laging av protokoll.
/*
Protokoll:
TRA	|	TTL	|	Payload	|	Source MIP	|	Dest MIP	|
000	|  4bit |	9bit	|	8bit		|	8bit		|
TRA - Bestemmer Transport/Routing/ARP/ARP-return
TTL - Set to 15!
Source/Dest MIP - char!

*/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <inttypes.h>

#define ARP 15872
#define Routing 24064
#define Transport 40448
#define maxSize 1500

struct ether_frame
{
	uint8_t dst_addr[6];
	uint8_t src_addr[6];
	uint8_t eth_proto[2];
	char    contents[0];
} __attribute__((packed));

struct MIP_Frame
{
	uint8_t TRA_TTL_Payload[2];
	uint8_t srcMIP[1];
	uint8_t dstMIP[1];
} __attribute__((packed));

struct send
{
	struct MIP_Frame* frame;
	char message [maxSize-4];
}

int createSend(struct MIP_Frame* frame, char* msg, struct send *snd)
{
	ssize_t tst = sizeof(frame) + strlen(msg);
	if(tst > maxSize)	return 0;

	memcpy(snd->frame, frame, sizeof(frame));
	if(msg != NULL)	memcpy(snd->message, msg, strlen(msg));
	else	snd->message=NULL;

	return 1;
}

int recieveRaw(char* buf)
{
	struct ether_frame *frame = (struct ether_frame*)buf;
	ssize_t retv = recv(raw, buf, sizeof(buf), 0);

	#ifdef DEBUG
		printf("Received message with len=%zd\n", retv);
		
		printf("Destination address: ");
		uint8_t mac_dst[6] = frame->dst_addr
		int i;
		
		for(i = 0; i < 5; ++i)	printf("%02x:", mac_dst[i]);
		
		printf("%02x\n", mac_dst[5]);

		printf("Source address:      ");
		uint8_t mac_src[6] = frame->src_addr;
		
		for(i = 0; i < 5; ++i)	printf("%02x:", mac_src[i]);
		
		printf("%02x\n", mac_src[5]);

		printf("Protocol type:       %04x\n", ntohs(*((uint16_t*)frame->eth_proto)));
		printf("Contents:            %.*s\n", (int)retv-14, frame->contents);
	#endif
}

int createEtherFrame(struct send *snd, uint8_t* iface_hwaddr, uint8_t* dst, struct ether_frame *frame)
{

	assert(frame);

	//Ethernet broadcast addr.
	memcpy(frame->dst_addr, dst, 6);

	//Ethernet source addr.
	memcpy(frame->src_addr, iface_hwaddr, 6);

	//Ethernet protocol field
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	//Fill in the message.
	memcpy(frame->contents, snd, sizeof(snd));
}

int setARPReturn(char src, char dst, struct MIP_Frame* mipFrame){
	//TRA 000, Length 0
	memset(mipframe->TRA_TTL_Payload, 0, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(mipframe->srcMIP, 0, (sizeof(mipframe->mipframe->srcMIP)));
	mipframe->dstMIP[0] = (int) dst;
	mipframe->srcMIP[0] = (int) src;
	return 1;
}

int setARP(char src, struct MIP_Frame* mipFrame){
	//TRA 001, Length 0, DST = 11111111
	//0011111000000000 = 15872
	memset(mipframe->TRA_TTL_Payload, ARP, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 255, (sizeof(mipframe->mipframe->mipframe->dstMIP)));
	memset(mipframe->srcMIP, 0, (sizeof(mipframe->mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
	return 1;
}

int setTransport(char src, char dst, int payload, struct MIP_Frame* mipFrame){
	//TRA 100 1111 0000 0000 0 = 40448
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(&srcMIP, 0, (sizeof(mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
	mipframe->dstMIP[0] = (int) dst;
	int calc = Transport + (payload + 4);//?? Rett?

	if(payload+4 > maxSize)	return 0;

	memset(mipframe->TRA_TTL_Payload, calc, (sizeof(mipframe->TRA_TTL_Payload)));
	return 1;
}

int setRouting(char src, char dst, int payload, struct MIP_Frame* mipFrame){
	//Routing 010 1111 0000 0000 0 = 24064
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(&srcMIP, 0, (sizeof(mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
	mipframe->dstMIP[0] = (int) dst;
	int calc = Routing + (payload + 4); //??
	
	if(payload+4 > maxSize)	return 0;

	memset(mipframe->TRA_TTL_Payload, calc, (sizeof(mipframe->TRA_TTL_Payload)));
	return 1;
}

//TODO
/* ??????
Length:	
  the	
  length	
  of	
  the	
  payload	
  in	
  32-­‐bit	
  words	
  (i.e.	
  the	
  total	
  length	
  of	
  the	
  MIP	
  
packet	
  including	
  the	
  MIP	
  header	
  in	
  bytes	
  is	
  Length*4+4).	
  
	
  
Important	
  general	
  MIP	
  protocol	
  rule:	
  
The	
  payload	
  of	
  a	
  MIP	
  packet	
  is	
  always	
  a	
  multiple	
  of	
  4,	
  and	
  the	
  total	
  packet	
  size	
  
including	
  the	
  4-­‐byte	
  MIP	
  header	
  must	
  not	
  exceed	
  1500. 1 	
  MIP	
  does	
  not	
  fragment	
  
packets,	
  i.e.	
  it	
  cannot	
  accept	
  messages	
  that	
  do	
  not	
  fulfill	
  these	
  constraints.	
 */