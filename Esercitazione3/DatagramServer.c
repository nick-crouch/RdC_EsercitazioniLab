/* Server che fornisce la valutazione di un'operazione tra due interi */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <time.h>
#include <string.h>

#define LINE_LENGTH 256

/*Struttura di una richiesta*/
/********************************************************/
typedef struct{
  char fileName[LINE_LENGTH];
}Request;
/********************************************************/

// Realizzazione di un server sequenziale
int main(int argc, char **argv){
	int sd, port, len, num1, fd, ris, dim, max;
	const int on = 1;
    int ok;
    clock_t begin, end;
    char fn[LINE_LENGTH], str[LINE_LENGTH], c;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *clienthost;
	Request* req =(Request*)malloc(sizeof(Request));

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=2){
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}
	else{
		num1=0;
		while( argv[1][num1]!= '\0' ){
			if((argv[1][num1] < '0') || (argv[1][num1] > '9')){
				printf("Secondo argomento non intero\n");
				printf("Error: %s port\n", argv[0]);
				exit(2);
			}
			num1++;
		}  	
	  	port = atoi(argv[1]);
  		if (port < 1024 || port > 65535){
		      printf("Error: %s port\n", argv[0]);
		      printf("1024 <= port <= 65535\n");
		      exit(2);  	
  		}
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER ---------------------------------- */
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;  
	servaddr.sin_port = htons(port);  

	/* CREAZIONE, SETAGGIO OPZIONI E CONNESSIONE SOCKET -------------------- */
	sd=socket(AF_INET, SOCK_DGRAM, 0);
	if(sd <0){perror("creazione socket "); exit(1);}
	printf("Server: creata la socket, sd=%d\n", sd);

	if(setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket "); exit(1);}
	printf("Server: set opzioni socket ok\n");

	if(bind(sd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket "); exit(1);}
	printf("Server: bind socket ok\n");

	/* CICLO DI RICEZIONE RICHIESTE ------------------------------------------ */
	for(;;){ // Daemon Server
		len=sizeof(struct sockaddr_in);
		if (recvfrom(sd, req, sizeof(Request), 0, (struct sockaddr *)&cliaddr, &len)<0)
		{
            perror("recvfrom "); 
            continue;
        }

        
		printf("File Name ricevuto: %s\n", req->fileName);
		clienthost=gethostbyaddr( (char *) &cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
		if (clienthost == NULL) { 
            printf("client host information not found\n");
        } 
		else{
            printf("Operazione richiesta da: %s %i\n", clienthost->h_name,(unsigned)ntohs(cliaddr.sin_port)); 
        }
        
        // Controllo che il file esista nella directory del Server
        if ((fd = open(req->fileName, O_RDONLY)) < 0) {
            perror("Il file non esiste nella mia directory!\n ");
            max = -1;
        } else {
            printf("Il file %s esiste nella mia directory!\n ", req->fileName);
            // Redirezione file verso stdin
            close(0);
            dup(fd);
            close(fd);
            
            max = 0;
            begin = clock();
//            while ((c = getchar()) != EOF ) { // lettura byte a byte
           while (read(0, &c, sizeof(char)) >0) { // lettura con read
               if (c == '\n' || c == ' ') {
                   if (ris > max) {
                     max = ris;   
                   }
                ris = 0;   
               } else {
                ris++;   
               }
             //printf("%c", c);
           }
           
        
            
        }
        
        close(fd);
		
		end = clock();
        printf("\n\nThe elapsed time is %f seconds.\n\n",  (float) (end-begin)/CLOCKS_PER_SEC);
        printf("Invio esito operazione [%d]\n", max);
        
		max=htonl(max);
		if (sendto(sd, &max, sizeof(max), 0, (struct sockaddr *)&cliaddr, len)<0)
		{perror("sendto "); continue;}
	} //for
}
