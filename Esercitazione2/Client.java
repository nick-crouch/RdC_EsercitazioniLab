// PutFileClient.java

import java.net.*;
import java.io.*;

public class Client {

    public static void main(String[] args) throws IOException {

        InetAddress addr = null;
        int port = -1;

        try{ //check args
            if(args.length == 2){
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
            } else{
                System.out.println("Usage: java PutFileClient serverAddr serverPort");
                System.exit(1);
            }
        } //try

        catch(Exception e){
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java PutFileClient serverAddr serverPort");
            System.exit(2);
        }

        // oggetti utilizzati dal client per la comunicazione e la lettura del file
        // locale
        Socket socket = null;
        FileInputStream inFile = null;
        DataInputStream inSock = null;
        DataOutputStream outSock = null;
        String nomeFile = null;
        long sizeMin=0;

        // creazione stream di input da tastiera
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out
                .print("MultiplePutFileClient Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");

        try{
            while ( (nomeFile=stdIn.readLine()) != null){
                // se il file esiste ed è una directory, creo la socket
                if(new File(nomeFile).isDirectory()){
                    // creazione socket
                    try{
                        socket = new Socket(addr, port);
                        socket.setSoTimeout(30000);
                        System.out.println("Creata la socket: " + socket);
                    }
                    catch(Exception e){
                        System.out.println("Problemi nella creazione della socket: ");
                        e.printStackTrace();
                        System.out
                                .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");
                        continue;
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }

                    // Chiedo all'utente la soglia minima in bytes
                    System.out.println("Inserisci dimensioni di soglia in bytes");

                    try {
                        sizeMin = Long.parseLong(stdIn.readLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Inserisci un numero valido!");
                        System.exit(-1);
                    }

                    // creazione stream di input/output su socket
                    try{
                        inSock = new DataInputStream(socket.getInputStream());
                        outSock = new DataOutputStream(socket.getOutputStream());
                    }
                    catch(IOException e){
                        System.out
                                .println("Problemi nella creazione degli stream su socket: ");
                        e.printStackTrace();
                        System.out
                                .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                        continue;
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }
                }
                // Il file inserito non esiste o non è una dir
                else{
                    System.out.println("Il File inidicato non esiste o non è una directory!");
                    System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome directory: ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                }

                // Ciclo for per controllare i file all'interno della directory
                for (File f : new File(nomeFile).listFiles()) {
                    // se il file è un direttorio, passo al file successivo
                    if (f.isDirectory()) {
                        System.out.println("File " + f.toString() + " è una directory");
                        continue;
                    }
                    // se il file dimensione minore della minima richiesta, passo al successivo
                    if (f.length() < sizeMin) {
                        System.out.println("File " + f.toString() + " al di sotto della soglia " + sizeMin + " bytes" + " [" + f.length() + " bytes]");
                        continue;
                    } else {
                        // Trasimissione del nome del file e attesa esito
                        try {
                            outSock.writeUTF(f.getName());
                            System.out.println("Inviato il nome del file " + f.getName());
                        } catch (Exception e) {
                            System.out.println("Problemi nell'invio del nome di " + f.getName() + ": ");
                            e.printStackTrace();
                            System.exit(-2);
                        }

                        String esito;
                        try{
                            esito = inSock.readUTF();
                            System.out.println("Esito trasmissione: " + esito);
                            if (esito.equalsIgnoreCase("Salta File")) {
                                // Se il file esiste già nella dir del Server [esito = "Salta file"], passo al file successivo
                                continue;
                            } else { // Esito: Attiva
                                /* Trasferimento file */
                                    // creazione stream di input da file
                                try{
                                    inFile = new FileInputStream(f); // creazione fileInputStream
                                }
                                /*
                                 * abbiamo già verificato che esiste, a meno di inconvenienti, es.
                                 * cancellazione concorrente del file da parte di un altro processo, non
                                 * dovremmo mai incorrere in questa eccezione.
                                 */
                                catch(FileNotFoundException e){
                                    System.out
                                            .println("Problemi nella creazione dello stream di input da "
                                                    + nomeFile + ": ");
                                    e.printStackTrace();
                                    System.out
                                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                                    continue;
                                }

                                System.out.println("Inizio la trasmissione di " + f.getName());

                                // Trasimissione size del file
                                try {
                                    outSock.writeUTF(String.valueOf(f.length()));
                                    System.out.println("Inviato la dimensione del file " + f.getName());
                                } catch (Exception e) {
                                    System.out.println("Problemi nell'invio della dimensione di " + f.getName()
                                            + ": ");
                                    e.printStackTrace();
                                    System.exit(-2);
                                }

                                // trasferimento file
                                try{
                                    //FileUtility.trasferisci_a_linee_UTF_e_stampa_a_video(new DataInputStream(inFile), outSock);
                                    FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(inFile), outSock);
                                    inFile.close(); 			// chiusura file

                                    System.out.println("Trasmissione di " + nomeFile + " terminata ");
                                }
                                catch(SocketTimeoutException ste){
                                    System.out.println("Timeout scattato: ");
                                    ste.printStackTrace();
                                    socket.close();
                                    System.out
                                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                                    continue;
                                }
                                catch(Exception e){
                                    System.out.println("Problemi nell'invio di " + nomeFile + ": ");
                                    e.printStackTrace();
                                    socket.close();
                                    System.out
                                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                                    continue;
                                }

                            }




                        }
                        catch(SocketTimeoutException ste){
                            System.out.println("Timeout scattato: ");
                            ste.printStackTrace();
                            socket.close();
                            System.out
                                    .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                            // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                            continue;
                        }
                        catch(Exception e){
                            System.out
                                    .println("Problemi nella ricezione dell'esito, i seguenti: ");
                            e.printStackTrace();
                            socket.close();
                            System.out
                                    .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                            continue;
                            // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                        }
                    }
                }
                System.out
                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");

            }
            // chiudo la socket in downstream
            socket.shutdownOutput(); 	// chiusura socket in upstream, invio l'EOF al server
            socket.shutdownInput();
            System.out.println("Terminata la chiusura della socket: " + socket);
            socket.close();

            System.out.println("PutFileClient: termino...");
        }
        // qui catturo le eccezioni non catturate all'interno del while
        // quali per esempio la caduta della connessione con il server
        // in seguito alle quali il client termina l'esecuzione
        catch(Exception e){
            System.err.println("Errore irreversibile, il seguente: ");
            e.printStackTrace();
            System.err.println("Chiudo!");
            System.exit(3);
        }
    } // main
} // PutFileClient

