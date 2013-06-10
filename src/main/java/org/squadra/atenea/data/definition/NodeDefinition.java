package org.squadra.atenea.data.definition;

import org.neo4j.graphdb.Transaction;
import org.squadra.atenea.data.server.Neo4jServer;

public class NodeDefinition {
	
	Transaction transaction;
	
	public NodeDefinition(){
	}
	
	public void insertWord(String word){
		Neo4jServer.createNode("word", word, "words");
		this.transactionSuccess();
	}
	
	public void relateWords(String word1, String word2){
		
		Neo4jServer.relateNodes(
				Neo4jServer.createNode("word", word1, "words"),
				Neo4jServer.createNode("word", word2, "words")
		);
		
		this.transactionSuccess();
	}
	
	public void beginTransaction(){
		transaction = Neo4jServer.beginTransaction();
	}	
	
	public void endTransaction(){
		if ( transaction != null )
			transaction.finish();
		
	}	
	
	public void transactionSuccess(){
		if ( transaction != null )
			transaction.success();
	}
		
}
