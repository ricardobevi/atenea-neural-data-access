package org.squadra.atenea.data.definition;

import org.neo4j.graphdb.Transaction;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;

public class NodeDefinition {
	
	Transaction transaction;
	
	public NodeDefinition(){
	}
	
	public void relateWords(Word word1, Word word2){
		
		Neo4jServer.relateNodesByConcept(
				Neo4jServer.createNode(word1, "words"),
				Neo4jServer.createNode(word2, "words")
				);
		
		this.transactionSuccess();
	}
	
	public void relateWordsWithContext(Word word1, Word word2, String contextSentence){
		
		Neo4jServer.relateNodesByConceptWithContext(
				Neo4jServer.createNode(word1, "words"),
				Neo4jServer.createNode(word2, "words"),
				contextSentence
				);
		
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
	
	public void relateTypeOfDialogWords(Word word1, Word word2, long sentenceId, 
			int sequence, Integer[] probabilities, String typeOfDialog){
		
		Neo4jServer.relateNodesByType(
				Neo4jServer.createNode(word1, typeOfDialog),
				Neo4jServer.createNode(word2, "words"),
				sentenceId, sequence, probabilities
		);
		
		this.transactionSuccess();
	}
	
	public void relateWikiInfoWords(Word word1, Word word2, long sentenceId, 
			int sequence, Integer probability, String contentType){
		
		Neo4jServer.relateNodesByWikiType(
				Neo4jServer.createNode(word1, "words"),
				Neo4jServer.createNode(word2, "words"),
				sentenceId, sequence, probability, contentType
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
