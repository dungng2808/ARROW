package com.ezylang.evalex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Tokenizer_ArrowTest {

  @Test
  public void testParseSimpleExpression() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(opDict.hasInfixOperator("*")).thenReturn(true);
    when(opDict.getInfixOperator("*")).thenReturn(mock(OperatorIfc.class));

    Tokenizer tokenizer = new Tokenizer("1+2", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("1");
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.INFIX_OPERATOR);
    assertThat(tokens.get(1).getValue()).isEqualTo("+");
    assertThat(tokens.get(2).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(2).getValue()).isEqualTo("2");
  }

  @Test
  public void testParseWithImplicitMultiplication() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(true);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(opDict.hasInfixOperator("*")).thenReturn(true);
    when(opDict.getInfixOperator("*")).thenReturn(mock(OperatorIfc.class));

    Tokenizer tokenizer = new Tokenizer("(1)(2)", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(5);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.BRACE_OPEN);
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(1).getValue()).isEqualTo("1");
    assertThat(tokens.get(2).getType()).isEqualTo(Token.TokenType.INFIX_OPERATOR);
    assertThat(tokens.get(2).getValue()).isEqualTo("*");
    assertThat(tokens.get(3).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(3).getValue()).isEqualTo("2");
    assertThat(tokens.get(4).getType()).isEqualTo(Token.TokenType.BRACE_CLOSE);
  }

  @Test
  public void testParseInvalidExpressionMissingOperator() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("(1)(2)", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Missing operator");
  }

  @Test
  public void testParseStringLiteral() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("\"hello world\"", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.STRING_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("hello world");
  }

  @Test
  public void testParseNumberLiteral() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("123.456", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("123.456");
  }

  @Test
  public void testParseHexNumberLiteral() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("0x1A2B", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("0x1A2B");
  }

  @Test
  public void testParseVariable() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("variable", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.VARIABLE_OR_CONSTANT);
    assertThat(tokens.get(0).getValue()).isEqualTo("variable");
  }

  @Test
  public void testParseFunction() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(funcDict.hasFunction("sin")).thenReturn(true);
    when(funcDict.getFunction("sin")).thenReturn(mock(com.ezylang.evalex.functions.FunctionIfc.class));

    Tokenizer tokenizer = new Tokenizer("sin(x)", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.FUNCTION);
    assertThat(tokens.get(0).getValue()).isEqualTo("sin");
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.BRACE_OPEN);
    assertThat(tokens.get(2).getType()).isEqualTo(Token.TokenType.VARIABLE_OR_CONSTANT);
    assertThat(tokens.get(2).getValue()).isEqualTo("x");
  }

  @Test
  public void testParseBraces() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("(1+2)", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(5);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.BRACE_OPEN);
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(1).getValue()).isEqualTo("1");
    assertThat(tokens.get(2).getType()).isEqualTo(Token.TokenType.INFIX_OPERATOR);
    assertThat(tokens.get(2).getValue()).isEqualTo("+");
    assertThat(tokens.get(3).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(3).getValue()).isEqualTo("2");
    assertThat(tokens.get(4).getType()).isEqualTo(Token.TokenType.BRACE_CLOSE);
  }

  @Test
  public void testParseArrayNotAllowed() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("[1,2]", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Array open not allowed here");
  }

  @Test
  public void testParseStructureNotAllowed() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("a.b", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Structure separator not allowed here");
  }

  @Test
  public void testParseInvalidBraceBalance() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("(1+2", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Closing brace not found");
  }

  @Test
  public void testParseInvalidArrayBalance() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(true);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("[1,2", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Closing array not found");
  }

  @Test
  public void testParseInvalidTokenAfterInfixOperator() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(opDict.hasInfixOperator("+")).thenReturn(true);
    when(opDict.getInfixOperator("+")).thenReturn(mock(OperatorIfc.class));

    Tokenizer tokenizer = new Tokenizer("1++2", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Unexpected token after infix operator");
  }

  @Test
  public void testParseInvalidScientificFormat() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("1.2e+", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Illegal scientific format");
  }

  @Test
  public void testParseUndefinedFunction() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(funcDict.hasFunction("undefined")).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("undefined()", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Undefined function");
  }

  @Test
  public void testParseUndefinedOperator() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);
    when(opDict.hasInfixOperator("@")).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("1@2", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Undefined operator");
  }

  @Test
  public void testParseStringWithEscapes() throws ParseException {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("\"Hello \\\"World\\\"\"", config);
    List<Token> tokens = tokenizer.parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.STRING_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("Hello \"World\"");
  }

  @Test
  public void testParseStringWithoutClosingQuote() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("\"unclosed", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Closing quote not found");
  }

  @Test
  public void testParseUnknownEscapeCharacter() {
    ExpressionConfiguration config = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(config.getOperatorDictionary()).thenReturn(opDict);
    when(config.getFunctionDictionary()).thenReturn(funcDict);
    when(config.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(config.isArraysAllowed()).thenReturn(false);
    when(config.isStructuresAllowed()).thenReturn(false);

    Tokenizer tokenizer = new Tokenizer("\"\\z\"", config);
    assertThatThrownBy(() -> tokenizer.parse())
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Unknown escape character");
  }
}
