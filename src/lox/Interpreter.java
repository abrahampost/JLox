package lox;

import java.util.List;

import lox.Expr.Assign;
import lox.Expr.Binary;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Unary;
import lox.Expr.Variable;
import lox.Stmt.Var;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

	private Environment environment = new Environment();
	
	void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch(RuntimeError error) {
			Lox.runtimeError(error);
		}
	}
	
	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
			case GREATER:
				checkNumberOperands(expr.operator, left, right);
				return (double)left > (double)right;
			case GREATER_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left >= (double)right;
			case LESS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left < (double)right;
			case LESS_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left <= (double)right;
			case MINUS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left - (double)right;
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
			case PLUS:
				if (left instanceof Double && right instanceof Double) {
					return (double)left + (double)right;
				}
				
				if (left instanceof String && right instanceof String) {
					return (String)left + (String) right;
				}
				
				throw new RuntimeError(expr.operator, 
						"Operands must be two numbers or two strings.");
			case SLASH:
				return (double)left / (double)right;
			case STAR:
				return (double)left * (double)right;
		}
		
		//unreachable
		return null;
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
			case BANG:
				return !isTruthy(right);
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return -(double)right;
		}
		
		return null;
	}
	
	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers.");
	}
	
	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}
	
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}
	
	private void execute(Stmt stmt) {
		stmt.accept(this);
	}
	
	public Void visitPrintStmt(Stmt.Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}
	
	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			//if there is a value attached to the declaration, evaluate and save
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		return environment.get(expr.name);
	}
	
	private boolean isTruthy(Object object) {
		if (object == null) return false;
		if (object instanceof Boolean) return (boolean)object;
		return true;
	}
	
	private boolean isEqual(Object a, Object b) {
		//nil is only equal to nil
		if (a == null && b == null) return true;
		if (a == null) return false;
		
		return a.equals(b);
	}
	
	private String stringify(Object object) {
		if (object == null) return "nil";
		
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() -2);
			}
			return text;
		}
		
		return object.toString();
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		
		environment.assign(expr.name, value);
		return value;
	}

}
