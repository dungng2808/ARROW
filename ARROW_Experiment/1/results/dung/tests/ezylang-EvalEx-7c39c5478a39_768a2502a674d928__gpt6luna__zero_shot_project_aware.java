package com.ezylang.evalex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Tokenizer_ArrowTest {

  private ExpressionConfiguration configuration(boolean implicitMultiplication) {
    ExpressionConfiguration configuration = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc operators = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc functions = mock(FunctionDictionaryIfc.class);
    when(configuration.getOperatorDictionary()).thenReturn(operators);
    when(configuration.getFunctionDictionary()).thenReturn(functions);
    when(configuration.isImplicitMultiplicationAllowed()).thenReturn(implicitMultiplication);
    when(configuration.isArraysAllowed()).thenReturn(false);
    when(configuration.isStructuresAllowed()).thenReturn(false);
    return configuration;
  }

  @Test
  public void parsesNumbersAndInfixOperator() throws Exception {
    ExpressionConfiguration configuration = configuration(false);
    OperatorDictionaryIfc operators = configuration.getOperatorDictionary();
    when(operators.hasInfixOperator("+")).thenReturn(true);

    List<Token> tokens = new Tokenizer("1+2", configuration).parse();

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getValue()).isEqualTo("1");
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
    assertThat(tokens.get(1).getValue()).isEqualTo("+");
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.INFIX_OPERATOR);
    assertThat(tokens.get(2).getValue()).isEqualTo("2");
    assertThat(tokens.get(2).getType()).isEqualTo(Token.TokenType.NUMBER_LITERAL);
  }

  @Test
  public void skipsWhitespaceAndParsesDecimalAndHexNumbers() throws Exception {
    ExpressionConfiguration configuration = configuration(false);

    List<Token> tokens = new Tokenizer(" 12.5 \t 0xAf", configuration).parse();

    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(0).getValue()).isEqualTo("12.5");
    assertThat(tokens.get(0).getStartPosition()).isEqualTo(1);
    assertThat(tokens.get(1).getValue()).isEqualTo("0xAf");
    assertThat(tokens.get(1).getStartPosition()).isEqualTo(8);
  }

  @Test
  public void parsesStringLiteralAndEscapedNewline() throws Exception {
    ExpressionConfiguration configuration = configuration(false);

    List<Token> tokens = new Tokenizer("\"a\\nb\"", configuration).parse();

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(Token.TokenType.STRING_LITERAL);
    assertThat(tokens.get(0).getValue()).isEqualTo("a\nb");
  }

  @Test
  public void insertsImplicitMultiplicationBetweenNumberAndVariable() throws Exception {
    ExpressionConfiguration configuration = configuration(true);
    OperatorDictionaryIfc operators = configuration.getOperatorDictionary();
    when(operators.hasInfixOperator("*")).thenReturn(true);

    List<Token> tokens = new Tokenizer("2x", configuration).parse();

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getValue()).isEqualTo("2");
    assertThat(tokens.get(1).getValue()).isEqualTo("*");
    assertThat(tokens.get(1).getType()).isEqualTo(Token.TokenType.INFIX_OPERATOR);
    assertThat(tokens.get(2).getValue()).isEqualTo("x");
  }

  @Test
  public void rejectsImplicitMultiplicationWhenDisabled() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer("2x", configuration).parse());
  }

  @Test
  public void rejectsUnclosedBrace() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer("(1", configuration).parse());
  }

  @Test
  public void rejectsUnexpectedClosingBrace() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer(")", configuration).parse());
  }

  @Test
  public void rejectsUnclosedString() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer("\"abc", configuration).parse());
  }

  @Test
  public void rejectsUnknownOperator() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer("@", configuration).parse());
  }

  @Test
  public void rejectsInvalidScientificNotation() {
    ExpressionConfiguration configuration = configuration(false);

    assertThrows(ParseException.class, () -> new Tokenizer("1e", configuration).parse());
  }
}
