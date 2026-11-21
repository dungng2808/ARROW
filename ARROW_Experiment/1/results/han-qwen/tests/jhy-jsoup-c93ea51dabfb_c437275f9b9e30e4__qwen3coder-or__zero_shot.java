package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class HtmlTreeBuilder_ArrowTest {

    @Test
    public void testInitialiseParse() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertFalse(treeBuilder.baseUriSetFromDoc());
        assertNull(treeBuilder.getHeadElement());
        assertNull(treeBuilder.getFormElement());
        assertNull(treeBuilder.contextElement);
        assertNotNull(treeBuilder.formattingElements);
        assertNotNull(treeBuilder.tmplInsertMode);
        assertNotNull(treeBuilder.pendingTableCharacters);
        assertNotNull(treeBuilder.emptyEnd);
        assertTrue(treeBuilder.framesetOk());
        assertFalse(treeBuilder.isFosterInserts());
        assertFalse(treeBuilder.isFragmentParsing());
    }

    @Test
    public void testParseFragment() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String fragment = "<p>Hello</p>";
        Element context = new Element(Tag.valueOf("div"), "");
        List<Node> result = treeBuilder.parseFragment(fragment, context, "http://example.com", new Parser(new HtmlParser()));
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Element);
        assertEquals("p", ((Element)result.get(0)).tagName());
    }

    @Test
    public void testProcess() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        boolean result = treeBuilder.process(startTag);
        
        assertTrue(result);
    }

    @Test
    public void testTransition() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.state());
    }

    @Test
    public void testMarkInsertionMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.transition(HtmlTreeBuilderState.InBody);
        treeBuilder.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.originalState());
    }

    @Test
    public void testFramesetOk() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.framesetOk(false);
        assertFalse(treeBuilder.framesetOk());
        treeBuilder.framesetOk(true);
        assertTrue(treeBuilder.framesetOk());
    }

    @Test
    public void testGetDocument() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Document doc = treeBuilder.getDocument();
        assertNotNull(doc);
    }

    @Test
    public void testGetBaseUri() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        assertEquals("http://example.com", treeBuilder.getBaseUri());
    }

    @Test
    public void testIsFragmentParsing() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertFalse(treeBuilder.isFragmentParsing());
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        treeBuilder.fragmentParsing = true;
        assertTrue(treeBuilder.isFragmentParsing());
    }

    @Test
    public void testInsert() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        Element result = treeBuilder.insert(startTag);
        assertNotNull(result);
        assertEquals("div", result.tagName());
    }

    @Test
    public void testInsertStartTag() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element result = treeBuilder.insertStartTag("span");
        assertNotNull(result);
        assertEquals("span", result.tagName());
    }

    @Test
    public void testInsertEmpty() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("br");
        Element result = treeBuilder.insertEmpty(startTag);
        assertNotNull(result);
        assertEquals("br", result.tagName());
    }

    @Test
    public void testInsertComment() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Token.Comment commentToken = new Token.Comment();
        commentToken.data("test comment");
        treeBuilder.insert(commentToken);
        assertEquals(1, treeBuilder.getStack().size());
    }

    @Test
    public void testInsertCharacter() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Token.Character charToken = new Token.Character();
        charToken.data("Hello");
        treeBuilder.insert(charToken);
        assertEquals(1, treeBuilder.getStack().size());
    }

    @Test
    public void testPop() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertEquals(1, treeBuilder.getStack().size());
        Element popped = treeBuilder.pop();
        assertEquals(el, popped);
        assertEquals(0, treeBuilder.getStack().size());
    }

    @Test
    public void testPush() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertEquals(1, treeBuilder.getStack().size());
        assertEquals(el, treeBuilder.getStack().get(0));
    }

    @Test
    public void testOnStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.onStack(el));
        assertFalse(treeBuilder.onStack("span"));
    }

    @Test
    public void testGetFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        Element found = treeBuilder.getFromStack("div");
        assertEquals(el, found);
        assertNull(treeBuilder.getFromStack("span"));
    }

    @Test
    public void testRemoveFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.removeFromStack(el));
        assertFalse(treeBuilder.removeFromStack(el));
    }

    @Test
    public void testPopStackToClose() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("p"), "");
        treeBuilder.push(el1);
        treeBuilder.push(el2);
        Element popped = treeBuilder.popStackToClose("div");
        assertEquals(el1, popped);
        assertEquals(1, treeBuilder.getStack().size());
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        boolean changed = treeBuilder.resetInsertionMode();
        assertTrue(changed);
    }

    @Test
    public void testInScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.inScope("div"));
        assertFalse(treeBuilder.inScope("p"));
    }

    @Test
    public void testInListItemScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("li"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inListItemScope("div"));
    }

    @Test
    public void testInButtonScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("button"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.inButtonScope("button"));
        assertFalse(treeBuilder.inButtonScope("div"));
    }

    @Test
    public void testInTableScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("table"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inTableScope("div"));
    }

    @Test
    public void testInSelectScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("option"), "");
        treeBuilder.push(el);
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("div"));
    }

    @Test
    public void testOnStackNot() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        assertFalse(treeBuilder.onStackNot(new String[]{"div"}));
        assertTrue(treeBuilder.onStackNot(new String[]{"span"}));
    }

    @Test
    public void testSetHeadElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element head = new Element(Tag.valueOf("head"), "");
        treeBuilder.setHeadElement(head);
        assertEquals(head, treeBuilder.getHeadElement());
    }

    @Test
    public void testSetFosterInserts() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());
        treeBuilder.setFosterInserts(false);
        assertFalse(treeBuilder.isFosterInserts());
    }

    @Test
    public void testSetFormElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        FormElement form = new FormElement(Tag.valueOf("form"), "");
        treeBuilder.setFormElement(form);
        assertEquals(form, treeBuilder.getFormElement());
    }

    @Test
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element(Tag.valueOf("p"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el1);
        treeBuilder.push(el2);
        treeBuilder.generateImpliedEndTags();
        assertEquals(2, treeBuilder.getStack().size());
    }

    @Test
    public void testCloseElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("p"), "");
        treeBuilder.push(el);
        treeBuilder.closeElement("p");
        assertEquals(0, treeBuilder.getStack().size());
    }

    @Test
    public void testIsSpecial() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element el = new Element(Tag.valueOf("script"), "");
        assertTrue(treeBuilder.isSpecial(el));
        Element el2 = new Element(Tag.valueOf("div"), "");
        assertFalse(treeBuilder.isSpecial(el2));
    }

    @Test
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.push(el);
        treeBuilder.reconstructFormattingElements();
        assertEquals(1, treeBuilder.getStack().size());
    }

    @Test
    public void testPushActiveFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.pushActiveFormattingElements(el);
        assertEquals(1, treeBuilder.formattingElements.size());
    }

    @Test
    public void testClearFormattingElementsToLastMarker() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.clearFormattingElementsToLastMarker();
        assertEquals(0, treeBuilder.formattingElements.size());
    }

    @Test
    public void testGetActiveFormattingElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element(Tag.valueOf("div"), "");
        treeBuilder.pushActiveFormattingElements(el);
        Element found = treeBuilder.getActiveFormattingElement("div");
        assertEquals(el, found);
    }

    @Test
    public void testPushTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        assertEquals(1, treeBuilder.templateModeSize());
    }

    @Test
    public void testPopTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        HtmlTreeBuilderState popped = treeBuilder.popTemplateMode();
        assertEquals(HtmlTreeBuilderState.InBody, popped);
        assertEquals(0, treeBuilder.templateModeSize());
    }

    @Test
    public void testCurrentTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        HtmlTreeBuilderState current = treeBuilder.currentTemplateMode();
        assertEquals(HtmlTreeBuilderState.InBody, current);
    }
}
