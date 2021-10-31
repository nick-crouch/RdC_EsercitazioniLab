/* Client per richiedere l'eliminazine della riga di un file */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define DIM_BUFF 256

int main(int argc, char *argv[])
{
	int sd, port, fd_sorg, fd_dest, nread, numriga;
	char buff[DIM_BUFF];
	// FILENAME_MAX: lunghezza massima nome file. Costante di sistema.
	char nome_sorg[FILENAME_MAX+1], nome_dest[FILENAME_MAX+1];
	struct hostent *host;
	struct sockaddr_in servaddr;


	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=3){
		printf("Usage: %s serverAddress serverPort\n", argv[0]);
		exit(1);
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname(argv[1]);

	/*VERIFICA INTERO*/
	nread=0;
	while( argv[2][nread]!= '\0' ){
		if( (argv[2][nread] < '0') || (argv[2][nread] > '9') ){
			printf("Secondo argomento non intero\n");
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[2]);

	/* VERIFICA PORT e HOST */
	if (port < 1024 || port > 65535){
		printf("%s = porta scorretta...\n", argv[2]);
		exit(2);
	}
	if (host == NULL){
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}else{
		servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
		servaddr.sin_port = htons(port);
	}

	/* CORPO DEL CLIENT:
	ciclo di accettazione di richieste da utente ------- */
	printf("Inserire il nome di un file\n");
	while (gets(nome_sorg)){
		printf("File da spedire: __%s__\n", nome_sorg);

		/* Verifico l'esistenza del file */
		if((fd_sorg=open(nome_sorg, O_RDONLY))<0){
			perror("open file sorgente"); 
			printf("Qualsiasi tasto per procedere, EOF per fine: ");
			continue;
		}
		/*Verifico intero*/
		if(scanf("%d",&numriga)!=1){
			printf("Errore lettura intero");
			printf("Nome del file da spedire e numero linea, EOF per terminare: ");
			continue;
		}

		/* CREAZIONE SOCKET ------------------------------------ */
		sd=socket(AF_INET, SOCK_STREAM, 0);
		if(sd<0) {perror("apertura socket"); exit(1);}
		printf("Client: creata la socket sd=%d\n", sd);

		/* Operazione di BIND implicita nella connect */
		if(connect(sd,(struct sockaddr *) &servaddr, sizeof(struct sockaddr))<0){
			perror("connect"); exit(1);}
		printf("Client: connect ok\n");

		/*INVIO File e numero linea*/
		printf("Client: invio numero di linea\n");
		write(sd, &numriga, sizeof(int));

		printf("Client: invio file\n");
		while((nread=read(fd_sorg, &buff, DIM_BUFF))>0){
			write(sd,buff,nread);	//invio
		}
		printf("Client: file inviato\n");
		/* Chiusura socket in spedizione -> invio dell'EOF */
		shutdown(sd,1);

		/*RICEZIONE File*/
		printf("Client: ricevo e stampo alterato\n");
		while((nread=read(sd, &buff,DIM_BUFF))>0){
			write(fd_dest,&buff,nread);
			write(1,&buff,nread);
		}
		printf("Traspefimento terminato\n");
		/* Chiusura socket in ricezione */
		shutdown(sd, 0);
		/* Chiusura file */
		close(fd_sorg);
		close(fd_dest);
		close(sd);

		printf("Nome del file da ordinare, EOF per terminare: ");
	}//while
	printf("\nClient: termino...\n");
	exit(0);
	
}

