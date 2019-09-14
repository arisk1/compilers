import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.*;
import java.lang.*;

class Main {
    public static void main (String [] args)throws IOException{
	if(args.length != 1){
	    System.err.println("Usage: java Driver [fileName] ");
	    System.exit(1);
	}
    File dir = new File("./Offsets");
    // Tests whether the directory denoted by this abstract pathname exists.
    boolean exists = dir.exists();
    if(!exists){
        System.out.println("Directory " + dir.getPath() + " created ");
        dir.mkdir();
    }
    else{
        System.out.println("Directory " + dir.getPath() + " exists ");

    }
	FileInputStream fis = null;
	try{
	    fis = new FileInputStream(args[0]);
	    MiniJavaParser parser = new MiniJavaParser(fis);
	    System.err.println("Program parsed successfully.");
	    Goal root = parser.Goal();
        EvalVisitor eval = new EvalVisitor(); //first visitor
		root.accept(eval,null);

		/*System.out.println("-------------------------------------");
		System.out.println(">>Printing SymbolTable");
		System.out.println("-------------------------------------");*/
		//EvalVisitor.st.print_st(); //print symbol table
		String OffsetString = new String();
		if(args[0].indexOf("/")!=-1){
			String[] parts = args[0].split("/");
			OffsetString = parts[parts.length-1];
			String[] output = OffsetString.split("\\.");
			OffsetString = output[0];
		}
		else{
			String[] output = args[0].split("\\.");
			OffsetString = output[0];
		}
        //System.out.println(OffsetString);
		System.out.println(">>Offsets Printed to ./Offsets/" +OffsetString+ ".txt");
		System.out.println("-------------------------------------");
		EvalVisitor.st.print_offsets(OffsetString); //print offsets

        /////////////////////////////////////////////////////////////
        //**************LLVM VISITOR********************************/

        LlvmVisitor llvm1 = new LlvmVisitor(OffsetString);
        llvm1.v.print_llvm(OffsetString,root.accept(llvm1, null));
        System.out.println(">>llvm file printed to ./LLVMfiles/" +OffsetString+ ".ll");
		System.out.println("-------------------------------------");

	    //System.out.println(root.accept(eval, null));

	}
    catch(ParseException ex){
	    System.out.println(ex.getMessage());
	}
	catch(FileNotFoundException ex){
	    System.err.println(ex.getMessage());
	}
	finally{
	    try{
		if(fis != null) fis.close();
	    }
	    catch(IOException ex){
		System.err.println(ex.getMessage());
	    }
	}
	}
}
