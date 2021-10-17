import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class SwapClient {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        InetAddress inetAddress=InetAddress.getByName(args[0]);
        int port=Integer.parseInt(args[1]);
        String fileName=args[2];
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        byte[] buf = new byte[256];

        // creazione della socket datagram, settaggio timeout di 30s
        // e creazione datagram packet
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(30000);
            packet = new DatagramPacket(buf, buf.length, inetAddress, port);
            System.out.println("\nSwapClient: avviato");
            System.out.println("Creata la socket: " + socket);
        } catch (SocketException e) {
            System.out.println("Problemi nella creazione della socket: ");
            e.printStackTrace();
            System.out.println("SwapClient: interrompo...");
            System.exit(1);
        }
        //
        //Richiesta al DS Server
        try {
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            byte[] data = null;
            String richiesta = null;
            String risposta = null;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            richiesta = fileName;

                // riempimento e invio del pacchetto
                try {
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    doStream.writeUTF(richiesta);
                    data = boStream.toByteArray();
                    packet.setData(data);
                    socket.send(packet);
                    System.out.println("Richiesta inviata a " + inetAddress + ":" + port);
                } catch (IOException e) {
                    System.out.println("Problemi nell'invio della richiesta: ");
                    e.printStackTrace();
                    System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");

                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                }


                //risposta DS Server
                try {
                    // settaggio del buffer di ricezione
                    packet.setData(buf);
                    socket.receive(packet);
                    // sospensiva solo per i millisecondi indicati, dopodich� solleva una
                    // SocketException
                } catch (IOException e) {
                    System.out.println("Problemi nella ricezione del datagramma: ");
                    e.printStackTrace();
                    System.out
                            .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                }

                // analisi risposta DS Server --> porta RS Server
                int portRS=-1;
                try {
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);
                    risposta = diStream.readUTF();
                    System.out.println("Risposta: " + risposta);
                    portRS=Integer.parseInt(risposta);
                    if(portRS<=1024 || portRS>65535){
                        System.out.println("Errore porta");
                    }
                } catch (IOException e) {
                    System.out.println("Problemi nella lettura della risposta: ");
                    e.printStackTrace();
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                }

                catch(NumberFormatException nbe){
                    System.out.println("Problemi nella porta: ");
                    nbe.printStackTrace();
                }

            //


            //creazione socket con RS Server
            try {
                socket = new DatagramSocket();
                socket.setSoTimeout(30000);
                packet = new DatagramPacket(buf, buf.length, inetAddress, portRS);
                System.out.println("\nSwapClient: avviato");
                System.out.println("Creata la socket: " + socket);
            } catch (SocketException e) {
                System.out.println("Problemi nella creazione della socket: ");
                e.printStackTrace();
                System.out.println("SwapClient: interrompo...");
                System.exit(1);
            }
            //


            // Richiesta RS Server scambio righe
            // Siamo bloccati al punto di chiedere il numero di righe da scambiare
            int linea1=-1;
            int linea2=-1;
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out
                    .print("\nScrivere il numero di righe da scambiare separate da spazio: ");

            try{
                StringTokenizer st=new StringTokenizer(stdIn.readLine()," ");
                linea1 = Integer.parseInt(st.nextToken());
                linea2 = Integer.parseInt(st.nextToken());
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }


            //creazione pacchetto con i due numeri di riga da inviare a RS Server
            // invio pacchetto a RS Server
            DatagramPacket packetRS = null;
            DatagramSocket socketRS =null;
            try {
                socketRS = new DatagramSocket();
                socketRS.setSoTimeout(30000);
                packetRS = new DatagramPacket(buf, buf.length, inetAddress, portRS);
                System.out.println("\nRSS interrogato");
                System.out.println("Creata la socket: " + socketRS);
            } catch (SocketException e) {
                System.out.println("Problemi nella creazione della socket: ");
                e.printStackTrace();
                System.out.println("SwapClient: interrompo...");
                System.exit(1);
            }
            richiesta = linea1 +" "+linea2;

            try {
                boStream = new ByteArrayOutputStream();
                doStream = new DataOutputStream(boStream);
                doStream.writeUTF(richiesta);
                data = boStream.toByteArray();
                packetRS.setData(data);
                socketRS.send(packetRS);
                System.out.println("Richiesta inviata a " + inetAddress + ":" + portRS);
            } catch (IOException e) {
                System.out.println("Problemi nell'invio della richiesta: ");
                e.printStackTrace();
                System.out
                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");

                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
            }


            // ricezione risposta
            //attendo risposta e la metto in buf
            int esito=-1;
            try {
                // settaggio del buffer di ricezione
                packetRS.setData(buf);
                socketRS.receive(packetRS);
                // sospensiva solo per i millisecondi indicati, dopodich� solleva una
                // SocketException
            } catch (IOException e) {
                System.out.println("Problemi nella ricezione del datagramma: ");
                e.printStackTrace();
                System.out
                        .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
            }

            //ricezione risposta --> inserisco in esito la risposta
            try {
                biStream = new ByteArrayInputStream(packetRS.getData(), 0, packetRS.getLength());
                diStream = new DataInputStream(biStream);
                esito = Integer.parseInt(diStream.readUTF());
                
                //controllo sull'esito
                if(esito> 0){
                    System.out.println("Risposta: " + esito);

                }
            } catch (IOException e) {
                System.out.println("Problemi nella lettura della risposta: ");
                e.printStackTrace();
                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
            }
            catch(NumberFormatException nbe){
                System.out.println("Problemi nella porta: ");
                nbe.printStackTrace();
            }


            } // while
        // qui catturo le eccezioni non catturate all'interno del while
        // in seguito alle quali il client termina l'esecuzione
        catch (Exception e) {
            e.printStackTrace();

    }}
}
