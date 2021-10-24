//  Server Sequenziale
import java.io.*;
import java.net.*;

public class ServerSeq {
    public static final int PORT = 8040; // porta default per server
    public static void main(String [] args){
        int port = -1;
        String workingDirectory = System.getProperty("user.dir");
        File wDir;

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
            while(true){
                Socket clientSocket = null;
				DataInputStream inSock = null;
				DataOutputStream outSock = null;

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

                //stream I/O e ricezione nome file
				String nomeFile;
				try {
                    inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
					nomeFile = inSock.readUTF();
		        }
				catch(SocketTimeoutException ste){
                    System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
                    .print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					continue;          
				}
				catch (IOException e) {
                    System.out
                    .println("Problemi nella creazione degli stream di input/output "
                    + "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
                System.out.println("Ho ricevuto il nomeFile: " + nomeFile);
                for (File f : wDir.listFiles()) {
                    if(nomeFile.equals(f.getName())){
                        System.out.println("Il file e' gia' presente nel direttorio");
                        outSock.writeUTF("Salta File");

                    }else{
                        continue;
                    }
                }

                Long sizeFile;
                



            }
        } catch (Exception e) {
            e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Errore irreversibile, PutFileServerSeq: termino...");
			System.exit(3);
        }

    }
}
