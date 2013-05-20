package org.atenea.data.definition;

import org.atenea.data.server.Neo4jServer;
import org.neo4j.graphdb.Transaction;

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
