package org.squadra.atenea.data.query;

import java.util.ArrayList;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.squadra.atenea.base.word.Word;
import org.squadra.atenea.data.server.Neo4jServer;
import org.squadra.atenea.parser.model.SimpleSentence;

public class QuestionQuery {
	
	/**
	 * Devuelve un conjunto de oraciones que contienen las palabras clave ingreadas.
	 * @param words Lista de palabras claves a buscar en una oracion.
	 * @return Listado de oraciones
	 */
	public ArrayList<SimpleSentence> findSentencesByKeyWords(ArrayList<String> words) {
		
		Neo4jServer.beginTransaction();
		
		String startNodes = "";
		String matches = "";
		String where = "";
		
		for (int i = 0; i < words.size(); i++) {
			
			if (i > 0) {
				startNodes += " , ";
				matches += " , ";
				
				if (i == 1) {
					where += " WHERE ";
				}
				else if (i > 1) {
					where += " AND ";
				}
				
				where += " relation" + (i-1) + ".sentenceId = relation" + i + ".sentenceId ";
			}
			
			startNodes += " node" + i + " = node:words('name:\"" + words.get(i) + "\"') ";
			matches += "(node" + i + ")-[relation" + i + ":SENTENCE]->() ";
		}
		
		String query = 
				
				  " START "
				+ 		startNodes
				+ " MATCH "
				+ 		matches
				+   where
				+ " WITH "
				+ "     relation0 "
				+ " START "
				+ "   	startNode = node:words('*:*') "
				+ " MATCH "
				+ "     (startNode)-[relation:SENTENCE]->(endNode) "
				+ " WHERE "
				+ "     relation.sentenceId = relation0.sentenceId "
				+ " RETURN "
				+ "		DISTINCT startNode, relation, endNode "
				+ " ORDER BY "
				+ "     relation.sentenceId, relation.sequence ASC; ";
		
		System.out.println(query);
		
		// Ejecuto la consulta
		ExecutionResult result = Neo4jServer.engine.execute(query);
		
		// Convierto el resultado en un conjunto de oraciones
		return resultToResponseWords(result);
	}
	
	
	/**
	 * Devuelve las oraciones que correspondan al titulo y subtitulo ingresados.
	 * @param title Titulo del articulo de Wikipedia
	 * @param synonim Sinonimo relacionado al titulo (sustantivo, verbo, etc)
	 * @return Listado de oraciones
	 */
	public String findSentencesFromAdditionalInfo(
			String title, String synonim, String contentType) {
		
		Neo4jServer.beginTransaction();
		
		String whereContentType = "";
		if (contentType != null && !contentType.equals("")) {
			whereContentType = " AND relation.contentType = '" + contentType + "' ";
		}
		
		String query = 
				
				  " START "
				+ "		title = node:words('*:*') "
				+ " MATCH "
				+ "		(title)-[relation0:WIKI_INFO]->(synonim) "
				+ " WHERE "
				+ "		title.name =~ \".*" + title + ".*\" AND "
				+ "		synonim.name = \"" + synonim + "\""
				+ " WITH "
				+ "     title, synonim, relation0 "
				+ " MATCH "
				+ "     (synonim)-[relation:WIKI_INFO]->(body) "
				+ " WHERE "
				+ "     relation.sentenceId = relation0.sentenceId " + whereContentType
				+ " RETURN "
				+ "		relation, body "
				+ " ORDER BY "
				+ "     relation.sentenceId, relation.sequence ASC; ";
		
		System.out.println(query);
		
		// Ejecuto la consulta
		ExecutionResult result = Neo4jServer.engine.execute(query);
		
		// Armo la respuesta, concatenando las oraciones
		String answers = "";
		
		for ( Map<String, Object> row : result ) {
			
			Node node = (Node) row.get("body");
			answers += node.getProperty("name") + ". ";
		}
		
		return answers;
	}
	
	
	/**
	 * Convierte el resultado de una consulta de base de datos a una lista de oraciones.
	 * @param result Resultado de una consulta Neo4j (conjunto de nodos y relaciones)
	 * @return Listado de oraciones con los objetos Word cargados de la base de datos
	 */
	private ArrayList<SimpleSentence> resultToResponseWords(ExecutionResult result) {
		
		ArrayList<SimpleSentence> responses = new ArrayList<>();
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
			
			Word word = Neo4jServer.nodeToWord(endNode);
			response.add(word);
			
			if (word.getName().equals(".")) {
				responses.add(new SimpleSentence(response));
				response.clear();
				firstRel = true;
			}
		}
		return responses;
	}
	
	
}
