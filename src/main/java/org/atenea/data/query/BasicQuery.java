package org.atenea.data.query;

import java.util.ArrayList;
import java.util.Iterator;

import org.atenea.data.server.Neo4jServer;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class BasicQuery {
	
	public ArrayList<String> getRelatedWords( String word ){
		
		ArrayList<String> relatedNodes = new ArrayList<String>();
		
		
		Node foundNode = Neo4jServer.getNode("word", word, "words");
		
		Iterator<Relationship> relationIt = foundNode.getRelationships().iterator();
		
		//cargo todas las palabras relacionadas.
		while( relationIt.hasNext() )
			relatedNodes.add( (String) relationIt.next().getEndNode().getProperty("word"));
		
		return relatedNodes;
	}
	
	public ExecutionResult excecuteQuery(String query){
		return Neo4jServer.excecuteQuery(query);	
	}

}
