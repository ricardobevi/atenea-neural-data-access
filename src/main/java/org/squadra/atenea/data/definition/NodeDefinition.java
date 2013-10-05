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
	
	public void relateWords(Word word1, Word word2, long sentenceId, int sequence){
		
		Neo4jServer.relateNodesBySentenceType(
				Neo4jServer.createNode(word1, "words"),
				Neo4jServer.createNode(word2, "words"),
				sentenceId, sequence
		);
		
		this.transactionSuccess();
	}
	
	public void relateDialogWords(Word word1, Word word2, long sentenceId, 
			int sequence, Integer[] probabilities){
		
		Neo4jServer.relateNodesByDialogType(
				Neo4jServer.createNode(word1, "dialogTypes"),
				Neo4jServer.createNode(word2, "words"),
				sentenceId, sequence, probabilities
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
