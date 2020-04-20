import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//Druv Narain
//COSC 455 Project 2
public class RunTestCases {

	public static void main(String[] args) throws FileNotFoundException {
		
		//Add input file to tokenize/parse here in place of "testcases.txt". Then run this java file
		File testcases = new File("testcases.txt");
		
		
		Scanner fileReader = new Scanner(testcases);
		LexicalAnalyzer scanner = new LexicalAnalyzer();
		String parseLine;
		ArrayList<Token> t;
		Parser parseT = new Parser();
		int line = 1;
		
		while(fileReader.hasNextLine()) {
			parseLine = fileReader.nextLine();
			System.out.println();
			System.out.println("Source lines is: " + parseLine);
			scanner.tokenizeLine(parseLine);
			t = scanner.getTokenList();
			try {
				if(parseT.parseTokens(t)) {
					System.out.println("Line " + line + " is syntactically correct");
					parseT.printAST();
					line++;
				}
			} catch (ExpressionErrorException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		fileReader.close();
	}
}
