package org.squadra.atenea.data.server;

import org.mortbay.log.Log;

import lombok.extern.log4j.Log4j;

@Log4j
public class NeuralDataAccess {
	
	private static boolean isDBStarted = false; 
	
	public static void init(){
		
		if ( !isDBStarted ){
			
			try{
				// Levanto la base de datos y cargo la cache
				Neo4jServer.init("./graphDB");
				//NeuralDataAccess.loadCache();
				isDBStarted = true;
				
				warmUp();
				
			} catch (IllegalStateException e){
				log.error("Base de datos bloqueada.");
			}
		}
		
	}
	
	
	public static void init(String path){
		
		if ( !isDBStarted ){
			
			try{
				Neo4jServer.init(path);
				isDBStarted = true;
				
				warmUp();
				
			} catch (IllegalStateException e){
				log.error("Base de datos bloqueada.");
			}
		}
		
	}
	
	private static void warmUp(){
		Log.debug("warming up database");
		
		Neo4jServer.engine.execute("START node = node(0) RETURN node");
		
		Log.debug("database warmed up");
	}
	
	public static void stop(){
		Neo4jServer.stop();
	}
	
	public static void loadCache() {
		if (!isDBStarted) {
			Neo4jServer.loadDialogCache();
		}
	}
	
	
}
