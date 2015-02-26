#include <inttypes.h>

#define ARP 15872
#define ARPret 7680
#define Routing 24064
#define Transport 40448
#define maxSize 1500

struct ether_frame
{
	uint8_t dst_addr[6];
	uint8_t src_addr[6];
	uint8_t eth_proto[2];
	char    contents[0];
} __attribute__((packed));

struct MIP_Frame
{
	uint16_t TRA_TTL_Payload[1];
	char srcMIP[1];
	char dstMIP[1];
	char message[0];
} __attribute__((packed));