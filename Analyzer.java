
	/*****************************************************************************************
	*  File Description:                                                                     *
	*  This file creates the analyzer for the back-end of the program.                       *
	*  The analyzer class acts analyzes both syntax and semantic analyzer                    *
	*****************************************************************************************/

//  --------------------------------------------------  [PACKAGE DECLARATION]
	package src;

//	--------------------------------------------------	[JAVA IMPORTS]
	import java.util.*;
	import javax.swing.*;
	import gui.*;

//  --------------------------------------------------  [CLASS SPECIFICATION]
public class Analyzer {

	/***************
	*  Attributes  *
	***************/
	private ArrayList<Lexeme> lexlist;
	private Hashtable<Lexeme,Lexeme> storage;
	private Set<Lexeme> keys;
	private Lexeme it;
	private Lexeme noob;
	private Lexeme lastLexeme;
	private Parser parser;
	private Terminal terminal;
	private Boolean isComplete;
	private static int current = 0;

	/**************************
	*  Analyzer Constructors  *
	**************************/
	public Analyzer(Terminal terminal) {
		this.terminal = terminal;
		this.it = new Lexeme("IT","Global Variable");
		this.noob = new Lexeme(null,"NOOB Literal");
		this.storage = new Hashtable<Lexeme,Lexeme>();
	}

	/****************
	*    Methods    *
	****************/

	public boolean analyze(ArrayList<Lexeme> lexlist) {
		/*-------------------------------------------------------------------------------------------------
			Method Description: This method starts the syntax analysis.
		-------------------------------------------------------------------------------------------------*/
		this.lexlist = lexlist;
		this.keys = this.storage.keySet();

		while (current != this.lexlist.size()) {
			Lexeme errorFree = determineType(this.lexlist.get(current));
			if (errorFree == null) {
				this.isComplete = true;
				return false;
			}
			this.lastLexeme = this.lexlist.get(current);
			this.lexlist.remove(current);
		} 

		return true;
	}

	private Lexeme determineType(Lexeme lexeme) {

		// lexeme not in program
		if (!lexeme.getLexType().equals("Code Delimiter") && this.isComplete==null) {
			this.terminal.error(8003, 2);
			return null;
		}

		switch(lexeme.getLexType()) {
			case "Code Delimiter": return codeDelimiter(lexeme);
			case "Variable Declaration": return variableDeclaration(lexeme);
			case "Variable Identifier": return variableIdentifier(lexeme);
			case "Assignment Operator": return assignmentOperator(lexeme);
			case "Arithmetic Operator": return arithmeticOperator(lexeme);
			case "Global Variable": return globalVariable(lexeme);
			case "Numbr Literal": return literal(lexeme);
			case "Numbar Literal": return literal(lexeme);
			case "Yarn Literal": return literal(lexeme);
			case "Troof Literal": return literal(lexeme);
			case "Output Keyword": return visible(lexeme);
			case "Concatenation": return smoosh(lexeme);
			case "Input Keyword": return gimmeh(lexeme);
			case "Boolean Operator": return booleanOperator(lexeme);
			default: return null;
		}
	}

	private Lexeme checkStorage(Lexeme lexeme) {
		for (Lexeme i: this.keys) {
			if (lexeme.getRegex().equals(i.getRegex())) return i;
		} return null;
	}

	/****************
	*    Getters    *
	****************/
	public Hashtable<Lexeme,Lexeme> getStorage(){ return this.storage; }
	public ArrayList<Lexeme> getLexemes() {return this.lexlist;}
	public Lexeme getIt() {return this.it;}
	public int getCurrent(){ return this.current;}
	public boolean getStatus() {
		if (this.isComplete == null) return true;
		return this.isComplete;
	}

	/****************
	*    Setters    *
	****************/
	public void reset(){
		this.isComplete = null;
		this.storage.clear();
	}

	public void loadDefault() { this.lastLexeme = null; }
	public void setParser(Parser parser) { this.parser = parser; }

/*******************************************************************************************************************
	>> FUNCTIONS NEEDED FOR ANALYSIS
*******************************************************************************************************************/

	private Lexeme codeDelimiter(Lexeme lexeme) {

		// Condition 1: The program has not been initialized.
		if (this.isComplete == null) {
			if (lexeme.getRegex().equals("HAI")) {
				this.isComplete = false;
				return lexeme;
			} else if (lexeme.getRegex().equals("KTHXBYE")) {
				this.terminal.error(8101, 1);
				return null;
			}

		// Condition 2: The program has been initialized.
		} else if (this.isComplete == false) {
			if (lexeme.getRegex().equals("HAI")) {
				this.terminal.error(8005, 2);
				return null;
			} else if (lexeme.getRegex().equals("KTHXBYE")) {
				this.isComplete = true;
				return lexeme;
			}

		// Condition 3: The program has finished.
		} else if (this.isComplete == true) {
			if (lexeme.getRegex().equals("HAI")) {
				this.terminal.error(8000, 1);
				return null;
			} else if (lexeme.getRegex().equals("KTHXBYE")) {
				this.terminal.error(8001, 1);
				return null;
			} 

		} return lexeme;
	}

	private Lexeme variableDeclaration(Lexeme lexeme) {

		this.lastLexeme = lexeme;
		this.keys = this.storage.keySet();

		// Condition 1: Variable Declaration Without Arguments
		if (this.lexlist.size() == 1) {
			this.terminal.error(8102,2);
			return null;
		} 

		// Condition 2: Variable Declaration With An Argument
		this.lexlist.remove(current);
		Lexeme variable = determineType(this.lexlist.get(current));
		if (variable==null) return null;

		// Condition 3: No Additional Statement Included
		if (this.lexlist.size() == 1) {
			this.storage.put(variable, this.noob);
			return lexeme;
		}

		// Condition 4: Variable Assignment -- (Optional)
		this.lexlist.remove(current);
		if (!this.lexlist.get(current).getLexType().equals("Variable Assignment")) {
			this.terminal.error(8202,2);
			return null;
		}

		// Condition 5: Variable Assignment Without Arguments
		if (this.lexlist.size() == 1) {
			this.terminal.error(8103,2);
			return null;
		}

		// Condition 6: Variable Assignment With An Argument
		this.lexlist.remove(current);
		Lexeme value = determineType(this.lexlist.get(current));
		if (value==null) return null;

		// Condition 7: Variable Declaration With Too Many Arguments
		if (this.lexlist.size() != 1) {
			this.terminal.error(8050,2);
			return null;
		}

		// Condition 8: Variable Value is a variable
		if (value.getLexType().equals("Variable Identifier")) {
			value = checkStorage(value);
			this.storage.put(variable, this.storage.get(value)); return lexeme;
		}

		// Condition 9: Variable Value is NOT a variable
		this.storage.put(variable, this.storage.get(it));
		return lexeme;
	}

	private Lexeme variableIdentifier(Lexeme lexeme) {

		Boolean isFound = false;

		// Search: Lexeme in Table
		Lexeme temporaryVariable = checkStorage(lexeme);
		//this.lastLexeme = temporaryVariable;
		// Condition 1: Variable Not In The Storage
		if(temporaryVariable==null) {

			// Condition 1.1: Variable will be used for declaration
			if (this.lastLexeme == null) {
				System.out.println("VISIBLE went here");
				this.terminal.error(8104,2);
				return null;

			} else if (this.lastLexeme.getLexType().equals("Variable Declaration")) {
				this.storage.put(this.it,lexeme);
				return lexeme;
			} else if (this.lastLexeme.getLexType().equals("Output Keyword")) {
				current++;
				this.terminal.error(8104,2);
				current = 0;
				return null;
			}else {
				System.out.println("VISIBLE went here XXXXX");
				this.terminal.error(8104,2);
				return null;
			}

		// Condition 2: Variable Is In Storage
		} else {

			// Condition 2.1: Statements found before the variable
			if (this.lastLexeme!=null) {
				this.storage.put(this.it,lexeme);
				return lexeme;

			// Condition 2.2: No Statements found before the variable
			} else {

				this.lastLexeme =lexeme;

				// Condition 2.2.1: Statements found after the variable
				if (this.lexlist.size() > 1) this.storage.put(this.it,temporaryVariable);

				// Condition 2.2.2: No following statements
				else this.storage.put(this.it, this.storage.get(temporaryVariable));

				return lexeme;
			}
		}
	}

	private Lexeme assignmentOperator(Lexeme lexeme) {
	
		// Condition 1: No Preceding Statements Found
		if (this.lastLexeme == null) {
			this.terminal.error(8105,2);
			return null;

		} else {

		// Condition 2: No Statements Found After R Operator
			if (this.lexlist.size() == 1) {
				this.terminal.error(8106,2);
				return null;
			}

		// Condition 3: Invalid Argument for Operator
			Lexeme it_temp = checkStorage(it);
			Lexeme variable = this.storage.get(it_temp);
			this.lexlist.remove(current);
			Lexeme value = determineType(this.lexlist.get(current));
			if (value == null) {
				this.terminal.error(8200,2);
				return null;
			}

		// Condition 4: Valid Argument for Operator
			this.storage.put(variable, this.storage.get(it));
		} return lexeme;	
	}

	private Lexeme arithmeticOperator(Lexeme lexeme) {
		
		Object x=null,y=null;

		Lexeme operation = lexeme; int index = this.lexlist.indexOf(lexeme);

		// Condition 1: No Statements found after operator
		if (this.lexlist.size() == index+1) {
			this.terminal.error(8108,2);
			return null;
		}

		// Condition 2: Argument X is invalid
		Lexeme variableX = determineType(this.lexlist.get(index+1));
		Lexeme tempX = null;
		if (variableX==null) {
			this.terminal.error(8108,2);
			return null;
		}

		// Condition 3: Argument X is valid
		switch(variableX.getLexType()) {
			case "Numbr Literal": x = Integer.parseInt(this.storage.get(it).getRegex()); break;
			case "Numbar Literal": x = Float.valueOf(this.storage.get(it).getRegex().trim()).floatValue(); break;
			case "Yarn Literal":
				tempX = this.parser.parse(variableX.getRegex());
				if (tempX.getLexType().equals("Numbr Literal")) x = Integer.parseInt(tempX.getRegex());
				else if (tempX.getLexType().equals("Numbar Literal")) x = Float.valueOf(tempX.getRegex().trim()).floatValue();
				else {this.terminal.error(8500,2); return null; }
				break;
			case "Variable Identifier":
				tempX = this.checkStorage(variableX);
				if (this.storage.get(this.checkStorage(variableX)).getLexType().equals("Numbr Literal")) x = Integer.parseInt(this.storage.get(this.checkStorage(variableX)).getRegex());
				else if (this.storage.get(this.checkStorage(variableX)).getLexType().equals("Numbar Literal")) x = Float.valueOf(this.storage.get(this.checkStorage(variableX)).getRegex().trim()).floatValue();
				else if (this.storage.get(this.checkStorage(variableX)).getLexType().equals("Yarn Literal")) {
					tempX = this.parser.parse(this.storage.get(this.checkStorage(variableX)).getRegex());
					if (tempX.getLexType().equals("Numbr Literal")) x = Integer.parseInt(tempX.getRegex());
					else if (tempX.getLexType().equals("Numbar Literal")) x = Float.valueOf(tempX.getRegex().trim()).floatValue();
					else {this.terminal.error(8500,2); return null; }
				}
				break;
			default: return null;
		}

		// Condition 4: Uses AN operator
		this.lexlist.remove(index+1);
		if(!this.lexlist.get(index+1).getLexType().equals("Operation Keyword")) {
			this.terminal.error(8107,2);
			return null;
		} this.lexlist.remove(index+1);

		// Condition 5: No statements found after AN Operator
		if (this.lexlist.size() == index+1) {
			this.terminal.error(8108,2);
			return null;
		}

		// Condition 6: Argument Y is invalid
		Lexeme variableY = determineType(this.lexlist.get(index+1));
		Lexeme tempY = null;
		if (variableY==null) {
			return null;
		}

		// Condition 7: Argument Y is valid
		switch(variableY.getLexType()) {
			case "Numbr Literal": y = Integer.parseInt(this.storage.get(it).getRegex()); break;
			case "Numbar Literal": y = Float.valueOf(this.storage.get(it).getRegex().trim()).floatValue(); break;
			case "Yarn Literal":
				tempY = this.parser.parse(variableY.getRegex());
				if (tempY.getLexType().equals("Numbr Literal")) y = Integer.parseInt(tempY.getRegex());
				else if (tempY.getLexType().equals("Numbar Literal")) y = Float.valueOf(tempY.getRegex().trim()).floatValue();
				else {this.terminal.error(8500,2); return null;}
				break;
			case "Variable Identifier":
				tempY = this.checkStorage(variableY);
				else if (this.storage.get(this.checkStorage(variableY)).getLexType().equals("Numbr Literal")) y = Integer.parseInt(this.storage.get(this.checkStorage(variableY)).getRegex());
				else if (this.storage.get(this.checkStorage(variableY)).getLexType().equals("Numbar Literal")) y = Float.valueOf(this.storage.get(this.checkStorage(variableY)).getRegex().trim()).floatValue();
				else if (this.storage.get(this.checkStorage(variableY)).getLexType().equals("Yarn Literal")) {
					tempY = this.parser.parse(this.storage.get(this.checkStorage(variableY)).getRegex());
					if (tempY.getLexType().equals("Numbr Literal")) y = Integer.parseInt(tempY.getRegex());
					else if (tempY.getLexType().equals("Numbar Literal")) y = Float.valueOf(tempY.getRegex().trim()).floatValue();
					else {this.terminal.error(8500,2); return null; }
				}
				break;
			default: this.terminal.error(8203,2); return null;
		} this.lexlist.remove(index+1);

		// Condition 8: Both X and Y are valid
		Lexeme result = new Lexeme(null,"NOOB Literal");

			// Condition 8.1: Both X and Y are integers
		if (x instanceof Integer && y instanceof Integer) {
			switch (operation.getRegex()) {
				case "SUM OF": result = integerOperator(x,y,1); break;
				case "DIFF OF": result = integerOperator(x,y,2); break;
				case "PRODUKT OF": result = integerOperator(x,y,3); break;
				case "QUOSHUNT OF": result = integerOperator(x,y,4); break;
				case "MOD OF": result = integerOperator(x,y,5); break;
				case "BIGGR OF": result = integerOperator(x,y,6); break;
				case "SMALLR OF": result = integerOperator(x,y,7); break;
				default: break;
			}
		} else if (x instanceof Float || y instanceof Float) {
			switch (operation.getRegex()) {
				case "SUM OF": result = floatOperator(x,y,1); break;
				case "DIFF OF": result = floatOperator(x,y,2); break;
				case "PRODUKT OF": result = floatOperator(x,y,3); break;
				case "QUOSHUNT OF": result = floatOperator(x,y,4); break;
				case "MOD OF": result = floatOperator(x,y,5); break;
				case "BIGGR OF": result = floatOperator(x,y,6); break;
				case "SMALLR OF": result = floatOperator(x,y,7); break;
				default: break;
			}
		}
		
		this.storage.put(it,result);
		return result;
	}

	private Lexeme integerOperator(Object x, Object y, int code) {

		int a = (int) x; int b = (int) y; int answer = 0;

		switch (code) {
			case 1: answer = a+b; break;
			case 2: answer = a-b; break;
			case 3: answer = a*b; break;
			case 4: answer = a/b; break;
			case 5: answer = a%b; break;
			case 6:
				if (a>b) answer = a;
				else answer = b;
				break;
			case 7:
				if (a<b) answer = a;
				else answer = b;
				break;
			default: break;
		}

		return new Lexeme(Integer.toString(answer),"Numbr Literal");
	}

	private Lexeme floatOperator(Object x, Object y, int code) {

		float answer=0;
		float a = ((Number)x).floatValue();
		float b = ((Number)x).floatValue();

		switch (code) {
			case 1: answer = a+b; break;
			case 2: answer = a-b; break;
			case 3: answer = a*b; break;
			case 4: answer = a/b; break;
			case 5: answer = a%b; break;
			case 6:
				if (a>b) answer = a;
				else answer = b;
				break;
			case 7:
				if (a<b) answer = a;
				else answer = b;
				break;
			default: break;
		}

		return new Lexeme(Float.toString(answer),"Numbar Literal");
	}

	private Lexeme globalVariable (Lexeme lexeme) {

		if (this.lastLexeme == null) {
			return lexeme;
		} else if (this.lastLexeme.getLexType().equals("Variable Declaration")) {
			this.terminal.error(8006,1);
			return null;
		} return lexeme;
	}

	private Lexeme literal(Lexeme lexeme) {
		this.storage.put(this.it, lexeme);
		return lexeme;
	}

	private Lexeme gimmeh(Lexeme lexeme) {


		this.lexlist.remove(current);
		if(this.lexlist.size() == 0) {
			this.terminal.error(8109, 2);
			return null;
		}
		
		Lexeme variable = this.checkStorage(this.lexlist.get(current));

		if (variable != null) {
			JFrame popUp = new JFrame();
			String input = JOptionPane.showInputDialog(popUp,"KYAH PENGENG INPUT... KYAH. TEH. (o3o)", null);
			this.storage.put(this.it, new Lexeme(input, "Yarn Literal"));
			this.storage.put(variable, this.storage.get(this.it));
			return lexeme;
		}

		this.terminal.error(8104,2);
		return null;

		
	}

	private Lexeme visible(Lexeme lexeme) {

		// Condition 1: Preceding Statements Before Visible
		if (this.lastLexeme != null) {
			this.terminal.error(8300, 2);
			return null;

		} else {
			this.lastLexeme = lexeme;
		// Condition 2: No Statements Found After Visible
			if (this.lexlist.size() == 1) {
				this.terminal.error(8301, 2);
				return null;
			}

		// Condition 3: Valid Statements Found
			String message = "";
			while (this.lexlist.size() > 1) {
				
				Lexeme temporaryLexeme = determineType(this.lexlist.get(current+1));

				// Condition 3.1: The statement to be printed is invalid
				if (temporaryLexeme==null) return null;

				// Condition 3.2: The statement to be printed is a literal
				else if (temporaryLexeme.getLexType().equals("Numbr Literal") || temporaryLexeme.getLexType().equals("Numbar Literal") || temporaryLexeme.getLexType().equals("Yarn Literal") || temporaryLexeme.getLexType().equals("Troof Literal")){
					temporaryLexeme = new Lexeme(this.parser.removeQuotes(temporaryLexeme.getRegex()),temporaryLexeme.getLexType());
					message += temporaryLexeme.getRegex() + " ";

				// Condition 3.3: The statement to be printed is a variable
				} else if (temporaryLexeme.getLexType().equals("Variable Identifier") || temporaryLexeme.getLexType().equals("Global Variable")) {
					message += this.storage.get(this.checkStorage(temporaryLexeme)).getRegex() + " ";

				// Condition 3.4: The statements were not met
				} else return null;

		 		this.lexlist.remove(current+1);
			
			} message+= "\n";

			this.storage.put(this.it, new Lexeme(message, "Yarn Literal"));
			this.terminal.print(message);
				
		} return lexeme;
	}

	private Lexeme smoosh(Lexeme lexeme) {

		String concatenated = "", tailString = "";
		int index = this.lexlist.indexOf(lexeme);

		// Condition 1: Invalid X Argument
		Lexeme variableX = determineType(this.lexlist.get(index+1));
		if (variableX==null) {
			this.terminal.error(8602,2);
			return null;
		} concatenated = this.storage.get(it).getRegex();

		// Condition 2: No values after SMOOSH keyword
		if(this.lexlist.size() == index+1) {
			this.terminal.error(8107,2);
			return null;
		} 

		// Condition 3: Infinite Arity
		while (this.lexlist.size() != index+1) {

			// Condition 3: Uses AN keyword
			this.lexlist.remove(index+1);
			if(!this.lexlist.get(index+1).getLexType().equals("Operation Keyword")) {
				this.terminal.error(8107,2);
				return null;
			}

			// Condition 4: Invalid Y Argument
			this.lexlist.remove(index+1);
			Lexeme variableY = determineType(this.lexlist.get(index+1));
			if (variableY == null) {
				this.terminal.error(8602,2);
				return null;
			} tailString = this.storage.get(it).getRegex();

			this.lexlist.remove(index+1);
			concatenated += tailString;
		}

		this.storage.put(it, new Lexeme(concatenated, "Yarn Literal"));
		return new Lexeme(concatenated, "Yarn Literal");
	}


	private Lexeme booleanOperator(Lexeme lexeme) {

		Object x=null,y=null;

		Lexeme operation = lexeme; int index = this.lexlist.indexOf(lexeme);

		// Condition 1: No Statements found after operator
		if (this.lexlist.size() == index+1) {
			this.terminal.error(8110,2);
			return null;
		}

		// Condition 2: Argument X is invalid
		Lexeme variableX = determineType(this.lexlist.get(index+1));
		Lexeme tempX = null;
		if (variableX == null) {
			this.terminal.error(8108,2);
			return null;
		} 

		// Condition 3: Argument X is valid

		switch(variableX.getLexType()) {
			case "Troof Literal" : 
				if (variableX.getRegex().equals("WIN")) { x = true; }
				else if (variableX.getRegex().equals("FAIL")) { x = false; }
				break;

			case "Variable Identifier" : 
				tempX = this.checkStorage(variableX);
				if (this.storage.get(tempX).getLexType().equals("Troof Literal")){
					if (this.storage.get(tempX).getRegex().equals("WIN")) { x = true; }
					else if (this.storage.get(tempX).getRegex().equals("FAIL")) { x = false; }
					break;
				} else {
					this.terminal.error(8700, 2);
					return null;
				}
				
			default: break;
		}

		// Condition 4: Uses AN operator
		this.lexlist.remove(index+1);
		if(!this.lexlist.get(index+1).getLexType().equals("Operation Keyword")) {
			this.terminal.error(8107,2);
			return null;
		} this.lexlist.remove(index+1);

		// Condition 5: No statements found after AN Operator
		if (this.lexlist.size() == index+1) {
			this.terminal.error(8108,2);
			return null;
		}

		// Condition 6: Argument Y is invalid
		Lexeme variableY = determineType(this.lexlist.get(index+1));
		Lexeme tempY = null;
		if (variableY == null) {
			this.terminal.error(8108,2);
			return null;
		} 

		// Condition 7: Argument Y is valid

		switch(variableY.getLexType()) {
			case "Troof Literal" : 
				if (variableY.getRegex().equals("WIN")) { y = true; }
				else if (variableY.getRegex().equals("FAIL")) { y = false; }
				break;

			case "Variable Identifier" : 
				tempY = this.checkStorage(variableY);
				if (this.storage.get(tempY).getLexType().equals("Troof Literal")){
					if (this.storage.get(tempY).getRegex().equals("WIN")) { y = true; }
					else if (this.storage.get(tempY).getRegex().equals("FAIL")) { y = false; }
					break;
				} else {
					this.terminal.error(8700, 2);
					return null;
				}
				
			default: break;
		}

		// Condition 8: Both X and Y are valid
		Lexeme result = new Lexeme(null,"NOOB Literal");

		// Condition 8.1: Both are TROOFs
		if (x instanceof Boolean && y instanceof Boolean) {
			switch (operation.getRegex()) {
				case "BOTH OF": result = boolOperator(x,y,1); break;
				case "EITHER OF": result = boolOperator(x,y,2); break;
				case "WON OF": result = boolOperator(x,y,3); break;
				// case "NOT": result = boolOperator(x,y,4); break;
				default: break;
			}
		}

		this.lexlist.remove(index+1);
		System.out.println("result: "+ result.getRegex());
		this.storage.put(this.it,result);
		return result; 
	}

	private Lexeme boolOperator(Object x, Object y, int code) {
		Boolean a = (Boolean) x; Boolean b = (Boolean) y; boolean answer = true;

		System.out.println(a);
		System.out.println(b);


		switch (code) {
			case 1: answer = a && b; break;
			case 2: answer = a || b; break;
			case 3: 
				if (a == b) { // xor
					answer = false;
				} else { answer = true; }

				break;
			// case 4: answer = a/b; break;
			default: break;
		}

		String result = "";
		if(answer == true) result = "WIN";
		else result = "FAIL";

		return new Lexeme(result,"Troof Literal");
	}
}