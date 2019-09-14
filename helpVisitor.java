import syntaxtree.*;
import visitor.GJDepthFirst;

public class helpVisitor extends  GJDepthFirst<String, String>{

/*SOME PRIMARY EXPRESSIONS */

	public String visit(IntegerLiteral il,String argu){
		String int_literal = il.f0.toString();
		//System.out.println("IM AN ID_LITERAL : " + id_literal + "\n" );
		return "int_literal:"+ int_literal;
	}

	public String visit(TrueLiteral tl,String argu){
		Boolean t = true;
		String true_boolean = Boolean.toString(t);
		//System.out.println("IM A TRUE : " + true_boolean  + "\n" );
		return true_boolean;
	}

	public String visit(FalseLiteral fl,String argu){
		Boolean f = false;
		String false_boolean = Boolean.toString(f);
	//	System.out.println("IM A FALSE : " + false_boolean  + "\n" );
		return false_boolean;
	}

	public String visit(Identifier iid, String argu) {
		String id=iid.f0.toString();
		//System.out.print("IM AN IDENTIFIER : " + id  + "\n");
		return id;
  	}

	public String visit(ThisExpression te,String argu){
		String this_expr = te.f0.toString();
		//System.out.println("IM A THIS_EXPRESSION : " + this_expr  + "\n" );
		return this_expr;
	}
	/*TYPES*/
	public String visit(IntegerType it,String argu)
	{
		String inttype = it.f0.toString();
		//String integerType = "int";
		return inttype;
	}
	public String visit(BooleanType bt,String argu)
	{
		String booleantype = bt.f0.toString();
		return booleantype;
	}
	public String visit(ArrayType at,String argu)
	{
		at.f0.accept(this,argu); //int
		at.f1.accept(this,argu); //[
		at.f2.accept(this,argu); //]
		String arrayType = "intArray";
		return arrayType;
	}



}
