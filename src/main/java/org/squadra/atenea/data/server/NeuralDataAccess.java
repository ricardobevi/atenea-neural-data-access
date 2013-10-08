package org.squadra.atenea.data.server;

import lombok.extern.log4j.Log4j;

@Log4j
public class NeuralDataAccess {
	
	private static boolean isDBStarted = false; 
	
	public static void init(){
		
		if ( !isDBStarted ){
			
			try{
				
				Neo4jServer.init("./graphDB");
				isDBStarted = true;
				
			} catch (IllegalStateException e){
				log.error("Base de datos bloqueada.");
			}
			
		}
		
	}
	
	public static void stop(){
		
		Neo4jServer.stop();
		
	}
	
	
}
