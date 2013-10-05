package org.squadra.atenea.data.query;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.squadra.atenea.data.server.Neo4jServer;
import org.squadra.atenea.data.server.NeuralDataAccess;

public class DialogQuery {
	
	public static void main(String args[]) {
		
		NeuralDataAccess.init();
		DialogQuery dq = new DialogQuery();
		dq.findRandomSentenceByDialogType("$_SALUDO");
	}

	public void findRandomSentenceByDialogType(String dialogType) {
		
		ExecutionResult result = findSentencesByDialogType(dialogType);
		
		String rows = "";
		
		for ( Map<String, Object> row : result )
		{
		    for ( Entry<String, Object> column : row.entrySet() )
		    {
		        rows += column.getKey() + ": " + column.getValue() + "; ";
		    }
		    rows += "\n";
		}
		System.out.println(rows);
	}
	
	
	private ExecutionResult findSentencesByDialogType(String dialogType) {
		
		Neo4jServer.beginTransaction();
		String query = 
				  " START "
				+ "     n = node:dialogTypes('word:" + dialogType + "')"
				+ " MATCH "
				+ "     (n)-[r:DIALOG]->(m)"
				+ " RETURN "
				+ "     n, r, m"
				+ " ORDER BY "
				+ "     r.sentenceId ASC;";
		
		ExecutionResult result = Neo4jServer.engine.execute(query);
		return result;
	}
	
	private ExecutionResult findSentenceById(String id) {
		
		Neo4jServer.beginTransaction();
		String query = 
				  " START "
				+ "     n = node:words('*:*')"
				+ " MATCH "
				+ "     (n)-[r:SENTENCE]->(m)"
				+ " WHERE "
				+ "     r.sentenceId = " + id
				+ " RETURN "
				+ "     n, r, m"
				+ " ORDER BY "
				+ "     r.sequence ASC;";
		
		ExecutionResult result = Neo4jServer.engine.execute(query);
		return result;
	}
	
	
}
