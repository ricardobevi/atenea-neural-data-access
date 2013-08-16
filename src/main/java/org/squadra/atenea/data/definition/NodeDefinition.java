package org.squadra.atenea.data.definition;

import org.neo4j.graphdb.Transaction;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;

public class NodeDefinition {
	
	Transaction transaction;
	
	public NodeDefinition(){
	}
	
	public void insertWord(String word){
		//Neo4jServer.createNode(word, "words");
		this.transactionSuccess();
	}
	
	public void relateWords(Word word1, Word word2, long numberSentence, int sequence){
		
		Neo4jServer.relateNodes(
				Neo4jServer.createNode(word1, "words"),
				Neo4jServer.createNode(word2, "words"),
				numberSentence, sequence
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
