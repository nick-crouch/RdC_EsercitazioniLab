import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class Client {

    public static void main(String[] args) throws IOException {

        /**
         * @Usage: java Client serverAddr serverPort
         */

        InetAddress addr = null;
        int port = -1;

        try{ //check args
            if(args.length == 2){
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
            } else{
                System.out.println("Usage: java Client serverAddr serverPort");
                System.exit(1);
            }
        }catch(Exception e){
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java PutFileClient serverAddr serverPort");
            System.exit(2);
        }

        /* Oggetti utilizzati dal client per la comunicazione e la lettura del file
            locale
         */
        String direttorio;
        Socket socket = null;
        FileInputStream outFile = null;
        DataInputStream inSock = null;
        DataOutputStream outSock = null;


        // Buffered reader per creazione stream di input da tastiera
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Inserisci nome del direttorio");

        while((direttorio = reader.readLine()) != null){
             File dirFile = new File(direttorio); // NON VANNO FATTE ASSEGNAZIONI DENTRO I CICLI WHILE
            // Se il file non è un direttorio, ricomincio il ciclo

            if(!dirFile.exists() ||  !dirFile.isDirectory()){
                System.out.println("Il file " + direttorio + " non è una directory o non esiste");

                System.out
                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                continue;
            }

            System.out.println("Inserisci dimensioni di soglia in bytes");
            long sizeMin=0;

            try {
                sizeMin = Long.parseLong(reader.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido");
                System.exit(-1);
            }

            try{
                //  Creazione Socket
                socket = new Socket(addr, port);
                socket.setSoTimeout(30000);
                System.out.println("Creata la socket: " + socket);
            }
            catch(Exception e){
                System.out.println("Problemi nella creazione della socket: ");
                e.printStackTrace();
                System.out
                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                continue;
                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
            }
            // Creazione stream di input/output su socket
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

            // Ciclo for per controllare i file all'interno della directory
            String esito;
            for (File f : new File(direttorio).listFiles()) {
                if(f.isDirectory()){
                    continue;
                    // se il file è un direttorio, passo al file successivo
                }
                if(f.length() < sizeMin){
                    System.out.println("File " + f.toString() + " al di sotto della soglia " + sizeMin + " bytes" + " [" + f.length() + " bytes]");
                    continue;
                }else{
                    // Trasimissione del nome del file
                    try{
                        outSock.writeUTF(f.getName());
                        System.out.println("Inviato il nome del file " + f.getName());
                    }
                    catch(Exception e){
                        System.out.println("Problemi nell'invio del nome di " + f.getName()
                                + ": ");
                        e.printStackTrace();
                        System.out
                                .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                        continue;
                    }

                    // ricezione esito
                    try{
                        esito = inSock.readUTF();
                        System.out.println("Esito trasmissione: " + esito);
                        if(esito.equalsIgnoreCase("Attiva")){
                            try{
                                //  Trasferimento file
                                outFile = new FileInputStream(f.getPath());
                                outSock.writeLong(f.length());
                                FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(outFile), outSock);
                                outFile.close(); // chiusura del file

                                System.out.println("Inviato il nome del file " + f.getName() + " di dimensioni: " + f.length());
                            }
                            catch(Exception e){
                                System.out.println("Problemi nell'invio del nome di " + f.getName()
                                        + ": ");
                                e.printStackTrace();
                                System.out
                                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                                continue;
                            }
                        }else{
                            System.out.print("Esito file " + f.getName() +": " + esito +"\nControllo file successivo.\n");
                            continue;
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
                }

            }
            System.out.println("Inserisci nome del direttorio");
        }
        socket.close(); // chiusura socket una volta che l'utente ha inviato EOF
        System.out.println("MultiplePutClient: termino...");
    }

}