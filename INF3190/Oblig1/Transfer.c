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

#include "Structs.h"

//HERE!!!
int arpRet(struct send* recv)
{
	struct MIP_Frame* frm = send->frame;
	uint8_t tst = frm->TRA_TTL_Payload[0];
	if(tst == 0)	return 1;
	return 0;
}

int arp(struct send* recv)
{
	struct MIP_Frame* frm = send->frame;
	char tst = frm->TRA_TTL_Payload[0];
	if()	return 1;
	return 0;
}

int caseFind(struct ether_frame* frame)
{	
	//Arp-answer
	if(arpRet(frame->contents)){

	//Arp-package
	} else if(arp(frame->contents)){

	//Transport
	}else{

	}
}

//Tolk MIP-header og velg rett handlingsmønster!
int findCase(struct ether_frame* frame, int debug)
{
	caseFind(frame);

	if(debug){
		//Printstuff.jpg
	}
}

//Send raw
int sendRaw(int fd, struct ether_frame *snd)
{
	ssize_t err=send(fd, snd, sizeof(snd), 0);

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
int recRaw(int fd, struct ether_frame *recv)
{
	ssize_t err=recv(fd, recv, sizeof(recv), 0);

	if(err==-1 || err==0)	return 0;

	return 1;
}

//Recieve IPC
int recIPC(int fd, char* buf)
{
	ssize_t err = read(fd, buf, sizeof(buf));
	if(err==-1)	return 0;

	if(buf[0] == '\0')	return 0;

	return 1;
}