package org.squadra.atenea.data.server;

import java.util.ArrayList;

import lombok.extern.log4j.Log4j;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.definition.Relation;
import org.squadra.atenea.data.query.DialogQuery;

@Log4j
public class Neo4jServer {

	public static GraphDatabaseService graphDb;
	public static WrappingNeoServerBootstrapper server;
	
	public static ArrayList <ArrayList <Word>> dialogCache;

	public static ExecutionEngine engine;
	
	/**
	 * Inicializa la base de datos.
	 * 
	 * @param databasePath La ruta donde estarán los archivos de la base de datos.
	 */
	public static void init(String databasePath) {

		try {

			log.debug("------------log data");
			graphDb = new GraphDatabaseFactory()
			.newEmbeddedDatabaseBuilder(databasePath)
			.setConfig( GraphDatabaseSettings.relationship_keys_indexable, "sentenceId" )
			.setConfig( GraphDatabaseSettings.relationship_auto_indexing, "true" )
			.newGraphDatabase();

			registerShutdownHook(graphDb);

			engine = new ExecutionEngine(graphDb);

			server = new WrappingNeoServerBootstrapper(
					(GraphDatabaseAPI) graphDb);
			
			server.start();
			
			loadDialogCache();

		} catch (Exception e) {
			log.error("Error stating database.", e);
		}

	}
	
	
	/**
	 * Carga la cache con las respuestas a los dialogos para agilizar el
	 * procesamiento.
	 */
	public static void loadDialogCache() {
		log.debug("Cargando cache de dialogos...");
		dialogCache = new DialogQuery().findAllSentences("dialogType");
		log.debug("Fin de carga de cache");
	}

	/**
	 * Crea un nuevo indice si este no existe ya, sino devuelve el ya existente.
	 * 
	 * @param index El nombre del índice a crear.
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
	 * @param attr El descriptor del atributo pricipal del nodo.
	 * @param value El valor del atributo principal.
	 * @param index El nombre del índice al cual se abrega el nodo.
	 * @return El nodo creado.
	 */
	public static Node getNode(String attr, String value, String index) {

		Node node = null;

		// obtener nodo si existe
		node = createOrGetIndex(index).get(attr, value).getSingle();

		return node;

	}

	
	/**
	 * Crea un nodo tipo Word
	 * @param value
	 * @param index
	 * @return
	 */
	public static Node createNode(Word value, String index) {

		Node node = null;

		// obtener nodo si existe
		Index<Node> nodesIndex = createOrGetIndex(index);
		Node searchNode = nodesIndex.get("name", value.getName()).getSingle();

		if (searchNode == null) {

			node = graphDb.createNode();
			setWordNodeProperties(node, value);
			
			nodesIndex.add(node, "name", value.getName());

		} else {
			node = searchNode;
		}

		return node;
	}
	
	
	/**
	 * Setea las propiedades necesarias del objeto Word al nodo.
	 * @param node
	 * @param word
	 */
	private static void setWordNodeProperties(Node node, Word word) {
		node.setProperty("name", word.getName());
		//TODO: para el baseWord hay que crear un nodo aparte.
		node.setProperty("baseWord", word.getBaseWord());
		node.setProperty("type", word.getType());
		node.setProperty("subtype", word.getSubType());
		node.setProperty("gender", word.getGender());
		node.setProperty("number", word.getNumber());
		node.setProperty("mode", word.getPerson());
		node.setProperty("tense", word.getTense());
		node.setProperty("person", word.getPerson());
	}
	
	
	/**
	 * Recibe un nodo y devuelve un objeto Word con las propiedades
	 * seteadas obtenidas del nodo.
	 * @param node
	 * @return Objeto Word con las propiedades obtenidas del nodo.
	 */
	public static Word nodeToWord(Node node) {
		
		Word word = new Word((String) node.getProperty("name"));
		
		try {
			word.setBaseWord((String) node.getProperty("baseWord"));
			word.setType((String) node.getProperty("type"));
			word.setSubType((String) node.getProperty("subtype"));
			word.setGender((String) node.getProperty("gender"));
			word.setNumber((String) node.getProperty("number"));
			word.setMode((String) node.getProperty("mode"));
			word.setTense((String) node.getProperty("tense"));
			word.setPerson((String) node.getProperty("person"));
		} catch (NotFoundException e) {
			log.error("Propiedad no encontrada.");
		}
		return word;
	}
	

	/**
	 * Relaciona dos nodos con una relacion tipo oracion y agrega las
	 * propiedades de ID de oracion y secuencia.
	 * @param node1
	 * @param node2
	 * @param numberSentence
	 * @param sequence
	 * @return relacion creada entre los nodos
	 */
	public static Relationship relateNodesBySentenceType (Node node1, Node node2,
			long sentenceId, int sequence) {

		Relationship relationship;
		
		relationship = node1.createRelationshipTo(node2,
				DynamicRelationshipType.withName(Relation.Types.SENTENCE.toString()));
		relationship.setProperty("sentenceId", sentenceId);
		relationship.setProperty("sequence", sequence);

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
	
	
	/**
	 * Relaciona dos nodos con una relacion tipo dialogo y agrega las
	 * propiedades de ID de oracion y secuencia.
	 * @param node1
	 * @param node2
	 * @param sentenceId
	 * @param sequence
	 * @param probabilities
	 * @return relacion creada entre los nodos.
	 * @author Leandro Morrone
	 */
	public static Relationship relateNodesByDialogType (Node node1, Node node2, 
			long sentenceId, int sequence, Integer[] probabilities) {
		
		Relationship relationship;
		
		relationship = node1.createRelationshipTo(node2, 
				DynamicRelationshipType.withName(Relation.Types.DIALOG.toString()));
		relationship.setProperty("sentenceId", sentenceId);
		relationship.setProperty("sequence", sequence);
		
		for (int i = 0; i < probabilities.length; i++) {
			relationship.setProperty("prob" + i, probabilities[i]);
		}
		
		return relationship;
	}
	

	/**
	 * Inicia una transaccion
	 * @return transaccion
	 */
	public static Transaction beginTransaction() {
		Transaction tx = graphDb.beginTx();
		return tx;
	}

	/**
	 * Ejecuta una query escrita en lenguaje cypher
	 * @param query
	 * @return resultado de la consulta
	 */
	public static ExecutionResult excecuteQuery(String query) {
		ExecutionResult result = engine.execute(query);
		return result;
	}

	/**
	 * Detiene la base de datos
	 */
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
