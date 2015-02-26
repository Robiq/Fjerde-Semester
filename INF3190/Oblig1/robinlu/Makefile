CC=gcc -g

Full: Client Server Daemon

Daemon: Daemon.o
	$(CC) -o Daemon Daemon.o

Client: Client.o
	$(CC) -o Client Client.o

Server: Server.o
	$(CC) -o Server Server.o

clean: cleanserver cleanclient cleandaemon

cleandaemon:
	rm -f Daemon Daemon.o

cleanserver:
	rm -f Server Server.o

cleanclient:
	rm -f Client Client.o

.PHONY: debug
target debug: CC += -DDEBUG
debug: Full

.PHONY: more
target more: CC += -Wall -Wextra
more: Full
