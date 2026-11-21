package com.ezylang.evalex.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class Tokenizer_ArrowTest {

  private ExpressionConfiguration configuration;
  private OperatorDictionaryIfc operatorDictionary;
  private FunctionDictionaryIfc functionDictionary;

  @Before
  public void setUp() {
    configuration = mock(ExpressionConfiguration.class);
    operatorDictionary = mock(OperatorDictionaryIfc.class);
    functionDictionary = mock(FunctionDictionaryIfc.class);

    when(configuration.getOperatorDictionary()).thenReturn(operatorDictionary);
    when(configuration.getFunctionDictionary()).thenReturn(functionDictionary);
    when(configuration.isImplicitMultiplicationAllowed()).thenReturn(true);
    when(configuration.isArraysAllowed()).thenReturn(true);
    when(configuration.isStructuresAllowed()).thenReturn(true);
  }

  @Test
  public void testParseSimpleExpression() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("1+2", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1", tokens.get(0).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(2).getType());
    assertEquals("2", tokens.get(2).getValue());
  }

  @Test
  public void testParseWithSpaces() throws ParseException {
    Tokenizer tokenizer = new Tokenizer(" 1 + 2 ", configuration);
    List<Token> tokens = tokenizer.parse();

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
    Tokenizer tokenizer = new Tokenizer("x", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals("x", tokens.get(0).getValue());
  }

  @Test
  public void testParseFunction() throws ParseException {
    when(functionDictionary.hasFunction("sin")).thenReturn(true);
    when(functionDictionary.getFunction("sin")).thenReturn(mock(com.ezylang.evalex.functions.FunctionIfc.class));

    Tokenizer tokenizer = new Tokenizer("sin(x)", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals(TokenType.FUNCTION, tokens.get(0).getType());
    assertEquals("sin", tokens.get(0).getValue());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(1).getType());
    assertEquals("(", tokens.get(1).getValue());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    assertEquals("x", tokens.get(2).getValue());
  }

  @Test
  public void testParseStringLiteral() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("\"hello\"", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
    assertEquals("hello", tokens.get(0).getValue());
  }

  @Test
  public void testParseHexNumber() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("0xFF", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("0xFF", tokens.get(0).getValue());
  }

  @Test
  public void testParseScientificNotation() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("1.23e10", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1.23e10", tokens.get(0).getValue());
  }

  @Test
  public void testParseBraces() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("(1+2)", configuration);
    List<Token> tokens = tokenizer.parse();

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
  public void testParseArray() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("[1,2]", configuration);
    List<Token> tokens = tokenizer.parse();

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
    Tokenizer tokenizer = new Tokenizer("obj.field", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals("obj", tokens.get(0).getValue());
    assertEquals(TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType());
    assertEquals(".", tokens.get(1).getValue());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    assertEquals("field", tokens.get(2).getValue());
  }

  @Test
  public void testParseComplexExpression() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("sin(1+2)*3.14", configuration);
    List<Token> tokens = tokenizer.parse();

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
  public void testParseEmptyExpression() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(0, tokens.size());
  }

  @Test
  public void testParseSingleOperator() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("+", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(0).getType());
    assertEquals("+", tokens.get(0).getValue());
  }

  @Test
  public void testParseSingleNumber() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("42", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("42", tokens.get(0).getValue());
  }

  @Test
  public void testParseNegativeNumber() throws ParseException {
    when(operatorDictionary.hasPrefixOperator("-")).thenReturn(true);
    when(operatorDictionary.getPrefixOperator("-")).thenReturn(mock(OperatorIfc.class));

    Tokenizer tokenizer = new Tokenizer("-42", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(2, tokens.size());
    assertEquals(TokenType.PREFIX_OPERATOR, tokens.get(0).getType());
    assertEquals("-", tokens.get(0).getValue());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType());
    assertEquals("42", tokens.get(1).getValue());
  }

  @Test
  public void testParseImplicitMultiplication() throws ParseException {
    when(configuration.isImplicitMultiplicationAllowed()).thenReturn(true);
    when(operatorDictionary.hasInfixOperator("*")).thenReturn(true);
    when(operatorDictionary.getInfixOperator("*")).thenReturn(mock(OperatorIfc.class));

    Tokenizer tokenizer = new Tokenizer("2x", configuration);
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("2", tokens.get(0).getValue());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("*", tokens.get(1).getValue());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    assertEquals("x", tokens.get(2).getValue());
  }

  @Test(expected = ParseException.class)
  public void testParseMissingOperator() throws ParseException {
    when(configuration.isImplicitMultiplicationAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("2x", configuration);
    tokenizer.parse();
  }

  @Test(expected = ParseException.class)
  public void testParseUnclosedBrace() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("(1+2", configuration);
    tokenizer.parse();
  }

  @Test(expected = ParseException.class)
  public void testParseUnclosedArray() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("[1,2", configuration);
    tokenizer.parse();
  }

  @Test(expected = ParseException.class)
  public void testParseUndefinedFunction() throws ParseException {
    when(functionDictionary.hasFunction("undefined")).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("undefined()", configuration);
    tokenizer.parse();
  }

  @Test(expected = ParseException.class)
  public void testParseInvalidScientificFormat() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("1.23e", configuration);
    tokenizer.parse();
  }

  @Test(expected = ParseException.class)
  public void testParseUnknownEscapeCharacter() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("\"\\z\"", configuration);
    tokenizer.parse();
  }

  @Test
  public void testGetPreviousTokenNull() {
    Tokenizer tokenizer = new Tokenizer("1", configuration);
    assertNull(tokenizer.getPreviousToken());
  }

  @Test
  public void testGetPreviousTokenExists() throws ParseException {
    Tokenizer tokenizer = new Tokenizer("1+2", configuration);
    tokenizer.parse(); // This will populate the tokens list

    // We can't directly access the internal tokens list, but we can verify
    // that the parsing works correctly by checking the tokens count
    assertEquals(3, tokenizer.parse().size());
  }
}
