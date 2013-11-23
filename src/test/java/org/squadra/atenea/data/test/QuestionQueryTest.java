package org.squadra.atenea.data.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squadra.atenea.data.query.QuestionQuery;
import org.squadra.atenea.data.server.NeuralDataAccess;
import org.squadra.atenea.parser.model.SimpleSentence;

public class QuestionQueryTest {

	@BeforeClass
	public static void initDataBase() {
		NeuralDataAccess.init();
	}
	
	@AfterClass
	public static void stopDataBase() {
		NeuralDataAccess.stop();
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

		String title = "José de San Martín";
		String subtitle = "cargo";
		String contentType = "nombre";
		
		QuestionQuery qq = new QuestionQuery();
		String answer = qq.findSentencesFromAdditionalInfo(title, subtitle, contentType);
		
		System.out.println(answer);
		
		assertTrue(true);
	}

}
