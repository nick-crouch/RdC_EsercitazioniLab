
import java.io.*;
import java.net.*;

class ServerConThread extends Thread{
    private Socket clientSocket = null;
    private File directory = null;

	public ServerConThread(Socket clientSocket, File directory) {
		this.clientSocket = clientSocket;
        this.directory = directory;
	}

    public void run(){
        
		DataInputStream inSock;
		DataOutputStream outSock;
		System.out.println("ServerConThread " + this.getId() + " Avviato");
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
					for (File f : directory.listFiles()) {
						if(nomeFile.equals(f.getName())){
							System.out.println("Il file " + nomeFile + " e' già presente nel direttorio");
							found = true;
							///f.delete(); // eliminazione file
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
					}catch (Exception e) {
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
		}catch(SocketTimeoutException ste){
			System.out.println("Timeout scattato: ");
			ste.printStackTrace();
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out
					.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
			
		}catch (IOException e) {
			System.out
					.println("Problemi nella creazione degli stream di input/output "
							+ "su socket: ");
			e.printStackTrace();
			// il server continua l'esecuzione riprendendo dall'inizio del ciclo
			
		}

    }
}


public class ServerCon {
    public static final int PORT = 8040; //default port
    public static void main(String[] args) throws IOException {
        int port = -1;
        String workingDirectory = System.getProperty("user.dir");
        File wDir;

        /* controllo argomenti */
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Port argument must be in range 1024-65535");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else if(args.length>1){
				port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
					System.out.println("Usage: java ServerCon or java ServerCon port or java ServerCon workingDirectory");
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
			System.out.println("Usage: java ServerCon or java ServerCon port or java ServerCon workingDirectory");
			System.exit(1);
		}

        wDir = new File(workingDirectory);
        
        
        ServerSocket serverSocket = null;
	    Socket clientSocket = null;

	    try {
	    	serverSocket = new ServerSocket(port);
	    	serverSocket.setReuseAddress(true);
	    	System.out.println("ServerCon: avviato ");
	    	System.out.println("Server: creata la server socket: " + serverSocket);
	    }
	    catch (Exception e) {
	    	System.err
	    		.println("Server: problemi nella creazione della server socket: "
	    				+ e.getMessage());
	    	e.printStackTrace();
	    	System.exit(1);
	    }
        
	    try {
            //  Server Daemon
	    	while (true) {
	    		System.out.println("Server: in attesa di richieste...\n");

	    		try {
	    			// bloccante fino ad una pervenuta connessione
	    			clientSocket = serverSocket.accept();
	    			clientSocket.setSoTimeout(30000);
	    			System.out.println("Server: connessione accettata: " + clientSocket);
	    		}
	    		catch (Exception e) {
	    			System.err
	    				.println("Server: problemi nella accettazione della connessione: "
	    						+ e.getMessage());
	    			e.printStackTrace();
	    			continue;
	    		}

	    		// serizio delegato ad un nuovo thread
	    		try {
	    			new ServerConThread(clientSocket, wDir).start();
	    		}
	    		catch (Exception e) {
	    			System.err.println("Server: problemi nel server thread: "
	    					+ e.getMessage());
	    			e.printStackTrace();
	    			continue;
	    		}

	    	} // while
	    }
	    // qui catturo le eccezioni non catturate all'interno del while
	    // in seguito alle quali il server termina l'esecuzione
	    catch (Exception e) {
	    	e.printStackTrace();
	    	// chiusura di stream e socket
	    	System.out.println("ServerCon: termino...");
	    	System.exit(2);
	    }


    }
}
