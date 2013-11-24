package org.squadra.atenea.data.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squadra.atenea.data.query.QuestionQuery;
import org.squadra.atenea.data.server.NeuralDataAccess;

public class QuestionQueryTest {

	@BeforeClass
	public static void initDataBase() {
		NeuralDataAccess.init();
	}
	
	@AfterClass
	public static void stopDataBase() {
		NeuralDataAccess.stop();
	}
	
	public void findResponse(String titulo, String sinonimo, String tipo) {
		
		QuestionQuery qq = new QuestionQuery();
		
		System.out.println("\n" + titulo + " - " + sinonimo);
		
		System.out.println("= " +
				qq.findSentencesFromAdditionalInfo(titulo, sinonimo, tipo));
	}
	
	
	//@Test
	public void testKeyWords() {

		ArrayList<String> words = new ArrayList<>();
		words.add("Argentina");
		words.add("Chile");
		words.add("Perú");
		words.add("Martín");
		
		QuestionQuery qq = new QuestionQuery();
		qq.findSentencesByKeyWords(words);
		
		assertTrue(true);
	}
	
	@Test
	public void testAdditionalInfo() {
		
		findResponse("José de San Martín", "cargo", "sustantivo");
		findResponse("José de San Martín", "cónyuge", "nombre");
		findResponse("José de San Martín", "nacimiento", "lugar");
		findResponse("José de San Martín", "nacer", "fecha");
		findResponse("José de San Martín", "morir", "lugar");
		findResponse("José de San Martín", "fallecimiento", "fecha");
		findResponse("Barack Obama", "estudiar", "lugar");
		findResponse("Barack Obama", "estudio", "lugar");
		findResponse("Barack Obama", "religión", "sustantivo");
		findResponse("Barack Obama", "creer", "sustantivo");
		findResponse("Barack Obama", "política", "sustantivo");
		findResponse("Barack Obama", "cónyuge", "nombre");
		findResponse("Lionel Messi", "gol", "cantidad");
		findResponse("Lionel Messi", "nacer", "lugar");
		findResponse("Lionel Messi", "nacer", "fecha");
		findResponse("Lionel Messi", "debut", "lugar");
		findResponse("Lionel Messi", "club", "lugar");
		findResponse("Diego Armando Maradona", "gol", "cantidad");
		findResponse("Diego Armando Maradona", "nacer", "lugar");
		findResponse("Diego Armando Maradona", "nacer", "fecha");
		findResponse("Diego Armando Maradona", "debut", "lugar");
		findResponse("Diego Armando Maradona", "club", "lugar");
		
		assertTrue(true);
	}

}
