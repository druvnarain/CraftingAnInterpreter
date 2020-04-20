import java.util.List;
import java.util.ArrayList;

//Druv Narain
//COSC 455 Project 2
public class Parser {

	private ArrayList<Token> tokenList;
	private int currentSymbol;
	private boolean syntaxCorrect;
	private AST ast;
	
	//Constructor
	public Parser(ArrayList<Token> tokenList) {
		this.tokenList = tokenList;
		currentSymbol = 0;
		syntaxCorrect = false;
		ast = new AST();
	}
	
	public Parser() {
		this.tokenList = null;
		currentSymbol = 0;
		syntaxCorrect = false;
		ast = new AST();
	}
	
	//parses an arraylist of tokens passed from lexical analyzer
	public boolean parseTokens(ArrayList<Token> tokenList) throws ExpressionErrorException {
		this.tokenList = tokenList;
		syntaxCorrect = false;
		currentSymbol = 0;
		ast = new AST();
		expression();
		syntaxCorrect = true;	
		return syntaxCorrect;
	}	
	
	//overload method
	public boolean parseTokens() throws ExpressionErrorException {
		return parseTokens(tokenList);
	}
	
	//Methods detailing grammar for parsing
	private void expression() throws ExpressionErrorException {
		booleanExpression();
		//matchExpected("EOF");
		
	}
	
	private void booleanExpression() throws ExpressionErrorException {
		booleanTerm();
		while(currentTypeIs("or")) {
			ast.insertNode(tokenList.get(currentSymbol));
			getNextSymbol();
			booleanTerm();
		}
		if(!matchExpected("EOF", ")")) {
			String message = generateErrorMessage("EOF", ")");
			throw new ExpressionErrorException(message);
		}
	}
	
	private void booleanTerm() throws ExpressionErrorException {
		booleanFactor();
		if(currentTypeIs("and")) {
			ast.insertNode(tokenList.get(currentSymbol));
			getNextSymbol();
			booleanFactor();
		}
		if(!matchExpected("EOF", "or", ")")) {
			String message = generateErrorMessage("EOF", "or", ")");
			throw new ExpressionErrorException(message);
		}
	}
	
	private void booleanFactor() throws ExpressionErrorException {
		if(currentTypeIs("not")) {
			getNextSymbol();
		}
		arithmeticExpression();
		if(currentTypeIs("<", "="))
		{
			ast.insertNode(tokenList.get(currentSymbol));
			getNextSymbol();
			arithmeticExpression();
		}
		if(!matchExpected("EOF", "or", "and", ")")) {
			String message = generateErrorMessage("EOF", "or", "and", ")");
			throw new ExpressionErrorException(message);
		}
	}
	
	private void arithmeticExpression() throws ExpressionErrorException {
		term();
		while(currentTypeIs("+", "-")) {
			ast.insertNode(tokenList.get(currentSymbol));
			getNextSymbol();
			term();
		}
		if(!matchExpected("EOF", "or", "and", "not", "<", "=", ")")) {
			String message = generateErrorMessage("EOF", "or", "and", "not", "<", "=", ")");
			throw new ExpressionErrorException(message);
		}
	}
	
	private void term() throws ExpressionErrorException {
		factor();
		while(currentTypeIs("*", "/")) {
			ast.insertNode(tokenList.get(currentSymbol));
			getNextSymbol();
			factor();
		}
		if(!matchExpected("EOF", "or", "and", "not", "<", "=", "+", "-", ")")) {
			String message = generateErrorMessage("EOF", "or", "and", "not", "<", "=", "+", "-", ")");
			throw new ExpressionErrorException(message);
		}
	}

	
	private void factor() throws ExpressionErrorException {
		if(currentTypeIs("false", "true", "NUM")) {
			literal();
		}
		else if(currentTypeIs("ID")) {
			identifier();
		}
		else if(currentTypeIs("(")) {
			getNextSymbol();
			expression();
			if(matchExpected(")")) {
				getNextSymbol();
			}
			else {
				String message = generateErrorMessage(")");
				throw new ExpressionErrorException(message);
			}
		}
		else {
			if(!tokenList.get(currentSymbol).type.equalsIgnoreCase("EOF")) {
				String message = generateErrorMessage("literal, identifier, (");
				throw new ExpressionErrorException(message);
			}
		}
	}

	private void literal() throws ExpressionErrorException {
		if(currentTypeIs("false", "true")) 
			booleanLiteral();
		else if (currentTypeIs("NUM")) 
			integerLiteral();	
	}
	
	private void booleanLiteral() throws ExpressionErrorException {
		ast.insertNode(tokenList.get(currentSymbol));
		getNextSymbol();		
	}
	
	private void integerLiteral() throws ExpressionErrorException {
		ast.insertNode(tokenList.get(currentSymbol));
		getNextSymbol();
	}
	
	private void identifier() throws ExpressionErrorException {
		ast.insertNode(tokenList.get(currentSymbol));
		getNextSymbol();	
	}
	
	private void getNextSymbol() {
		if(!tokenList.get(currentSymbol).type.equalsIgnoreCase("EOF"))
			currentSymbol++;
	}
	
	//Helper methods
	
	//Checks next, doesnt consume
	private boolean currentTypeIs(String...possibleTokens) {
		if(currentSymbol < tokenList.size())
		for(String pt : possibleTokens) {
			if(!tokenList.get(currentSymbol).type.equalsIgnoreCase("EOF"))
				if(tokenList.get(currentSymbol).type.equalsIgnoreCase(pt))
					return true;
		}
		return false;			
	}
	
	//Checks next, consume token following this method
	private boolean matchExpected(String... expectedTokens) throws ExpressionErrorException {
		boolean validExpression = false;
		Token currentToken = tokenList.get(currentSymbol);
		for(String expectedType : expectedTokens) {
			if(currentToken.type.equalsIgnoreCase(expectedType)) {
				validExpression = true;	
				break;
			}
		}
		return validExpression;
	}
	
	//For extra credit, tells where grammar is wrong
	private String generateErrorMessage(String... expectedTokens) {
		Token currentToken = tokenList.get(currentSymbol);
		String message = "Error! Encountered " + currentToken.type + " at line " + currentToken.line + 
				", position " + currentToken.position + "\nExpected: ";
		for(String exp : expectedTokens) message += "\"" + exp + "\" ";
		return message;
	}
	
	public void printAST() {
		ast.printTree();
	}
	
	//Abstract syntax tree class for extra credit
	class AST {
		String[] operators = {"or", "and", "not", "<", "=", "+", "-", "/", "*", "(", ")"};
		String[] terminals = {"ID", "NUM", "false", "true"};
		private Node root;
		private Node current;
		public AST() {
			root = new Node();
			current = root;
		}
		
		void insertNode(Token t) {
			Node newnode = new Node();
			newnode.symbol = t;
			
			if(current.symbol == null)
				current = root = newnode;	
			else {
				if(equalsTerminal(current.symbol) && equalsOperator(newnode.symbol)) {
					current.parentOperator = newnode;
					newnode.leftTerminal = current;
					root = current = newnode;
				}
				else if(equalsTerminal(t)) {
					if(current.leftTerminal == null)
						current.leftTerminal = newnode;
					else if(current.rightTerminal == null)
						current.rightTerminal = newnode;
				}				
				else if(equalsOperator(t)) {
						current.rightTerminal.parentOperator = newnode;
						newnode.leftTerminal = current.rightTerminal;
						current.rightTerminal = newnode;
						newnode.parentOperator = current;
						//root = current;
						current = current.rightTerminal;
				}
			}
		}
		
		boolean equalsOperator(Token t) {
			boolean isOperator = false;
			for(String op : operators)
				if(t.type.equalsIgnoreCase(op))
					isOperator = true;
			return isOperator;	
		}
		boolean equalsTerminal(Token t) {
			boolean isTerminal = false;
			for(String ter: terminals)
				if(t.type.equalsIgnoreCase(ter))
					isTerminal = true;
			return isTerminal;	
		}
		
		class Node {
			Token symbol;
			Node parentOperator;
			Node leftTerminal;
			Node rightTerminal;
			
			Node() {
				this.symbol = null;
				this.parentOperator = null;
				this.leftTerminal = null;
				this.rightTerminal = null;
			}
		}
		
		int height(Node root) {
            if (root == null) 
               return 0; 
            else
            { 
                //get height of each subtree
                int leftHeight = height(root.leftTerminal); 
                int rightHeight = height(root.rightTerminal); 

                //use the largest height
                if (leftHeight > rightHeight) 
                    return(leftHeight+1); 
                else return(rightHeight+1);  
            } 
        } 
		
        void printTree() {
            int height = height(root);
            int i;
            System.out.println("Syntax Tree (lower levels of tree belong to last right now on previous line): ");
            System.out.print("\t ");
            for(i = 1; i <= height; i++) {
                printLevel(root, i);
                System.out.println();
                System.out.print("     ");
                for(int y = 1; y< i; y++) System.out.print("  ");
            }
        }

        //method used for printing RBTree in order of level
        void printLevel(Node root, int i) {
            if(root == null)
                return;
            if(i == 1) {
	            	if(equalsOperator(root.symbol)) {
	            		//for(int y = 0; y < padding; y++) System.out.print(" ");
	                    System.out.printf("(%s)    ", root.symbol.type);
	            	}
	            	else { 
	            		//for(int y = 0; y < padding; y++) System.out.print(" ");
	            		System.out.printf("(%s)    ", root.symbol.value);
	            	}
            	
            }
            else if(i > 1) {
                printLevel(root.leftTerminal, i-1);
                printLevel(root.rightTerminal, i-1);
            }
        }
	}
}
