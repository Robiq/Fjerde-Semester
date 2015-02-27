#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <inttypes.h>

//To get the struct-info
#include "Transfer.c"

int createEtherFrame(struct MIP_Frame *snd, size_t sndSize, uint8_t* iface_hwaddr, uint8_t* dst, struct ether_frame *frame)
{
	//Make sure the ethernet-frame exists
	assert(frame);

	//Ethernet broadcast addr.
	memcpy(frame->dst_addr, dst, 6);

	//Ethernet source addr.
	memcpy(frame->src_addr, iface_hwaddr, 6);

	//Ethernet protocol field
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	//Fill in the frame with the MIP-frame.
	memcpy(frame->contents, snd, (sizeof(struct MIP_Frame)+sndSize));

	return 1;
}

int setARPReturn(char *src, char *dst, struct MIP_Frame* mipFrame){
	//TRA 000, Length 0
	//000 1111 0000 0000 0 = 7680

	//Set TRA_TTL_Payload to bit-pattern above
	mipFrame->TRA_TTL_Payload[0] = (uint16_t) ARPret;
	
	//Set Destination-MIP and Source-MIP.
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	memcpy(mipFrame->srcMIP, src, sizeof(char));
	
	return 1;
}

int setARP(char *src, struct MIP_Frame* mipFrame){
	//TRA 001, Length 0, DST = 11111111
	//001 1111 0000 0000 0 = 15872

	//Set TRA_TTL_Payload to bit-pattern above
	mipFrame->TRA_TTL_Payload[0] = (uint16_t) ARP;

	//Set Destination-MIP and Source-MIP. Destination-MIP is unknown, so it is set like this: DST = 11111111 = 255
	uint8_t b = (uint8_t) 255;
	memset(mipFrame->dstMIP, (uint8_t)b, (sizeof(char)));
	memcpy(mipFrame->srcMIP, src, strlen(src));

	return 1;
}
//Returns 1 if success, 0 if failure.
int setTransport(char *src, char *dst, size_t payload, char msg[], struct MIP_Frame* mipFrame){
	//TRA 100
	//100 1111 0000 0000 0 = 40448

	//Set Destination-MIP and Source-MIP.
	memcpy(mipFrame->srcMIP, src, sizeof(char));
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	
	//Calculate the TRA_TTL_Payload value
	int calc = Transport + (payload/4);
	
	//If payload is not a multiple of 4, return error
	if(payload%4 != 0){
		#ifdef DEBUG
		printf("Payload not a multiple of 4!\n");
		#endif
		return 0;
	}
	//If payload + size of MIP-frame (4bytes) is bigger than maximum size, return error.
	if(payload+4 > maxSize){
		#ifdef DEBUG
		printf("Payload to big!\n");
		#endif
		return 0;
	}
	//Set payload.
	mipFrame->TRA_TTL_Payload[0] = calc;
	//Set the message.
	memcpy(mipFrame->message, msg, payload);

	return 1;
}

int setTempTransp(char *src, size_t payload, char msg[], struct MIP_Frame* mipFrame)
{
	//TRA 100 
	//100 1111 0000 0000 0 = 40448
	//Make sure the destination is = 0
	memset(mipFrame->dstMIP, 0, (sizeof(char)));
	//Set the MIP-source
	memcpy(mipFrame->srcMIP, src, (sizeof(char)));
	//Calculate the TRA_TTL_Payload value
	int calc = Transport + (payload/4);
	
	//If payload is not a multiple of 4, return error
	if(payload%4 != 0){
		#ifdef DEBUG
		printf("Payload not a multiple of 4!\n");
		#endif
		return 0;
	}
	//If payload + size of MIP-frame (4bytes) is bigger than maximum size, return error.
	if(payload+4 > maxSize){
		#ifdef DEBUG
		printf("Payload to big!\n");
		#endif
		return 0;
	}
	//Set payload
	mipFrame->TRA_TTL_Payload[0] = calc;
	//Set the message
	memcpy(mipFrame->message, msg, payload);

	return 1;
}

int finalTransp(char* dst, struct MIP_Frame* mipFrame)
{
	//Set the destination MIP
	memcpy(mipFrame->dstMIP, dst, sizeof(char));

	return 1;
}

int setRouting(char* src, char* dst, size_t payload, char msg[], struct MIP_Frame* mipFrame){
	//Routing 010 1111 0000 0000 0 = 24064
	//Set destination-MIP and source-MIP
	memcpy(mipFrame->srcMIP, src, sizeof(char));
	memcpy(mipFrame->dstMIP, dst, sizeof(char));
	//Calculate the TRA_TTL_Payload value
	int calc = Routing + (payload/4);
	
	//If payload is not a multiple of 4, return error
	if(payload%4 != 0){
		#ifdef DEBUG
		printf("Payload not a multiple of 4!\n");
		#endif
		return 0;
	}
	//If payload + size of MIP-frame (4bytes) is bigger than maximum size, return error.
	if(payload+4 > maxSize){
		#ifdef DEBUG
		printf("Payload to big!\n");
		#endif
		return 0;
	}
	//Set payload
	mipFrame->TRA_TTL_Payload[0] = calc;
	//Set the message
	memcpy(mipFrame->message, msg, strlen(msg));

	return 1;
}