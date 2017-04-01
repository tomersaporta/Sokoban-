package boot;

import controller.server.MyServer;
import controller.server.SokobanClientHandler;

public class Run {

	public static void main(String[]args){
		
		SokobanClientHandler ch=new SokobanClientHandler();
		
		int port=7000;
		MyServer theServer=new MyServer(port, ch);
		theServer.start();
		System.out.println("Tomer Test Git");
		System.out.println("Tomer Test Git2");
	}
}
	
