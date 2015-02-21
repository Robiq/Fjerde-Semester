#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include <assert.h>
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

/**
 * Prints the mac-address
 * @param mac The mac address to print
 */
static void printmac(uint8_t mac[6])
{
	int i;
	for(i = 0; i < 5; ++i)
		printf("%02x:", mac[i]);
	printf("%02x\n", mac[5]);
}

int main(int argc, char* argv[])
{
	int sock,i;
	uint8_t iface_hwaddr[6];
	const char* interface;

	if(argc != 2)
	{
		printf("USAGE: %s [interface]\n", argv[0]);
		printf(" interface: The interface to send packets on\n");
	}

	interface = argv[1];

	/* AF_PACKET = raw socket interface
	 * SOCK_RAW  = we cant the l2 header intact (SOCK_DGRAM removes header)
	 * ETH_P_ALL = all ethernet protocols
	 */
	if((sock = socket(AF_PACKET, SOCK_RAW, 0xFFFF)) == -1)
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

	while(1)
	{
		/* Ethernet MTU is < 1600 bytes */
		char buf[1600];
		struct ether_frame *frame = (struct ether_frame*)buf;
		ssize_t retv = recv(sock, buf, sizeof(buf), 0);

		printf("Received message with len=%zd\n", retv);
		printf("Destination address: ");
		printmac(frame->dst_addr);
		printf("Source address:      ");
		printmac(frame->src_addr);
		printf("Protocol type:       %04x\n", ntohs(*((uint16_t*)frame->eth_proto)));
		printf("Contents:            %.*s\n", (int)retv-14, frame->contents);	
	}
	
	close(sock);
	return 0;
}
