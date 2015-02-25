//socket(AF_PACKET,RAW_SOCKET,...) means L2 socket , Data-link Layer Protocol= Ethernet

//https://austinmarton.wordpress.com/2011/09/14/sending-raw-ethernet-packets-from-a-specific-interface-in-c-on-linux/

#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include <assert.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/un.h>

#include <arpa/inet.h>
#include <sys/socket.h>
#include <linux/if_packet.h>
#include <net/ethernet.h>

#include <net/if.h>

#include <sys/ioctl.h>
#include <bits/ioctls.h>

//To get Protocol.c, 
#include "Protocol.c"

#define ETH_P_MIP 0xFF
#define maxCon 10


struct Arp_list
{
	char MIP[1];
	uint8_t MAC[6];
	struct Arp_list *next;
};

int raw, ipc, frmSet=0;;
char *daemonName, *tmpBuf;
struct Arp_list* first;
uint8_t myAdr[6];
struct MIP_Frame *tmpFrame;

void clearArp()
{
	struct Arp_list* this=first;
	struct Arp_list* prev;
	
	while(this->next != NULL){
		prev=this;
		this=this->next;
		free(prev);
	}
	free(this);
}

void closeProg(){
	close(ipc);
	close(raw);
	unlink(daemonName);
	free(daemonName);
	if(frmSet){
		free(tmpBuf);
		free(tmpFrame);
	}
	clearArp();

	printf("\nSystem closing!\n");
	exit(0);
}

static int get_if_hwaddr(int sock, const char* devname, uint8_t hwaddr[6])
{
	struct ifreq ifr;
	memset(&ifr, 0, sizeof(ifr));
	
	assert(strlen(devname) < sizeof(ifr.ifr_name));
	strcpy(ifr.ifr_name, devname);

	if(ioctl(sock, SIOCGIFHWADDR, &ifr) < 0)
	{
		perror("ioctl");
		return -4;
	}

	memcpy(hwaddr, ifr.ifr_hwaddr.sa_data, 6*sizeof(uint8_t));

	return 0;
}

//Finds mac-addr
int findArp(char dst, uint8_t* macAd)
{
	struct Arp_list* this=first;
	while(this->next != NULL){
		if(this->MIP[0] == dst){
			macAd=this->MAC;
			return 1;
		}
		this=this->next;
	}
	return 0;
}
//Saves an APR-resoult
int saveArp(char dst, uint8_t* mac)
{
	struct Arp_list* this=first;
	while(this->next != NULL)	this=this->next;

	struct Arp_list* add=malloc(sizeof(struct Arp_list));
	add->MIP[0]= dst;
	memcpy(add->MAC, mac, 6);
	this->next=add;
	add->next=NULL;

	return 1;
}
//Print Arp-table
void printArp()
{
	printf("\nArp_list:\n");
	struct Arp_list* this=first;
	while(this->next != NULL){
		printf("MIP-ADR: %c\n", this->MIP[0]);
		printf("MAC-ADR: ");
		printMAC(this->MAC);
		this=this->next;
	}

	printf("MIP-ADR: %c\n", this->MIP[0]);
	printf("MAC-ADR: ");
	printMAC(this->MAC);
}

void decodeBuf(const char* buf, char* msg, char* dst)
{	
	int i;
	for(i=0;i<strlen(buf);i++){
		if(buf[i] == '_' && buf[i+1] == '_'){
			dst[0]=buf[i+2];
			break;
		}
		msg[i]=buf[i];
	}
	msg[i]='\0';
}

int main(int argc, char* argv[]){

	if(argc != 3){
		printf("Usage: <Daemon-name> <Interface>\n");
		return -1;
	}


	const char* interface = argv[2];
	uint8_t iface_hwaddr[6];

	//Handles ctrl+c, for closing the server
	signal(SIGINT, closeProg);

	int err, accpt=-1;
	fd_set fds;

	//Creates a socket for requests.
	raw=socket(AF_PACKET, SOCK_RAW, 0xFFFF);

	if(raw == -1){
		perror("Socket");
		close(raw);
		return -2;
	}

	//Makes sure that the socket always can be re-used, if recently used for another connection
	int activate=1;
	err = setsockopt(raw, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));
	//If above operation gives error.
	if(err==-1){
		perror("setsockopt");
		close(raw);
		return -3;
	}

	//
	if(get_if_hwaddr(raw, interface, iface_hwaddr) != 0)	return -3;

	memcpy(myAdr, iface_hwaddr,sizeof(iface_hwaddr));

	#ifdef DEBUG
	/* Print the hardware address of the interface */
	printf("HW-addr: ");
	printMAC(iface_hwaddr);
	#endif

	/* Bind the socket to the specified interface */
	struct sockaddr_ll device;
	memset(&device, 0, sizeof(device));

	device.sll_family = AF_PACKET;
	device.sll_ifindex = if_nametoindex(interface);

	//Binds the address-information to the socket
	err=bind(raw, (struct sockaddr*)&device, sizeof(device));
	//If the above operation gives an error.
	if(err == -1){
		perror("bind");
		close(raw);
		return -5;
	}

	ipc = socket(AF_UNIX, SOCK_SEQPACKET, 0);

	//Makes sure that the socket always can be re-used, if recently used for another connection
	activate=1;
	err = setsockopt(ipc, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	if(ipc == -1){
		perror("Socket");
		return -6;
	}
	
	daemonName = malloc(strlen(argv[1])+1);
	strcpy(daemonName, argv[1]);

	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemonName, sizeof(bindaddr.sun_path));

	if(bind(ipc, (struct sockaddr*)&bindaddr, sizeof(bindaddr)) == -1){
		perror("Bind");
		close(ipc);
		free(daemonName);
		return -7;
	}

	//Starts listening for connections on the request-socket. Max queue is set to 5.
	err=listen(ipc, 5);
	//If the above operation gives error.
	if(err == -1){
		perror("listen");
		close(ipc);
		free(daemonName);
		return -9;
	}

	first = malloc(sizeof (struct Arp_list));
	first->next=NULL;

	int fd_max;
	int i=0;

	if(raw > ipc)	fd_max = raw;
	else if (ipc > raw)	fd_max = ipc;

	//Runs the server
	while(i<maxCon){
		//initializes the FD_SETS
		FD_ZERO(&fds);

		FD_SET(raw, &fds);
		FD_SET(ipc, &fds);
		if(accpt!=-1){
			FD_SET(accpt, &fds);
			fd_max=accpt;
		}
		
		//Checks if sockets are avalible.
		int sel=select(fd_max+1, &fds, NULL, NULL, NULL);
		//If the above operation gives error
		if(sel==-1){
			perror("select");
			close(raw);
			close(ipc);
			clearArp();
			free(daemonName);
			return -6;
		}

		//Looks for a socket connected to earlier!
		if(FD_ISSET(accpt, &fds)){

			char buf[maxSize];

			recIPC(accpt, buf);

			//Håndter __ som skiller msg fra address
			char msg[maxSize];
			char dst[1];
			decodeBuf(buf, msg, dst);
			
			size_t msgsize = sizeof(struct ether_frame)+ sizeof(struct send);
			struct ether_frame *frame = malloc(msgsize);
			struct MIP_Frame *mipFrame = malloc(sizeof(struct MIP_Frame));
			
			size_t sndSize = strlen(msg);

			uint8_t mac[6];

			if(findArp(dst[0], mac)){
				//Create frames
				if(!setTransport(daemonName, dst, sndSize, mipFrame)){
					//ERROR!
					perror("Error during framecreation(Transport)");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet)	free(tmpFrame);
					free(daemonName);
					free(frame);
					free(mipFrame);

					return -12;
				}

				struct send *sendInfo = malloc(sizeof(struct send) + sizeof(mipFrame));

				if(!createSend(mipFrame, msg, sendInfo)){
					//ERROR!
					perror("Error during framecreation(SEND)");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet)	free(tmpFrame);
					free(daemonName);
					free(frame);
					free(mipFrame);
					free(sendInfo->frame);
					free(sendInfo);
					return -13;
				}
				createEtherFrame(sendInfo, myAdr, mac, frame);
				
				//Send raw
				if(!sendRaw(raw, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet)	free(tmpFrame);
					free(daemonName);
					free(frame);
					free(mipFrame);
					free(sendInfo->frame);
					free(sendInfo);
					return -11;
				}

				free(mipFrame);
				free(frame);
				free(sendInfo->frame);
				free(sendInfo);
				
			} else{

				if(frmSet)	free(tmpFrame);

				tmpFrame = malloc(sizeof(struct MIP_Frame));
				frmSet=1;

				uint8_t dst_addr[6];

				//Create arp-frame
				err = setARP(daemonName, mipFrame);
				//Create send-struct & ether-frame
				memcpy(dst_addr, "\xFF\xFF\xFF\xFF\xFF\xFF", 6);
				//TODO WUT
				msg[0]='\0';

				struct send *sendInfo = malloc(sizeof(struct send) + sizeof(mipFrame));

				if(!createSend(mipFrame, NULL, sendInfo)){
					//ERROR!
					perror("Error during framecreation(SEND)");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet)	free(tmpFrame);
					free(daemonName);
					free(frame);
					free(mipFrame);
					free(sendInfo->frame);
					free(sendInfo);
					return -13;
				}
				createEtherFrame(sendInfo, myAdr, dst_addr, frame);
				
				//SEND!
				if(!sendRaw(raw, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();
					free(tmpFrame);
					free(daemonName);
					free(frame);
					free(mipFrame);
					free(sendInfo->frame);
					free(sendInfo);
					return -11;
				}

				free(mipFrame);
				free(frame);

				if(!setTempTransp(daemonName, sndSize, tmpFrame)){
					//ERROR!
					printf("Error during framecreation(TempTrans), line 617!\n");
					close(raw);
					close(ipc);
					unlink(daemonName);
					clearArp();

					if(frmSet)	free(tmpFrame);
					free(daemonName);
					free(sendInfo->frame);
					free(sendInfo);
					return -12;
				}
				tmpBuf = strdup(buf);
				free(sendInfo->frame);
				free(sendInfo);

				printf("SENTTORAW\n");
			}		
		}

		//Checks if the request-socket is in the FD_SET.
		if(FD_ISSET(raw, &fds)){

			char recvbuf[maxSize];
			struct ether_frame *recvframe = (struct ether_frame*)recvbuf;

			printf("ARRIVE\n");

			//Connected with a raw socket
			err=recRaw(raw, (struct ether_frame*) recvbuf);
			if(!err){
				printf("Error while reciving from raw!\n");
				close(raw);
				close(ipc);
				clearArp();

				if(frmSet){
					free(tmpFrame);
					free(tmpBuf);
				}
				
				free(daemonName);
				return -11;
			}

			printf("ARRIVE2\n");

			int debug=0;

			#ifdef DEBUG
			//Print information on message recieved
			//Sender+Reciever MIP-adress
			debug=1;
			#endif
			
			struct send *recvd = (struct send*) recvframe->contents;
			
			//Create ethernet-frame & send-struct!
			size_t msgsize = sizeof(struct ether_frame) + sizeof(struct send);
			struct ether_frame *frame = malloc(msgsize);
			
			printf("ARRIVE3\n");

			err=findCase(recvframe, debug);


			if (err==-1){
				printf("Faulty frame/frames recived!\n");
				close(raw);
				close(ipc);
				clearArp();

				if(frmSet){
					free(tmpFrame);
					free(tmpBuf);
				}
				free(daemonName);
				free(frame);
				return -8;
				//Recieved Arp-response.
			} else if(err == 2){
				//Save in Arp-cache
				saveArp(recvd->frame->srcMIP[1], recvframe->src_addr);
				//Finalize saved mip-frame!
				finalTransp(recvd->frame->srcMIP, tmpFrame);
				//Add send-struct
				struct send *sendInfo = malloc(sizeof(struct send) + sizeof(tmpFrame));
				if(!createSend(tmpFrame, tmpBuf, sendInfo)){
					//ERROR!
					perror("Error during framecreation (SEND)");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
						free(tmpBuf);
					}
					free(daemonName);
					free(frame);
					free(sendInfo->frame);
					free(sendInfo);
					return -13;
				}
				//Add everything to ethernet-frame & send!
				createEtherFrame(sendInfo, myAdr, recvframe->src_addr, frame);

				//SEND
				if(!sendRaw(raw, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
						free(tmpBuf);
					}
					free(daemonName);
					free(frame);
					free(sendInfo->frame);
					free(sendInfo);
					return -11;
				}

				free(frame);
				free(tmpFrame);
				free(tmpBuf);
				frmSet=0;
				free(sendInfo->frame);
				free(sendInfo);

			//Revieced Arp-request
			} else if(err == 3){
				uint8_t mac[6];
				//Is it in my Arp-cache? If not, add!
				if(!findArp((char)recvd->frame->srcMIP[1], mac)){
					//Save in Arp-cache
					saveArp((char)recvd->frame->srcMIP[1], recvframe->src_addr);
				}
				//Send Arp-response!
				struct MIP_Frame* frm = malloc(sizeof(struct MIP_Frame));
				//Create Arp-response-frame.
				setARPReturn(daemonName, recvd->frame->srcMIP, frm);
				//Create send-struct
				struct send *sendInfo = malloc(sizeof(struct send) + sizeof(frm));
				if(!createSend(frm, NULL, sendInfo)){
					//ERROR!
					perror("Error during framecreation(SEND)");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
						free(tmpBuf);
					}
					free(daemonName);
					free(sendInfo->frame);
					free(sendInfo);
					free(frm);
					free(frame);
					return -13;
				}
				//Create ethernet-package
				createEtherFrame(sendInfo, myAdr, recvframe->src_addr, frame);

				//SEND!
				if(!sendRaw(raw, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
						free(tmpBuf);
					}
					free(daemonName);
					free(sendInfo->frame);
					free(sendInfo);
					free(frm);
					free(frame);
					return -11;
				}

				free(frame);
				free(frm);
				free(sendInfo->frame);
				free(sendInfo);

			//Recived transport
			}else{
				//Send IPC
				if(!sendIPC(ipc, recvd->message)){
					perror("Error during IPC");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
						free(tmpBuf);
					}
					free(daemonName);
					free(frame);
					return -9;
				}
			}
			

			#ifdef DEBUG
			printArp();
			#endif


		}
		
		if(FD_ISSET(ipc, &fds)){
			//Connected with ipc
			//TODO - connects here, then fails!
			accpt = accept(ipc, NULL, NULL);
			if(accpt == -1){
				close(raw);
				close(ipc);
				clearArp();

				if(frmSet)	free(tmpFrame);
				free(daemonName);
				return -10;
			}
		}
		i++;
	}
	unlink(daemonName);
	free(daemonName);
	return 0;
}