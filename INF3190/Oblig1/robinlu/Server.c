#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>
#include <signal.h>
#include <stdlib.h>

//Max size for MIP_Frame
#define maxSize 1500

//Global socket and socketname-variables
int sock;
char* daemonName;

//Closes the server, closes socket and frees socketname
void closeProg()
{
	close(sock);
	unlink(daemonName);
	free(daemonName);
	printf("\nSystem closing!\n");
	exit(0);
}
//Main
int main(int argc, char* argv[])
{

	//Handles ctrl+c, for closing the server
	signal(SIGINT, closeProg);

	if(argc != 2){
		printf("\"Usage: <Daemon-name>\n");
		return -1;
	}

	//Creates socket
	sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}

	//Saves daemonname
	daemonName = malloc(strlen(argv[1])+1);
	strcpy(daemonName, argv[1]);

	//makes sure the socket can be re-used
	int activate=1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	//Binds the name to the socket
	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemonName, sizeof(bindaddr.sun_path));

	//Connects to the socket
	if(connect(sock, (struct sockaddr*)&bindaddr, sizeof(bindaddr)) == -1){
		perror("Error during connection to socket");
		free(daemonName);
		close(sock);
		return -3;
	}

	while(1){
		//Recives information from Daemon
		char buf[maxSize];
		ssize_t recieved = read(sock, buf, maxSize);
		//When we recive something
		if(recieved > 0){
			//For testing the timeval-struct in Client.
			//usleep(500000);
			buf[recieved] = 0;
			//Print the information and send "Pong" back.
			printf("Recieved '%s' from client!\n", buf);
			write(sock, "Pong", 4);
		}
	}
	//Close the socket.
	close(sock);
}