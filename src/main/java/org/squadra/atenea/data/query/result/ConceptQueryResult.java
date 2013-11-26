package org.squadra.atenea.data.query.result;

import java.util.ArrayList;


public class ConceptQueryResult {

	private ArrayList<String> nodes;
	private ArrayList<String> contextSentences;
	
	private String rawNodes;
	private String rawContextSentence;
	
	private Long totalWeight;
	
	
	
	public ConceptQueryResult() {
		nodes = new ArrayList<String>();
		totalWeight = (long) -1;
		rawContextSentence = "";
		rawNodes = "";
		contextSentences = new ArrayList<String>();
	}
	
		
	/*
	 * Setters and Getters
	 */

	public ArrayList<String> getNodes() {
		return nodes;
	}



	public ArrayList<String> getContextSentences() {
		return contextSentences;
	}



	public String getRawNodes() {
		return rawNodes;
	}



	public String getRawContextSentence() {
		return rawContextSentence;
	}



	public Long getTotalWeight() {
		return totalWeight;
	}


	public void setRawNodes(String rawNodes) {
		this.rawNodes = rawNodes;
		parseRawNodes();
	}



	public void setRawContextSentence(String rawContextSentence) {
		this.rawContextSentence = rawContextSentence;
		this.parseRawSentences();
	}



	public void setTotalWeight(Long totalWeight) {
		this.totalWeight = totalWeight;
	}
	
	
	private void parseRawSentences(){
			
		int i = 0;
		
		while( i < rawContextSentence.length() ){
			
			String contextSentence = "";
			
			if( rawContextSentence.charAt( i ) == '{' &&
				rawContextSentence.charAt( i + 1 ) == '{'){
				
				i = i + 2;
				
				//TODO: no es lo mas elegante, pero no se me ocurre otra forma :S
				while( 
						(rawContextSentence.charAt( i ) != '}' &&
					    rawContextSentence.charAt( i + 1 ) != '}') 
					    
					    ||
					    
					    (rawContextSentence.charAt( i ) != '}' &&
					    rawContextSentence.charAt( i + 1 ) == '}')
					    
					    ||
					    
					    (rawContextSentence.charAt( i ) == '}' &&
					    rawContextSentence.charAt( i + 1 ) != '}')
					    
					  ){
					
					contextSentence += rawContextSentence.charAt( i );
					
					i++;
				}
				
				if( !contextSentence.equals("") )
					contextSentences.add(contextSentence);
				
			}
				
			i++;
		}
		
	}
	
	private void parseRawNodes(){
		
		String rawNodesReplaced = rawNodes.replaceAll("\\[", "").replaceAll("\\]", "");
		
		String[] nodesStrings = rawNodesReplaced.split(",");
		
		for (int i = 0; i < nodesStrings.length; i++) {
			nodes.add( nodesStrings[i] );
		}

		
	}

	@Override
	public String toString() {
		String result = "";
		
		result += "<ConceptQueryResult>\n";
		
		result += "  <TotalWeight>" + totalWeight + "</TotalWeight>\n";
		
		result += "  <Nodes>\n";
		
		for (int i = 0; i < nodes.size(); i++) {
			result += "    [" + i + "]  " + nodes.get(i) + "\n";
		}
		
		result += "  </Nodes>\n";
		
		result += "  <ContextSentences>\n";
				
		for (int i = 0; i < contextSentences.size(); i++) {
			result += "    [" + i + "]  " + contextSentences.get(i) + "\n";
		}
		
		result += "  </ContextSentences>\n";
		
		result += "</ConceptQueryResult>\n\n";
		
		return result;
	}
		
	
	
	
}
