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

int arpRet(struct MIP_Frame* recv)
{
	int x;
	x = (int) recv->TRA_TTL_Payload[0];
	if(x == ARPret)	return 1;
	
	return 0;
}
//Check if frame is
int arp(struct MIP_Frame* recv)
{
	int x;
	x = (int) recv->TRA_TTL_Payload[0];
	if(x == ARP)	return 1;
	return 0;
}

//Tolk MIP-header og velg rett handlingsmønster!
int findCase(struct MIP_Frame *frm)
{
	//Arp-answer
	if(arpRet(frm)){
		printf("ARPRET\n");
		//Save in ARP-list, send saved packet, in daemon
		return 2;
	//Arp-package
	} else if(arp(frm)){
		printf("ARP\n");
		//Return ARP-response-packet & save sender in ARP-cache, in daemon
		return 3;
	//Transport
	}else{
		printf("OTHER\n");
		//Send IPC-packet, in daemon
		if(frm->message != NULL)	return 1;
		//Error! Something wrong with message
		return -1;
	}
}

//Send raw
int sendRaw(int fd, ssize_t size, struct ether_frame *snd)
{	
	printf("Src: ");
	printMAC(snd->src_addr);
	printf("Dst: ");
	printMAC(snd->dst_addr);
	printf("Eth_Proto: %04x\n", ntohs(*((uint16_t*)snd->eth_proto)));


	ssize_t err=send(fd, snd, size , 0);

	printf("Sent: %d\n", (int)err);

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Send IPC
int sendIPC(int fd, char* buf)
{
	ssize_t err = write(fd, buf, sizeof(buf));

	if(err==-1 || err==0)	return 0;

	buf[err]='\0';

	return 1;
}

//Recieve raw
int recRaw(int fd, char buf[])
{
	ssize_t err=recv(fd, buf, 1600, 0);

	if(err==-1 || err==0)	return 0;

	buf[err]='\0';

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