import java.io.*;
import java.util.*;

public class ClassesSymbolTable extends MethodsSymbolTable {
	Boolean IsMain = false;
	String ClassName = null;
	String ExtendClassName = null;
	HashMap<String,String> fieldsMap = new HashMap<String,String>(); //to store fields of class
	LinkedHashMap<String,MethodsSymbolTable> methodsMap = new LinkedHashMap<String,MethodsSymbolTable>(); //to store Methods
}
