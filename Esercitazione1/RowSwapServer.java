import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.StringTokenizer;

public class RowSwapServer extends Thread {
    private String nomeFile;
    //private DatagramSocket socket;
    private int port;
    DatagramPacket packet = null;

    byte[] buf = new byte[256];

    public RowSwapServer(String nomeFile, int port) {
        this.port = port;
        this.nomeFile = nomeFile;
        //  port=port;
    }

    @Override
    public void run() {
        try {
            int indexLine1 = -1, indexLine2 = -1, esito = -1, clientPort = -1;
            DatagramSocket socket = new DatagramSocket(port);
            InetAddress clientAddress = null;
            byte[] data = null;
            String richiesta = null;
            String fileOut=nomeFile;
            while (true) {
                try {
                    data = new byte[256];
                    packet = new DatagramPacket(data, data.length);
                    System.out.println("RowSwap: " + this.getName() + " in attesa");
                    socket.receive(packet);

                    System.out.println("\nricevuto pacchetto");
                    clientPort = packet.getPort();
                    clientAddress = packet.getAddress();
                    packet.setAddress(clientAddress);
                    packet.setPort(clientPort);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
                    richiesta= dataInputStream.readUTF();

                    System.out.println("\n"+richiesta);
                    StringTokenizer st=new StringTokenizer(richiesta," ");
                    indexLine1=Integer.parseInt(st.nextToken());
                    indexLine2=Integer.parseInt(st.nextToken());
                    System.out.println(indexLine1 +" "+indexLine2);
            /*
            try {
                buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);
                System.out.println("RowSwap: " + this.getName() + " in attesa");
                socket.receive(packet);

                clientPort = packet.getPort();
                clientAddress = packet.getAddress();
                packet.setAddress(clientAddress);
                packet.setPort(clientPort);
                System.out.println("RowSwap: " + this.getName() + " ricevuto pacchetto da Client");

                packet.setData(buf);
                socket.receive(packet);

            } catch (IOException e) {
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
                st = new StringTokenizer(richiesta, " ");
                indexLine1 = Integer.parseInt(st.nextToken());
                indexLine2 = Integer.parseInt(st.nextToken());
                System.out.println("Richiesta linea " +indexLine1 +" "+indexLine2);
            } catch (Exception e) {
                System.err.println("Problemi nella lettura della richiesta: "
                        +indexLine1 + " " + indexLine2);
                e.printStackTrace();
                continue;
                // il server continua a fornire il servizio ricominciando dall'inizio
                // del ciclo
            }*/
                    try {
                        FileReader fileReader = null;
                        FileWriter fileWriter = null;
                        BufferedReader bf = null;
                        String[] linee = null;
                        try {
                            fileReader = new FileReader(nomeFile);
                            fileWriter = new FileWriter(fileOut);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        ;
                        bf = new BufferedReader(fileReader);
                        bf.mark(0);
                        //  Conteggio dimensione necessaria per inizializzare l'array
                        int numLinee =0;
                        linee = Arrays.stream(bf.lines().toArray()).toArray(String[]::new);
                        //linee= Arrays.copyOf(bf.lines(),bf.lines().count(),String[].class);
                        if (indexLine1 <= 0 || indexLine1 > linee.length) {
                            System.out.println("Errore");
                        }
                        if (indexLine2 <= 0 || indexLine2 > linee.length) {
                            System.out.println("Errore");
                        }
                        //System.out.println(bf.readLine());
                        /*while ((linea = bf.readLine()) != null) {
                            linee[j]=linea;
                            System.out.println(linea);
                            System.out.println(linee[j]);
                            j++;
                        }*/
                        //  Scambio
                        String temp;
                        temp = linee[indexLine1];
                        linee[indexLine1] = linee[indexLine2];
                        linee[indexLine2] = temp;
                      //  bf.close();
                        //  Scrittura file di output
                        for (String linea : linee) {
                            fileWriter.write(linea+"\n");
                        }

                        //  Stampa ad output
                /*boStream = new ByteArrayOutputStream();
                doStream = new DataOutputStream(boStream);
                for (int i = 0; i<linee.length; i++) {
                    doStream.writeChars(linee[i]);
                    data = boStream.toByteArray();
                    packet.setData(data, 0, data.length);
                    socket.send(packet);
                }*/


                        bf.close();
                        fileReader.close();
                        fileWriter.close();
                        System.out.println("Success");
                        esito=1;
                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                    //  Risposta al Client
                    ByteArrayOutputStream boStream=null;
                    DataOutputStream doStream=null;
                    try {
                        boStream = new ByteArrayOutputStream();
                        doStream = new DataOutputStream(boStream);
                        doStream.writeInt(esito);
                        data = boStream.toByteArray();
                        packet.setData(data);
                        socket.send(packet);
                        System.out.println("Risposta inviata a " + packet.getAddress() + ":" + port);
                    } catch (IOException e) {
                        System.out.println("Problemi nell'invio della richiesta: ");
                        e.printStackTrace();
                        System.out
                                .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");

                        // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    }
                }catch(Exception e){

                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
