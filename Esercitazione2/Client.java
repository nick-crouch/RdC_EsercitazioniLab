import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class Client {

    public static void main(String[] args) throws IOException {
    
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

        }

        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String direttorio;
        System.out.println("Inserisci nome del direttorio");
        while((direttorio = reader.readLine()) != null){
            File dirFIle = new File(direttorio);
            if(dirFIle.exists() && !dirFIle.isDirectory()){
                System.out.println("Il file " + direttorio + "non e' una directory o non esiste");
                continue;
            }
            
            System.out.println("Inserisci dimensioni di soglia in B");
            long sizeMin=0;
            try {
                sizeMin = Long.parseLong(reader.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido");
                System.exit(-1);
            }
    
            //  Creazione Socket
            Socket socket = null;
            FileInputStream outFile = null;
            DataInputStream inSock = null;
            DataOutputStream outSock = null;
    
            try{
                socket = new Socket(addr, port);
                socket.setSoTimeout(30000);
                System.out.println("Creata la socket: " + socket);
            }
            catch(Exception e){
                System.out.println("Problemi nella creazione della socket: ");
                e.printStackTrace();
                System.out
                    .print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome file: ");
                //System exit?
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
                
                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
            }
            
            // Ciclo for per controllare i file all'interno della directory
            String esito;
            for (File f : dirFIle.listFiles()) {
                if(f.isDirectory()){
                    continue;
                }
                if(f.length() < sizeMin){
                    System.out.println("File " + f.toString() + "di sotto della soglia " + sizeMin);
                    continue;
                }else{
                    try{
                        outSock.writeUTF(f.getName());
                        outSock.writeLong(f.length());
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
                    // ricezione esito
                    try{
                        esito = inSock.readUTF();
                        System.out.println("Esito trasmissione: " + esito);
                        if(esito.equalsIgnoreCase("Attiva")){
                            try{
                                //  Invio file
                                outFile = new FileInputStream(f.getName());
                                FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(outFile), outSock);
                                outFile.close();
                                
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
        }
        
}