package lexical;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Tokenizer {
	
	private List<Token> tokenList;
	private String fileName;
	public static final String UTF8_BOM = "\uFEFF";
	
	public void tokenize(String fileName){
		
		this.fileName = fileName;
		String fileString = null;
		StringBuilder buildToken = new StringBuilder();
		List<Token> tokenList = new ArrayList<Token>();

		try {		
			fileString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
			fileString = deleteUTF8BOM(fileString);
								
		} catch (FileNotFoundException e){ //Catch if file isn't found.		
		System.out.println("File not found.");
		e.printStackTrace();
		System.exit(1);				
		} catch (IOException e){
			System.out.println("Could not read file.");
			e.printStackTrace();
			System.exit(2);	
		}
		
		char[] fileCharArray = fileString.toCharArray();
		for (int i = 0; i< fileCharArray.length; ){
			
			//Get rid of comments
			if ( fileCharArray[i] == '#' || (fileCharArray[i] == '/' && fileCharArray[i+1] =='/')){
				while (fileCharArray[i] != '\r' && fileCharArray[i] != '\n'){
					i++;
				}
				continue;
			}
			else if (Character.isWhitespace(fileCharArray[i])){ //Get rid of whitespace
				i++;
				continue;
			}
			else if (Character.isLetter(fileCharArray[i])){ //Could be an identifier or keyword
				while (Character.isLetterOrDigit(fileCharArray[i])){
					buildToken.append(fileCharArray[i++]);
				}

			}
			else if (Character.isDigit(fileCharArray[i])){ //Create NUMBER token
				while (Character.isDigit(fileCharArray[i])){
					buildToken.append(fileCharArray[i++]);
				}
			}
			else if (fileCharArray[i] == '+' || fileCharArray[i] == '*' || fileCharArray[i] == '-' || 
					 fileCharArray[i] == '/' || fileCharArray[i] == '.' || fileCharArray[i] == ';' || 
					 fileCharArray[i] == '[' || fileCharArray[i] == ']' || fileCharArray[i] == '{' ||
					 fileCharArray[i] == '}' || fileCharArray[i] == '(' || fileCharArray[i] == ')' ||
					 fileCharArray[i] == ','){ //All of these are tokens

				buildToken.append(fileCharArray[i++]);
			}
			else if (fileCharArray[i] == '<') { //Could be assignment, less than, less than equal
				buildToken.append(fileCharArray[i++]);
				if (fileCharArray[i] == '-' || fileCharArray[i] == '='){
					buildToken.append(fileCharArray[i++]);					
				}
			}
			else if (fileCharArray[i] == '>'){
				buildToken.append(fileCharArray[i++]);
				if 	(fileCharArray[i] == '=')
					buildToken.append(fileCharArray[i++]);
			}
			else if (fileCharArray[i] == '!'){
				buildToken.append(fileCharArray[i++]);
				if 	(fileCharArray[i] == '=')
					buildToken.append(fileCharArray[i++]);
			}
			else if (fileCharArray[i] == '='){
				buildToken.append(fileCharArray[i++]);
				if 	(fileCharArray[i] == '=')
					buildToken.append(fileCharArray[i++]);
			}
			else {
				buildToken.append(fileCharArray[i++]); //This should be an invalid character
			}				
			
			//Regardless we add the token to our list
			tokenList.add(Token.getType(buildToken.toString()));
			buildToken.setLength(0); //empty out our buffer
			
		}
		
		this.tokenList = tokenList;
		
	}
	
	public List<Token> getTokenList(){
		return tokenList;
	}
	
    private static String deleteUTF8BOM(String fileString) {
        if (fileString.startsWith(UTF8_BOM)) {
            fileString = fileString.substring(1);
        }
        return fileString;
    }

	public String getFileName() {
		return this.fileName;
	}
		

}



