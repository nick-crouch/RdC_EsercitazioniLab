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
import LineServer;

public class DiscoveryServer {
    ArrayList<Integer> ports=new ArrayList<Integer>();

    public static void main(String[] args) {
        int port=-1
        if(args.length==0){
            port=3596;
        }
        else if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java LineServer [serverPort>1024]");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.out.println("Usage: java LineServer [serverPort>1024]");
                System.exit(1);
            }
        } else {
            System.out.println("Usage: java LineServer [serverPort>1024]");
            System.exit(1);
        }
    }
    String newFile;
    RowSwapServer rowSwapServer=new RowSwapServer(newFile,new DatagramSocket(port));
}
