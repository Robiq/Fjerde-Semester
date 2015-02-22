//Alle recieve & send instruksjoner + laging av protokoll.
/*
Protokoll:
TRA	|	TTL	|	Payload	|	Source MIP	|	Dest MIP	|
000	|  4bit |	9bit	|	8bit		|	8bit		|
TRA - Bestemmer Transport/Routing/ARP/ARP-return
TTL - Set to 15!
Source/Dest MIP - char!

000000000
     8421
    16
   32
  64
 128
256

511
*/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <inttypes.h>


#define TTL 15

struct MIP_Frame
{
	uint8_t TRA_TTL_Payload[2];
	uint8_t srcMIP[1];
	uint8_t dstMIP[1];
} __attribute__((packed));

struct MIP_Frame mipFrame = malloc(sizeof(MIP_Frame));

int setARPReturn(char src, char dst){
	//TRA 000, Length 0
	memset(mipframe->TRA_TTL_Payload, 0, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(mipframe->srcMIP, 0, (sizeof(mipframe->mipframe->srcMIP)));
	mipframe->dstMIP[0] = (int) dst;
	mipframe->srcMIP[0] = (int) src;

}

int setARP(char src){
	//TRA 001, Length 0, DST = 11111111
	//0011111000000000 = 15872
	memset(mipframe->TRA_TTL_Payload, 15872, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 255, (sizeof(mipframe->mipframe->mipframe->dstMIP)));
	memset(mipframe->srcMIP, 0, (sizeof(mipframe->mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
}

int setTransport(char src, char dst, int payload){
	//TRA 100 
	memset(mipframe->TRA_TTL_Payload, 0, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(&srcMIP, 0, (sizeof(mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
	mipframe->dstMIP[0] = (int) dst;
	//Legg til payload og sett TRA_TTL_Payload deretter
}

int setRouting(){
	//TRA 010memset(mipframe->TRA_TTL_Payload, 0, (sizeof(mipframe->TRA_TTL_Payload)));
	memset(mipframe->dstMIP, 0, (sizeof(mipframe->dstMIP)));
	memset(&srcMIP, 0, (sizeof(mipframe->srcMIP)));
	mipframe->srcMIP[0] = (int) src;
	mipframe->dstMIP[0] = (int) dst;
	//Legg til payload og sett TRA_TTL_Payload deretter
}