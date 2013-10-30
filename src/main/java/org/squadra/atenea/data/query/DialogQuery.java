package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;
import org.squadra.atenea.parser.model.SimpleSentence;

/**
 * Clase con metodos para realizar consultas en la base de datos, referidas
 * al dialogo cotidiano e interjecciones.
 * @author Leandro Morrone
 *
 */
@Log4j
public class DialogQuery {
	
	/**
	 * Busca en la base de datos una respuesta aleatoria segun el tipo de dialogo
	 * @param indexType Tipo de indice (igual al de la base de datos)
	 * @param dialogType Tipo de dialogo
	 * @return SimpleSentence con la respuesta
	 */
	public SimpleSentence findRandomSentenceByDialogType(String indexType, String dialogType) {
		
		// Obtengo las posibles respuestas al tipo de dialogo
		ExecutionResult result = findSentencesByDialogType(indexType, dialogType);
		
		// Selecciono una respuesta aleatoria segun el horario
		Long sentenceId = lottery(result);
		
		// Obtengo las posibles respuestas al tipo de dialogo
		ExecutionResult result2 = findSentenceById(sentenceId);
		
		// Convierto la respuesta de nodos a texto
		ArrayList<Word> response = resultToResponseWords(result2);
		
		return new SimpleSentence(response);
	}
	
	
	/**
	 * Busca en la base de datos todas las respuestas posibles a un tipo de dialogo
	 * @param indexType Tipo de dialogo
	 * @return Lista de SimpleSentences con las oraciones
	 */
	public ArrayList<SimpleSentence> findAllSentences(String indexType) {
		
		ArrayList<SimpleSentence> responses = new ArrayList<>();
		
		// Obtengo todas los IDs de las respuestas
		ExecutionResult result = findSentencesByDialogType(indexType, "*");
		
		// Obtengo las respuestas de cada ID
		for ( Map<String, Object> row : result )
		{
			ExecutionResult result2 = findSentenceById(
					(Long)((Relationship) row.get("relation")).getProperty("sentenceId"));
			
			ArrayList<Word> response = new ArrayList<>();
			
			response.add(new Word( 
					(String)((Node) row.get("startNode")).getProperty("name") ));
			
			response.addAll(resultToResponseWords(result2));
			SimpleSentence ss = new SimpleSentence(response);
			log.debug(" = " + ss.toString());
			responses.add(ss);
		}
		
		return responses;
	}
	
	
	private ExecutionResult findSentencesByDialogType(String indexType, String dialogType) {
		
		Neo4jServer.beginTransaction();
		String query = 
				  " START "
				+ "     startNode = node:" + indexType + "('name:" + dialogType + "')"
				+ " MATCH "
				+ "     (startNode)-[relation:DIALOG]->(endNode)"
				+ " RETURN "
				+ "     startNode, relation"
				+ " ORDER BY "
				+ "     relation.sentenceId ASC;";
		
		ExecutionResult result = Neo4jServer.engine.execute(query);
		return result;
	}
	
	private ExecutionResult findSentenceById(Long id) {
		
		Neo4jServer.beginTransaction();
		String query = 
				  " START "
				+ "     startNode = node:words('*:*')"
				+ " MATCH "
				+ "     (startNode)-[relation:SENTENCE]->(endNode)"
				+ " WHERE "
				+ "     relation.sentenceId = " + id
				+ " RETURN "
				+ "     startNode, relation, endNode"
				+ " ORDER BY "
				+ "     relation.sequence ASC;";
		
		ExecutionResult result = Neo4jServer.engine.execute(query);
		return result;
	}
	
	
	private Long lottery(ExecutionResult result) {
		
		Integer maxRandom = 0;
		Integer currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		String probField = getProbFieldByTime(currentHour, result);
		ArrayList<Relationship> relations = new ArrayList<>();
		
		for ( Map<String, Object> row : result )
		{
			Relationship relation = (Relationship) row.get("relation");
			relations.add(relation);
			
			Integer probability = (Integer) relation.getProperty(probField);
			maxRandom += probability;
			
			//System.out.println(relation.getProperty("sentenceId"));
		}
		
		Integer ticket = (int) Math.round(Math.random() * maxRandom);
		
		System.out.println("Max: " + maxRandom + " Ticket: " + ticket);
		
		Integer sumLottery = 0;
		
		for ( Relationship relation : relations )
		{
			sumLottery += (Integer) relation.getProperty(probField);
			//System.out.println("Prob: " + relation.getProperty(probField) + " - Incr:" + sumLottery);
			
			if (ticket <= sumLottery) {
				System.out.println("ID ganador: " + relation.getProperty("sentenceId"));
				return (Long) relation.getProperty("sentenceId");
			}
			
		}
		// Por aca no deberia salir nunca
		log.warn("No se encontro ninguna respuesta en la base de datos.");
		return -1l;
	}
	
	
	private String getProbFieldByTime(Integer currentHour, ExecutionResult result) {
		
		String probField = "";
		
		if (currentHour >= 5 && currentHour < 8 ) {
			probField = "prob0";
		}
		if (currentHour >= 8 && currentHour < 13 ) {
			probField = "prob1";
		}
		if (currentHour >= 13 && currentHour < 20 ) {
			probField = "prob2";
		}
		if (currentHour >= 20 || currentHour < 2 ) {
			probField = "prob3";
		}
		if (currentHour >= 2 && currentHour < 5 ) {
			probField = "prob4";
		}
		
		try {
			result.columnAs(probField);
		}
		catch (NotFoundException e) {
			log.warn("Columna no encontrada en el resultado de la query.");
			return "prob0";
		}
		return probField;
	}
	
	private ArrayList<Word> resultToResponseWords(ExecutionResult result) {
		
		ArrayList<Word> response = new ArrayList<>();
		boolean firstRel = true;
		
		for ( Map<String, Object> row : result )
		{
			if (firstRel) {
				Node startNode = (Node) row.get("startNode");
				response.add(Neo4jServer.nodeToWord(startNode));
				firstRel = false;
			}
			
			Node endNode = (Node) row.get("endNode");
			response.add(Neo4jServer.nodeToWord(endNode));
		}
		return response;
	}
	
}
