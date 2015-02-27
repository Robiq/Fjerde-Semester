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
//Prints the MAC-address given as parameter
void printMAC(uint8_t* mac)
{
	int i;
	//Print the first 5 parts.
	for(i = 0; i < 5; ++i)
		printf("%02x:", mac[i]);
	//Prints the last part with a linechange.
	printf("%02x\n", mac[5]);
}
//Checks if the MIP-frame (Parameter) is an ARP-return-frame.
//Returns 1 if it is, returns 0 if it isn't.
int arpRet(struct MIP_Frame* recv)
{
	int x;
	x = (int) recv->TRA_TTL_Payload[0];
	if(x == ARPret)	return 1;
	
	return 0;
}
//Check if MIP-frame (Parameter) is an ARP-frame
//Returns 1 if it is, returns 0 if it isn't
int arp(struct MIP_Frame* recv)
{
	int x;
	x = (int) recv->TRA_TTL_Payload[0];
	if(x == ARP)	return 1;
	return 0;
}

//Handles the MIP-frame (Parameter) and finds what kind of frame it is (TRA). Then gives the Daemon the information for correct interpretation.
//Returns -1 if faulty frame, returns 1 if it is a Transport-frame, returns 2 if it is an ARP-return frame, and returns 3 if it is an ARP-frame
int findCase(struct MIP_Frame *frm)
{
	//Arp-return-frame
	if(arpRet(frm)){
		#ifdef DEBUG
		printf("Type: ARP-Return\n");
		#endif
		//In daemon: save in ARP-list, send saved packet.
		return 2;
	//Arp-package
	} else if(arp(frm)){
		#ifdef DEBUG
		printf("Type: ARP\n");
		#endif
		//In daemon: return ARP-response-packet & save sender in ARP-cache.
		return 3;
	//Transport
	}else{
		#ifdef DEBUG
		printf("Type: Transport\n");
		#endif
		//In daemon: send IPC-packet
		if(frm->message != NULL)	return 1;
		//Error! Something wrong with message
		return -1;
	}
}

//Sends information through a raw-socket.
//Param 1: Socket, Param 2: Size of information, Param 3: Frame to send
//Returns 0 if nothing is sent or socket is closed. Returns 1 on success.
int sendRaw(int fd, ssize_t size, struct ether_frame *snd)
{	
	#ifdef DEBUG
	printf("Sending information:\n");
	printf(" Src: ");
	printMAC(snd->src_addr);
	printf("Dst: ");
	printMAC(snd->dst_addr);
	printf("Eth_Proto: %04x\n", ntohs(*((uint16_t*)snd->eth_proto)));
	#endif

	ssize_t err=send(fd, snd, size , 0);
	#ifdef DEBUG
	printf("Sent: %d bytes\n", (int)err);
	#endif

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Send information over IPC-socket.
//Param 1: Socket, Param 2: Message to be sent
//Returns 1 on success, returns 0 if nothing is sent or if socket is closed
int sendIPC(int fd, char* buf)
{
	ssize_t err = write(fd, buf, sizeof(buf));

	#ifdef DEBUG
	printf("Sent over IPC: %d bytes\n", (int) err);
	#endif

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Recives information through a raw-socket
//Param 1: Socket, Param 2: Buffer for reciving information
//Returns 0 if nothing is recived or socket is closed. Returns 1 on success.
int recRaw(int fd, char buf[])
{
	ssize_t err=recv(fd, buf, 1600, 0);

	if(err==-1 || err==0)	return 0;

	//Null-terminate the recived information
	buf[err]='\0';

	return 1;
}

//Recives information through an IPC-socket
//Param 1: Socket, Param 2: Buffer for reciving information
//Returns 0 if nothing is recived or socket is closed. Returns 1 on success.
int recIPC(int fd, char* buf)
{
	
	ssize_t err = read(fd, buf, sizeof(buf));
	if(err==-1 || err==0)	return 0;

	if(buf[0] == '\0')	return 0;

	//Null-terminate the recived information
	buf[err]='\0';

	return 1;
}