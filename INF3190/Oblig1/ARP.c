#include <inttypes.h>
#include <stdio.h>

struct ARP-list
{
	char MIP;
	uint8_t MAC[6];
	struct ARP-list *next=NULL;
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
