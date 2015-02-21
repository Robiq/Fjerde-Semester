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

/*
	 Requires uid=0 or CAP_NET_RAW
	 sudo setcap CAP_NET_RAW=ep send
*/

/**
 * Struct definition of an ethernet frame
 */
struct ether_frame
{
	uint8_t dst_addr[6];
	uint8_t src_addr[6];
	uint8_t eth_proto[2];
	char    contents[0];
} __attribute__((packed));


/**
 * Retrieves the hardware address of the given network device
 *
 * @param sock    Socket to use for the IOCTL
 * @param devname Name of the network device (for example "eth0")
 * @param hwaddr  Buffer to write the hardware address to
 * @return Zero on success, -1 otherwise
 */
static int get_if_hwaddr(int sock, const char* devname, uint8_t hwaddr[6])
{
	struct ifreq ifr;
	memset(&ifr, 0, sizeof(ifr));
	
	assert(strlen(devname) < sizeof(ifr.ifr_name));
	strcpy(ifr.ifr_name, devname);

	if(ioctl(sock, SIOCGIFHWADDR, &ifr) < 0)
	{
		perror("ioctl");
		return -1;
	}

	memcpy(hwaddr, ifr.ifr_hwaddr.sa_data, 6*sizeof(uint8_t));

	return 0;
}

int main(int argc, char* argv[])
{
	int sock,i;
	uint8_t iface_hwaddr[6];
	const char* interface;

	if(argc != 3)
	{
		printf("USAGE: %s [interface] [msg]\n", argv[0]);
		printf(" interface: The interface to send packets on\n");
		printf(" msg: The message to send\n");
		return 0;
	}

	interface = argv[1];

	/* AF_PACKET = raw socket interface
	 * SOCK_RAW  = we cant the l2 header intact (SOCK_DGRAM removes header)
	 * ETH_P_ALL = all ethernet protocols
	 */
	if((sock = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL))) == -1)
	{
		perror("socket");
		return -1;
	}


	if(get_if_hwaddr(sock, interface, iface_hwaddr) != 0)
		return -2;

	/* Print the hardware address of the interface */
	printf("HW-addr: ");
	for(i = 0; i < 5; ++i)
		printf("%02x:", iface_hwaddr[i]);
	printf("%02x\n", iface_hwaddr[5]);

	/* Bind the socket to the specified interface */
	struct sockaddr_ll device;
	memset(&device, 0, sizeof(device));

	device.sll_family = AF_PACKET;
	device.sll_ifindex = if_nametoindex(interface);
	
	if(bind(sock, (struct sockaddr*)&device, sizeof(device)))
	{
		perror("Could not bind socket");
		close(sock);
		return -3;
	}

	/* Construct a raw ethernet frame */
	/* Allocate space for the header and space for the message */
	size_t msgsize = sizeof(struct ether_frame)+strlen(argv[2]);
	struct ether_frame *frame = malloc(msgsize);
	assert(frame);
	
	/* Ethernet broadcast address */	
	memcpy(frame->dst_addr, "\xFF\xFF\xFF\xFF\xFF\xFF", 6);

	/* Fill in our source address */
	memcpy(frame->src_addr, iface_hwaddr, 6);
	
	/* Ethernet protocol field = 0xFFFF (MIP) */
	frame->eth_proto[0] = frame->eth_proto[1] = 0xFF;

	/* Fill in the message */	
	memcpy(frame->contents, argv[2], strlen(argv[2]));

	/* Send the packet */
	ssize_t retv = send(sock, frame, msgsize, 0);
	printf("Message size=%zu, sent=%zd\n", msgsize, retv);

	close(sock);
	return 0;
}
