package org.atenea.data.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atenea.data.definition.Relation;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;

public class Neo4jServer {

	public static GraphDatabaseService graphDb;
	public static WrappingNeoServerBootstrapper server;
	
	private static Logger log = LogManager.getLogger("org.atenea.data.server");
	
	private static ExecutionEngine engine;
	
	/**
	 * Inicializa la base de datos.
	 * 
	 * @param databasePath La ruta donde estarán los archivos de la base de datos.
	 */
	public static void init(String databasePath){

		try{
			
			graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabase(databasePath);
			
			registerShutdownHook(graphDb);
					
			engine = new ExecutionEngine( graphDb );

			server = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) graphDb);
			
			server.start();
			
		} catch (Exception e){
			log.error("Error stating database.", e);
		}
		
	}
	
	/**
	 * Crea un nuevo indice si este no existe ya, sino devuelve
	 * el ya existente.
	 * 
	 * @param index El nombre del índice a crear.
	 * @return El indice creado.
	 */
	public static Index<Node> createOrGetIndex(String index){
		
		Index<Node> retIndex;

		retIndex = graphDb.index().forNodes(index);
		
		return retIndex;
		
	}
	

	/**
	 * Crea un nuevo nodo u obtiene uno ya existente.
	 * 
	 * @param attr El descriptor del atributo pricipal del nodo.
	 * @param value El valor del atributo principal.
	 * @param index El nombre del índice al cual se abrega el nodo.
	 * @return El nodo creado.
	 */
	public static Node getNode(String attr, String value, String index){
		
		Node node = null;
		
		//obtener nodo si existe
		node = createOrGetIndex(index).get(attr, value).getSingle();

		
		return node;
		
	}
	
	public static Node createNode(String attr, String value, String index){
		
		Node node = null;
		
		//obtener nodo si existe
		Index<Node> nodesIndex = createOrGetIndex(index);
		Node searchNode = nodesIndex.get(attr, value).getSingle();
		
		if ( searchNode == null ){
			
			node = graphDb.createNode();
			node.setProperty(attr, value);
			
			nodesIndex.add(node, attr, value);
			
		} else {
			node = searchNode;
		}
		
		return node;
		
	}
	
	
	public static Relationship relateNodes( Node node1, Node node2 ){
		
		Relationship relationship;
		
		relationship = node1.createRelationshipTo( node2, Relation.Types.REL );
		
		return relationship;
		
	}
	
	public static Transaction beginTransaction(){
		Transaction tx = graphDb.beginTx();
		return tx;
	}
	
	public static ExecutionResult excecuteQuery(String query){
		ExecutionResult result = engine.execute( query );
		return result;
	}

	public static void stop(){

		try{
			graphDb.shutdown();
			server.stop();			
		} catch (Exception e){
			log.error("Error stopping database.", e);
		}
		
	}
	
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Neo4jServer.graphDb.shutdown();
			}
		});
	}
	
	
}
