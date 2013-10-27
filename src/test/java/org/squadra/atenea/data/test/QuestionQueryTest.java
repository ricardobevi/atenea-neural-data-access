package org.squadra.atenea.data.test;

import static org.junit.Assert.*;

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
	
	@Test
	public void test() {

		ArrayList<String> words = new ArrayList<>();
		words.add("Argentina");
		words.add("Chile");
		words.add("Perú");
		words.add("Martín");
		
		QuestionQuery qq = new QuestionQuery();
		qq.findAnswers(words);
		
		assertTrue(true);
	}

}
