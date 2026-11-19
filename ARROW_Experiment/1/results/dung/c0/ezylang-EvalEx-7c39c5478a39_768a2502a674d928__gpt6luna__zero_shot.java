package com.ezylang.evalex.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.ezylang.evalex.config.ExpressionConfiguration;
import java.util.List;
import org.junit.Test;

public class Tokenizer_ArrowTest {

  @Test
  public void parsesNumberAndVariableWithImplicitMultiplication() throws Exception {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    List<Token> tokens = new Tokenizer("2 x", configuration).parse();

    assertEquals(3, tokens.size());
    assertEquals("2", tokens.get(0).getValue());
    assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("*", tokens.get(1).getValue());
    assertEquals(Token.TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("x", tokens.get(2).getValue());
    assertEquals(Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
  }

  @Test
  public void parsesDecimalAndInfixOperator() throws Exception {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    List<Token> tokens = new Tokenizer("12.5+3", configuration).parse();

    assertEquals(3, tokens.size());
    assertEquals("12.5", tokens.get(0).getValue());
    assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals(Token.TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("3", tokens.get(2).getValue());
    assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(2).getType());
  }

  @Test
  public void skipsWhitespaceAndParsesParentheses() throws Exception {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    List<Token> tokens = new Tokenizer(" ( 1 ) ", configuration).parse();

    assertEquals(3, tokens.size());
    assertEquals(Token.TokenType.BRACE_OPEN, tokens.get(0).getType());
    assertEquals("1", tokens.get(1).getValue());
    assertEquals(Token.TokenType.BRACE_CLOSE, tokens.get(2).getType());
  }

  @Test
  public void parsesStringLiteral() throws Exception {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    List<Token> tokens = new Tokenizer("\"hello\"", configuration).parse();

    assertEquals(1, tokens.size());
    assertEquals("hello", tokens.get(0).getValue());
    assertEquals(Token.TokenType.STRING_LITERAL, tokens.get(0).getType());
  }

  @Test
  public void parsesEmptyExpression() throws Exception {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    List<Token> tokens = new Tokenizer("", configuration).parse();

    assertEquals(0, tokens.size());
  }

  @Test
  public void rejectsUnclosedBrace() {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    assertThrows(ParseException.class, () -> new Tokenizer("(1", configuration).parse());
  }

  @Test
  public void rejectsUnexpectedClosingBrace() {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    assertThrows(ParseException.class, () -> new Tokenizer(")", configuration).parse());
  }

  @Test
  public void rejectsUnclosedString() {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    assertThrows(ParseException.class, () -> new Tokenizer("\"hello", configuration).parse());
  }

  @Test
  public void rejectsInfixOperatorWithoutFollowingOperand() {
    ExpressionConfiguration configuration = new ExpressionConfiguration();
    assertThrows(ParseException.class, () -> new Tokenizer("1+", configuration).parse());
  }
}
