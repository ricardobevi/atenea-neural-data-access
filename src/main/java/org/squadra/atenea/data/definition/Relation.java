package org.squadra.atenea.data.definition;

import org.neo4j.graphdb.RelationshipType;

public class Relation {
	public static enum Types implements RelationshipType {
		SENTENCE,
		DIALOG,
		VERB,
		WIKI_INFO
	}
}
