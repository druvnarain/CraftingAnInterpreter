
//Druv Narain
//COSC 455 Project 2
public class Token {

	//Instance variables for Token subclass
	int line;			//For line in file
	int position;
	String type;   	//Type of Token (ex. NUM, terminals..)                                      
	String value;      //Actual token in file                                      
                                        
	//Constructor for Token subclass
	Token(String type, String value, int line, int position) {
		this.type = type;                                             
	    this.value = value;                                                                              
	    this.line = line;  
	    this.position = position;
	} 
	Token(String type, String value) {
		this.type = type;
		this.value = value;
	}
}
