import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

//Druv Narain
//COSC 455 Project 2
public class LexicalAnalyzer {

	
	/********************************************************************/

	/**Instance Variables for LexicalAnalyzer**/
	private String source;
	private ArrayList<Token> tokens = new ArrayList<>(); //Tokens for string will be stored here
	private int start; //Pointer for starting character of token in source string
	private int current; //Pointer  that iterates through each word/token in source string
	private int line; //Line number in file
	private HashMap<String, String> keywords; //table for keywords   
	
	/**Constructor for LexicalAnalyzer**/
	public LexicalAnalyzer() {
		this.start = 0;
		this.current = 0;
		this.line = 0;
		keywords = new HashMap<String, String>();
		tokens = new ArrayList<>();
		//Populate keyword table
		keywords.put("begin", "begin");	keywords.put("end", "end");
		keywords.put("bool", "bool");	keywords.put("int", "int");
		keywords.put("if", "if");		keywords.put("then", "then");
		keywords.put("else", "else");	keywords.put("fi", "fi");
		keywords.put("do", "do");		keywords.put("while", "while");
		keywords.put("od", "od");		keywords.put("print", "print");
		keywords.put("and", "and");		keywords.put("or", "or");
		keywords.put("not", "not");		keywords.put("false", "false");
		keywords.put("true", "true");
	}
	
	/**Public Methods**/
	
	//For getting tokens from file passed by user
	public void tokenizeFile(File sourceFile) throws FileNotFoundException {
		//Reset everything
		tokens = new ArrayList<>();
		start = current = line = 0;
		
		Scanner fileReader = new Scanner(sourceFile);
		try {
			while(fileReader.hasNextLine()) {
				//get a line of text from the file
				source = fileReader.nextLine();  
				line++;
				//scan the line for tokens
				scanToken();
				//reset the pointers for the line to the beginning
				current = start = 0;
			}
			
			addToken("EOF","", line, current);
			fileReader.close();
			printTokens();
		}
		catch(InvalidSyntaxException e) {
			printTokens();
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public void tokenizeLine(String sourceLine) throws FileNotFoundException {
		//Reset everything
		tokens = new ArrayList<>();
		start = current = line = 0;
		source = sourceLine;
		try {
			//scan the line for tokens
			scanToken();
			//reset the pointers for the line to the beginning
			current = start = 0;	
			addToken("EOF","", line, current);
			printTokens();
		}
		catch(InvalidSyntaxException e) {
			printTokens();
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	
	//Reads character in source string, adds appropriate token to tokens list
	public void scanToken() throws InvalidSyntaxException {
		while(current < source.length()) {
			char c = nextChar();  
			start = current - 1;  //Encountering a character is the start of possible token
			switch(c) {
				//Scan for single character tokens
				case '(': addToken("(", "", line, start); break;
				case ')': addToken(")", "", line, start); break;
				case ';': addToken(";", "", line, start); break;
				case '=': addToken("=", "", line, start); break;
				case '<': addToken("<", "", line, start); break;
				case '+': addToken("+", "", line, start); break;
				case '-': addToken("-", "", line, start); break;
				case '*': addToken("*", "", line, start); break;
				case '_': addToken("_", "", line, start); break;			
				//Multiple character tokens
				case '/':
					//Check to see if its a comment section
					if(match('/')) {	
						//Skip comment line
						while(lookAhead() != '\n' && !isAtEnd()) 
							nextChar(); 
					}
					else {
						addToken("/", "", line, start);
					} 
					break;
				case ':':
					if(match('=')) {
						addToken(":=", "", line, start); 
					}
					else {
						System.out.println("Invalid token");
					} 
					break;				
				//Pass through white space created by tabs, spaces, new lines
				case ' ': break;
				case '\r': break; 
				case '\t': break;
				case '\n': line++; break;
				//Now scan for literals, identifiers
				default: 
					//Check for NUM token
					if(Character.isDigit(c)) {
						number();
					}
					//Check for ID/keywords
					else if(Character.isLetter(c)) {
						word();
					}
					//If we reached here it means we encountered something outside the language
					else {
						String message = "Invalid character at line " + line + ", position " + start + ": " + c;
						throw new InvalidSyntaxException(message);
					}			
			}
		}
	}
	
	//Prints out tokens after parsing file
	public void printTokens() {
		System.out.printf("%-22s%-22s%-22s%-22s\n","Token Type","Lexeme","Line", "Position");
		System.out.println("-----------------------------"
				+ "-------------------------------------------");  
		for(Token t : tokens) 
			System.out.printf("%-22s%-22s%-22d%-22d\n", t.type, t.value, t.line,t.position);
		System.out.println();
	}
	
	//Possibly use later for other parts of project for retrieving list of tokens to pass to next analyzer...
	public ArrayList<Token> getTokenList() {
		return tokens;
	}
	
	/**Private Helper methods for LexicalAnalyzer**/
	
	//For determining whether we reached the end of the source string
	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	//Advance and retrieve next character in source string
	private char nextChar() {
		current++; //Current is already pointing to the next character, so move it to the next one
		return source.charAt(current - 1);
	}
	
	//For adding multiple character tokens/literals to the tokens list
	private void addToken(String token, String literal, int line, int position) {
		tokens.add(new Token(token, literal,line, position));
	}
	
	//For finding NUM tokens
	private void number() {
		//Check for subsequent digits
		while(Character.isDigit(lookAhead())) 
			nextChar();
		addToken("NUM", source.substring(start,current), line, start);
	}
	
	//For finding ID or keyword tokens
	private void word() {
		//Look for subsequent valid alphanumerics for ID/keyword
		while(isAlphaNumeric(lookAhead())) 
			nextChar();
		
		//To make sure ID/keyword ends in valid characters
		if(validCharacter(lookAhead(), source.indexOf(lookAhead())))
		{
			//Determine if derived word is a keyword on hash map
			String word = source.substring(start,current).replaceAll(" ", "").toLowerCase();
			String type = keywords.get(word);
			
			//If no token is found in keyword list, type is Identifier
			if(type == null) 
				type = "ID";
			
			addToken(type, source.substring(start, current), line, start);
		}
	}
	
	//Looks at next character in word to figure out if multi-character tokens
	private boolean match(char expected) {
		//If at end of line, should be false
		if(isAtEnd()) 
			return false;
		//If its not the right character, return false
		if(source.charAt(current) != expected) 
			return false;
		//Else its the correct match
		current++;
		return true;
	}
	
	//Looks at next character but doesn't consume it
	private char lookAhead() {
		if(isAtEnd()) 
			return '\0';
		return source.charAt(current);
	}
	
	//Used for scanning possible identifiers and validating them
	private boolean isAlphaNumeric(char c) {
		return Character.isLetter(c) || Character.isDigit(c) || c == '_';
	}
	
	//Used after encountering first valid character at begining of potential ID/keyword
	private boolean validCharacter(char c, int index) {
		boolean flag = false;
		switch(c) {
		//Scan for single character tokens
		case '(': 
		case ')': 
		case ';': 
		case '=': 
		case '<': 
		case '+': 
		case '-': 
		case '*': 
		case '_': flag = true; break; 		
		//Multiple character tokens
		case '/':
			//Check to see if its a comment section
			if(match('/', index+1)) {	

			}
			else {
				flag = true;
			} 
			break;
		case ':':
			if(match('=', index+1)) 
				flag = true;
			break;				
		//Pass through white space created by tabs, spaces, new lines
		case ' ': 
		case '\r': 
		case '\t': 
		case '\n':
		case '\0': flag = true;
		}
		return flag;
	}
	
	//Method override to validate characters following a possible Identifier
	//Ex) raven:=test should be valid
	private boolean match(char expected, int index) {
		//If at end of line, should be false
		if(isAtEnd()) 
			return false;
		//If its not the right character, return false
		if(source.charAt(index) != expected) 
			return false;
		return true;
	}
	
	
	/********************************************************************/
	
	//Throws exception when encountering naughty characters in file
	public class InvalidSyntaxException extends Exception {
		public InvalidSyntaxException(String message) {
			super(message);
		}
	}
}
