import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class EvalVisitor extends  helpVisitor{

	//for the creation of Symbol Table
	public static SymbolTable st = new SymbolTable();
	public static ClassesSymbolTable currentCst = new ClassesSymbolTable();
	public static MethodsSymbolTable currentMst = new MethodsSymbolTable();
	public static Boolean insideMethod = false;

	//for the calculation of offsets
	public static String keyOffset;
	public static int varCalc;
	public static int methodCalc;


	/**
	* f0 -> "class"
	* f1 -> Identifier
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier
	* f12 -> ")"
	* f13 -> "{"
 	* f14 -> (VarDeclaration)*
	* f15 -> (Statement)*
	* f16 -> "}"
	* f17 -> "}"
	*/
    public String visit(MainClass mc, String argu)
    {
		mc.f0.accept(this,argu); //"class"
		String s1 = mc.f1.accept(this,argu); //Identifier
		mc.f2.accept(this,argu); // "{"
		mc.f3.accept(this,argu); // "public"
		mc.f4.accept(this,argu); // "static"
		mc.f5.accept(this,argu); // "void"
		mc.f6.accept(this,argu); // "main"
		mc.f7.accept(this,argu); // "("
		mc.f8.accept(this,argu); // "String"
		mc.f9.accept(this,argu); // "["
		mc.f10.accept(this,argu); // "]"
		String s2 = mc.f11.accept(this,argu); //Identifier
		mc.f12.accept(this,argu); // ")"
		mc.f13.accept(this,argu); // "{"
		currentCst.IsMain = true;
		mc.f14.accept(this,argu); // (VarDeclaration)*
		mc.f15.accept(this,argu); // "(Statement)*
		mc.f16.accept(this,argu); // "}"
		mc.f17.accept(this,argu); // "}"

		ClassesSymbolTable cst = new ClassesSymbolTable();
		//System.out.println("\nMainClassName : " +s1 + "\n");
		currentCst.ClassName = s1;
		//currentCst.ExtendClassName = null;
		st.classesMap.put("main",currentCst);
		currentCst = cst;


		return null;
    }
    /**
    * f0 -> "class"
    * f1 -> Identifier
    * f2 -> "{"
    * f3 -> (VarDeclaration)*
    * f4 -> (MethodDeclaration)*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration cd,String argu)
    {
        cd.f0.accept(this,argu); //Class
        String cclassname = cd.f1.accept(this,argu); //Identifier
		//search the name of class
		//so it will not get redifined
		if(st.search_classmap(cclassname)){
			System.out.println(">>ERROR");
			System.out.println(">>Class:"+cclassname+" previsouly defined !");
			System.out.println("*	*	*	*	*	*	*	*	*	*	*");
			System.exit(0);
		}
		currentCst.ClassName = cclassname;
        cd.f2.accept(this,argu); // "{"
		insideMethod = false; //we are inside a class
        cd.f3.accept(this,argu); //(VarDeclaration)*
        cd.f4.accept(this,argu); //(MethodDeclaration)*
        cd.f5.accept(this,argu); //"}"

		ClassesSymbolTable cst = new ClassesSymbolTable();
    //    System.out.println("\nNotMainClassName : " + cclassname + "\n");
		//currentCst.ExtendClassName = null;
		//currentCst.IsMain = false;
		st.classesMap.put(currentCst.ClassName,currentCst);
		currentCst = cst;
		varCalc = 0; //we start over the calculations for every new class that
		methodCalc = 0;// does not extend an existing one
        return null;
    }
	/**
    * f0 -> "class"
    * f1 -> Identifier
	* f2 -> "extends"
	* f3 -> Identifier
    * f4 -> "{"
    * f5 -> (VarDeclaration)*
    * f6 -> (MethodDeclaration)*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration ced,String argu)
    {
		ced.f0.accept(this,argu); //Class
        String className1 = ced.f1.accept(this,argu); //Identifier
		if(st.search_classmap(className1)){
			System.out.println(">>ERROR");
			System.out.println(">>Class:"+className1+" previsouly defined!");
			System.out.println("*	*	*	*	*	*	*	*	*	*	*");
			System.exit(0);
		}
		currentCst.ClassName = className1;
        ced.f2.accept(this,argu); // "extends"
        String extendsClassName1 = ced.f3.accept(this,argu); // Identifier
		currentCst.ExtendClassName = extendsClassName1;
		//we continue the calculation of offsets from where the extended class
		//finished its calculation
		//search in arraylist for the extended class
		int holditeratorVal=-1;
		int holditeratorMethod=-1;
		for(int i=0;i<st.keyToOffsetMap.size();i++){
			String help = st.keyToOffsetMap.get(i);
			String[] output = help.split("\\.");
			String ecn = output[0];
			//System.out.println(ecn);
			if(ecn.equals(extendsClassName1) && (st.varOrMethod.get(i)).contains("var")){
				holditeratorVal = i;
			}
			if(ecn.equals(extendsClassName1) && (st.varOrMethod.get(i)).equals("method")){
				holditeratorMethod = i;
			}
		}
		if(holditeratorVal == -1 && holditeratorMethod == -1){
			if(currentCst.ExtendClassName.equals((EvalVisitor.st.classesMap.get("main")).ClassName)){
			}
			else{
				System.out.println(">>ERROR");
				System.out.println(">>Extended Class:"+currentCst.ExtendClassName+"not defined!");
				System.out.println("*	*	*	*	*	*	*	*	*	*	*");
				System.exit(0);
			}
		}
		if(holditeratorVal != -1){
			varCalc = st.valueToOffsetMap.get(holditeratorVal);
			if(st.varOrMethod.get(holditeratorVal).equals("var-int")){
				varCalc+=4;
			}
			else if(st.varOrMethod.get(holditeratorVal).equals("var-boolean")){
				varCalc+=1;
			}
			else if(st.varOrMethod.get(holditeratorVal).equals("var-pointer")){
				varCalc+=8;
			}
		}
		if(holditeratorMethod != -1){
			methodCalc = st.valueToOffsetMap.get(holditeratorMethod);
			methodCalc+=8;

		}

        ced.f4.accept(this,argu); // "{"
		insideMethod = false; //we are inside a class
        ced.f5.accept(this,argu); // (VarDeclaration)*
		ced.f6.accept(this,argu); // (MethodDeclaration)*
		ced.f7.accept(this,argu); // "}"

		//System.out.println("\nExtendedClassName : " + extendsClassName1 + "\n");
		ClassesSymbolTable cst = new ClassesSymbolTable();
		//currentCst.IsMain = false;
		st.classesMap.put(currentCst.ClassName,currentCst);
		currentCst = cst;
		return null;


	}
    /**
    * f0 -> Type
    * f1 -> Identifier
    * f2 -> ";"
    */
    public String visit(VarDeclaration vd,String argu)
    {
        String t1 = vd.f0.accept(this,argu); //Type
        String id1 = vd.f1.accept(this,argu); //Identifier
        vd.f2.accept(this,argu); // ";"

	//	System.out.println("\nVarDeclaration of: " + id1 + "->Type:"+ t1 + "\n" );
	//	System.out.println("\nI'm inside class : " + currentCst.ClassName);
	//	System.out.println("I am inside a method  : " + insideMethod);
		if(insideMethod){
			//search for a duplicate name in this.method
			if(! (st.search_vars(id1,currentMst.variablesMap)) && ! (st.search_vars(id1,currentMst.parameterListMap)) ){
				currentMst.variablesMap.put(id1,t1);
			}
			else{
				System.out.println(">>ERROR");
				System.out.println(">>Variable:"+ id1 +" already declared!");
				System.out.println("*	*	*	*	*	*	*	*	*	*	*");
				System.exit(0);
			}
		}
		else{
			//search for a duplicate name in this classes fields
			if(! (st.search_fields(id1,currentCst.fieldsMap)) ){
				currentCst.fieldsMap.put(id1,t1);
			}
			else{
				System.out.println(">>ERROR");
				System.out.println(">>Variable:"+ id1 +" already declared!");
				System.out.println("*	*	*	*	*	*	*	*	*	*	*");
				System.exit(0);
			}
		}
	//	System.out.println("class:"+currentCst.ClassName+"|varName:"+id1);
		if(!insideMethod && !currentCst.IsMain ){
			if(t1.equals("int")){
				keyOffset = currentCst.ClassName + "." + id1;
				st.keyToOffsetMap.add(keyOffset);
				st.varOrMethod.add("var-int");
				st.valueToOffsetMap.add(varCalc);
				varCalc += 4;
			}
			else if(t1.equals("boolean")){
				keyOffset = currentCst.ClassName + "." + id1;
				st.keyToOffsetMap.add(keyOffset);
				st.varOrMethod.add("var-boolean");
				st.valueToOffsetMap.add(varCalc);
				varCalc +=1;
			}
			else{
				keyOffset = currentCst.ClassName + "." + id1;
				st.keyToOffsetMap.add(keyOffset);
				st.varOrMethod.add("var-pointer");
				st.valueToOffsetMap.add(varCalc);
				varCalc +=8;
			}
		}


        return null;
    }
    /**
    * f0 -> "public"
    * f1 -> Type
    * f2 -> Identifier
    * f3 -> "("
    * f4 -> (FormalParameterList)?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> (VarDeclaration)*
    * f8 -> (Statement)*
    * f9 -> "return"
    * f10 -> Expression
    * f11 -> ";"
    * f12 -> "}"
    */
    public String visit(MethodDeclaration md,String argu)
    {
        md.f0.accept(this,argu); // "public"
        String returnType = md.f1.accept(this,argu); //Type
		currentMst.ReturnTypeOfMethod = returnType;
        String mdName = md.f2.accept(this,argu); //Identifier
		currentMst.MethodName = mdName;
		//first we check if the method is already defined in the class
        if(st.search_methodsDuplicate_error(mdName,currentCst.methodsMap)){
            System.out.println(">>ERROR");
            System.out.println(">>Method: "+mdName+" already defined in "+ currentCst.ClassName +" class!");
            System.out.println("*	*	*	*	*	*	*	*	*	*	*");
            System.exit(0);
        }
		boolean mybool=true;
		for(int i=0;i<st.keyToOffsetMap.size();i++){
			String help = st.keyToOffsetMap.get(i);
			String[] output = help.split("\\.");
			String ecn = output[1];
			String ecn1 = output[0];
			if(mdName.equals(ecn) && (st.varOrMethod.get(i)).equals("method") && !(currentCst.ClassName.equals(ecn1))){
				if(currentCst.ExtendClassName == null){
					keyOffset = currentCst.ClassName + "." + mdName;
					st.keyToOffsetMap.add(keyOffset);
					st.varOrMethod.add("method");
					st.valueToOffsetMap.add(methodCalc);
					methodCalc += 8;
				}
				else{ //override
					//den kanei kati
				}
				mybool=false;
			}
		}
		if(mybool){
			keyOffset = currentCst.ClassName + "." + mdName;
			st.keyToOffsetMap.add(keyOffset);
			st.varOrMethod.add("method");
			st.valueToOffsetMap.add(methodCalc);
			methodCalc += 8;
		}
        md.f3.accept(this,argu); //"("
        if(md.f4.present()){
    	       md.f4.accept(this, argu); //(FormalParameterList)?
    	}
        md.f5.accept(this,argu); //")"
        md.f6.accept(this,argu); //"{"
		insideMethod = true; //we are inside a method
        md.f7.accept(this,argu); // (VarDeclaration)*
        md.f8.accept(this,argu); // (Statement)*
        md.f9.accept(this,argu); // "return"
        md.f10.accept(this,argu); // Expression
        md.f11.accept(this,argu); //";"
        md.f12.accept(this,argu); //"}"

		MethodsSymbolTable mst = new MethodsSymbolTable();
		currentCst.methodsMap.put(mdName,currentMst);
		currentMst = mst;
        //System.out.println("\nMethodDeclaration of: " + mdName );
        //System.out.println("->returnType:"+ returnType + "\n" );
        return null;
    }
	/**
	* f0 ->type
	* f1 ->Identifier
	*/
	public String visit(FormalParameter fp,String argu)
    {
		String type1 = fp.f0.accept(this,argu);
		String id1 = fp.f1.accept(this,argu);

		currentMst.parameterListMap.put(id1,type1);
		//System.out.println("\nParameter : " + id1 + " |type: " + type1);
		return null;
	}

}
