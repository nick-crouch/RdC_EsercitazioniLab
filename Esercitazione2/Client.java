import java.io.BufferedReader;

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
        direttorio = reader.readLine();
        
        if(!(new File(direttorio).exists())){
            System.out.println("Direttorio inesistente");
        }
        System.out.println("Inserisci dimensioni di soglia");
        try {
            int sizeMin = Integer.parseInt(reader.readLine());
        } catch (NumberFormatException e) {
            
        }
        


        // Ciclo for per controllare i file all'interno della directory


    }
}