package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        Document doc = Jsoup.parse("<html><body><p>Hello</p></body></html>");
        Element context = doc.body();
        List<Node> result = treeBuilder.parseFragment("<div>World</div>", context, "http://example.com", new Parser(new HtmlParser()));
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Element);
        assertEquals("div", result.get(0).nodeName());
    }

    @Test
    public void testProcessToken() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name = "p";
        startTag.attributes = new Attributes();
        
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
        Element root = new Element("html", "http://example.com");
        treeBuilder.stack.add(root);
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name = "p";
        startTag.attributes = new Attributes();
        
        Element result = treeBuilder.insert(startTag);
        assertNotNull(result);
        assertEquals("p", result.tagName());
    }

    @Test
    public void testInsertStartTag() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element root = new Element("html", "http://example.com");
        treeBuilder.stack.add(root);
        
        Element result = treeBuilder.insertStartTag("div");
        assertNotNull(result);
        assertEquals("div", result.tagName());
    }

    @Test
    public void testInsertEmpty() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element root = new Element("html", "http://example.com");
        treeBuilder.stack.add(root);
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name = "br";
        startTag.attributes = new Attributes();
        startTag.setSelfClosing();
        
        Element result = treeBuilder.insertEmpty(startTag);
        assertNotNull(result);
        assertEquals("br", result.tagName());
    }

    @Test
    public void testPop() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element("html", "http://example.com");
        Element el2 = new Element("body", "http://example.com");
        treeBuilder.stack.add(el1);
        treeBuilder.stack.add(el2);
        
        Element popped = treeBuilder.pop();
        assertEquals(el2, popped);
        assertEquals(1, treeBuilder.stack.size());
    }

    @Test
    public void testPush() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("html", "http://example.com");
        
        treeBuilder.push(el);
        assertEquals(1, treeBuilder.stack.size());
        assertEquals(el, treeBuilder.stack.get(0));
    }

    @Test
    public void testOnStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("html", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.onStack(el));
        assertFalse(treeBuilder.onStack("body"));
    }

    @Test
    public void testGetFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("html", "http://example.com");
        treeBuilder.stack.add(el);
        
        Element found = treeBuilder.getFromStack("html");
        assertEquals(el, found);
        
        Element notFound = treeBuilder.getFromStack("body");
        assertNull(notFound);
    }

    @Test
    public void testRemoveFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element("html", "http://example.com");
        Element el2 = new Element("body", "http://example.com");
        treeBuilder.stack.add(el1);
        treeBuilder.stack.add(el2);
        
        boolean removed = treeBuilder.removeFromStack(el1);
        assertTrue(removed);
        assertEquals(1, treeBuilder.stack.size());
        assertEquals(el2, treeBuilder.stack.get(0));
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("html", "http://example.com");
        treeBuilder.stack.add(el);
        
        boolean changed = treeBuilder.resetInsertionMode();
        assertTrue(changed);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.state());
    }

    @Test
    public void testInScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("html", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.inScope("html"));
        assertFalse(treeBuilder.inScope("body"));
    }

    @Test
    public void testInListItemScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("li", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inListItemScope("div"));
    }

    @Test
    public void testInButtonScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("button", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.inButtonScope("button"));
        assertFalse(treeBuilder.inButtonScope("div"));
    }

    @Test
    public void testInTableScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("table", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inTableScope("div"));
    }

    @Test
    public void testInSelectScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("option", "http://example.com");
        treeBuilder.stack.add(el);
        
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("div"));
    }

    @Test
    public void testOnStackNot() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element("html", "http://example.com");
        Element el2 = new Element("body", "http://example.com");
        treeBuilder.stack.add(el1);
        treeBuilder.stack.add(el2);
        
        assertTrue(treeBuilder.onStackNot(new String[]{"html"}));
        assertFalse(treeBuilder.onStackNot(new String[]{"html", "body"}));
    }

    @Test
    public void testSetHeadElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element head = new Element("head", "http://example.com");
        treeBuilder.setHeadElement(head);
        assertEquals(head, treeBuilder.getHeadElement());
    }

    @Test
    public void testIsFosterInserts() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertFalse(treeBuilder.isFosterInserts());
        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());
    }

    @Test
    public void testSetFormElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com");
        treeBuilder.setFormElement(form);
        assertEquals(form, treeBuilder.getFormElement());
    }

    @Test
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el1 = new Element("p", "http://example.com");
        Element el2 = new Element("div", "http://example.com");
        treeBuilder.stack.add(el1);
        treeBuilder.stack.add(el2);
        
        treeBuilder.generateImpliedEndTags();
        assertEquals(2, treeBuilder.stack.size());
    }

    @Test
    public void testCloseElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("p", "http://example.com");
        treeBuilder.stack.add(el);
        
        treeBuilder.closeElement("p");
        assertEquals(0, treeBuilder.stack.size());
    }

    @Test
    public void testIsSpecial() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element el1 = new Element("script", "http://example.com");
        Element el2 = new Element("div", "http://example.com");
        
        assertTrue(treeBuilder.isSpecial(el1));
        assertFalse(treeBuilder.isSpecial(el2));
    }

    @Test
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser(new HtmlParser()));
        Element el = new Element("p", "http://example.com");
        treeBuilder.stack.add(el);
        
        treeBuilder.reconstructFormattingElements();
        assertEquals(1, treeBuilder.stack.size());
    }

    @Test
    public void testPushActiveFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element el = new Element("p", "http://example.com");
        treeBuilder.pushActiveFormattingElements(el);
        assertEquals(1, treeBuilder.formattingElements.size());
    }

    @Test
    public void testInsertMarkerToFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.insertMarkerToFormattingElements();
        assertEquals(1, treeBuilder.formattingElements.size());
        assertNull(treeBuilder.formattingElements.get(0));
    }

    @Test
    public void testCurrentTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertNull(treeBuilder.currentTemplateMode());
        
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.currentTemplateMode());
    }

    @Test
    public void testTemplateModeSize() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertEquals(0, treeBuilder.templateModeSize());
        
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        assertEquals(1, treeBuilder.templateModeSize());
    }

    @Test
    public void testPopTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.popTemplateMode());
        assertNull(treeBuilder.popTemplateMode());
    }

    @Test
    public void testIsContentForTagData() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertTrue(treeBuilder.isContentForTagData("script"));
        assertTrue(treeBuilder.isContentForTagData("style"));
        assertFalse(treeBuilder.isContentForTagData("div"));
    }
}
