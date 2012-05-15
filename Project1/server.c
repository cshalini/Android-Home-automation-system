/*
This code primarily comes from
http://www.prasannatech.net/2008/07/socket-programming-tutorial.html
and
http://www.binarii.com/files/papers/c_sockets.txt
 */
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <termios.h>
//1 if using Macintosh anything else if Linux. NO WINDOWS!
#define MAC 1

int PORT_NUMBER;
char arduinoBuf[4];
int sendTextToArduino = 0;
char temperatureBuf[4];

void start_server()
{
	// structs to represent the server and client
	struct sockaddr_in server_addr,client_addr;
	int sock; // socket descriptor

	// 1. socket: creates a socket descriptor that you later use to make other system calls
	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("Socket");
		exit(1);
	}
	int temp;
	if (setsockopt(sock,SOL_SOCKET,SO_REUSEADDR,&temp,sizeof(int)) == -1) {
		perror("Setsockopt");
		exit(1);
	}
	// configure the server
	server_addr.sin_port = htons(PORT_NUMBER); // specify port number
	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = INADDR_ANY;
	bzero(&(server_addr.sin_zero),8);

	// 2. bind: use the socket and associate it with the port number
	if (bind(sock, (struct sockaddr *)&server_addr, sizeof(struct sockaddr)) == -1) {
		perror("Unable to bind");
		exit(1);
	}
	// 3. listen: indicates that we want to listn to the port to which we bound; second arg is number of allowed connections
	if (listen(sock, 5) == -1) {
		perror("Listen");
		exit(1);
	}
    int fdArduino;
    if(MAC == 1) { 
        fdArduino = open("/dev/tty.usbmodem621", O_RDWR | O_NOCTTY | O_NDELAY); 
    }
    else {
        fdArduino = open("/dev/ttyACM0", O_RDWR);
    }
	while(1) {
		printf("\nServer waiting for connection on port %d\n", PORT_NUMBER);
		fflush(stdout);

		// 4. accept: wait until we get a connection on that port
		int sin_size = sizeof(struct sockaddr_in);
		int fd = accept(sock, (struct sockaddr *)&client_addr,(socklen_t *)&sin_size);
		printf("Server got a connection from (%s, %d)\n", inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));

		// 5. recv: read incoming message into buffer
		char recv_data[1024];
		int bytes_received = recv(fd,recv_data,1024,0);
		// null-terminate the string
		recv_data[bytes_received] = '\0';

		printf("Server received message from android app: %s\n", recv_data);

		// handshake with java program
		// Q = send temp back to android
		// X0 = turn off X10 device
		// X1 = turn on X10 device
		if (strlen(recv_data) > 0) {

            struct termios options;
            tcgetattr(fdArduino, &options);
            cfsetispeed(&options, 9600);
            cfsetospeed(&options, 9600);
            tcsetattr(fdArduino, TCSANOW, &options);

			if (recv_data[0] == 'X') {
				if (recv_data[1] == '0') {
					if (fdArduino == -1) printf("ERROR\n");

					arduinoBuf[0] = 'X';
					arduinoBuf[1] = '0';
                    arduinoBuf[2] = '\0';// turn off x10

					int chars_written = write(fdArduino, arduinoBuf, strlen(arduinoBuf)); // write to Arduino
					printf("Server wrote value to arduino: %s\n", arduinoBuf);

				} else if (recv_data[1] == '1') {
					
					if (fdArduino == -1) printf("ERROR\n");
					arduinoBuf[0] = 'X';
					arduinoBuf[1] = '1';
                    arduinoBuf[2] = '\0';// turn off x10

					int chars_written = write(fdArduino, arduinoBuf, strlen(arduinoBuf)); // write to Arduino
					printf("Server wrote value to arduino: %s\n", arduinoBuf);
				}
			}
            else if (recv_data[0] == 'Q') {
				// get temp				
                printf("FD = %d\n", fdArduino);
				if (fdArduino == -1) printf("ERROR\n");

				// send Q for query
				arduinoBuf[0] = 'Q';
				arduinoBuf[1] = '|';
				arduinoBuf[2] = '\0';

				int chars_written = write(fdArduino, arduinoBuf, strlen(arduinoBuf)); // write to Arduino
				printf("Server wrote value to arduino: %s\n", arduinoBuf);
				sleep(2.5);

				int chars_read = read(fdArduino, &temperatureBuf, 3); // Read temperature from Arduino to buf
				printf("Server chars read from arduino: %d\n", chars_read);
				temperatureBuf[chars_read] = '\0';
				printf("Server received value from arduino: %s\n", temperatureBuf);
				
				// Send temperature to android app
				send(fd, temperatureBuf, strlen(temperatureBuf), 0);
				printf("Server sent message to android app: %s\n", temperatureBuf);
			}
		}
		// 7. close: close the socket connection
		close(fd);
		printf("Server closed connection\n");
	}
}

int main(int argc, char *argv[])
{
	// check the number of arguments
	if (argc != 2)
	{
		printf("\nUsage: server [port_number]\n");
		exit(0);
	}
	PORT_NUMBER = atoi(argv[1]);
    start_server();    
}
