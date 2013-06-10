package org.squadra.atenea.data.server;


public class NeuralDataAccess {
	
	private static boolean isDBStarted = false; 
	
	public static void init(){
		
		if ( !isDBStarted ){
			
			try{
				
				Neo4jServer.init("./graphDB");
				isDBStarted = true;
				
			} catch (IllegalStateException e){
				//TODO: log db lockeada
			}
			
		}
		
	}
	
	public static void stop(){
		
		Neo4jServer.stop();
		
	}
	
	
}
