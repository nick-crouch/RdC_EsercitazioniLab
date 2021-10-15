import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
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
            //String nomeFile = null;
            int numLinea1 = -1;
            int numLinea2 = -1;
            String richiesta = null;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream = null;
            StringTokenizer st = null;
            ByteArrayOutputStream boStream = null;
            DataOutputStream doStream = null;
            String linea = null;
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
                // nomeFile = st.nextToken();
                numLinea1 = Integer.parseInt(st.nextToken());
                numLinea2 = Integer.parseInt(st.nextToken());
                System.out.println("Richiesta linea " + numLinea1);
            } catch (Exception e) {
                System.err.println("Problemi nella lettura della richiesta: "
                        + numLinea1 + " " + numLinea2);
                e.printStackTrace();
                continue;
                // il server continua a fornire il servizio ricominciando dall'inizio
                // del ciclo
            }
        }
        ScambiaRighe(fileIn,"output.txt",numLinea1,numLinea2);
    }

    public void ScambiaRighe(String fileIn,String fileOut,int riga1,int riga2) throws IOException {
        FileReader fileReader = null;
        int countRighe=1;
        FileWriter fileWriter = null;
        BufferedReader bf=null;
        ArrayList<String> linee=new ArrayList<String>();
        char x;
        try{
            fileReader=new FileReader(fileIn);
            fileWriter = new FileWriter(fileOut);
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
        String linea;
        while ((linea=bf.readLine())!=null){
            linee.add(linea);
        }
        countRighe=linee.size();
        if(riga1<=0 || riga1>countRighe){
            System.out.println("Errore");
        }
        if(riga2<=0 || riga2>countRighe){
            System.out.println("Errore");
        }
        String line1=linee.get(riga1);
        String line2=linee.get(riga2);

        // scambio
        linee.set(riga2,line1);
        linee.set(riga1,line2);
        for(String l : linee) {
            fileWriter.write(l);
        }
        bf.close();
        fileReader.close();
        fileWriter.close();
        System.out.println("Success");
    }
}
