package lexical;

//List of tokens and their "values" in the comments
public enum TokenTypes {
	
	errorToken,			// ("", 0)
	
	timesToken,			// ("*", 1)
	divToken,			// ("/", 2)
	
	plusToken,			// ("+", 11)
	minusToken,			// ("-", 12)
	
	eqlToken,			// ("==", 20)
	neqToken,			// ("!=", 21)
	lssToken,			// ("<", 22)
	geqToken,			// (">=", 23)
	leqToken,			// ("<=", 24)
	gtrToken,			// (">", 25)
	
	periodToken,		// (".", 30)
	commaToken,			// (",", 31),
	openbracketToken,	// ("[", 32)
	closebracketToken,	// ("]", 34)
	closeparenToken,	// (")", 35)
	
	becomesToken,		// ("<-", 40)
	thenToken,			// ("then", 41)
	doToken,			// ("do", 42)
	
	openparenToken,		// ("(", 50)

	number,				// ("", 60)
	ident,				// ("", 61)

	semiToken,			// (";", 70)

	endToken,			// ("}", 80)
	odToken,			// ("od", 81)
	fiToken,			// ("fi", 82)
	
	elseToken,			// ("else", 90)
	
	letToken,			// ("let", 100)
	callToken,			// ("call", 101)
	ifToken,			// ("if", 102)
	whileToken,			// ("while", 103)
	returnToken,		// ("return", 104)
	
	varToken,			// ("var", 110)
	arrToken,			// ("array", 111)
	funcToken,			// ("function", 112)
	procToken,			// ("procedure", 113)
	
	beginToken,			// ("{", 150)
	mainToken,			// (main, 200)
	eofToken;			// ('\0', 255)
	

}
