all: compile

compile:
	java -jar ../jtb132di.jar minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac helpVisitor.java
	javac SymbolTable.java
	javac ClassesSymbolTable.java
	javac MethodsSymbolTable.java
	javac EvalVisitor.java
	javac LlvmVisitor.java
	javac VtableFunctions.java
	javac Main.java
clean:
	rm -f *.class *~
