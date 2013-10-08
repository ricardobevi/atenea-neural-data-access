package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Iterator;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.squadra.atenea.data.definition.NodeDefinition;
import org.squadra.atenea.data.server.Neo4jServer;

public class BasicQuery {
	

	public ArrayList<String> getRelatedWords( String word ){
		
		ArrayList<String> relatedNodes = new ArrayList<String>();
		
		Node foundNode = Neo4jServer.getNode("word", word, "words");
		
		if ( foundNode != null ){
		
			Iterable<Relationship> relationships = foundNode.getRelationships();
			
			if ( relationships != null ){
				
				Iterator<Relationship> relationIt = relationships.iterator();
				
				//cargo todas las palabras relacionadas.
				while( relationIt.hasNext() )
					relatedNodes.add( (String) relationIt.next().getEndNode().getProperty("word") );
				
			}
		
		}
		
		return relatedNodes;
	}
	
	public ExecutionResult excecuteQuery(String query){
		return Neo4jServer.excecuteQuery(query);	
	}

}
