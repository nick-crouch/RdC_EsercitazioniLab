#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256

int main(int argc, char* argv[]){
	int fd, readValues, bytes_to_write, written, righe, i, dim;
	char *file_out, *ret;
	char riga[MAX_STRING_LENGTH], buf[MAX_STRING_LENGTH];
	
	//controllo numero argomenti
	if (argc != 2){ 
		perror(" numero di argomenti sbagliato"); exit(1);
	} 
	
	file_out = argv[1];	
    fd = open(file_out, O_WRONLY|O_CREAT|O_TRUNC, 00640);
	if (fd < 0){
		perror("P0: Impossibile creare/aprire il file");
		exit(2);
	}
	
	do {
	printf("Inserisci riga\n");
    ret = gets(buf); // consumare il fine linea
    if (ret) 
    {
        dim = strlen(buf);
        buf[dim] = "\n";
        buf[dim+1] = "\0";
        
        written = write(fd, buf, dim+1);
    }
    } while (ret);
	close(fd);
}
