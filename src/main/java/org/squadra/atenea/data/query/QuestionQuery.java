package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;

public class QuestionQuery {


	public ArrayList<Word> findAnswer(ArrayList<String> words) {
		
		ExecutionResult result = findSentencesByKeyWords(words);
		ArrayList<Word> answer = resultToResponseWords(result);
		return answer;
		
	}
	
	
	private ExecutionResult findSentencesByKeyWords(ArrayList<String> words) {
		
		Neo4jServer.beginTransaction();
		
		String startNodes = "";
		String matches = "";
		String where = "";
		
		for (int i = 0; i < words.size(); i++) {
			
			if (i > 0) {
				startNodes += " , ";
				matches += " , ";
				
				if (i == 1) {
					where += " WHERE ";
				}
				else if (i > 1) {
					where += " AND ";
				}
				
				where += " relation" + (i-1) + ".sentenceId = relation" + i + ".sentenceId ";
			}
			
			startNodes += " node" + i + " = node:words('name:\"" + words.get(i) + "\"') ";
			matches += "(node" + i + ")-[relation" + i + ":SENTENCE]->() ";
		}
		
		String query = 
				
				  " START "
				+ 		startNodes
				+ " MATCH "
				+ 		matches
				+   where
				+ " WITH "
				+ "     relation0 "
				+ " START "
				+ "   	startNode = node:words('*:*') "
				+ " MATCH "
				+ "     (startNode)-[relation:SENTENCE]->(endNode) "
				+ " WHERE "
				+ "     relation.sentenceId = relation0.sentenceId "
				+ " RETURN "
				+ "		DISTINCT startNode, relation, endNode "
				+ " ORDER BY "
				+ "     relation.sentenceId, relation.sequence ASC; ";
		
		System.out.println(query);
		
		ExecutionResult result = Neo4jServer.engine.execute(query);
		return result;
		
	}
	
	
	private ArrayList<Word> resultToResponseWords(ExecutionResult result) {
		
		ArrayList<Word> response = new ArrayList<>();
		boolean firstRel = true;
		
		for ( Map<String, Object> row : result )
		{
			if (firstRel) {
				Node startNode = (Node) row.get("startNode");
				response.add(Neo4jServer.nodeToWord(startNode));
				firstRel = false;
			}
			
			Node endNode = (Node) row.get("endNode");
			response.add(Neo4jServer.nodeToWord(endNode));
		}
		return response;
	}
	
	
}
