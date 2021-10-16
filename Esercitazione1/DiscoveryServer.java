import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class DiscoveryServer {
    //  Vecio non possiamo usare ArrayList
    //  ArrayList<Integer> ports=new ArrayList<Integer>();

    public static void main(String[] args) {
        int port=-1;
        if(args.length < 3){   //Minimo 3 argomenti: PortaDiscoveryServer file.txt PortaRowSwapServer
            System.out.println("Usage: java DiscoveryServer [serverPort>1024] file1.txt port1 ... fileN.txt portN");
            System.exit(-1);
        }
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
        //  RowSwapServers
        int numCoppie = (args.length - 1)/2;    // Il numero di coppie file-porta
        String[] files = new String[numCoppie];
        int[] ports = new int[numCoppie];
        int k = 0;
        // Parto dall'argomento 1 e prendo tutte le coppie file-porta 
        for(int i = 1; i < args.length-1; i =+ 2, k++){
            files[k] = args[i];
            ports[k] = Integer.parseInt(args[i+1]); 
        }
        // k == numCoppie
        //  Creazione k RowSwapServers
        RowSwapServer RSServers[] = new RowSwapServer[k];
        try {
            for(int i = 0; i < k; i++){
                //  Run di tutti i thread associati ai files
                RSServers[i] = new RowSwapServer(files[i], new DatagramSocket(ports[k]));
                RSServers[i].start();
            }
        } catch (SocketException s) {
            System.out.println("Errore Socket");
        }
        // TODO Risposta al Cliente
    }
}
