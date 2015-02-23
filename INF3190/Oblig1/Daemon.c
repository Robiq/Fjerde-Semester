//socket(AF_PACKET,RAW_SOCKET,...) means L2 socket , Data-link Layer Protocol= Ethernet

//https://austinmarton.wordpress.com/2011/09/14/sending-raw-ethernet-packets-from-a-specific-interface-in-c-on-linux/

//Select-server. Recieve package, print msg, send "ping" to correct host via new raw-socket and wait.

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

struct ether_frame
{
	uint8_t dst_addr[6];
	uint8_t src_addr[6];
	uint8_t eth_proto[2];
	char    contents[0];
} __attribute__((packed));

struct ARP-list
{
	char MIP;
	uint8_t MAC[6];
	struct ARP-list *next=NULL;
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

int createEtherFrame(char* msg, uint8_t* iface_hwaddr, struct ether_frame *frame)
{

	assert(frame);

	//Ethernet broadcast addr.
	memcpy(frame->dst_addr, ,6); //WHAT HERE?? TODO

	//Ethernet source addr.
	memcpy(frame->src_addr, iface_hwaddr, 6);

	//Ethernet protocol field
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	//Fill in the message.
	memcpy(frame->contents, msg, strlen(msg));
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

	const char* daemonName = argv[1];
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
	for(i = 0; i < 5; ++i)
		printf("%02x:", iface_hwaddr[i]);
	printf("%02x\n", iface_hwaddr[5]);
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
		//Checks if the request-socket is in the FD_SET.
		if(FD_ISSET(raw, &fds)){
			char buffer[maxSize];
			//Connected with a raw socket
			recieveRaw(buffer);
			//If not in ARP-table: ADD! TODO
			#ifdef DEBUG
			//Print information on message recieved
			//Sender+Reciever MIP-adress, and current status of ARP-cache (Print linkedlist)!
			#endif
		}
		
		if(FD_ISSET(ipc, &fds)){
			//Connected with ipc

			char buf[50];

			int accpt = accept(ipc, NULL, NULL);
			read(cfd, buf, sizeof(buf));

			//Håndter __ som skiller msg fra address
			char[maxSize] msg;
			char dst[1];
			decodeBuf(buf, msg, dst);
			
			size_t msgsize = sizeof(struct ether_frame)+strlen(msg);
			struct ether_frame *frame = malloc(msgsize);
			
			uint8_t mac[6];

			if(findARP(dst, mac)){
				//Send msg
			} else{
				//Send ARP, motta ARP, lagre ARP, send msg.
				//Send ARP
				//Motta ARP
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