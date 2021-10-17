import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;



public class DiscoveryServer {
    //  Vecio non possiamo usare ArrayList
    //  ArrayList<Integer> ports=new ArrayList<Integer>();

    public static void main(String[] args) {
        if(args.length < 3){   //Minimo 3 argomenti: PortaDiscoveryServer file.txt PortaRowSwapServer
            System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1.txt port1 ... fileN.txt portN");
            System.exit(-1);
        }
        int port=-1;
        try {
            port = Integer.parseInt(args[0]);
            // controllo che la porta sia nel range consentito 1024-65535
            if (port < 1024 || port > 65535) {
                System.out.println("Porta fuori range");
                System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1.txt port1 ... fileN.txt portN");
                System.exit(-1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Errore intero");
            System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1.txt port1 ... fileN.txt portN");
            System.exit(-2);
        }
        System.out.println("DiscoveryServer: avviato su porta " + port);

        //  RowSwapServers
        int numCoppie = (args.length - 1)/2;    // Il numero di coppie file-porta
        String[] files = new String[numCoppie];
        int[] ports = new int[numCoppie];
        int k = 0;
        // Parto dall'argomento 1 e prendo tutte le coppie file-porta 
        for(int i = 1; i < args.length-1; i += 2, k++){
            files[k] = args[i];
            ports[k] = Integer.parseInt(args[i+1]); 
        }
        // k == numCoppie

        //  TODO Controllo che tutti file e le porte siano diversi

        //  Creazione k RowSwapServers
        RowSwapServer RSServers[] = new RowSwapServer[k];
        try {
            for(int i = 0; i < k; i++){
                //  Run di tutti i thread associati ai files
                RSServers[i] = new RowSwapServer(files[i], new DatagramSocket(ports[i]));
                RSServers[i].start();
                System.out.println("RSServer #" + i + ": avviato su porta " + ports[i] +" con file " + files[i]);
            }
        } catch (SocketException s) {
            System.out.println("Errore Socket");
        }
       
        DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

        try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("DS: creata la socket " + socket);
		}
		catch (SocketException e) {
			System.out.println("DS: Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

        try {
			String nomeFile = null;
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;

			while (true) {
				System.out.println("\nIn attesa di richieste...");
				
				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "
							+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: " + nomeFile);
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				// Risposta al Client
				try {
                    //  Cerco il file della richiesta
                    boolean found = false;
                    int i = 0;
                    while(!found && i<numCoppie){
                        found = richiesta.equals(files[i]);
                        i++;
                    }
                    
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    if(found){
                        //  Comunico al cliente la porta del RSServer
                        doStream.writeInt(ports[i]);
                        data = boStream.toByteArray();
                        packet.setData(data, 0, data.length);
                        socket.send(packet);
                    }else{
                        //  Comunico al cliente che non ho trovato la porta tramite l'intero -1
                        doStream.writeInt(-1);
                        data = boStream.toByteArray();
                        packet.setData(data, 0, data.length);
                        socket.send(packet);
                    }
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

			} // while

		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("LineServer: termino...");
		socket.close();
	}
        
    
}
