#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <time.h>
#include <sys/un.h>
#include <signal.h>
#include <stdlib.h>

int sock;

//Closes the server
void closeProg()
{
	close(sock);
	unlink(socketName);	
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

	const char* daemon = argv[1];

	sock=socket(AF_UNIX, SOCK_SEQPACKET, 0);
	if(sock == -1){
		perror("Error while creating socket!");
		return -2;
	}

	int activate=1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &activate, sizeof(int));

	struct sockaddr_un bindaddr;
	bindaddr.sun_family = AF_UNIX;
	strncpy(bindaddr.sun_path, daemon, sizeof(bindaddr.sun_path));

	if(bind(sock, (struct sockaddr*)&bindaddr, sizeof(bindaddr)))
	{
		perror("Error when binding socket!");
		return -3;
	}

	if(listen(sock, SOMAXCONN)){
		perror("Error during listening to socket!");
		return -4;
	}

	while(1){
		int con = accept(sock, NULL, NULL);
		char buf[100];
		ssize_t recieved = read(con, buf, 99);
		if(recieved > 0){
			buf[recieved] = 0;
			printf("Recieved '%s' from client!\n", buf);
			write(con, "Pong", 4);
		}

		close(con);
	}

	close(sock);
	unlink(socketName);
	return 0;
}