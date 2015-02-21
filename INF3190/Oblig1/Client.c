#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>

#define socketName "localSocket"

int main(int argc, char* argv[])
{

	if(argc != 4){
		printf("\"Usage: <Daemon-name> <reciever-adress> <message> \n");
		return -1;
	}

	const char* daemon = argv[1];
	const char* recvAddr = argv[2];
	const char* message = argv[3];

	//printf("%s\n%s\n%s\n", argv[1], argv[2], argv[3]);

	int sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}
	
	int activate=1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, socketName, sizeof(bindaddr.sun_path));

	if(connect(sock, (struct sockaddr*)&bindaddr, sizeof(bindaddr))){
		perror("Error during connection to socket");
		return -3;
	}

	write(sock, message, sizeof(message));
	time_t start;
	start = time (NULL);

	ssize_t recieved=0;
	double diff=0;

	while(diff < 1.0 && recieved == 0){

		char buf[5];

		recieved = read(sock, buf, 5);
		
		time_t now;
		now = time(NULL);

		if(recieved < 0){
			perror("Error during read from socket");
			return -7;
		} else if(recieved != 0){
			buf[recieved]=0;
			printf("Message recieved: %s\n", buf);
		}

		diff = difftime((now), (start));
	}
	if(diff != 1.0){
		printf("Time between sending ping and recieving response was: %f seconds\n", diff);
	} else{
		printf("Timeout!\n");
	}
	
	close(sock);
	return 0;
}