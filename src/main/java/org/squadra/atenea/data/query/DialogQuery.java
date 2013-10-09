package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;

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
	 * @param dialogType Tipo de dialogo
	 * @return Lista de palabras que conforman la respuesta
	 */
	public ArrayList<Word> findRandomSentenceByDialogType(String dialogType) {
		
		// Obtengo las posibles respuestas al tipo de dialogo
		ExecutionResult result = findSentencesByDialogType(dialogType);
		
		// Selecciono una respuesta aleatoria segun el horario
		Long sentenceId = lottery(result);
		
		// Obtengo las posibles respuestas al tipo de dialogo
		ExecutionResult result2 = findSentenceById(sentenceId);
		
		// Convierto la respuesta de nodos a texto
		ArrayList<Word> response = resultToResponseWords(result2);
		
		return response;
	}
	
	
	private ExecutionResult findSentencesByDialogType(String dialogType) {
		
		Neo4jServer.beginTransaction();
		String query = 
				  " START "
				+ "     startNode = node:dialogTypes('name:" + dialogType + "')"
				+ " MATCH "
				+ "     (startNode)-[relation:DIALOG]->(endNode)"
				+ " RETURN "
				+ "     relation"
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
		String probField = getProbFieldByTime(currentHour);
		ArrayList<Relationship> relations = new ArrayList<>();
		
		System.out.println("Hora actual: " + currentHour);
		
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
			
			if (ticket < sumLottery) {
				System.out.println("ID ganador: " + relation.getProperty("sentenceId"));
				return (Long) relation.getProperty("sentenceId");
			}
			
		}
		// Por aca no deberia salir nunca
		log.warn("No se encontro ninguna respuesta en la base de datos.");
		return -1l;
	}
	
	
	private String getProbFieldByTime(Integer currentHour) {
		
		if (currentHour >= 5 && currentHour < 8 ) {
			return "prob0";
		}
		if (currentHour >= 8 && currentHour < 13 ) {
			return "prob1";
		}
		if (currentHour >= 13 && currentHour < 20 ) {
			return "prob2";
		}
		if (currentHour >= 20 || currentHour < 2 ) {
			return "prob3";
		}
		if (currentHour >= 2 && currentHour < 5 ) {
			return "prob4";
		}
		return "prob2"; 
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
