package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String[] args) throws IOException {
		//Should pass this in as an arg
		String outputDir = "./src/lox";
		defineAst(outputDir, "Expr", Arrays.asList(
			"Assign		: Token name, Expr value",
			"Binary		: Expr left, Token operator, Expr right",
			"Grouping	: Expr expression",
			"Literal	: Object value",
			"Logical	: Expr left, Token operator, Expr right",
			"Unary		: Token operator, Expr right",
			"Variable	: Token name"
		));
		
		defineAst(outputDir, "Stmt", Arrays.asList(
			"Block		: List<Stmt> statements",
			"Expression	: Expr expression",
			"If			: Expr condition, Stmt thenBranch, Stmt elseBranch",
			"Print		: Expr expression",
			"Var		: Token name, Expr initializer",
			"While		: Expr condition, Stmt body"
		));
	}
	
	private static void defineAst(String outputDir, String baseName, List<String> types)
		throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		writer.println("package lox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("abstract class " + baseName + " {");
		
		writer.println();
		defineVisitor(writer, baseName, types);
		writer.println();
		
		for (String type : types) {
			//pull the information from the description string
			String[] info = type.split(":");
			String className = info[0].trim();
			String fields = info[1].trim();
			defineType(writer, baseName, className, fields);
			writer.println();
		}
		
		//the base accept method
		writer.println("	abstract<R> R accept(Visitor<R> visitor);");
		
		writer.println("}");
		writer.close();
	}
	
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("	interface Visitor<R> {");
		
		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("		R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}
		
		writer.println("	}");
	}
	
	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		writer.println("	static class " + className + " extends " + baseName + " {");
		
		//constructor creation
		writer.println("		" + className + "(" + fieldList + ") {");
		
		//store parameters in fields
		String[] fields = fieldList.split(", ");
		for (String field: fields) {
			String name = field.split(" ")[1];
			writer.println("			this." + name + " = " + name + ";");
		}
		
		writer.println("		}");
		
		//Visitor pattern makes sure each generated class is sent to the proper method
		writer.println();
		writer.println("		<R> R accept(Visitor<R> visitor) {");
		writer.println("			return visitor.visit" + className + baseName + "(this);");
		writer.println("		}");
		
		//Fields
		writer.println();
		for (String field: fields) {
			writer.println("		final " + field + ";");
		}
		
		writer.println("	}");
	}
}
