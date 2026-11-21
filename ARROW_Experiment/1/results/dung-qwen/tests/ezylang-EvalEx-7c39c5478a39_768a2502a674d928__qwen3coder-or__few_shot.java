package com.ezylang.evalex.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.ezylang.evalex.config.DefaultExpressionConfiguration;
import com.ezylang.evalex.config.ExpressionConfiguration;
import java.util.List;
import org.junit.Test;

public class Tokenizer_ArrowTest {

  @Test
  public void testParseSimpleExpression() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("1+2", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(3, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1", tokens.get(0).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(2).getType());
    assertEquals("2", tokens.get(2).getValue());
  }

  @Test
  public void testParseVariable() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("x", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals("x", tokens.get(0).getValue());
  }

  @Test
  public void testParseStringLiteral() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("\"hello\"", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
    assertEquals("hello", tokens.get(0).getValue());
  }

  @Test
  public void testParseFunction() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("sin(x)", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(3, tokens.size());
    assertEquals(TokenType.FUNCTION, tokens.get(0).getType());
    assertEquals("sin", tokens.get(0).getValue());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(1).getType());
    assertEquals("(", tokens.get(1).getValue());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    assertEquals("x", tokens.get(2).getValue());
  }

  @Test
  public void testParseBraces() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("(1+2)", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(5, tokens.size());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(0).getType());
    assertEquals("(", tokens.get(0).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType());
    assertEquals("1", tokens.get(1).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(2).getType());
    assertEquals("+", tokens.get(2).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(3).getType());
    assertEquals("2", tokens.get(3).getValue());
    assertEquals(TokenType.BRACE_CLOSE, tokens.get(4).getType());
    assertEquals(")", tokens.get(4).getValue());
  }

  @Test
  public void testParseNumber() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("123.456", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("123.456", tokens.get(0).getValue());
  }

  @Test
  public void testParseHexNumber() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("0xFF", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("0xFF", tokens.get(0).getValue());
  }

  @Test
  public void testParseScientificNotation() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("1.23E10", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1.23E10", tokens.get(0).getValue());
  }

  @Test
  public void testParseComplexExpression() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("sin(1+2)*3", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(7, tokens.size());
    assertEquals(TokenType.FUNCTION, tokens.get(0).getType());
    assertEquals("sin", tokens.get(0).getValue());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(1).getType());
    assertEquals("(", tokens.get(1).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(2).getType());
    assertEquals("1", tokens.get(2).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(3).getType());
    assertEquals("+", tokens.get(3).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(4).getType());
    assertEquals("2", tokens.get(4).getValue());
    assertEquals(TokenType.BRACE_CLOSE, tokens.get(5).getType());
    assertEquals(")", tokens.get(5).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(6).getType());
    assertEquals("*", tokens.get(6).getValue());
  }

  @Test
  public void testParseWithSpaces() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer(" 1 + 2 ", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(3, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1", tokens.get(0).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(2).getType());
    assertEquals("2", tokens.get(2).getValue());
  }

  @Test
  public void testParseEmptyExpression() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(0, tokens.size());
  }

  @Test
  public void testParseSingleOperator() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("*", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(0).getType());
    assertEquals("*", tokens.get(0).getValue());
  }

  @Test
  public void testParseMultipleOperators() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("++", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(2, tokens.size());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(0).getType());
    assertEquals("+", tokens.get(0).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("+", tokens.get(1).getValue());
  }

  @Test
  public void testParseComma() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("func(1,2)", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(5, tokens.size());
    assertEquals(TokenType.FUNCTION, tokens.get(0).getType());
    assertEquals("func", tokens.get(0).getValue());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(1).getType());
    assertEquals("(", tokens.get(1).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(2).getType());
    assertEquals("1", tokens.get(2).getValue());
    assertEquals(TokenType.COMMA, tokens.get(3).getType());
    assertEquals(",", tokens.get(3).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(4).getType());
    assertEquals("2", tokens.get(4).getValue());
  }

  @Test
  public void testParseArray() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("[1,2]", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(5, tokens.size());
    assertEquals(TokenType.ARRAY_OPEN, tokens.get(0).getType());
    assertEquals("[", tokens.get(0).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType());
    assertEquals("1", tokens.get(1).getValue());
    assertEquals(TokenType.COMMA, tokens.get(2).getType());
    assertEquals(",", tokens.get(2).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(3).getType());
    assertEquals("2", tokens.get(3).getValue());
    assertEquals(TokenType.ARRAY_CLOSE, tokens.get(4).getType());
    assertEquals("]", tokens.get(4).getValue());
  }

  @Test
  public void testParseStructureSeparator() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("obj.field", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(3, tokens.size());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals("obj", tokens.get(0).getValue());
    assertEquals(TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType());
    assertEquals(".", tokens.get(1).getValue());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    assertEquals("field", tokens.get(2).getValue());
  }

  @Test
  public void testParseWithEscapedCharacters() throws ParseException {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("\"Hello \\\"World\\\"\"", config);
    List<Token> tokens = tokenizer.parse();
    
    assertNotNull(tokens);
    assertEquals(1, tokens.size());
    assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
    assertEquals("Hello \"World\"", tokens.get(0).getValue());
  }

  @Test
  public void testParseInvalidExpression() {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("1+", config);
    
    try {
      tokenizer.parse();
      assertTrue(false); // Should not reach here
    } catch (ParseException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testParseUnbalancedBraces() {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("(1+2", config);
    
    try {
      tokenizer.parse();
      assertTrue(false); // Should not reach here
    } catch (ParseException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testParseUnbalancedArrays() {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("[1,2", config);
    
    try {
      tokenizer.parse();
      assertTrue(false); // Should not reach here
    } catch (ParseException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testParseInvalidOperator() {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("1@2", config);
    
    try {
      tokenizer.parse();
      assertTrue(false); // Should not reach here
    } catch (ParseException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testParseUndefinedFunction() {
    ExpressionConfiguration config = new DefaultExpressionConfiguration();
    Tokenizer tokenizer = new Tokenizer("undefined_func()", config);
    
    try {
      tokenizer.parse();
      assertTrue(false); // Should not reach here
    } catch (ParseException e) {
      assertNotNull(e);
    }
  }
}
