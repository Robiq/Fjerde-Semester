CC=gcc -g

Full: Client Server Daemon

Daemon: Daemon.o Protocol.o Transfer.o
	$(CC) -o Daemon Daemon.o Transfer.o Protocol.o

Client: Client.o
	$(CC) -o Client Client.o

Server: Server.o
	$(CC) -o Server Server.o

Transfer: Transfer.o
	$(CC) -o Transfer Transfer.o

Protocol:	Protocol.o
	$(CC) -o Protocol Protocol.o

clean: cleanserver cleanclient cleandaemon cleanTransfer cleanProtocol

cleandaemon:
	rm -f Daemon Daemon.o

cleanserver:
	rm -f Server Server.o

cleanclient:
	rm -f Client Client.o

cleanProtocol:
	rm -f Protocol Protocol.o

cleanTransfer:
	rm -f Transfer Transfer.o

.PHONY: debug
target debug: CC += -DDEBUG
debug: Full

.PHONY: more
target more: CC += -Wall -Wextra
more: Full
