package org.squadra.atenea.data.query.result;

import java.util.ArrayList;
import java.util.Iterator;


public class ConceptQueryResultCollection implements Iterable<ConceptQueryResult> {

	private ArrayList<ConceptQueryResult> conceptQueryResults;

	public ConceptQueryResultCollection() {
		conceptQueryResults = new ArrayList<ConceptQueryResult>();
	}
	
	public void addResult(ConceptQueryResult result){
		conceptQueryResults.add(result);
	}

	public ConceptQueryResult getTopResult(){
		ConceptQueryResult result;
				
		if( conceptQueryResults.size() > 0 )
			result = conceptQueryResults.get(0);
		else
			result = new ConceptQueryResult();
		
		return result;
	}
	
	@Override
	public Iterator<ConceptQueryResult> iterator() {
		return conceptQueryResults.iterator();
	}

	@Override
	public String toString() {
		
		String result = "";
		
		for (int i = 0; i < conceptQueryResults.size(); i++) {
			result += conceptQueryResults.get(i).toString() + "\n";
		}
		
		return result;
	}
	
	

}
