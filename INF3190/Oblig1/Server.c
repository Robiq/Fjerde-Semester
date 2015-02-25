#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>
#include <signal.h>
#include <stdlib.h>
#define maxSize 1500

int sock;
char* daemonName;

//Closes the server
void closeProg()
{
	close(sock);
	unlink(daemonName);
	free(daemonName);
	printf("\nSystem closing!\n");
	exit(0);
}

int main(int argc, char* argv[])
{

	//Handles ctrl+c, for closing the server
	signal(SIGINT, closeProg);

	if(argc != 2){
		printf("\"Usage: <Daemon-name>\n");
		return -1;
	}


	sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}

	daemonName = malloc(strlen(argv[1])+1);
	strcpy(daemonName, argv[1]);

	int activate=1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemonName, sizeof(bindaddr.sun_path));

	if(connect(sock, (struct sockaddr*)&bindaddr, sizeof(bindaddr)) == -1){
		perror("Error during connection to socket");
		free(daemonName);
		close(sock);
		return -3;
	}

	while(1){
		char buf[maxSize];
		ssize_t recieved = read(sock, buf, maxSize);
		if(recieved > 0){
			buf[recieved] = 0;
			printf("Recieved '%s' from client!\n", buf);
			write(sock, "Pong", 4);
		}

	}
	close(sock);
}