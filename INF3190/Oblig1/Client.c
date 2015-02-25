#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>


int main(int argc, char* argv[])
{

	if(argc != 4){
		printf("\"Usage: <Daemon-name> <reciever-adress> <message> \n");
		return -1;
	}


	//printf("%s\n%s\n%s\#n", argv[1], argv[2], argv[3]);

	int sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}
	
	char* daemonName = malloc(strlen(argv[1])+1);
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
	
	size_t s = write(sock, complMsg, strlen(complMsg));
	
	free(complMsg);
	free(mipRecv);
	free(mipAdr);
	free(message);

	
	if(s==-1){
		perror("Error during sending");
		close(sock);
		free(daemonName);
		return -7;
	}
	time_t start;
	start = time (NULL);

	ssize_t recieved=0;
	double diff=0;

	printf("SentMSG\n");

	while(diff < 1.0 && recieved == 0){

		char buf[5];
		
		if(recieved < 0){
			perror("Error during read from socket");
			close(sock);
			free(daemonName);
			return -8;
		} else if(recieved != 0){
			buf[recieved]=0;
			printf("Message recieved: %s\n", buf);
		}

		if(diff>1.0)	break;

		recieved = read(sock, buf, 5);
		
		time_t now;
		now = time(NULL);

		diff = difftime((now), (start));

	}
	if(diff < 1.0){
		printf("Time between sending ping and recieving response was: %f seconds\n", diff);
	} else{
		printf("Timeout!\n");
	}
	free(daemonName);
	close(sock);
	return 0;
}