package esercizio0Reti;

import java.io.*;

public class Consumatore {
	public static void main(String[] args) {
		FileReader r = null;
		char ch;
		String str=null;
		int x;
		
		if (args.length != 1 && args.length!=2){
			System.out.println("Utilizzo: consumatore carattere [<inputFilename>]");
			System.exit(0);
		}
		
		str=args[0];
	  
		try {
			if(args.length==2)
				r = new FileReader(args[1]);
			else 
				r = new FileReader(FileDescriptor.in);
		} catch(FileNotFoundException e){
			System.out.println("File non trovato");
			System.exit(1);
		}
		try {

			while ((x = r.read()) >= 0) { 
				ch = (char) x;
				if(!str.contains(ch+""))
					System.out.print(ch);
			}
			r.close();
		} catch(IOException ex){
			System.out.println("Errore di input");
			System.exit(2);
		}
}
}