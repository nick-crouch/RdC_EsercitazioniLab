//  Server Sequenziale
import java.io.*;
import java.net.*;

public class ServerSeq {
    public static final int PORT = 54321; // porta default per server

    public static void main(String [] args){
        int port = -1;
        String workingDirectory = System.getProperty("user.dir");
        File wDir = null;

        /* controllo argomenti */
        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
                    System.exit(1);
                }
            } else if (args.length == 0) {
                port = PORT;
            } else if(args.length>1){
                port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
                    System.exit(1);
                }
                workingDirectory = args[1];
                wDir = new File(workingDirectory);
                if(!wDir.isDirectory()){
                    System.out.println("Il parametro " + workingDirectory + " non e' un direttorio");
                    System.exit(-1);
                }
            }
        } //try
        catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out
                    .println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
            System.exit(1);
        }

        wDir = new File(workingDirectory);

        /* preparazione socket e in/out stream */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("ServerSeq: avviato ");
            System.out.println("Creata la server socket: " + serverSocket);
        }
        catch (Exception e) {
            System.err.println("Problemi nella creazione della server socket: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }

        try {
            // Server Daemon
            while(true){
                Socket clientSocket = null;
                DataInputStream inSock = null;
                DataOutputStream outSock = null;

                System.out.println("\nIn attesa di richieste...");
                try {
                    clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(30000); //timeout altrimenti server sequenziale si sospende
                    System.out.println("Connessione accettata: " + clientSocket + "\n");
                }
                catch (SocketTimeoutException te) {
                    System.err
                            .println("Non ho ricevuto nulla dal client per 30 sec., interrompo "
                                    + "la comunicazione e accetto nuove richieste.");
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    continue;
                }
                catch (Exception e) {
                    System.err.println("Problemi nella accettazione della connessione: "
                            + e.getMessage());
                    e.printStackTrace();
                    // il server continua a fornire il servizio ricominciando dall'inizio
                    // del ciclo, se ci sono stati problemi
                    continue;
                }

                try {
                    inSock = new DataInputStream(clientSocket.getInputStream());
                    outSock = new DataOutputStream(clientSocket.getOutputStream());
                    String nomeFile;
                    //  Finche' il cliente e' connesso
                    while (clientSocket.isConnected()) {
                        nomeFile = inSock.readUTF();
                        //elaborazione e comunicazione esito
                        FileOutputStream outFile = null;
                        //  Dead Code
                    
                        if (nomeFile == null) {
                            System.out.println("Problemi nella ricezione del nome del file: ");
                            clientSocket.close();
                            continue;
                        
                        } else {
                            System.out.println("Ho ricevuto il nomeFile: " + nomeFile);
                            boolean found = false;
                            // controllo su file
                            for (File f : wDir.listFiles()) {
                                if(nomeFile.equals(f.getName())){
                                    System.out.println("Il file " + nomeFile + " e' già presente nel direttorio");
                                    found = true;
                                    break;
                                }else{
                                    continue;
                                }
                            }
                            if(found){
                                outSock.writeUTF("Salta File");
                            }else{
                                System.out.println("Il file " + nomeFile + " non e' presente nel direttorio");
                                outSock.writeUTF("Attiva");
                            }
                            
                            outFile = new FileOutputStream(nomeFile);
        
                            long sizeFile;
                            // ricezione del file e dimensione
                            try {
                                // ricezione dimensione
                                sizeFile = Long.parseLong(inSock.readUTF());
        
        
                                System.out.println("Ricevo il file " + nomeFile + ": [" + sizeFile + " bytes] \n");
                                byte[] rBytes = new byte[(int)sizeFile];
                                inSock.read(rBytes, 0, (int)sizeFile);
                                outFile.write(rBytes);
                                /**
                                    FileUtility.trasferisci_a_byte_file_binario(inSock,
                                            new DataOutputStream(outFile));
                                */
                                System.out.println("\nRicezione del file " + nomeFile
                                        + " terminata\n");
                                outFile.close();				// chiusura file
                            }catch(SocketTimeoutException ste){
                                    System.out.println("Timeout scattato: ");
                                    ste.printStackTrace();
                                    clientSocket.close();
                                    System.out
                                    .print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                                    continue;
                                }
                                catch (Exception e) {
                                    System.err
                                    .println("\nProblemi durante la ricezione e scrittura del file: "
                                    + e.getMessage());
                                    e.printStackTrace();
                                    clientSocket.close();
                                    System.out.println("Terminata connessione con " + clientSocket);
                                    continue;
                                }
                        }//Fine Else
                    }//Fine While
                    
                    clientSocket.shutdownInput();	//chiusura socket (downstream)
                    // ritorno esito positivo al client
                    outSock.writeUTF( "File salvato lato server");
                    clientSocket.shutdownOutput();	//chiusura socket (upstream)
                    System.out.println("\nTerminata connessione con " + clientSocket);
                    clientSocket.close();
                    
                   //Fine Try 
                }catch(SocketTimeoutException ste){
                    System.out.println("Timeout scattato: ");
                    ste.printStackTrace();
                    clientSocket.close();
                    System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    continue;
                }catch (IOException e) {
                    System.out
                            .println("Problemi nella creazione degli stream di input/output "
                                    + "su socket: ");
                    e.printStackTrace();
                    // il server continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                }
                        
                
                

            }// Fine While Daemon
           //Fine Try Daemon 
        } catch (Exception e) {
        e.printStackTrace();
        // chiusura di stream e socket
        System.out.println("Errore irreversibile, PutFileServerSeq: termino...");
        System.exit(3);
        }
    }
}