package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Transaction;
import org.squadra.atenea.data.query.result.ConceptQueryResult;
import org.squadra.atenea.data.query.result.ConceptQueryResultCollection;
import org.squadra.atenea.data.server.Neo4jServer;


public class ConceptQuery {
	
	private static int MAX_TIMEOUT = 4000;
	
	private static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	
	private ArrayList<String> concepts;
	
	private ExecutorService executor;

	public ConceptQuery() {
		concepts = new ArrayList<String>();
		executor = Executors.newFixedThreadPool(10);
	}
	
	public ConceptQuery(Integer maxThreads) {
		concepts = new ArrayList<String>();
		executor = Executors.newFixedThreadPool(maxThreads);
	}
	
	public ConceptQueryResultCollection conceptSearch(Integer maxDepth){
		
		List<ConceptQueryResultCollection> result = Collections.synchronizedList( new ArrayList<ConceptQueryResultCollection>() );
		
		if( concepts.size() > 1 ){
			
			ArrayList<SearchThread> searchThreads = new ArrayList<SearchThread>();
			
			Transaction tx = Neo4jServer.beginTransaction();
			
			try {
				
				for (int i = 1; i <= maxDepth; i++) {
					
					SearchThread searchThread = new SearchThread(i);
					
					executor.execute(searchThread);
					
					searchThreads.add( searchThread );
					
				}
							
				executor.awaitTermination(MAX_TIMEOUT, TimeUnit.MILLISECONDS);
				
				tx.success();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				tx.finish();
			}
	
	        
	        for (int i = 0; i < searchThreads.size(); i++) {
	        	
	        	if ( searchThreads.get(i).getResult() != null &&
	        		 searchThreads.get(i).getResult().getTopResult().getNodes().size() > 0 )
	        		result.add( searchThreads.get(i).getResult() );
	        	
			}


		} else {
			result.add(new ConceptQueryResultCollection());
		}
		
		ConceptQueryResultCollection conceptQueryResultCollection;
	
		if(result.size() > 0){
			conceptQueryResultCollection = result.get(result.size() - 1);
		} else {
			conceptQueryResultCollection = new ConceptQueryResultCollection();
		}
		
		return conceptQueryResultCollection;
		
	}
	
	@Deprecated
	public ConceptQueryResultCollection conceptSearch(){
		ConceptQueryResultCollection result;
		
		if( concepts.size() > 1 ){
			result = findResults(2);
			result = findResults(3);
			result = findResults(4);
		} else {
			result = new ConceptQueryResultCollection();
		}
		
		return result;
	}
	
	public ConceptQueryResultCollection findResults(Integer depth){
		ConceptQueryResultCollection result = new ConceptQueryResultCollection();
		
		String query = bakeQuery(depth);
		
		System.out.println("QUERY:" + query);
		
		ExecutionResult execResults = null;
					
		Transaction tx = Neo4jServer.beginTransaction();
		
		try {
			execResults = Neo4jServer.excecuteQuery(query);
			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tx.finish();
		}
					
	    for (Map<String, Object> execResult : execResults) {
			Iterator< Map.Entry<String, Object> > it = execResult.entrySet().iterator();
			ConceptQueryResult conceptQueryResult = new ConceptQueryResult();
			
			while (it.hasNext()) {
				Map.Entry<String, Object> column = (Map.Entry<String, Object>) it.next();
				
				if( column.getKey().equals("nodes") )
					conceptQueryResult.setRawNodes( column.getValue().toString() );
				
				else if( column.getKey().equals("totalWeight") )
					conceptQueryResult.setTotalWeight( (Long) column.getValue() );
				
				else if ( column.getKey().equals("contextSentence") )
					conceptQueryResult.setRawContextSentence( column.getValue().toString() );
				
			}
			
			result.addResult(conceptQueryResult);
					
		}

	
		return result;
	}
	
	public void rateRelations(Integer rate){
		
		if ( concepts.size() > 0 ){
		
			String bakedQuery = bakeRelationsRateQuery(rate);
			
			System.out.println("RATE QUERY:" + bakedQuery);
			
			Neo4jServer.excecuteTransactionalQuery(bakedQuery);
			
		}
		
	}

	public void addConcept(String concept){
		concepts.add(concept);
	}
	
	public void clearConcepts(){
		concepts.clear();
	}
	
	
	private String bakeRelationsRateQuery(Integer rate){
		
		String bakedQuery = "";
		
		bakedQuery += bakeStart();
					
		bakedQuery += bakeMatch(1);
		
		bakedQuery += bakeWhereLiteral();
		
		bakedQuery += " FOREACH (n IN relationships(path) : SET n.weight = n.weight + " + rate + ") RETURN a ";
		
		return bakedQuery;
		
	}
	
	private String bakeQuery(Integer depth){
		
		String bakedQuery = "";
		
		bakedQuery += bakeStart();
					
		bakedQuery += bakeMatch(depth);
		
		bakedQuery += bakeWhereLiteral();
		
		bakedQuery += bakeReturn();
		
		return bakedQuery;
		
	}
	
	
	private String bakeStart(){
		
		String bakedStart = "";
		
		bakedStart += "START ";
		
		for (int i = 0; i < concepts.size() - 1; i++) {
			bakedStart += alphabet[i] + " = node:words('baseWord:*'), ";
		}
		
		bakedStart += alphabet[concepts.size() - 1] + " = node:words('baseWord:*') ";
		
		return bakedStart;
		
	}
	
	
	private String bakeMatch(Integer depth){
		
		String bakedMatch = "";
		
		bakedMatch += " MATCH "
				+ " path = ";
	
		bakedMatch += alphabet[0] + "-[relation:CONCEPT*.."+ depth +"]->" + alphabet[1];
		
		for (int i = 2; i < concepts.size(); i++) {
			bakedMatch += "-[relation:CONCEPT*.."+ depth +"]->" + alphabet[i];
		}
		
		return bakedMatch;
		
	}
	
	private String bakeMatchUndirected(Integer depth){
		
		String bakedMatch = "";
		
		bakedMatch += " MATCH "
				+ " path = ";
	
		bakedMatch += alphabet[0] + "-[relation:CONCEPT*.."+ depth +"]-" + alphabet[1];
		
		for (int i = 2; i < concepts.size(); i++) {
			bakedMatch += "-[relation:CONCEPT*.."+ depth +"]-" + alphabet[i];
		}
		
		return bakedMatch;
		
	}
	
	@SuppressWarnings("unused")
	private String bakeWhere(){
		
		String bakedWhere = "";
		
		bakedWhere += " WHERE ";
		
		
		for (int i = 0; i < concepts.size() - 1; i++) {
			bakedWhere += " LOWER(" + alphabet[i] + ".baseWord) =~ '.*" + concepts.get(i) + ".*' AND ";
		}
		
		bakedWhere += " LOWER(" + alphabet[concepts.size() - 1] + ".baseWord) =~ '.*" + concepts.get(concepts.size() - 1) + ".*' ";
		
		return bakedWhere;
	}
	
	
	private String bakeWhereLiteral(){
		
		String bakedWhere = "";
		
		bakedWhere += " WHERE ";
		
		
		for (int i = 0; i < concepts.size() - 1; i++) {
			bakedWhere += " HAS(" + alphabet[i] + ".baseWord) AND ";
			bakedWhere += " LOWER(" + alphabet[i] + ".baseWord) = '" + concepts.get(i) + "' AND ";
		}
		
		bakedWhere += " HAS(" + alphabet[concepts.size() - 1] + ".baseWord) AND ";
		bakedWhere += " LOWER(" + alphabet[concepts.size() - 1] + ".baseWord) = '" + concepts.get(concepts.size() - 1) + "' ";
		
		return bakedWhere;
	}
	
	private String bakeReturn(){
		String bakedReturn = "";
		
		bakedReturn += 
				" RETURN " +
				"  EXTRACT(p in NODES(path) : p.baseWord) as nodes, " +
				"  reduce(acc=0, r in relationships(path): acc + r.weight) as totalWeight, " +
				"  EXTRACT(r in relationships(path) : r.contextSentence) as contextSentence " +
				" ORDER BY totalWeight DESC";
		
		
		return bakedReturn;
	}
	
	
	public class SearchThread implements Runnable {

		Integer depth;
		ConceptQueryResultCollection result;
		
		public SearchThread() {
			depth = 2;
			result = null;
        }
		
		public SearchThread(Integer depth) {
			this.depth = depth;
			this.result = null;
        }
		
	    public void run(){
	    	
	    	try {
	    		
	    		result = findResults(depth);
	    		
	    	} catch( Exception e ){
	    		e.printStackTrace();
	    	}
	       
	    }
	    
	    public ConceptQueryResultCollection getResult(){
	    	return result;
	    }
	    
	 }
	

	
}
