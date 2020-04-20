import javax.management.RuntimeErrorException;

//Druv Narain
//COSC 455 Project 2
public class ExpressionErrorException extends Exception {
	//Throws exception when encountering naughty characters in file
	public ExpressionErrorException(String message) {
			super(message);
	}
}
