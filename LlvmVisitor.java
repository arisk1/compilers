import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.io.*;
import java.lang.*;

public class LlvmVisitor extends helpVisitor{

    public static ClassesSymbolTable currentCst = new ClassesSymbolTable();
    public static MethodsSymbolTable currentMst = new MethodsSymbolTable();
    public static Boolean nestedEpxr = false;
    public static Boolean returnflag=false;
    public static Boolean returnisID = false;
    public static Boolean allocationbool = false;
    public static Boolean exprlistalloc = false;
    public static String returnTypeName = null;
    public static int intlit = 0;
    public static int TorF = -1; // 0 for false 1 for true
    public static String exprtype1 = null;
    public static String msgexprtype = null;
    public static ArrayList<String> exprList = new ArrayList<String>();
    public static Boolean exprlistbool = false;
    public static int ifLabels = 0;
    public static int loopLabels = 0;
    public static int oobLabels = 0;
    public static int andLabels = 0;
    public static int arrallocLabels = 0;
    public static int callocBytes = 0; //starting point
    public static int call_register = -1; // keep register for call function
    String VtableInfo; //name of file
    String llvmFileBuffer; //buffer which will be written in .ll file
    VtableFunctions v;
    int registerCounter;
    LlvmVisitor(String ReadVtable){
        this.VtableInfo = ReadVtable;
        this.v = new VtableFunctions();
        this.registerCounter = 0;
        //first line of llvm file is standard
        //so we add it to the buffer + create vtables
        this.llvmFileBuffer = "@." + VtableInfo + "_vtable = global [0 x i8*] []" + v.createVt(VtableInfo) + v.writeStandardFuncs();
    }

    public String SearchInVtable(String x) throws IOException {
        ArrayList <String> VtableStrings = new ArrayList<String>();
        VtableStrings = v.readVtableFromTxt(VtableInfo);
        System.out.println(VtableStrings);
        return "notready";
    }

     public String visit(Goal g,String argu) {
         //return  llvm file as a buffer
         // we create the folfer with llvm files
         File dir = new File("./LLVMfiles");
         // Tests whether the directory denoted by this abstract pathname exists.
         boolean exists = dir.exists();
         if(!exists){
             System.out.println("Directory " + dir.getPath() + " created ");
             dir.mkdir();
         }
         else{
             System.out.println("Directory " + dir.getPath() + " exists ");
         }
         g.f0.accept(this, argu); //main class
         g.f1.accept(this,argu); // (TypeDeclaration)*
         return this.llvmFileBuffer;
     }


    public String visit(MainClass mc, String argu)
    {
		mc.f0.accept(this,argu); //"class"
		String s1 = mc.f1.accept(this,argu); //Identifier
        currentCst.ClassName = s1;
        currentCst.IsMain = true;
        llvmFileBuffer += "\ndefine i32 @main(){\n";
		String s2 = mc.f11.accept(this,argu); //Identifier
		mc.f14.accept(this,argu); // (VarDeclaration)*
		mc.f15.accept(this,argu); // "(Statement)*
        llvmFileBuffer += "\n\tret i32 0\n"; //just for main
        llvmFileBuffer += "\n}\n";
        ClassesSymbolTable cst = new ClassesSymbolTable();
        currentCst = cst;
		return null;
    }
    public String visit(ClassDeclaration cd,String argu)
   {
       String cclassname = cd.f1.accept(this,argu); //Identifier
       currentCst.ClassName = cclassname;
      // insideMethod = false; //we are inside a class
       //cd.f3.accept(this,argu); //(VarDeclaration)*
       cd.f4.accept(this,argu); //(MethodDeclaration)*
       ClassesSymbolTable cst = new ClassesSymbolTable();
       currentCst = cst;
       return null;
   }

   public String visit(ClassExtendsDeclaration ced,String argu)
    {
        String className1 = ced.f1.accept(this,argu); //Identifier
        currentCst.ClassName = className1;
        String extendsClassName1 = ced.f3.accept(this,argu); // Identifier
        currentCst.ExtendClassName = extendsClassName1;
        //insideMethod = false; //we are inside a class
        //ced.f5.accept(this,argu); // (VarDeclaration)*
        ced.f6.accept(this,argu); // (MethodDeclaration)*
        ClassesSymbolTable cst = new ClassesSymbolTable();
        currentCst = cst;
        return null;
    }

    public String visit(MethodDeclaration md,String argu)
 {
     registerCounter = 0; //reset registers
     String returnType = md.f1.accept(this,argu); //Type
     currentMst.ReturnTypeOfMethod = returnType;
     String mdName = md.f2.accept(this,argu); //Identifier
     currentMst.MethodName = mdName;
     llvmFileBuffer += v.defineMethod(currentMst.ReturnTypeOfMethod,currentMst.MethodName,currentCst.ClassName);
     //insideMethod = true; //we are inside a method
     md.f7.accept(this,argu); // (VarDeclaration)*
     md.f8.accept(this,argu); // (Statement)*
     returnTypeName = null;
     TorF = -1;
     intlit=-1;
     returnflag=true;
     String returnInMethod = md.f10.accept(this,argu); // Expression
     returnflag=false;

     String retVal = "\n\tret ";
     switch(currentMst.ReturnTypeOfMethod){
         case "int":{
             retVal += "i32 ";
             break;
         }
         case "boolean":{
             retVal += "i1 ";
             break;
         }
         case "intArray":{
             retVal += "i32* ";
             break;
         }
         default:{
             retVal += "i8* ";
         }
     }
     if(intlit!=-1){
         llvmFileBuffer += retVal + intlit ;
         //registerCounter+=1;
     }
     else{
         if(TorF == 1 || TorF == 0){
            llvmFileBuffer += retVal + TorF ;
        }
        else {
            llvmFileBuffer += retVal + "%_" + (registerCounter)+"\n";
        }
     }
     llvmFileBuffer += "\n}\n";
     MethodsSymbolTable mst = new MethodsSymbolTable();
     currentMst = mst;
     return null;
 }

     public String visit(VarDeclaration vd,String argu)
     {
         String t1 = vd.f0.accept(this,argu); //Type
         String id1 = vd.f1.accept(this,argu); //Identifier
         switch(t1){
             case "int":{
                 t1 = "i32\n";
                 break;
             }
             case "boolean":{
                 t1 = "i1\n";
                 break;
             }
             case "intArray":{
                 t1 = "i32*\n";
                 break;
             }
             default:{
                 t1= "i8*\n";
             }
         }
         llvmFileBuffer +=  "\t%" + id1 +" = alloca "+t1;
         return null;
     }

    public String visit(IfStatement is,String argu)
    {
            returnflag = true;
            String exprType = is.f2.accept(this,argu);//Expression
            returnflag = false;
            int if1 = ifLabels;
            int if2 = ifLabels+1;
            int if3 = ifLabels +2;
            ifLabels +=3;
            llvmFileBuffer += "\tbr i1 %_" + registerCounter ;
            llvmFileBuffer += ", label %if" +(if1)+" ,label %if";
            llvmFileBuffer += (if2)+"\n";
            llvmFileBuffer +="\nif" + (if1)+":\n\n";
            is.f4.accept(this,argu); //Statement
            llvmFileBuffer += "\n\tbr label %if"+(if3)+"\n";
            llvmFileBuffer +="\nif" + (if2)+":\n\n";
            is.f6.accept(this,argu); //Statement
            llvmFileBuffer += "\n\tbr label %if"+(if3)+"\n";
            llvmFileBuffer +="\nif" + (if3)+":\n\n";
            return null;
    }

    public String visit(WhileStatement is,String argu)
    {
            int wl1 = loopLabels;
            int wl2 = loopLabels+1;
            int wl3 = loopLabels +2;
            loopLabels+=3;
            llvmFileBuffer += "\n\tbr label %loop" + wl1 + "\n" ;
            llvmFileBuffer += "\nloop" + wl1 + ":\n";
            returnflag = true;
            String exprType = is.f2.accept(this,argu);//Expression
            returnflag = false;
            llvmFileBuffer += "\tbr i1 %_" + registerCounter ;
            llvmFileBuffer += ", label %loop" +(wl2)+" ,label %loop";
            llvmFileBuffer += (wl3)+"\n";
            llvmFileBuffer +="\nloop" + (wl2)+":\n\n";
            is.f4.accept(this,argu); //Statement
            llvmFileBuffer += "\n\tbr label %loop"+(wl1)+"\n";
            llvmFileBuffer +="\nloop" +wl3+":\n\n";
            return null;
    }

    public String visit(ArrayLookup al,String argu)
    {
        String type1 = al.f0.accept(this,argu); //PrimaryExpression
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = load i32, i32* %_" + registerCounter + "\n";
        registerCounter+=1;
        int myreg = registerCounter-1;
        int myreg2 = registerCounter;
        intlit=-1;
        String type2 = al.f2.accept(this,argu); //PrimaryExpression
        if(intlit!=-1){
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = icmp ult i32 " + intlit + ", %_" + myreg2 + "\n";
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = icmp ult i32 %_" + registerCounter + ", %_" + myreg2 + "\n";

        }
        llvmFileBuffer += "\tbr i1 %_" +(registerCounter+1)+ ", label %oob" + oobLabels + ", label %oob" + (oobLabels+1) + "\n";
        llvmFileBuffer += "\noob" + oobLabels + ":\n";
        if(intlit != -1){
            llvmFileBuffer += "\t%_" + (registerCounter+2) + " = add i32 " + intlit + ", 1\n";
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+2) + " = add i32 %_" + registerCounter+ ", 1\n";

        }
        llvmFileBuffer += "\t%_" + (registerCounter+3) + " = getelementptr i32, i32* %_" + myreg;
        llvmFileBuffer += ", i32 %_"+(registerCounter+2) + "\n";
        llvmFileBuffer += "\t%_" + (registerCounter+4) + " = load i32, i32* %_" + (registerCounter+3)+"\n";
        llvmFileBuffer += "\tbr label %oob" + (oobLabels+2) + "\n";
        llvmFileBuffer += "\noob" + (oobLabels+1) + ":\n";
        llvmFileBuffer += "\tcall void @throw_oob()\n";;
        llvmFileBuffer += "\tbr label %oob" + (oobLabels+2) + "\n";
        llvmFileBuffer += "\noob" + (oobLabels+2) + ":\n";
        oobLabels += 3;
        registerCounter += 4;
        exprtype1 = "int";
        return "%_"+registerCounter;
    }

    public String visit(AssignmentStatement as,String argu)
    {
        String id = as.f0.accept(this,argu);//Identifier
        String typeOfId = new String();
        boolean isField = false;
        boolean isVar = false;
        //System.out.println("IM THE IDENTIFIER --->" + id);
        int fieldreg=-1;
        if(currentCst.IsMain){
          //System.out.println("e");
          typeOfId = EvalVisitor.st.search_classmap_type(id,"main",currentMst.MethodName);
          //isField = v.fieldFinder(id,"main");
        }
        else{
          //System.out.println("o");
          typeOfId = EvalVisitor.st.search_classmap_type(id,currentCst.ClassName,currentMst.MethodName);
          isField = v.fieldFinder(id,currentCst.ClassName);
          //check if the id is shadowing the feild in this method
          if(isField){
               if(v.searchvarsvt(id,currentCst.ClassName,currentMst.MethodName)){
                   isVar = true;
                   typeOfId = v.type_isvar(id,currentCst.ClassName,currentMst.MethodName);
               }
          }
        }
        //we got the type of id
        if(typeOfId==null){
            System.out.println(">>ERROR");
            System.out.println(">>Identifier: " +id+ " not defined");
            System.out.println("*	*	*	*	*	*	*	*	*	*	*");
            System.exit(0);
        }
        if(isField && !isVar ){
            if(currentCst.IsMain){

            }
            else{
                llvmFileBuffer += "\t%_" + (registerCounter+1) + " = getelementptr ";
                llvmFileBuffer += "i8,i8* %this, i32 " + v.getVtableOffset(currentCst.ClassName,id,this.VtableInfo);
                switch(typeOfId){
                    case "int":{
                        llvmFileBuffer += " \n";
                        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                        llvmFileBuffer += (registerCounter+1) + " to i32*\n";
                        break;
                    }
                    case "boolean":{
                        llvmFileBuffer += " \n";
                        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                        llvmFileBuffer += (registerCounter+1) + " to i1*\n";
                        break;
                    }
                    case "intArray":{
                        llvmFileBuffer += " \n";
                        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                        llvmFileBuffer += (registerCounter+1) + " to i32**\n";
                        break;
                    }
                    default:{
                        llvmFileBuffer += " \n";
                        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                        llvmFileBuffer += (registerCounter+1) + " to i8**\n";
                    }
                }
                registerCounter+=2;
                fieldreg = registerCounter;
            }
        }
        intlit = -1;
        TorF = -1;
        returnflag=true;
        String exprType = as.f2.accept(this,argu);//Expression
        returnflag=false;
        String mytype = null;
        if(exprType.contains("%")){
            //System.out.println(exprtype1+"=========================");
            llvmFileBuffer += "\tstore ";
            switch(exprtype1){
                case "int":
                    mytype = "i32";
                    llvmFileBuffer += mytype +" "+exprType+", ";
                    break;
                case "intArray":
                        mytype = "i32*";
                        llvmFileBuffer += mytype +" "+exprType+", ";
                        break;
                case "boolean":
                    mytype = "i1";
                    llvmFileBuffer += mytype + " "+exprType+", ";
                    break;
                default:
                    mytype = "i8*";
                    llvmFileBuffer += mytype + " "+exprType+", ";
                }
            }
            else{
                llvmFileBuffer += "\tstore ";
                switch(exprType){
                    case "int":
                        mytype = "i32";
                        /*if(allocationbool){
                            llvmFileBuffer += mytype +" %_"+call_register+", ";
                            allocationbool=false;
                        }
                        else*/ if(intlit!=-1){
                            llvmFileBuffer += mytype +" "+intlit+", ";
                        }
                        else{
                            llvmFileBuffer += mytype +" %_"+registerCounter+", ";
                        }
                        break;
                    case "intArray":
                        mytype = "i32*";
                        /*if(allocationbool){
                            llvmFileBuffer += mytype +" %_"+call_register+", ";
                            allocationbool=false;
                        }
                        else{*/
                            llvmFileBuffer += mytype +" %_"+registerCounter+", ";
                        //}
                        break;
                    case "boolean":
                        mytype = "i1";
                        /*if(allocationbool){
                            llvmFileBuffer += mytype +" %_"+call_register+", ";
                            allocationbool=false;
                        }
                        else*/ if(TorF!=-1){
                            llvmFileBuffer += mytype + " "+TorF+", ";
                        }
                        else{
                            llvmFileBuffer += mytype +" %_"+registerCounter+", ";
                        }
                        break;
                    default:
                        mytype = "i8*";
                        if(allocationbool){
                            llvmFileBuffer += mytype +" %_"+call_register+", ";
                            allocationbool=false;
                        }else{
                            llvmFileBuffer += mytype +" %_"+registerCounter+", ";
                        }
                    }
            }
            if(fieldreg==-1){
                llvmFileBuffer += mytype + "* %" + id + "\n" ;
            }
            else{
                llvmFileBuffer += mytype + "* %_" + fieldreg + "\n" ;
            }
            //check the types
            // if exprtype is child of typeOfid
            //change the type of typeOfid in this method
            if(!exprType.contains("%") && !(exprType.equals(typeOfId)) && (exprType!=null) && (!isField) && (!exprType.equals("int")) && (!exprType.equals("boolean")) && (!exprType.equals("intArray")) ){
                if(v.check_child(typeOfId , exprType)){
                    //if you enter this if replace idtype with exprType
                    if(currentCst.IsMain){
                        v.replaceIdType(id,exprType,currentMst.MethodName,"main");
                    }
                    else{
                        v.replaceIdType(id,exprType,currentMst.MethodName,currentCst.ClassName);
                    }
                }
        }

        return null;

    }

    public String visit(ArrayAssignmentStatement aas,String argu)
    {
        String id = aas.f0.accept(this,argu);//Identifier
        String typeOfId = new String();
        boolean isField = false;
        boolean isVar = false;
        int fieldreg=-1;
        if(currentCst.IsMain){
          //System.out.println("e");
          typeOfId = EvalVisitor.st.search_classmap_type(id,"main",currentMst.MethodName);
          //isField = v.fieldFinder(id,"main");
        }
        else{
          //System.out.println("o");
          typeOfId = EvalVisitor.st.search_classmap_type(id,currentCst.ClassName,currentMst.MethodName);
          isField = v.fieldFinder(id,currentCst.ClassName);
          //check if the id is shadowing the feild in this method
          if(isField){
               if(v.searchvarsvt(id,currentCst.ClassName,currentMst.MethodName)){
                   isVar = true;
                   typeOfId = v.type_isvar(id,currentCst.ClassName,currentMst.MethodName);
               }
          }
        }
        //we got the type of id
        if(typeOfId==null){
            System.out.println(">>ERROR");
            System.out.println(">>Identifier: " +id+ " not defined");
            System.out.println("*	*	*	*	*	*	*	*	*	*	*");
            System.exit(0);
        }
        if(isField&&!isVar){
            if(currentCst.IsMain){

            }
            else{
                llvmFileBuffer += "\t%_" + (registerCounter+1) + " = getelementptr ";
                llvmFileBuffer += "i8,i8* %this, i32 " + v.getVtableOffset(currentCst.ClassName,id,this.VtableInfo);
                llvmFileBuffer += " \n";
                llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                llvmFileBuffer += (registerCounter+1) + " to i32**\n";
                llvmFileBuffer += "\t%_" + (registerCounter+3) + " = load i32*, i32** %_" + (registerCounter+2) + "\n";
                registerCounter+=3;
                fieldreg = registerCounter;
            }
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = load i32*, i32** %" + id + "\n";
            registerCounter+=1;
            fieldreg = registerCounter;
        }
        intlit = -1;
        int posintlit = -1;
        String eType = aas.f2.accept(this,argu);//Expression
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = load i32, i32* %_" + fieldreg + "\n";
        if(intlit!=-1){
            llvmFileBuffer += "\t%_" + (registerCounter+2) + " = icmp ult i32 " +intlit+ ", %_" + (registerCounter+1) + "\n";
            posintlit = intlit;
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+2) + " = icmp ult i32 %_" + registerCounter + ", %_" + (registerCounter+1) + "\n";
        }
        llvmFileBuffer += "\tbr i1 %_" + (registerCounter+2) + ", label %oob" + (oobLabels) + ", label %oob" + (oobLabels+1) + "\n";
        llvmFileBuffer += "\noob" + oobLabels + ":\n";
        int fieldreg2 = registerCounter;
        int myob2 = oobLabels+1;
        int myob3 = oobLabels+2;
        oobLabels += 3;
        registerCounter+= 2;
        int addreg = registerCounter;
        intlit = -1;
        TorF = -1;
        returnflag=true;
        String exprType = aas.f5.accept(this,argu);//Expression
        returnflag=false;
        if(posintlit!=-1){
            llvmFileBuffer+= "\t%_" + (registerCounter+1) + " = add i32 " + posintlit + ", 1\n";

        }
        else{
            llvmFileBuffer+= "\t%_" + (registerCounter+1) + " = add i32 %_" + fieldreg2 + ", 1\n";
        }
        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = getelementptr i32, i32* %_" + fieldreg + ", i32 %_" + (registerCounter+1) + "\n";
        String mytype = null;
        if(exprType.contains("%")){
            //System.out.println(exprtype1+"=========================");
            mytype = "i32";
            llvmFileBuffer += "\tstore ";
            llvmFileBuffer += mytype +" "+exprType+", ";

            }
        else{
            llvmFileBuffer += "\tstore ";
            mytype = "i32";
            if(intlit!=-1){
                llvmFileBuffer += mytype +" "+intlit+", ";
            }
            else{
                llvmFileBuffer += mytype +" %_"+registerCounter+", ";
            }
        }
        llvmFileBuffer += mytype + "* %_" + (registerCounter+2) + "\n" ;
        llvmFileBuffer += "\tbr label %oob" + myob3 + "\n";
        llvmFileBuffer += "\noob" + myob2 + ":\n";
        llvmFileBuffer += "\tcall void @throw_oob()\n"+"\tbr label %oob" + (myob3) + "\n";
        llvmFileBuffer += "\noob" + (myob3) + ":\n";
        registerCounter+=2;
        //oobLabels+=1;
        return null;
    }



    public String visit(NotExpression ne,String argu)
    {
        TorF = -1;
        returnflag=true;
        if(exprlistbool){
            exprlistbool=false;}
        int helpreg = registerCounter;
        String t = ne.f1.accept(this,argu); //"Clause"
        returnflag = false;
        exprlistbool = true;
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = xor ";
        if(TorF != -1 && (helpreg==registerCounter) ){
            llvmFileBuffer += "i1 1 ," + TorF + "\n";
        }
        else{
            llvmFileBuffer += "i1 1 , %_" + registerCounter + "\n";
        }
        registerCounter+=1;
        exprtype1 = "boolean";
        return "%_" + registerCounter;
    }

    public String visit(AndExpression ande,String argu)
    {
        int lbl1 = andLabels;int lbl2 = andLabels+1;
        int lbl3 = andLabels+2;int lbl4 = andLabels+3;
        andLabels+=4;
        TorF = -1;
        int hreg = registerCounter;
        ande.f0.accept(this,argu);
        int helpreg = registerCounter;
        llvmFileBuffer += "\tbr label %andclause"+lbl1+"\n";
        llvmFileBuffer += "\nandclause" + lbl1 + ":\n";
        if(hreg == helpreg && TorF != -1){
            llvmFileBuffer +="\tbr i1 " + TorF + ", label %andclause" + lbl2;
        }
        else{
            llvmFileBuffer +="\tbr i1 %_" + helpreg + ", label %andclause" + lbl2;
        }
        llvmFileBuffer += ", label %andclause" + lbl3 + "\n";
        llvmFileBuffer += "\nandclause" + lbl2 + ":\n";
        int h2reg = registerCounter;
        TorF = -1;
        ande.f2.accept(this, null);
        int helpreg2 = registerCounter;
        llvmFileBuffer += "\tbr label %andclause" + lbl3 + "\n";
        llvmFileBuffer += "\nandclause" + lbl3 + ":\n";
        llvmFileBuffer += "\tbr label %andclause" + lbl4 + "\n";
        llvmFileBuffer += "\nandclause" + lbl4 + ":\n";
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = phi i1 [ 0, %andclause" + lbl1;
        if(h2reg == helpreg2 && TorF != -1){
            llvmFileBuffer += " ], [ " + TorF + ", %andclause" + lbl3 + " ]\n";
        }
        else{
            llvmFileBuffer += " ], [ %_" + helpreg2 + ", %andclause" + lbl3 + " ]\n";
        }
        registerCounter+=1;
        exprtype1 = "boolean";
        return "%_" + registerCounter;
    }

    public String visit(CompareExpression ce,String argu)
   {
       boolean comparebool1 = false;
       int compareval1 = 0;
       int holdreg=0;
       boolean comparebool2 = false;
       int compareval2=0;
       returnflag = true;
       if(exprlistbool){
           exprlistbool=false;
       }
       intlit=-1;
       int helpreg = registerCounter;
       String type1 = ce.f0.accept(this,argu); //PrimaryExpression
       returnflag = false;
       exprlistbool=true;
       if(helpreg != registerCounter){
           holdreg = registerCounter;
       }
       else{
           comparebool1 = true;
           compareval1 = intlit;
       }
       returnflag = true;
       if(exprlistbool){
           exprlistbool=false;
       }
       intlit=-1;
       int helpreg2 = registerCounter;
       String type2 = ce.f2.accept(this,argu); //PrimaryExpression
       returnflag = false;
       exprlistbool=true;
       if(intlit != -1 && (helpreg2 == registerCounter)){
           comparebool2 = true;
           compareval2 = intlit;
       }
       llvmFileBuffer += "\t%_" + (registerCounter+1) + " = icmp slt i32 ";
       if(comparebool1 && comparebool2){
           llvmFileBuffer += compareval1 + ", " + compareval2 + "\n";
       }
       else if(comparebool1){
           llvmFileBuffer += compareval1 + ", %_"+ registerCounter + "\n";
       }
       else if(comparebool2){
           llvmFileBuffer += "%_"+ holdreg+ ", "+ compareval2 + "\n";
       }
       else{
           llvmFileBuffer += "%_"+ holdreg+ ", %_"+ registerCounter + "\n";
       }
       registerCounter+=1;
       exprtype1 = "boolean";
       return "%_" + registerCounter;
   }

   public String visit(TimesExpression te,String argu)
   {
       int timesResult=0;
       boolean timesresbool = false;
       int firstreg=-1;
       returnflag=true;
       if(exprlistbool){
           exprlistbool=false;}
       intlit=-1;
       int helpreg = registerCounter;
       String type1 = te.f0.accept(this,argu); //PrimaryExpression
       exprlistbool=true;
      // System.out.println("edo eiami " + type1);
       returnflag=false;
       if(intlit!=-1 && (helpreg == registerCounter)){
           timesResult = intlit;
           timesresbool = true;
       }
       else if(returnisID){

           firstreg = registerCounter;
           returnisID = false;
       }
       returnflag=true;
       if(exprlistbool){
           exprlistbool=false;}
       intlit=-1;
       int helpreg2 = registerCounter;
       String type2 = te.f2.accept(this,argu); //PrimaryExpression
       exprlistbool=true;
       returnflag=false;
       if(!timesresbool){
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
           llvmFileBuffer += "mul i32 %_" + firstreg;
           if((helpreg2==registerCounter)&&intlit!=-1){
               llvmFileBuffer += ", "+intlit+"\n";
           }
           else{
               llvmFileBuffer += ", %_"+registerCounter+"\n";
           }
           registerCounter+=1;
       }
       else if(intlit!=-1 && timesresbool && (helpreg2==registerCounter)){
           timesResult = timesResult * intlit;
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
           llvmFileBuffer += "mul i32 " + timesResult + ", 1\n";
           registerCounter+=1;
       }
       else if(returnisID){
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
           llvmFileBuffer += "mul i32 %_" + (registerCounter+0);
           if(timesresbool){
               llvmFileBuffer += ", "+timesResult+"\n";
           }
           else{
               llvmFileBuffer += ", %_"+firstreg+"\n";
           }
           registerCounter+=1;
           returnisID = false;
       }
       else if(intlit!=-1){
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
           llvmFileBuffer += "mul i32 %_" + firstreg + ", "+intlit+"\n";
           registerCounter+=1;
       }
       exprtype1 = "int";
       return "%_" + registerCounter ;
   }

   public String visit(MinusExpression me,String argu)
    {
        int minusResult=0;
        boolean minusresbool = false;
        int firstreg=-1;
        returnflag=true;
        if(exprlistbool){
            exprlistbool=false;}
        intlit=-1;
        int helpreg = registerCounter;
        String type1 = me.f0.accept(this,argu); //PrimaryExpression
        exprlistbool=true;;
        returnflag=false;
        if(intlit!=-1&&(helpreg == registerCounter)){
            minusResult = intlit;
            minusresbool = true;
        }
        else if(returnisID){
           // llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
            //llvmFileBuffer += "i32, i32* %"+returnTypeName+"\n";
            //registerCounter+=1;
            firstreg = registerCounter;
            returnisID = false;
        }
        returnflag=true;
        if(exprlistbool){
            exprlistbool=false;}
        intlit=-1;
        int helpreg2 = registerCounter;
        String type2 = me.f2.accept(this,argu); //PrimaryExpression
        returnflag=false;
        exprlistbool=true;
        if(intlit!=-1 && minusresbool && (helpreg2==registerCounter)){
            minusResult = minusResult - intlit;
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            if(minusResult < 0){ minusResult *= -1;}
            llvmFileBuffer += "sub i32 " +" 0, " + minusResult + "\n";
            registerCounter+=1;
        }
        else if(intlit!=-1 && (helpreg2==registerCounter) ){
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "sub i32 %_" + firstreg + ", "+intlit+"\n";
            registerCounter+=1;
        }
        else if(returnisID){

            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "sub i32 " ;
            if(minusresbool){
                llvmFileBuffer += ""+minusResult;
                llvmFileBuffer += ", " + (registerCounter+0) + "\n";
            }
            else{
                llvmFileBuffer += "%_"+firstreg+"\n";
                llvmFileBuffer += ", " + (registerCounter+0) + "\n";
            }
            registerCounter+=1;
            returnisID = false;
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "sub i32 " ;
            if(minusresbool){
                llvmFileBuffer += ""+minusResult;
                llvmFileBuffer += ", " + (registerCounter+0) + "\n";
            }
            else{
                llvmFileBuffer += "%_"+firstreg+"\n";
                llvmFileBuffer += ", " + (registerCounter+0) + "\n";
            }
            registerCounter+=1;
        }
        exprtype1 = "int";
        return "%_" + registerCounter ;
    }

    public String visit(PlusExpression pe,String argu)
    {
        int plusResult=0;
        boolean plusresbool = false;
        int firstreg=-1;
        returnflag=true;
        if(exprlistbool){
            exprlistbool=false;}
        intlit=-1;
        int helpreg=registerCounter;
        String type1 = pe.f0.accept(this,argu); //PrimaryExpression
        exprlistbool=true;;
        returnflag=false;
        if(intlit!=-1&&(helpreg == registerCounter)){
            plusResult = intlit;
            plusresbool = true;
        }
        else if(returnisID){
           // llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
            //llvmFileBuffer += "i32, i32* %"+returnTypeName+"\n";
            //registerCounter+=1;
            firstreg = registerCounter;
            returnisID = false;
        }
        returnflag=true;
        if(exprlistbool){
            exprlistbool=false;}
        intlit=-1;
        int helpreg2= registerCounter;
        String type2 = pe.f2.accept(this,argu); //PrimaryExpression
        returnflag=false;
        exprlistbool=true;
        if(intlit!=-1 && plusresbool && (helpreg2==registerCounter)){
            plusResult = plusResult + intlit;
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "add i32 " + plusResult + ", 0\n";
            registerCounter+=1;
        }
        else if(intlit!=-1&&(helpreg2==registerCounter)){
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "add i32 %_" + firstreg + ", "+intlit+"\n";
            registerCounter+=1;
        }
        else if(returnisID){
            //llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
            //llvmFileBuffer += "i32, i32* %"+returnTypeName+"\n";
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "add i32 ";
            if(plusresbool){
                llvmFileBuffer += ""+plusResult;
                llvmFileBuffer += ", %_" + registerCounter+"\n";
            }
            else{
                llvmFileBuffer += "%_"+firstreg;
                llvmFileBuffer += ", %_" + registerCounter+"\n";
            }
            registerCounter+=1;
            returnisID = false;
        }
        else{
            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = ";
            llvmFileBuffer += "add i32 ";
            if(plusresbool){
                llvmFileBuffer += ""+plusResult;
                llvmFileBuffer += ", %_" + registerCounter+"\n";
            }
            else{
                llvmFileBuffer += "%_"+firstreg;
                llvmFileBuffer += ", %_" + registerCounter+"\n";
            }
            registerCounter+=1;
        }
        exprtype1 = "int";
        return "%_" + registerCounter ;
    }

    public String visit(ArrayLength alen,String argu)
    {
        returnflag=true;
        String type1 = alen.f0.accept(this,argu); //PrimaryExpression
        returnflag=false;
        returnisID=false;
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = load i32, i32* %_" + registerCounter + "\n";
        registerCounter+=1;
        exprtype1 = "int";
        return "%_" + registerCounter;
    }


    public String visit(MessageSend ms,String argu)
    {
       returnflag=true;
       exprlistbool=false;
       //System.out.println("im in -");
       String expr1 = ms.f0.accept(this,argu); //PrimaryExpression
       returnflag=false;
       int thisexpr = -1;
       int myreg=-1;
       if(expr1.contains("%_")){
           expr1 = msgexprtype;
       }
       if(expr1.equals("%this")){
           expr1 = currentCst.ClassName;
           thisexpr=1;
       }
       else{
           myreg = registerCounter;
       }
       exprlistbool=true;
       ms.f1.accept(this,argu); //"."
       String id1 = ms.f2.accept(this,argu); //"Identifier"
       //find the v table offset of id
       llvmFileBuffer += "\t; " + expr1 + "." + id1 + ": " ;
      //System.out.println("edo:" + id1);
      //System.out.println("edo:" + expr1);
       llvmFileBuffer += v.getVtableOffset(expr1,id1,this.VtableInfo)+"\n";
       // for the call we need 6 temporary registers
       // and we have the register from call
       if(allocationbool){
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = bitcast i8* %_" + call_register + " to i8***\n";
       }
       else{
           if(thisexpr == 1){
               llvmFileBuffer += "\t%_" + (registerCounter+1) + " = bitcast i8* " + "%this" + " to i8***\n";
            }
            else{
                llvmFileBuffer += "\t%_" + (registerCounter+1) + " = bitcast i8* %_" + myreg + " to i8***\n";
            }
       }
       llvmFileBuffer += "\t%_" + (registerCounter + 2) + " = load i8**, i8*** %_" + (registerCounter+1)+"\n" ;
       llvmFileBuffer += "\t%_" + (registerCounter + 3) + " = getelementptr i8*, i8** %_" + (registerCounter+2);
       llvmFileBuffer += ", i32 "+ v.getVtableOffset(expr1,id1,this.VtableInfo) +"\n";
       llvmFileBuffer += "\t%_" + (registerCounter + 4) + " = load i8*, i8** %_" + (registerCounter+3)+"\n" ;
       llvmFileBuffer += "\t%_" + (registerCounter+5) + " = bitcast i8* %_" + (registerCounter + 4);
       int reg = registerCounter+5;
       llvmFileBuffer += " to " + v.getMethodType(expr1,id1)+ " (i8*" + v.getMethodParameterTypes(expr1,id1);
       switch(v.getMethodType(expr1,id1)){
           case "i32":
                exprtype1="int";
                break;
            case "i32*":
                exprtype1="intArray";
                break;
            case "i1":
                exprtype1="boolean";
                break;
            default:
                exprtype1="catch_pointer";
       }
       llvmFileBuffer  += ")*\n" ;
       registerCounter += 5;
       if(ms.f4.present()){
           exprList.clear();// reset exprList
           ArrayList<Integer> crs = new ArrayList<Integer>();
           crs.add(call_register);
           ms.f4.accept(this, argu); //(ExpressionList)?
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = call " + v.getMethodType(expr1,id1) ;
           if(crs.get(crs.size()-1) != call_register){
               crs.add(call_register) ;
           }
           if(allocationbool){
               llvmFileBuffer += " %_" + reg + "(i8* %_" + crs.get(0) ;
           }
           else{
               if(thisexpr == 1){
                   llvmFileBuffer += " %_" + reg + "(i8* %this";
               }
               else{
                   llvmFileBuffer += " %_" + reg + "(i8* %_"+myreg;
               }
           }
           for(int i=1;i<crs.size();i++){
               exprList.add("%_"+crs.get(i));
           }
           llvmFileBuffer += v.expressionList(v.getMethodParameterTypes(expr1,id1),exprList);
       }
       else{
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = call " + v.getMethodType(expr1,id1) ;
          if(allocationbool){
               llvmFileBuffer += " %_" + reg + "(i8* %_" + call_register ;
           }
           else{
               if(thisexpr == 1){
                   llvmFileBuffer += " %_" + reg + "(i8* %this";
               }
               else{
                   llvmFileBuffer += " %_" + reg + "(i8* %_"+myreg;
               }
           }
       }
       call_register = registerCounter;
       llvmFileBuffer  += ")\n" ;
       registerCounter += 1;
       switch(v.getMethodType(expr1,id1)){
           case "i32":
                exprtype1="int";
                break;
            case "i32*":
                exprtype1="intArray";
                break;
            case "i1":
                exprtype1="boolean";
                break;
            default:
                exprtype1="catch_pointer";
       }
       allocationbool=false;
       return "%_" + registerCounter;
   }

   public String visit(ExpressionList el,String argu)
   {
       exprlistbool=true;
       intlit = -1;
       TorF = -1;
       int mreg = call_register;
       String expr = el.f0.accept(this,argu); //Expression
       exprlistbool=false;
      // System.out.println(intlit);
    // System.out.println(expr);
    /*if(exprl){
        String r = "%_"+mreg;
        exprList.add(r);
    }
     else*/ if(expr.contains("%")){
         exprList.add(expr);
       }
      else if( intlit!= -1 ){
           String a = ""+intlit;
           exprList.add(a);
       }
       else if(TorF != -1){
           String a = ""+TorF;
           exprList.add(a);
       }
       el.f1.accept(this,argu); //(ExpressionRest)*
       return null;
   }

   public String visit(ExpressionTerm et,String argu)
   {
       exprlistbool=true;
       intlit = -1;
       TorF = -1;
       et.f0.accept(this,argu); //","
       String expr = et.f1.accept(this,argu); //Expression
       //System.out.println(expr);
       exprlistbool=false;
       if(expr.contains("%")){
           exprList.add(expr);
         }
       else if( intlit!= -1 ){
           String a = ""+intlit;
           exprList.add(a);
       }
       else if(TorF != -1){
           String a = ""+TorF;
           exprList.add(a);
       }
       return null;
   }

   public String visit(BracketExpression be,String argu)
    {
        be.f0.accept(this,argu); //"("
        nestedEpxr = false;

        String expr = be.f1.accept(this,argu); //Expression
        be.f2.accept(this,argu); //")"
        nestedEpxr = true;

        String exprType = expr;
        return exprType;
    }

   public String visit(PrintStatement ps,String argu)
    {
        intlit=-1;
        returnflag = true;
        String printExpr = ps.f2.accept(this,argu); //Expression
        returnflag = false;
        returnisID = false;
        if(intlit!=-1 && !printExpr.contains("%")){
            llvmFileBuffer += "\tcall void (i32) @print_int(i32 " + intlit + ")\n";
        }
        else{
            llvmFileBuffer += "\tcall void (i32) @print_int(i32 %_" +  (registerCounter) + ")\n";
        }
        //registerCounter+=1;
        return null;
    }

    public String visit(AllocationExpression ae,String argu)
    {
        ae.f0.accept(this,argu); //"new"
        String objName = ae.f1.accept(this,argu);//id
        // for allocation we follow the standard llvm way
        if(registerCounter > 0){
            registerCounter+=1;
        }
        llvmFileBuffer += "\t%_" + registerCounter + " = call i8* @calloc(i32 1,i32 " ;
        callocBytes += v.classFieldsByteCount(objName);
        llvmFileBuffer += callocBytes + ")\n";
        callocBytes = 0; //reset calloc bytes
        // for the allocation we need 3 temporary registers
        // and we hold the register from call
        call_register = registerCounter;
        llvmFileBuffer += "\t%_" + (registerCounter+1) + " = bitcast i8* %_" + call_register + " to i8***\n";
        llvmFileBuffer += "\t%_" + (registerCounter+2) + " = getelementptr [";
        llvmFileBuffer+=  v.getNumOfMethods(objName)  +" x i8*], [" ;
        llvmFileBuffer+=  v.getNumOfMethods(objName)  +" x i8*]* @." + objName + "_vtable,i32 0, i32 0\n";
        llvmFileBuffer += "\tstore i8** %_" + (registerCounter+2) +", i8*** %_" + (registerCounter+1) +"\n";
        registerCounter += 2 ;
        nestedEpxr=true;
        allocationbool = true;
        return objName;
    }

    public String visit(ArrayAllocationExpression aae,String argu)
   {
       nestedEpxr = false;
       returnflag = true;
       intlit = - 1;
       int hreg = registerCounter;
       String exprType = aae.f3.accept(this,argu); //Expression
       returnflag = false;
       nestedEpxr = true;
       if(intlit!=-1 && hreg == registerCounter ){
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = icmp slt i32 " + intlit;
       }
       else{
           llvmFileBuffer += "\t%_" + (registerCounter+1) + " = icmp slt i32 %_" + registerCounter;
       }
       llvmFileBuffer += ",0\n" + "\t br i1 %_" + (registerCounter+1);
       llvmFileBuffer += ", label %array_alloc" + (arrallocLabels+1);
       llvmFileBuffer += ", label %array_alloc" + (arrallocLabels+2) + "\n";
       llvmFileBuffer += "\narray_alloc"+(arrallocLabels+1)+":\n"+"\tcall void @throw_oob()\n";
       llvmFileBuffer += "\tbr label %array_alloc" + (arrallocLabels+2) + "\n";
       llvmFileBuffer += "\narray_alloc" + (arrallocLabels+2) + ":\n";
       if(intlit!=-1 && hreg == registerCounter ){llvmFileBuffer += "\t%_" + (registerCounter+2) + " = add i32 " + intlit + ", 1\n";}
       else{llvmFileBuffer += "\t%_" + (registerCounter+2) + " = add i32 %_" + registerCounter + ", 1\n";}
       llvmFileBuffer +=  "\t%_" + (registerCounter+3) + " = call i8* @calloc(i32 4, i32 %_" + (registerCounter+2) + ")\n";
       llvmFileBuffer += "\t%_" + (registerCounter+4) + " = bitcast i8* %_" + (registerCounter+3) + " to i32*\n";
       if(intlit!=-1 && hreg == registerCounter ){llvmFileBuffer += "\tstore i32 " + intlit + ", i32* %_" + (registerCounter+4) + "\n";}
       else{llvmFileBuffer += "\tstore i32 %_" + registerCounter + ", i32* %_" + (registerCounter+4) + "\n";}
       exprtype1 = "intArray";
       registerCounter +=4;
       arrallocLabels += 2;
       return "%_" + registerCounter;
   }

   public String visit(PrimaryExpression pe,String argu)
   {
       String expr = pe.f0.accept(this,argu);
       
       if(nestedEpxr){
            nestedEpxr = false;
            return expr;
        }
       if(expr.contains("int_literal:")){
           String[] myIntLit = expr.split(":");
           intlit = Integer.parseInt(myIntLit[1]);
           expr = "int_literal";
       }
       switch(expr){
           case "int_literal":
               return "int";
           case "true":
               TorF = 1;
               return "boolean";
           case "false":
               TorF = 0;
               return "boolean";
           case "this":
                exprtype1 = "catch_pointer";
               return "%this";
           default:
               //we get an identifier
               //search SymbolTable
               String typeOfIdentifier = new String();
               Boolean isField = false;
               Boolean isVar = false;
               if(currentCst.IsMain){
                   //System.out.println("e");
                   typeOfIdentifier = EvalVisitor.st.search_classmap_type(expr,"main",currentMst.MethodName);
                   //isField = v.fieldFinder(expr,"main");
               }
               else{
                   //System.out.println("o");
                   typeOfIdentifier = EvalVisitor.st.search_classmap_type(expr,currentCst.ClassName,currentMst.MethodName);
                   isField = v.fieldFinder(expr,currentCst.ClassName);
                   //check if the id is shadowing the feild in this method
                   if(isField){
                        if(v.searchvarsvt(expr,currentCst.ClassName,currentMst.MethodName)){
                            isVar = true;
                            typeOfIdentifier = v.type_isvar(expr,currentCst.ClassName,currentMst.MethodName);
                        }
                   }

               }
               if(typeOfIdentifier!=null){
                   msgexprtype = typeOfIdentifier;
                    if(isField && !isVar ){
                        if(currentCst.IsMain){

                        }
                        else{
                            llvmFileBuffer += "\t%_" + (registerCounter+1) + " = getelementptr ";
                            llvmFileBuffer += "i8,i8* %this, i32 " + v.getVtableOffset(currentCst.ClassName,expr,this.VtableInfo);
                            switch(typeOfIdentifier){
                                case "int":{
                                    llvmFileBuffer += " \n";
                                    llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                                    llvmFileBuffer += (registerCounter+1) + " to i32*\n";
                                    break;
                                }
                                case "boolean":{
                                    llvmFileBuffer += " \n";
                                    llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                                    llvmFileBuffer += (registerCounter+1) + " to i1*\n";
                                    break;
                                }
                                case "intArray":{
                                    llvmFileBuffer += " \n";
                                    llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                                    llvmFileBuffer += (registerCounter+1) + " to i32**\n";
                                    break;
                                }
                                default:{
                                    llvmFileBuffer += " \n";
                                    llvmFileBuffer += "\t%_" + (registerCounter+2) + " = bitcast i8* %_";
                                    llvmFileBuffer += (registerCounter+1) + " to i8**\n";
                                }
                            }
                            registerCounter+=2;
                        }
                    }
                    if(exprlistbool){
                        //System.out.println("\n "+expr+" \n");
                        switch(typeOfIdentifier){
                            case "int":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i32, i32* ";
                                if(!isField || isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                exprtype1 = "int";
                                break;
                            }
                            case "boolean":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i1, i1* ";
                                if(!isField || isVar ){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                exprtype1 = "boolean";
                                break;
                            }
                            case "intArray":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i32*, i32** ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                exprtype1 = "intArray";
                                break;
                            }
                            default:{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i8*, i8** ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                exprtype1="catch_pointer";
                            }
                        }
                        registerCounter += 1;
                        return "%_"+registerCounter;
                    }
                    if(returnflag){
                        //System.out.println("b\n");
                        returnisID=true;
                        returnTypeName = expr;
                        switch(typeOfIdentifier){
                            case "int":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i32, i32* ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                break;
                            }
                            case "boolean":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i1, i1* ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                break;
                            }
                            case "intArray":{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i32*, i32** ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                                break;
                            }
                            default:{
                                llvmFileBuffer += "\t%_" + (registerCounter+1)+" = load ";
                                llvmFileBuffer += "i8*, i8** ";
                                if(!isField||isVar){
                                    llvmFileBuffer+= "%"+expr+"\n";
                                }
                                else{
                                    llvmFileBuffer+= "%_"+registerCounter+"\n";
                                }
                            }
                        }
                        registerCounter += 1;
                    }
                        return typeOfIdentifier;
                    }

               else{
                   System.out.println(">>ERROR");
                   System.out.println(">>Identifier "+expr+" not defined!");
                   System.out.println("*	*	*	*	*	*	*	*	*	*	*");
                   System.exit(0);
               }
       }
       return null;
   }



}
