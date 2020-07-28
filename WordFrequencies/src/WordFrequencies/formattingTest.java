package WordFrequencies;

import static org.junit.Assert.*;

import org.junit.Test;

public class formattingTest {

	/**
	 * This junit test confirms that the program is accurately formatting the input
	 */
	@Test
	public void test() {
		WordFrequencies test = new WordFrequencies();
		String outputString = test.format("hello, tHIs? is! a format. test;");
		assertEquals("hello this is a format test",outputString);
	}

}