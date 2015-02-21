//socket(AF_PACKET,RAW_SOCKET,...) means L2 socket , Data-link Layer Protocol= Ethernet

//https://austinmarton.wordpress.com/2011/09/14/sending-raw-ethernet-packets-from-a-specific-interface-in-c-on-linux/

//Select-server. Recieve package, print msg, send "ping" to correct host via new raw-socket and wait.

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>

#define socketName "localSocket"

int main(int argc, char* argv[]){

	if(argc != 2){
		printf("Usage: <Daemon-name>\n");
		return -1;
	}



}