#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>

//Main
int main(int argc, char* argv[])
{

	if(argc != 4){
		printf("\"Usage: <Daemon-name> <reciever-adress> <message> \n");
		return -1;
	}

	//Creates socket
	int sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}
	
	//Allocates memory for daemonname
	char* daemonName = malloc(strlen(argv[1])+1);
	strcpy(daemonName, argv[1]);
	
	//Makes sure the socket can be re-used
	int activate=1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	//Gives the socket the correct information
	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemonName, sizeof(bindaddr.sun_path));

	//Connects the socket
	if(connect(sock, (struct sockaddr*)&bindaddr, sizeof(bindaddr)) == -1){
		perror("Error during connection to socket");
		free(daemonName);
		close(sock);
		return -3;
	}
	
	//Creates message for daemon
	char add[] = "__";
	
	char* mipRecv = malloc(strlen(argv[2]) + strlen(add)+1);
	strcpy(mipRecv, add);

	char* mipAdr = malloc(strlen(argv[2])+1);
	strcpy(mipAdr, argv[2]);
	
	strncat(mipRecv, mipAdr, strlen(mipAdr));
	
	char* message = malloc(strlen(argv[3])+1);
	strcpy(message, argv[3]);

	char* complMsg = malloc(strlen(mipRecv) + strlen(message) +1);
	strcpy(complMsg, message);
	strncat(complMsg, mipRecv, strlen(mipRecv)+1);
	
	//Writes to daemon, via IPC-socket
	size_t s = write(sock, complMsg, strlen(complMsg));
	
	//Error during sending.
	if(s==-1){
		perror("Error during sending");
		close(sock);
		free(daemonName);
		return -7;
	//Socket closed in daemon
	} else if(s==0){
		printf("Socket closed!\n");
		close(sock);
		free(daemonName);
		return 0;
	}

	//Free memory
	free(complMsg);
	free(mipRecv);
	free(mipAdr);
	free(message);
	
	
	//Make timeout-struct & init select-variables
	struct timeval xit;
	fd_set acc;

	FD_ZERO(&acc);
	FD_SET(sock, &acc);

	//Set timeout to 1 sec
	xit.tv_sec=0;
	xit.tv_usec=1000000;

	//Select
	int err=select(sock+1, &acc, NULL, NULL, &xit);

	//Error!
	if(err==-1){
		perror("select");
		close(sock);
		free(daemonName);
		return -4;
		//Timeout!
	}else if(err==0){
		printf("Timeout!\n");
		//Connected
	}else{
		ssize_t recieved=0;

		char buf[5];

		//Reads information from daemon
		recieved = read(sock, buf, 5);
		
		//Error during reading from socket
		if(recieved < 0){
			perror("Error during read from socket");
			close(sock);
			free(daemonName);
			return -8;
			//Daemon closed the socket
		} else if(recieved == 0){
			printf("Daemon closed!\n");
			free(daemonName);
			close(sock);
			return 0;
			//Prints info
		}else{
			buf[recieved]=0;
			printf("Message recieved: %s\n", buf);
			printf("Time used: %f ms\n", ((1000000-xit.tv_usec)/1000.0));
		}
	}
	//Closes & frees memory.
	free(daemonName);
	close(sock);
	return 0;
}