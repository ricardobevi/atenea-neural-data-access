package org.squadra.atenea.data.server;

import lombok.extern.log4j.Log4j;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneBatchInserterIndexProviderNewImpl;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;


@Log4j
public class Neo4jBatch {

	private static BatchInserter inserter;
	private static BatchInserterIndex wordIndex;


	/**
	 * Inicializa el batch.
	 * 
	 * @param databasePath
	 *            La ruta donde estarán los archivos de la base de datos.
	 */
	public static void init(String databasePath) {

		try {

			log.debug("------------log data");
						
			inserter = BatchInserters.inserter( "insertedDB" );
			
			BatchInserterIndexProvider indexProvider =
			        new LuceneBatchInserterIndexProvider( inserter );
			
			//inicializo el indice
			wordIndex = indexProvider.nodeIndex( "words", MapUtil.stringMap( "type", "exact" ) );
			
			wordIndex.setCacheCapacity( "word", 100000 );

		} catch (Exception e) {
			log.error("Error stating database.", e);
		}
	}
	
	public static void stop() {

		try {
			inserter.shutdown();
		} catch (Exception e) {
			log.error("Error stopping database.", e);
		}

	}

	
}
