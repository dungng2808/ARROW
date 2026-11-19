package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.HtmlTreeBuilderState;
import org.jsoup.parser.Token;
import org.jsoup.parser.TreeBuilder;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilder_ArrowTest {

    @Test
    public void testInitialiseParse() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertEquals(HtmlTreeBuilderState.Initial, treeBuilder.state());
        assertNull(treeBuilder.originalState());
        assertFalse(treeBuilder.baseUriSetFromDoc);
        assertNull(treeBuilder.headElement);
        assertNull(treeBuilder.formElement);
        assertNull(treeBuilder.contextElement);
        assertNotNull(treeBuilder.formattingElements);
        assertNotNull(treeBuilder.tmplInsertMode);
        assertNotNull(treeBuilder.pendingTableCharacters);
        assertNotNull(treeBuilder.emptyEnd);
        assertTrue(treeBuilder.framesetOk);
        assertFalse(treeBuilder.fosterInserts);
        assertFalse(treeBuilder.fragmentParsing);
    }

    @Test
    public void testProcessToken() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Token token = new Token.StartTag();
        token.name("div");
        boolean result = treeBuilder.process(token);
        
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
    public void testIsFragmentParsing() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        assertFalse(treeBuilder.isFragmentParsing());
        
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        treeBuilder.fragmentParsing = true;
        assertTrue(treeBuilder.isFragmentParsing());
    }

    @Test
    public void testInsertElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        Element element = treeBuilder.insert(startTag);
        
        assertNotNull(element);
        assertEquals("div", element.tagName());
    }

    @Test
    public void testInsertStartTag() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = treeBuilder.insertStartTag("span");
        
        assertNotNull(element);
        assertEquals("span", element.tagName());
    }

    @Test
    public void testInsertEmpty() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("br");
        Element element = treeBuilder.insertEmpty(startTag);
        
        assertNotNull(element);
        assertEquals("br", element.tagName());
    }

    @Test
    public void testPop() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element1 = new Element("div", "http://example.com");
        Element element2 = new Element("span", "http://example.com");
        treeBuilder.push(element1);
        treeBuilder.push(element2);
        
        Element popped = treeBuilder.pop();
        assertEquals(element2, popped);
        assertEquals(1, treeBuilder.getStack().size());
    }

    @Test
    public void testPush() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        assertEquals(1, treeBuilder.getStack().size());
        assertEquals(element, treeBuilder.getStack().get(0));
    }

    @Test
    public void testOnStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.onStack(element));
        assertFalse(treeBuilder.onStack("span"));
    }

    @Test
    public void testGetFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        Element found = treeBuilder.getFromStack("div");
        assertEquals(element, found);
        
        Element notFound = treeBuilder.getFromStack("span");
        assertNull(notFound);
    }

    @Test
    public void testRemoveFromStack() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element1 = new Element("div", "http://example.com");
        Element element2 = new Element("span", "http://example.com");
        treeBuilder.push(element1);
        treeBuilder.push(element2);
        
        boolean removed = treeBuilder.removeFromStack(element1);
        assertTrue(removed);
        assertEquals(1, treeBuilder.getStack().size());
        assertEquals(element2, treeBuilder.getStack().get(0));
    }

    @Test
    public void testPopStackToClose() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element1 = new Element("div", "http://example.com");
        Element element2 = new Element("span", "http://example.com");
        treeBuilder.push(element1);
        treeBuilder.push(element2);
        
        Element popped = treeBuilder.popStackToClose("div");
        assertEquals(element1, popped);
        assertEquals(1, treeBuilder.getStack().size());
        assertEquals(element2, treeBuilder.getStack().get(0));
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        boolean changed = treeBuilder.resetInsertionMode();
        assertTrue(changed);
    }

    @Test
    public void testInScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.inScope("div"));
        assertFalse(treeBuilder.inScope("span"));
    }

    @Test
    public void testInListItemScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("li", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.inListItemScope("li"));
        assertFalse(treeBuilder.inListItemScope("div"));
    }

    @Test
    public void testInButtonScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("button", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.inButtonScope("button"));
        assertFalse(treeBuilder.inButtonScope("div"));
    }

    @Test
    public void testInTableScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("table", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.inTableScope("table"));
        assertFalse(treeBuilder.inTableScope("div"));
    }

    @Test
    public void testInSelectScope() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("option", "http://example.com");
        treeBuilder.push(element);
        
        assertTrue(treeBuilder.inSelectScope("option"));
        assertFalse(treeBuilder.inSelectScope("div"));
    }

    @Test
    public void testOnStackNot() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        assertFalse(treeBuilder.onStackNot(new String[]{"div"}));
        assertTrue(treeBuilder.onStackNot(new String[]{"span"}));
    }

    @Test
    public void testIsFosterInserts() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertFalse(treeBuilder.isFosterInserts());
        
        treeBuilder.setFosterInserts(true);
        assertTrue(treeBuilder.isFosterInserts());
    }

    @Test
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        treeBuilder.reconstructFormattingElements();
        
        assertEquals(1, treeBuilder.formattingElements.size());
    }

    @Test
    public void testClearFormattingElementsToLastMarker() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        treeBuilder.insertMarkerToFormattingElements();
        treeBuilder.pushActiveFormattingElements(new Element("div", "http://example.com"));
        
        treeBuilder.clearFormattingElementsToLastMarker();
        
        assertEquals(0, treeBuilder.formattingElements.size());
    }

    @Test
    public void testGetActiveFormattingElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        Element found = treeBuilder.getActiveFormattingElement("div");
        assertEquals(element, found);
        
        Element notFound = treeBuilder.getActiveFormattingElement("span");
        assertNull(notFound);
    }

    @Test
    public void testInsertMarkerToFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        treeBuilder.insertMarkerToFormattingElements();
        
        assertEquals(1, treeBuilder.formattingElements.size());
        assertNull(treeBuilder.formattingElements.get(0));
    }

    @Test
    public void testPushTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        
        assertEquals(1, treeBuilder.templateModeSize());
    }

    @Test
    public void testPopTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        HtmlTreeBuilderState popped = treeBuilder.popTemplateMode();
        
        assertEquals(HtmlTreeBuilderState.InBody, popped);
        assertEquals(0, treeBuilder.templateModeSize());
    }

    @Test
    public void testCurrentTemplateMode() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertNull(treeBuilder.currentTemplateMode());
        
        treeBuilder.pushTemplateMode(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, treeBuilder.currentTemplateMode());
    }

    @Test
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element1 = new Element("p", "http://example.com");
        Element element2 = new Element("div", "http://example.com");
        treeBuilder.push(element1);
        treeBuilder.push(element2);
        
        treeBuilder.generateImpliedEndTags();
        
        assertEquals(2, treeBuilder.getStack().size());
    }

    @Test
    public void testCloseElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.push(element);
        
        treeBuilder.closeElement("div");
        
        assertEquals(0, treeBuilder.getStack().size());
    }

    @Test
    public void testIsSpecial() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("script", "http://example.com");
        assertTrue(treeBuilder.isSpecial(element));
        
        Element element2 = new Element("div", "http://example.com");
        assertFalse(treeBuilder.isSpecial(element2));
    }

    @Test
    public void testLastFormattingElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertNull(treeBuilder.lastFormattingElement());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        assertEquals(element, treeBuilder.lastFormattingElement());
    }

    @Test
    public void testPositionOfElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertEquals(-1, treeBuilder.positionOfElement(new Element("div", "http://example.com")));
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        assertEquals(0, treeBuilder.positionOfElement(element));
    }

    @Test
    public void testRemoveLastFormattingElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        assertNull(treeBuilder.removeLastFormattingElement());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        assertEquals(element, treeBuilder.removeLastFormattingElement());
        assertEquals(0, treeBuilder.formattingElements.size());
    }

    @Test
    public void testPushActiveFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        assertEquals(1, treeBuilder.formattingElements.size());
        assertEquals(element, treeBuilder.formattingElements.get(0));
    }

    @Test
    public void testRemoveFromActiveFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        treeBuilder.removeFromActiveFormattingElements(element);
        
        assertEquals(0, treeBuilder.formattingElements.size());
    }

    @Test
    public void testIsInActiveFormattingElements() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element element = new Element("div", "http://example.com");
        treeBuilder.pushActiveFormattingElements(element);
        
        assertTrue(treeBuilder.isInActiveFormattingElements(element));
        assertFalse(treeBuilder.isInActiveFormattingElements(new Element("span", "http://example.com")));
    }

    @Test
    public void testReplaceActiveFormattingElement() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element oldElement = new Element("div", "http://example.com");
        Element newElement = new Element("span", "http://example.com");
        treeBuilder.pushActiveFormattingElements(oldElement);
        
        treeBuilder.replaceActiveFormattingElement(oldElement, newElement);
        
        assertEquals(1, treeBuilder.formattingElements.size());
        assertEquals(newElement, treeBuilder.formattingElements.get(0));
    }

    @Test
    public void testInsertInFosterParent() {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        treeBuilder.initialiseParse(new StringReader(""), "http://example.com", new Parser());
        
        Element parent = new Element("div", "http://example.com");
        treeBuilder.push(parent);
        
        TextNode textNode = new TextNode("test");
        treeBuilder.insertInFosterParent(textNode);
        
        assertEquals(1, parent.childNodeSize());
        assertEquals("test", parent.childNode(0).toString());
    }
}
