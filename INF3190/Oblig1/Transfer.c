#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include <assert.h>
#include <stdlib.h>
#include <unistd.h>

#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>

#include <linux/if_packet.h>
#include <net/ethernet.h>

#include <net/if.h>

#include <sys/ioctl.h>
#include <bits/ioctls.h>

#include "Structs.h"

void printMAC(uint8_t* mac)
{
	int i;
	for(i = 0; i < 5; ++i)
		printf("%02x:", mac[i]);
	printf("%02x\n", mac[5]);
}

int arpRet(struct send* recv)
{
	struct MIP_Frame* frm = recv->frame;
	int x;
	x = (int) frm->TRA_TTL_Payload[0];
	if(x == ARPret)	return 1;
	
	return 0;
}
//Check if frame is
int arp(struct send* recv)
{
	struct MIP_Frame* frm = recv->frame;
	int x;
	x = (int) frm->TRA_TTL_Payload[0];
	if(x == ARP)	return 1;
	return 0;
}

int caseFind(struct ether_frame* frame)
{	
	//Arp-answer
	if(arpRet((struct send*)frame->contents)){
		//Save in ARP-list, send saved packet, in daemon
		return 2;
	//Arp-package
	} else if(arp((struct send*)frame->contents)){
		//Return ARP-response-packet & save sender in ARP-cache, in daemon
		return 3;
	//Transport
	}else{
		struct send *recvd= (struct send*) frame->contents;
		//Send IPC-packet, in daemon
		if(recvd->message != NULL)	return 1;
		//Error! Something wrong with message
		return -1;
	}
}

//Tolk MIP-header og velg rett handlingsmønster!
int findCase(struct ether_frame* frame, int debug)
{
	int a = caseFind(frame);

	if(debug && a!=-1){
		//Print stuff for DEBUG mode!
		printf("Destination address: ");
		printMAC(frame->dst_addr);
		printf("Source address:      ");
		printMAC(frame->src_addr);
		printf("Protocol type:       %04x\n", ntohs(*((uint16_t*)frame->eth_proto)));
		struct send *recvd= (struct send*)frame->contents;
		if(recvd->message != NULL)	printf("Contents: %s\n", recvd->message);
		else	printf("No content: ARP\n");
	}

	return a;
}

//Send raw
int sendRaw(int fd, struct ether_frame *snd)
{	
	printf("Src: ");
	printMAC(snd->src_addr);
	printf("Dst: ");
	printMAC(snd->dst_addr);
	printf("Eth_Proto: %04x\n", ntohs(*((uint16_t*)snd->eth_proto)));


	ssize_t err=send(fd, snd, sizeof(snd), 0);

	printf("Sent: %d\n", (int)err);

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Send IPC
int sendIPC(int fd, char* buf)
{
	ssize_t err = write(fd, buf, sizeof(buf));

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Recieve raw
int recRaw(int fd, char *recvd)
{
	ssize_t err=recv(fd, recvd, sizeof(maxSize), 0);

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Recieve IPC
int recIPC(int fd, char* buf)
{
	
	ssize_t err = read(fd, buf, sizeof(buf));
	if(err==-1)	return 0;

	if(buf[0] == '\0')	return 0;

	buf[err]='\0';

	return 1;
}