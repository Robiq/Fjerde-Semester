//socket(AF_PACKET,RAW_SOCKET,...) means L2 socket , Data-link Layer Protocol= Ethernet

//https://austinmarton.wordpress.com/2011/09/14/sending-raw-ethernet-packets-from-a-specific-interface-in-c-on-linux/

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

#define ETH_P_MIP 0xFF
#define maxCon 10
#define maxSize 1500

int raw, ipc;
struct ARP-list* first;
uint8_t myAdr[6];


struct ARP-list
{
	char MIP;
	uint8_t MAC[6];
	struct ARP-list *next=NULL;
}

void closeProg(){
	close(ipc);
	close(raw);
	unlink(socketName);
	
	struct ARP-list* this=first->next;
	struct ARP-list* prev=first;
	
	while(this->next != NULL){
		free(prev);
		prev=this;
		this=this->next
	}
	free(this);

	printf("\nSystem closing!\n");
	Exit(0);
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

void printMAC(uint8_t* mac)
{
	int i;
	for(i = 0; i < 5; ++i)
		printf("%02x:", mac[i]);
	printf("%02x\n", mac[5]);
}

//Finds mac-addr
int findARP(char dst, uint8_t* macAd)
{
	struct ARP-list* this=first->next;
	while(this->next != NULL){
		if(this->MIP == dst){
			macAd=this->MAC;
			return 1;
		}
		this=this->next
	}
	return 0;
}
//Saves an APR-resoult
int saveARP(char dst, uint8_t* mac)
{
	struct ARP-list* this=first->next;
	while(this->next != NULL)	this=this->next;

	struct ARP-list* add=malloc(sizeof(struct ARP-list));
	memcpy(add->MIP, dst, 1);
	memcpy(add->MAC, mac, 6);
	this->next=add;

	return 1;
}
//Print ARP-table
void printARP()
{
	printf("\nARP-list:\n");
	struct ARP-list* this=first->next;
	while(this->next != NULL){
		printf("MIP-ADR: %c\n", this->MIP);
		printf("MAC-ADR: ");
		printMAC(this->MAC);
		this=this->next;
	}

	printf("MIP-ADR: %c\n", this->MIP);
	printf("MAC-ADR: ");
	printMAC(this->MAC);
}

void decodeBuf(const char* buf, char* msg, char* dst)
{	
	int i;
	for(i=0;i<strlen(buf);i++){
		if(buf[i] == '_' && buf[i+1] == '_'){
			dst=buf[i+2];
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

	first = malloc(sizeof (struct ARP-list));

	const char[1] daemonName = argv[1];

	const char* interface = argv[2];
	uint8_t iface_hwaddr[6];

	//Handles ctrl+c, for closing the server
	signal(SIGINT, closeProg);

	int err;
	fd_set fds;

	//Creates a socket for requests.
	raw=socket(AF_PACKET, SOCK_RAW, 0xFFFF);

	if(raw == -1){
		perror("Socket");
		return -2;
	}

	//Makes sure that the socket always can be re-used, if recently used for another connection
	int activate=1;
	err = setsockopt(raw, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));
	//If above operation gives error.
	if(err==-1){
		perror("setsockopt");
		return -3;
	}

	//
	if(get_if_hwaddr(raw, interface, iface_hwaddr) != 0)	return -3;

	myAdr=iface_hwaddr;

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
	err=bind(raw, (struct sockaddr*)&device, sizeof(device);
	//If the above operation gives an error.
	if(err == -1){
		perror("bind");
		close(sock);
		return -5;
	}

	ipc = socket(AF_UNIX,SOCK_SEQPACKET, 0);

	if(ipc == -1){
		perror("Socket");
		return -6;
	}

	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemonName, sizeof(bindaddr.sun_path));

	if(bind(ipc, (struct sockaddr*)&bindaddr, sizeof(bindaddr)) == -1){
		perror("Bind");
		return -7;
	}

	if(listen(ipc, 10) == -1){
		perror("Listen");
		return -8;
	}

	//Starts listening for connections on the request-socket. Max queue is set to 5.
	err=listen(request_sd, 5);
	//If the above operation gives error.
	if(err == -1){
		perror("listen");
		return -9;
	}

	int fd_max;
	int i=0;

	if(raw > ipc)	fd_max = raw;
	else	fd_max = ipc;

	//Runs the server
	while(i<maxCon){
		//initializes the FD_SETS
		FD_ZERO(&fds);

		FD_SET(raw, &fds);
		FD_SET(ipc, &fds);
		
		//Checks if sockets are avalible.
		int sel=select(highest+1, &fds, NULL, NULL, NULL);
		//If the above operation gives error
		if(sel==-1){
			perror("select");
			return -6;
		}

		struct MIP_Frame *tmpFrame = malloc(sizeof(MIP_Frame));

		//Checks if the request-socket is in the FD_SET.
		if(FD_ISSET(raw, &fds)){
			char recvbuf[maxSize];
			struct ether_frame *recvframe = (struct ether_frame*)recvbuf;

			//Connected with a raw socket
			err=recieveRaw(recvbuf);
			if(!err){
				printf("Error while reciving from raw!\n");
				return -7;
			}


			int debug=0:

			#ifdef DEBUG
			//Print information on message recieved
			//Sender+Reciever MIP-adress
			debug=1;
			#endif
			
			err=findCase(recvframe, debug);
			if (err==-1){
				printf("Faulty ethernet-frame!\n");
				return -8;
			}
			
			struct send *recvd=recvframe->contents;

			uint8_t tmp[6]:
			
			//If not in ARP-table: ADD!
			if(!findARP(recvd->frame->srcMIP, tmp)){
				saveARP(recvd->frame->srcMIP, recvframe->src_addr);
			}

			#ifdef DEBUG
			printARP();
			#endif


		}
		
		if(FD_ISSET(ipc, &fds)){
			//Connected with ipc

			char buf[50];

			int accpt = accept(ipc, NULL, NULL);
			if(accpt == -1)	return -9;

			recIPC(accpt, buf);

			//Håndter __ som skiller msg fra address
			char[maxSize] msg;
			char dst[1];
			decodeBuf(buf, msg, dst);
			
			size_t msgsize = sizeof(struct ether_frame)+strlen(msg);
			struct ether_frame *frame = malloc(msgsize);
			struct MIP_Frame *mipFrame = malloc(sizeof(MIP_Frame));
			struct send *sendInfo = malloc(sizeof(struct send));
			size_t sndSize = strlen(msg);
			

			uint8_t mac[6];
			uint8_t dst_addr[6]:

			if(findARP(dst, mac)){
				//Send msg
				setTransport(daemonName[0], dst[0], sndSize, mipFrame);
				createSend(mipFrame, msg, sendInfo);
				createEtherFrame(sendInfo, myAdr, mac, frame);
				
				
			} else{
				//Send ARP, motta ARP, lagre ARP, send msg.
				err = setARP(daemonName[0], mipFrame);
				//Send ARP
				memcpy(dst_addr, "\xFF\xFF\xFF\xFF\xFF\xFF", 6);
				msg[0]='\0';
				createSend(mipFrame, NULL, sendInfo);
				createEtherFrame(sendInfo, myAdr, dst_addr, frame);
				setTempTrans(daemonName[0], sndSize, tmpFrame);

				//Motta ARP
				//Skjer i raw-socketen over!!! TODO
				//Lagre
				saveARP(dst, mac);
				//Send msg
			}
		}
		i++
	}
	unlink(socketName);
	return 0;
}