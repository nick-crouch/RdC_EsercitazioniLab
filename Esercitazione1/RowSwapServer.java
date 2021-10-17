import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.StringTokenizer;

public class RowSwapServer extends Thread{
    private String nomeFile;
    private DatagramSocket socket;
    DatagramPacket packet = null;

    byte[] buf = new byte[256];

    public RowSwapServer(String nomeFile,DatagramSocket socket) {
        this.socket=socket;
        this.nomeFile=nomeFile;
    }

    public void Run(){
        while(true) {
            int indexLine1 = -1;
            int indexLine2 = -1;
            String richiesta = null;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            StringTokenizer st = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            String linea = "";
            byte[] data = null;
            try {
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
                st = new StringTokenizer(richiesta);
                indexLine1 = Integer.parseInt(st.nextToken());
                indexLine2 = Integer.parseInt(st.nextToken());
                System.out.println("Richiesta linea " +indexLine1);
            } catch (Exception e) {
                System.err.println("Problemi nella lettura della richiesta: "
                        +indexLine1 + " " + indexLine2);
                e.printStackTrace();
                continue;
                // il server continua a fornire il servizio ricominciando dall'inizio
                // del ciclo
            }
            try {
                FileReader fileReader = null;
                FileWriter fileWriter = null;
                BufferedReader bf=null;
                String[] linee = null;
                try{
                    fileReader=new FileReader(nomeFile);
                    fileWriter = new FileWriter("output.txt");
                }
                catch(IOException e){
                    System.out.println(e);
                }
                try{
                    bf=new BufferedReader(fileReader);
                }
                catch(Exception e){
                    System.out.println(e);
                }
                //  Conteggio dimensione necessaria per inizializzare l'array
                int numLinee = 0;
                while ((linea=bf.readLine())!=null){
                    numLinee++;
                }
                linee = new String[numLinee];
                if(indexLine1<=0 ||indexLine1>numLinee){
                    System.out.println("Errore");
                }
                if(indexLine2<=0 || indexLine2>numLinee){
                    System.out.println("Errore");
                }

                //  Scambio
                String temp;
                temp = linee[indexLine1];
                linee[indexLine1] = linee[indexLine2];;
                linee[indexLine2] = temp;

                //  Scrittura file di output
                for(int i = 0; i < linee.length; i++) {
                    fileWriter.write(linee[i]);
                }

                //  Stampa ad output
                boStream = new ByteArrayOutputStream();
                doStream = new DataOutputStream(boStream);
                for (int i = 0; i<linee.length; i++) {
                    doStream.writeChars(linee[i]);
                    data = boStream.toByteArray();
                    packet.setData(data, 0, data.length);
                    socket.send(packet);
                }


                bf.close();
                fileReader.close();
                fileWriter.close();
                System.out.println("Success");
            }
            catch (IOException e) {
                
                e.printStackTrace();
            }

            //  Risposta al Client
        }
    }

}
