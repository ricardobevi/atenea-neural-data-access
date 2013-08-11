package org.squadra.atenea.data.server;

import lombok.extern.log4j.Log4j;

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
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.definition.Relation;

@Log4j
public class Neo4jServer {

	public static GraphDatabaseService graphDb;
	public static WrappingNeoServerBootstrapper server;

	private static ExecutionEngine engine;

	/**
	 * Inicializa la base de datos.
	 * 
	 * @param databasePath
	 *            La ruta donde estarán los archivos de la base de datos.
	 */
	public static void init(String databasePath) {

		try {

			log.debug("------------log data");
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
					databasePath).newGraphDatabase();

			registerShutdownHook(graphDb);

			engine = new ExecutionEngine(graphDb);

			server = new WrappingNeoServerBootstrapper(
					(GraphDatabaseAPI) graphDb);

			server.start();

		} catch (Exception e) {
			log.error("Error stating database.", e);
		}

	}

	/**
	 * Crea un nuevo indice si este no existe ya, sino devuelve el ya existente.
	 * 
	 * @param index
	 *            El nombre del índice a crear.
	 * @return El indice creado.
	 */
	public static Index<Node> createOrGetIndex(String index) {

		Index<Node> retIndex;

		retIndex = graphDb.index().forNodes(index);

		return retIndex;

	}

	/**
	 * Crea un nuevo nodo u obtiene uno ya existente.
	 * 
	 * @param attr
	 *            El descriptor del atributo pricipal del nodo.
	 * @param value
	 *            El valor del atributo principal.
	 * @param index
	 *            El nombre del índice al cual se abrega el nodo.
	 * @return El nodo creado.
	 */
	public static Node getNode(String attr, String value, String index) {

		Node node = null;

		// obtener nodo si existe
		node = createOrGetIndex(index).get(attr, value).getSingle();

		return node;

	}

	public static Node createNode(Word value, String index) {

		Node node = null;

		// obtener nodo si existe
		Index<Node> nodesIndex = createOrGetIndex(index);
		Node searchNode = nodesIndex.get("word", value.getName()).getSingle();

		if (searchNode == null) {

			node = graphDb.createNode();
			node.setProperty("word", value.getName());
			// node.setProperty("type", value.getType());
			// node.setProperty("subtype", value.getSubType());
			// node.setProperty("gender", value.getGender());
			// node.setProperty("number", value.getNumber());
			// node.setProperty("mode", value.getPerson());
			// node.setProperty("tense", value.getTense());
			// node.setProperty("person", value.getPerson());

			nodesIndex.add(node, "word", value.getName());

		} else {
			node = searchNode;
		}

		return node;

	}

	/**
	 * 
	 * @param node1
	 * @param node2
	 * @param numberSentence
	 * @param sequence
	 *            sequence in the sentence
	 * @return
	 */
	public static Relationship relateNodes(Node node1, Node node2,
			long numberSentence, int sequence) {

		Relationship relationship;

		relationship = node1.createRelationshipTo(node2,
				Relation.Types.SENTENCE);
		relationship.setProperty("seq", sequence);
		relationship.setProperty("id", numberSentence);

		// Si la 2da palabra es un verbo, asociarlo con el verbo sin conjugar
		// if (node2.getProperty("type") == "verbo")
		// {
		// Relationship relationToVerb;
		// relationToVerb = node2.createRelationshipTo( createNode(new Word(),
		// "words")
		// , Relation.Types.VERB );
		// //relationToVerb.setProperty("seq", sequence);
		// relationToVerb.setProperty("id", numberSentence);
		// }

		return relationship;

	}

	public static Transaction beginTransaction() {
		Transaction tx = graphDb.beginTx();
		return tx;
	}

	public static ExecutionResult excecuteQuery(String query) {
		ExecutionResult result = engine.execute(query);
		return result;
	}

	public static void stop() {

		try {
			graphDb.shutdown();
			server.stop();
		} catch (Exception e) {
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
