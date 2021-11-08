#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define DIM_BUFF 100
#define STRING_LENGTH 256
#define LENGTH_FILE_NAME 20
#define max(a,b) ((a) > (b) ? (a) : (b))

typedef struct{
    char nome_dir[LENGTH_FILE_NAME];
	char nome_file[LENGTH_FILE_NAME];
    char parola[STRING_LENGTH];

}Request;

/*Funzione conteggio file in un direttorio*/
/********************************************************/
int conta_file (char *name){
	DIR *dir;
	struct dirent * dd;
	int count = 0;
	dir = opendir (name);
	if (dir==NULL) return -1;
	while ((dd = readdir(dir)) != NULL){
		printf("Trovato il direttorio %s\n", dd-> d_name);
		count++;
	}
	/*Conta anche direttorio stesso e padre*/
	printf("Numero totale di file %d\n", count);
	closedir (dir);
	return count;
}
/********************************************************/

void gestore(int signo){
	int stato;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}
/********************************************************/

int main(int argc, char **argv){
	int  listenfd, connfd, udpfd, fd_file, nready, maxfdp1;
	const int on = 1;
	char buff[DIM_BUFF], nome_file[LENGTH_FILE_NAME], n, nome_dir[LENGTH_FILE_NAME],nome_dir_livello_2[LENGTH_FILE_NAME];
    char *fn, *word;
    char zero=0;
	fd_set rset;
	int len, nread, nwrite, num = 0, ris, port;
	struct sockaddr_in cliaddr, servaddr;
    Request* req =(Request*)malloc(sizeof(Request));
    FILE *fp;


	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=2){
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}

	nread = 0;
	while (argv[1][nread] != '\0'){
		if ((argv[1][nread] < '0') || (argv[1][nread] > '9')){
			printf("Terzo argomento non intero\n");
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[1]);
	if (port < 1024 || port > 65535){
		printf("Porta scorretta...");
		exit(2);
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER E BIND ---------------------------- */
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);
	printf("Server avviato\n");

	/* CREAZIONE SOCKET TCP ------------------------------------------------ */
	listenfd=socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd <0){perror("apertura socket TCP "); exit(1);}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);

	if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket TCP"); exit(2);}
	printf("Set opzioni socket TCP ok\n");

	if (bind(listenfd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket TCP"); exit(3);}
	printf("Bind socket TCP ok\n");

	if (listen(listenfd, 5)<0){perror("listen"); exit(4);}
	printf("Listen ok\n");

	/* CREAZIONE SOCKET UDP ------------------------------------------------ */
	udpfd=socket(AF_INET, SOCK_DGRAM, 0);
	if(udpfd <0)
	{perror("apertura socket UDP"); exit(5);}
	printf("Creata la socket UDP, fd=%d\n", udpfd);

	if(setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket UDP"); exit(6);}
	printf("Set opzioni socket UDP ok\n");

	if(bind(udpfd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket UDP"); exit(7);}
	printf("Bind socket UDP ok\n");

	/* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE -------------------------------- */
	signal(SIGCHLD, gestore);

	/* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
	FD_ZERO(&rset);
	maxfdp1=max(listenfd, udpfd)+1;

	/* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
	for(;;){
		FD_SET(listenfd, &rset);
		FD_SET(udpfd, &rset);

		if ((nready=select(maxfdp1, &rset, NULL, NULL, NULL))<0){
			if (errno==EINTR) continue;
			else {perror("select"); exit(8);}
		}

		/* GESTIONE RICHIESTE DI GET DI UN FILE ------------------------------------- */
		/*if (FD_ISSET(listenfd, &rset)){
			printf("Ricevuta richiesta di conteggio file\n");
			len=sizeof(struct sockaddr_in);
			if (recvfrom(listenfd, &nome_dir, sizeof(nome_dir), 0, (struct sockaddr *)&cliaddr, &len)<0)
			{perror("recvfrom"); continue;}*/

         
         if (FD_ISSET(listenfd, &rset)){
			printf("Ricevuta richiesta di get di un file\n");
			len = sizeof(struct sockaddr_in);
			if((connfd = accept(listenfd,(struct sockaddr *)&cliaddr,&len))<0){
				if (errno==EINTR) continue;
				else {perror("accept"); exit(9);}
			}
			
			if (fork()==0){ /* processo figlio che serve la richiesta di operazione */
                
				close(listenfd);
                int fd_dir;
                int count=0;
                char dir_element[DIM_BUFF];
                char *nestedFile;
                DIR *dir, *dir2;
                struct dirent * dd, * dd2;
                
				printf("Dentro il figlio, pid=%i\n", getpid());

				for (;;){
					if (read(connfd, &nome_dir, sizeof(nome_dir))<=0)
				{ perror("read"); break; }

				printf("Richiesto direttorio %s\n", nome_dir);
				dir=opendir(nome_dir);
                int len_dir=strlen(nome_dir);
                if(dir!=NULL){
//                     write(connfd,"S",1);
                    while((dd=readdir(dir))!=NULL){
//                         printf("%s\n",dd->d_name);
                       if((strcmp(dd->d_name,".")!=0) && (strcmp(dd->d_name,"..")!=0))
                        {   
//                             printf("%s\n",dd->d_name);
                            if(dd->d_type == DT_DIR){
//                                 printf("opendir\n");
                                strcpy(nome_dir_livello_2,nome_dir);
                                strcat(nome_dir_livello_2,"/");
                                strcat(nome_dir_livello_2, dd->d_name);
                                printf("Apro directory: %s\n",nome_dir_livello_2);
                                dir2=opendir(nome_dir_livello_2);
                                if(dir2!=NULL){
                                    printf("Secondo livello:\n");
                                    while((dd2=readdir(dir2))!=NULL){
                                    if((strcmp(dd2->d_name,".")!=0) && (strcmp(dd2->d_name,"..")!=0)){
//                                         printf("\t%s\n",dd2->d_name);
                                        if(dd2->d_type==DT_DIR){
                                            
                                            printf("Trovata directory %s\n",dd2->d_name);
                                        }
                                        else{
                                            nestedFile= dd2->d_name;
                                            printf("Trovato file %s\n",dd2->d_name);
                                            printf("Leggo e invio il file richiesto\n");
                                            write(connfd,"S",1);
                                            /*fd_file= open(dd2->d_name, O_RDONLY);
                                            while((nread=read(fd_file, buff, sizeof(buff)))>0){
                                            if ((nwrite=write(connfd, buff, nread))<0)
                                            {perror("write"); break;}
                                            }*/
                                            write(connfd, nestedFile, strlen(nestedFile)+1);
                                            
                                            /* invio al client segnale di terminazione: zero binario */
                                            write(connfd, &zero, 1);
                                            close(fd_file);
                                            
                                        }
                                //strcpy(buff,dd2->d_name);
                                //write(connfd,buff,sizeof(buff);
//                                     }
                                
                                    }
                                }
                                 
                            }
                                else{
                                    printf("Errore\n");
                                     write(connfd,"N",1);
                                }
                                closedir(dir2);
                            }
                            else{
                                printf("File %s\n",dd->d_name);
                                continue;
                                
                            }
                        }
                    }
                    closedir(dir);
                }
            
				}//for
				printf("Figlio %i: chiudo connessione e termino\n", getpid());
				close(connfd);
				exit(0);
		} }/* fine gestione richieste di file */

		/* GESTIONE RICHIESTE DI ELIMINAZIONE PAROLA ------------------------------------------ */
		if (FD_ISSET(udpfd, &rset)){
			printf("Ricevuta richiesta di eliminazione parola su file\n");

			len=sizeof(struct sockaddr_in);
            if (recvfrom(udpfd, req, sizeof(Request), 0, (struct sockaddr *)&cliaddr, &len)<0)
			{perror("recvfrom"); continue;}
			
			printf("Nome file:%s\n", req->nome_file);

			printf("Richiesta eliminazione occorrenze di %s nel file %s\n",  req->parola,req->nome_file);
            
            /* ALGORITMO ELIMINA OCCORRENZE */
            int fd;
            int res = 0;
            char word[DIM_BUFF];
            char punt[] = ".;,:!?";
            char temp[DIM_BUFF];
            int n;
            
            if ((fp = fopen(req->nome_file, "r+")) == NULL) {
                printf("Errore nell'apertura del file");
                num = -1;
                }
            
            
            while ((n=fscanf(fp, "%s", &word)) != EOF) {
                // TODO: scanf eliminando la punteggiatura [. , : ;]
                
                 printf("%s\n", temp);
                 //printf("%s\n", word);
                 if (strcmp(temp, req->parola) == 0) {
               // if (strcmp(word, req->parola) == 0) {
                    num++;
                } else {
                // TODO: eliminazione parole che corrispondono a quella inserita
                 //fwrite(word, strlen(word), 1, fp);
                }
            }
                fclose(fp);
        
    
			
            
			printf("Risultato del conteggio: %i\n", num);

			/*
			* Cosa accade se non commentiamo le righe di codice qui sotto?
			* Cambia, dal punto di vista del tempo di attesa del client,
			* l'ordine col quale serviamo le due possibili richieste?
			* Cosa cambia se utilizziamo questa realizzazione, piuttosto
			* che la prima?
			*
			*/
			/*
			printf("Inizio sleep\n");
			sleep(30);
			printf("Fine sleep\n");*/
            
			ris=htonl(num);
			if (sendto(udpfd, &ris, sizeof(ris), 0, (struct sockaddr *)&cliaddr, len)<0)
			{perror("sendto"); continue;}
		} /* fine gestione richieste di conteggio */

	} /* ciclo for della select */
	/* NEVER ARRIVES HERE */
	exit(0);
}
