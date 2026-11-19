package com.ezylang.evalex.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.ezylang.evalex.config.ExpressionConfiguration;
import java.util.List;
import org.junit.Test;

public class Tokenizer_ArrowTest {

  @Test
  public void parsesNumberAndInfixOperator() throws Exception {
    Tokenizer tokenizer = new Tokenizer("1+2", new ExpressionConfiguration());
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals("1", tokens.get(0).getValue());
    assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals(Token.TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("2", tokens.get(2).getValue());
    assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(2).getType());
  }

  @Test
  public void skipsWhitespace() throws Exception {
    Tokenizer tokenizer = new Tokenizer("  12   +  3 ", new ExpressionConfiguration());
    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size());
    assertEquals("12", tokens.get(0).getValue());
    assertEquals("+", tokens.get(1).getValue());
    assertEquals("3", tokens.get(2).getValue());
  }

  @Test
  public void parsesStringLiteral() throws Exception {
    Tokenizer tokenizer = new Tokenizer("\"hello\"", new ExpressionConfiguration());
    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size());
    assertEquals("hello", tokens.get(0).getValue());
    assertEquals(Token.TokenType.STRING_LITERAL, tokens.get(0).getType());
  }

  @Test
  public void rejectsUnclosedBrace() {
    Tokenizer tokenizer = new Tokenizer("(1+2", new ExpressionConfiguration());

    assertThrows(ParseException.class, tokenizer::parse);
  }

  @Test
  public void rejectsUnclosedString() {
    Tokenizer tokenizer = new Tokenizer("\"hello", new ExpressionConfiguration());

    assertThrows(ParseException.class, tokenizer::parse);
  }
}
