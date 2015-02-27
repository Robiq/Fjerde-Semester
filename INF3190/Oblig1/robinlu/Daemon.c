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

//Defines the maximum connections to the select-server! Can be changed for testing purposes!
#define maxCon 1000

//Arp-register struct. Contains the MIP-address and the MAC-address of a daemon, and a pointer to the next ARP-entry.
struct Arp_list
{	
	char MIP[1];
	uint8_t MAC[6];
	struct Arp_list *next;
};

//Global variables, raw&ipc are sockets, retSet handles if ret is given a value, and the same for frmSet and tmpFrame.  
int raw, ipc, retSet=0, frmSet=0;
char ret[1];
struct MIP_Frame *tmpFrame;
char *daemonName;
//Len saves the lengt of the message, first is the pointer to the base node of the ARP-registry and myAdr is the MAC-address of this daemon.
size_t len=0;
struct Arp_list* first;
uint8_t myAdr[6];

//Clears the ARP-registry
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
//Closes the program and frees all allocated variables.
void closeProg(){
	close(ipc);
	close(raw);
	unlink(daemonName);
	free(daemonName);
	if(frmSet){
		free(tmpFrame);
	}
	clearArp();

	printf("\nSystem closing!\n");
	exit(0);
}
//Saves the MAC-address of this daemon in the variable hwaddr, which is the third parameter. Returns 0 if failed and 1 if success.
static int get_if_hwaddr(int sock, const char* devname, uint8_t hwaddr[6])
{
	struct ifreq ifr;
	//clears the struct
	memset(&ifr, 0, sizeof(ifr));
	//Makes sure the struct has room for the content and copies content
	assert(strlen(devname) < sizeof(ifr.ifr_name));
	strcpy(ifr.ifr_name, devname);
	//Grabs address
	if(ioctl(sock, SIOCGIFHWADDR, &ifr) < 0)
	{
		perror("ioctl");
		return -4;
	}
	//Copies all the results and saves them.
	memcpy(hwaddr, ifr.ifr_hwaddr.sa_data, 6*sizeof(uint8_t));

	return 0;
}
//Finds the corresponding MAC-address to the MIP-address. The first parameter is the MIP-address, and the second is where we store the MAC-address found.
//Returns 1 on success, returns 0 on failure.
int findArp(char dst[], uint8_t* macAd)
{	
	if(first->next!=NULL){
		struct Arp_list* this=first->next;
		//Loops through the ARP-registry for the correct MAC-address
		do{
			if(this->MIP[0] == (char)dst[0]){
				memcpy(macAd, this->MAC, 6);
				return 1;
			}
			this=this->next;
		}while(this->next != NULL);
	}
	return 0;
}
//Saves an APR-result in the ARP-registry. Parameters are the MIP-address and the MAC-address.
int saveArp(char dst[], uint8_t* mac)
{
	struct Arp_list* this=first;
	//Finds the last entry
	while(this->next != NULL)	this=this->next;
	//Creates a new entry and adds the information. Then adds the entry to the end of the list.
	struct Arp_list* add=malloc(sizeof(struct Arp_list));
	add->MIP[0]= (char) dst[0];
	memcpy(add->MAC, mac, 6);
	this->next=add;
	add->next=NULL;

	return 1;
}
//Prints the whole Arp-table.
void printArp()
{
	printf("\nArp_list:\n");
	if(first->next != NULL){
		struct Arp_list* this=first->next;
		while(this->next != NULL){
			printf("MIP-ADR: %c\n", this->MIP[0]);
			printf("MAC-ADR: ");
			printMAC(this->MAC);
			this=this->next;
		}

		printf("MIP-ADR: %c\n", this->MIP[0]);
		printf("MAC-ADR: ");
		printMAC(this->MAC);
		printf("---------------------\n\n");
	}
}
//Decodes the input from the client. The first parameter is the raw input and the other parameters are where the correct information is stored.
void decodeBuf(const char* buf, char* msg, char* dst)
{	
	int i;
	//Loops through input string
	for(i=0;i<strlen(buf);i++){
		//If it finds "__", then it saves the char after "__" as the destination MIP-address and breaks the for-loop.
		if(buf[i] == '_' && buf[i+1] == '_'){
			dst[0]=buf[i+2];
			break;
		}
		//Saves the message taken from the buffer.
		msg[i]=buf[i];
	}
	//Null-terminates the string.
	msg[i]='\0';
}
//Main
int main(int argc, char* argv[]){

	if(argc != 3){
		printf("Usage: <Daemon-name> <Interface>\n");
		return -1;
	}

	//Saves the interface we want to connect the raw socket toa s a constant. 
	const char* interface = argv[2];

	uint8_t iface_hwaddr[6];

	//Handles ctrl+c, for closing the server
	signal(SIGINT, closeProg);

	//Err hanles different errors through the program. Accpt is set to 0, and the fd_set is declared, so the select-server works as intended.
	int err, accpt=0;
	fd_set fds;

	//Creates a raw socket for requests.
	raw=socket(AF_PACKET, SOCK_RAW, 0xFFFF);
	//error
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

	//Saves the MAC-address of this daemon, in iface_hwaddr. Returns 1 if success, 0 if failure.
	if(get_if_hwaddr(raw, interface, iface_hwaddr) != 0)	return -3;

	//Copy contents of iface_hwaddr to myAdr, which is global.
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

	//Creates a UNIX-socket for IPC.
	ipc = socket(AF_UNIX, SOCK_SEQPACKET, 0);
	//Error
	if(ipc == -1){
		perror("Socket");
		return -6;
	}

	//Makes sure that the socket always can be re-used, if recently used for another connection
	activate=1;
	err = setsockopt(ipc, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));
	//Saves the name of the daemon.
	daemonName = malloc(strlen(argv[1])+1);
	strcpy(daemonName, argv[1]);

	//Binds the daemonName to the IPC socket and then binds the socket.
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
	//Create memory for the root node in the ARP-registry.
	first = malloc(sizeof (struct Arp_list));
	first->next=NULL;

	int fd_max;
	int i=0;

	//Selects which socket to connect to.
	if(raw > ipc)	fd_max = raw;
	else if (ipc > raw)	fd_max = ipc;

	//Runs the server
	while(i<maxCon){
		//initializes the FD_SETS
		FD_ZERO(&fds);
		//Includes both sockets in the fd_set
		FD_SET(raw, &fds);
		FD_SET(ipc, &fds);
		//If IPC is connected, accpt is set for that connection. This is the IPC-connection we want to use if we have already achieved a connection.
		if(accpt!=0){
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

		//Looks for the socket we connected to earlier! Sees if it is in fd_set fds
		if(FD_ISSET(accpt, &fds)){

			char buf[maxSize]={0};

			//Reads from accpt IPC socket. Stores information in the variable buf.
			if(!recIPC(accpt, buf)){
				//If nothing is to be read, the connection has terminated.
				close(accpt);
				accpt=0;
				if(raw > ipc)	fd_max=raw;
				else	fd_max=ipc;

				//Handles the input from accpt.
			}else{

				char msg [maxSize]={0};
				char dst[1];
				//Handles the input, and separates the message and the destination, if there is one.
				decodeBuf(buf, msg, dst);
				//If a character has been set as a return-address, this is the correct destination.
				if(retSet){
					//Changes dst and resets retSet
					dst[0]=ret[0];
					retSet=0;
				}

				#ifdef DEBUG
				printf("In accpt:\nmsg: %s. Len: %d\n", msg, (int)strlen(msg));
				printf("Dst: %d\n", (int)dst[0]);
				#endif

				//Declares structs and sizes
				size_t sndSize = strlen(msg);
				size_t msgsize = (sizeof(struct ether_frame) + sndSize + sizeof(struct MIP_Frame));
				struct ether_frame *frame = malloc(msgsize);
				struct MIP_Frame *mipFrame = malloc(sizeof(struct MIP_Frame) + sndSize);
				uint8_t mac[6];

				//Tries to find the destination MIP-address(first parameter) in the ARP-registry. The second parameter stores the correct MAC-address, set by the function, if there is a match.
				//Returns 1 on success, 0 on failure.
				if(findArp(dst, mac)){
					//Creates a MIP_Frame with the correct information. Returns 1 on success, 0 on failure.
					//Param 1: MIP-Source, Param2: MIP-Destination, Param3: Payload, Param4: Message, Param5: MIP-frame
					if(!setTransport(daemonName, dst, sndSize, msg, mipFrame)){
						//ERROR!
						printf("Error during framecreation(Transport)\n");
						close(raw);
						close(ipc);
						clearArp();

						if(frmSet)	free(tmpFrame);
						free(daemonName);
						free(frame);
						free(mipFrame);

						return -12;
					}
					//Creates a Ethernet-frame with the correct information
					//Param1: MIP-frame, Param2: Messagesize, Param3: Source MAC, Param4: Destination MAC, Param5: Ethernet-frame
					createEtherFrame(mipFrame, sndSize, myAdr, mac, frame);
					
					//Send frame over raw socket. Returns 1 on success, and 0 on failure.
					//Param1: Socket, Param2: Size of frame, Param3: frame
					if(!sendRaw(raw, msgsize, frame)){
						perror("Error during raw sending");
						close(raw);
						close(ipc);
						clearArp();

						if(frmSet)	free(tmpFrame);
						free(daemonName);
						free(frame);
						free(mipFrame);
						return -11;
					}

					free(mipFrame);
					free(frame);
				//If the destination MIP-address is not in the ARP-registry.	
				}else{
					//If we have a temporary frame set, clear it!
					if(frmSet){
						free(tmpFrame);
					}
					//Allocate space for the temporary frame, and set frmSet to true (1).
					tmpFrame = malloc((sizeof(struct MIP_Frame)) + sndSize);
					frmSet=1;

					uint8_t dst_addr[6];

					//Create MIP-frame of type ARP
					err = setARP(daemonName, mipFrame);

					//Create MAC destination address, which broadcasts to all
					memcpy(dst_addr, "\xFF\xFF\xFF\xFF\xFF\xFF", 6);

					//Creates a Ethernet-frame with the correct information. Returns 1 on success, 0 on failure.
					//Param 1: MIP-frame, Param2: Messagesize, Param3: Source MAC, Param4: Destination MAC, Param5: Ethernet-frame
					createEtherFrame(mipFrame, 0, myAdr, dst_addr, frame);

					//Send frame over raw socket. Returns 1 on success, and 0 on failure.
					//Param1: Socket, Param2: Size of frame, Param3: frame
					if(!sendRaw(raw,(sizeof(struct ether_frame)+sizeof(struct MIP_Frame)), frame)){
						perror("Error during raw sending");
						close(raw);
						close(ipc);
						clearArp();
						free(tmpFrame);
						free(daemonName);
						free(frame);
						free(mipFrame);
						return -11;
					}

					free(mipFrame);
					free(frame);
					//Saves the size of tmpFrame in global variable
					len=sndSize;
					//Creates a MIP_Frame with the correct, currently avaliable information. Returns 1 on success, 0 on failure.
					//Param 1: MIP-Source, Param2: Payload, Param3: Message, Param4: MIP-frame
					if(!setTempTransp(daemonName, sndSize, msg, tmpFrame)){
						//ERROR!
						printf("Error during framecreation(TempTrans)!\n");
						close(raw);
						close(ipc);
						unlink(daemonName);
						clearArp();

						if(frmSet){
							free(tmpFrame);
						}
						free(daemonName);
						return -12;
					}
				}		
			}
		}

		//Checks if the raw-socket is in the FD_SET.
		if(FD_ISSET(raw, &fds)){
			
			//If there is no IPC-connection to/from this daemon, continue
			if(accpt == 0)	continue;

			//Create struct and buffer to recieve ethernet-frame
			char buf[1600];
			struct ether_frame *recvframe = (struct ether_frame*)buf;

			//Connected with a raw socket. Tries to recive information through the socket (Param 1), and save it to the buffer (Param 2).
			//Returns 1 if success, 0 if failure.
			err=recRaw(raw, buf);
			if(!err){
				printf("Error while reciving from raw!\n");
				close(raw);
				close(ipc);
				clearArp();

				if(frmSet){
					free(tmpFrame);
				}
				
				free(daemonName);
				return -11;
			}
			//Allocates memory and accesses the MIP-frame sent in the ethernet-frame
			struct MIP_Frame * recvdMIP = malloc(sizeof(recvframe->contents));
			recvdMIP = (struct MIP_Frame*) recvframe->contents;
			
			#ifdef DEBUG
				//Print information on message recieved				
				struct MIP_Frame* MIPu = malloc(sizeof(recvframe->contents));
				MIPu = (struct MIP_Frame*) recvframe->contents;

				//Print stuff for DEBUG mode!
				printf("\n\nDEBUG\n");
				printf("Destination address: ");
				printMAC(recvframe->dst_addr);
				printf("Source address:      ");
				printMAC(recvframe->src_addr);
				printf("Source MIPaddress: %d\n", (int) recvdMIP->srcMIP[0]);
				printf("Destination MIPaddress: %d\n", (int) recvdMIP->dstMIP[0]);
				printf("Protocol type:       %04x\n", ntohs(*((uint16_t*)recvframe->eth_proto)));
				if(MIPu->message[0] != '\0')	printf("Contents: %s\n", MIPu->message);
				else	printf("No content: ARP\n");	
			#endif

			//Create ethernet-frame and size variable!
			size_t msgsize;
			struct ether_frame *frame;

			//Finds what type of MIP-frame we recived. Parameter is the MIP-frame.
			//Returns -1 if error, 1 if it is a Transport-frame, 2 if it is a ARP-response-frame, and 3 if it is an ARP-frame.
			err=findCase(recvdMIP);

			//ERROR!
			if (err==-1){
				printf("Faulty frame/frames recived!\n");
				close(raw);
				close(ipc);
				clearArp();

				if(frmSet){
					free(tmpFrame);
				}
				free(daemonName);
				free(recvdMIP);
				free(recvframe);
				return -8;
				//Recieved Arp-response.
			} else if(err == 2){
				//Save in Arp-cache
				//Create MIP-frame to read information from ethernet-frame
				struct MIP_Frame* MIP = malloc(sizeof(recvframe->contents));
				MIP = (struct MIP_Frame*) recvframe->contents;
				//Save the MIP-address(Param 1) and the MAC-address(Param2) in the ARP-registry.
				saveArp(MIP->srcMIP, recvframe->src_addr);

				//Finalize saved mip-frame!
				//Add the destination MIP-address (Param1) to the MIP-frame created earlier (Param2)
				finalTransp(MIP->srcMIP, tmpFrame);

				//Create calculate total size of information and create ethernetframe with enough allocated memory.
				msgsize= sizeof(struct ether_frame) + sizeof(struct MIP_Frame) + len;
				frame=malloc(msgsize);

				///Creates a Ethernet-frame with the correct information
				//Param1: MIP-frame, Param2: Messagesize, Param3: Source MAC, Param4: Destination MAC, Param5: Ethernet-frame
				createEtherFrame(tmpFrame, len, myAdr, recvframe->src_addr, frame);

				//Send frame over raw socket. Returns 1 on success, and 0 on failure.
				//Param1: Socket, Param2: Size of frame, Param3: frame
				if(!sendRaw(raw, msgsize, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
					}
					free(recvframe);
					free(daemonName);
					free(frame);
					free(recvdMIP);
					return -11;
				}

				free(frame);
				free(tmpFrame);
				frmSet=0;

			//Revieced Arp-request
			} else if(err == 3){
				uint8_t mac[6];

				//Create MIP-frame to read recived information.				
				struct MIP_Frame* MIP_IP = malloc(sizeof(recvframe->contents));
				MIP_IP = (struct MIP_Frame*) recvframe->contents;

				//Is it in my Arp-cache? If not, add!
				//Tries to find the destination MIP-address(first parameter) in the ARP-registry. The second parameter stores the correct MAC-address, set by the function, if there is a match.
				//Returns 1 on success, 0 on failure.
				if(!findArp(MIP_IP->srcMIP, mac)){
					//Save in Arp-cache
					//Adds MIP-address(Param1) and MAC-address(Param2) to the ARP_registry
					saveArp(MIP_IP->srcMIP, recvframe->src_addr);
				}

				//Send Arp-response!
				//Creates MIP-frame for ARP-response
				struct MIP_Frame* frm1 = malloc(sizeof(struct MIP_Frame));
				char tmp[1];
				tmp[0] = (int) recvdMIP->srcMIP[0];
				//Create Arp-response-frame.
				//Param1: MIP-source, Param2: MIP-destination, Param3: MIP-frame
				setARPReturn(daemonName, tmp, frm1);
				
				//Calculate the correct size of the package and allocate memory for it in a ethernet-frame 
				msgsize= sizeof(struct ether_frame) + sizeof(struct MIP_Frame);
				frame=malloc(msgsize);

				//Creates a Ethernet-frame with the correct information
				//Param1: MIP-frame, Param2: Messagesize, Param3: Source MAC, Param4: Destination MAC, Param5: Ethernet-frame
				createEtherFrame(frm1, 0, myAdr, recvframe->src_addr, frame);

				//Send frame over raw socket. Returns 1 on success, and 0 on failure.
				//Param1: Socket, Param2: Size of frame, Param3: frame
				if(!sendRaw(raw, msgsize, frame)){
					perror("Error during raw sending");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
					}
					free(daemonName);
					free(frm1);
					free(frame);
					return -11;
				}

				free(frm1);
				free(frame);

			//Recived transport
			}else{
				//Send the recived information to IPC

				//Create MIP-frame to read recived information.				
				struct MIP_Frame * MIP_IP = malloc(sizeof(recvframe->contents));
				MIP_IP = (struct MIP_Frame*) recvframe->contents;

				//Send message over IPC socket. Returns 1 on success, and 0 on failure.
				//Param1: Socket, Param2: Message
				if(!sendIPC(accpt, MIP_IP->message)){
					perror("Error during IPC");
					close(raw);
					close(ipc);
					clearArp();

					if(frmSet){
						free(tmpFrame);
					}

					free(daemonName);
					return -9;
				}
				//Set the return MIP-address to the source of the recived message, so we know where to send answer
				ret[0]=MIP_IP->srcMIP[0];
				//retSet is set to true (1)
				retSet=1;
			}

			#ifdef DEBUG
			printf("\n");
			printArp();
			printf("END\n\n");
			#endif
		}
		
		//Finds a connection via IPC.
		if(FD_ISSET(ipc, &fds)){
			//Connected with ipc
			//Accept the connection and redirect it for further processing.
			accpt = accept(ipc, NULL, NULL);
			//Error!
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
	//Free global variables.
	closeProg();
	return 0;
}