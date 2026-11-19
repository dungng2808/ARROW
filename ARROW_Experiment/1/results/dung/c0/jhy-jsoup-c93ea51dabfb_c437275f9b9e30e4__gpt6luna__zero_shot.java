package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class HtmlTreeBuilder_ArrowTest {
    @Test
    public void initialStateAndStackAreEmpty() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertNull(builder.state());
        assertTrue(builder.getStack().isEmpty());
        assertNull(builder.getHeadElement());
        assertNull(builder.getFormElement());
        assertFalse(builder.isFragmentParsing());
        assertFalse(builder.isFosterInserts());
        assertTrue(builder.framesetOk());
    }

    @Test
    public void stackOperationsFindAndRemoveElements() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element html = new Element("html");
        Element body = new Element("body");
        builder.push(html);
        builder.push(body);

        assertTrue(builder.onStack(body));
        assertTrue(builder.onStack("html"));
        assertSame(body, builder.getFromStack("body"));
        assertSame(html, builder.aboveOnStack(body));
        assertTrue(builder.removeFromStack(body));
        assertFalse(builder.onStack(body));
        assertSame(html, builder.pop());
    }

    @Test
    public void stackSearchReturnsNullForMissingElement() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertNull(builder.getFromStack("missing"));
        assertFalse(builder.onStack("missing"));
    }

    @Test
    public void popStackToCloseRemovesThroughMatchingElement() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element html = new Element("html");
        Element body = new Element("body");
        Element paragraph = new Element("p");
        builder.push(html);
        builder.push(body);
        builder.push(paragraph);

        assertSame(body, builder.popStackToClose("body"));
        assertEquals(1, builder.getStack().size());
        assertSame(html, builder.getStack().get(0));
    }

    @Test
    public void popStackToCloseReturnsNullWhenMissing() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));

        assertNull(builder.popStackToClose("missing"));
        assertTrue(builder.getStack().isEmpty());
    }

    @Test
    public void popStackToBeforeKeepsNamedElement() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element html = new Element("html");
        Element body = new Element("body");
        Element paragraph = new Element("p");
        builder.push(html);
        builder.push(body);
        builder.push(paragraph);

        builder.popStackToBefore("body");

        assertEquals(2, builder.getStack().size());
        assertSame(html, builder.getStack().get(0));
        assertSame(body, builder.getStack().get(1));
    }

    @Test
    public void insertionModeResetsFromStack() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));
        builder.push(new Element("body"));

        assertTrue(builder.resetInsertionMode());
        assertEquals(HtmlTreeBuilderState.InBody, builder.state());
        assertFalse(builder.resetInsertionMode());
    }

    @Test
    public void scopeChecksRespectScopeBoundaries() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));
        builder.push(new Element("body"));
        builder.push(new Element("p"));

        assertTrue(builder.inScope("p"));
        assertFalse(builder.inScope("table"));
        assertTrue(builder.inButtonScope("p"));
        assertFalse(builder.inListItemScope("p"));
        assertTrue(builder.inTableScope("html"));
    }

    @Test
    public void formattingElementOperationsTrackElements() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");

        builder.pushActiveFormattingElements(bold);

        assertSame(bold, builder.lastFormattingElement());
        assertEquals(0, builder.positionOfElement(bold));
        assertTrue(builder.isInActiveFormattingElements(bold));
        assertSame(bold, builder.getActiveFormattingElement("b"));
        assertSame(bold, builder.removeLastFormattingElement());
        assertNull(builder.lastFormattingElement());
    }

    @Test
    public void formattingMarkerClearsElementsThroughMarker() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");
        Element italic = new Element("i");
        builder.pushActiveFormattingElements(bold);
        builder.insertMarkerToFormattingElements();
        builder.pushActiveFormattingElements(italic);

        builder.clearFormattingElementsToLastMarker();

        assertSame(bold, builder.lastFormattingElement());
        assertFalse(builder.isInActiveFormattingElements(italic));
    }

    @Test
    public void templateModesAreStackedAndPopped() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertNull(builder.currentTemplateMode());
        assertNull(builder.popTemplateMode());
        builder.pushTemplateMode(HtmlTreeBuilderState.InTemplate);
        assertEquals(1, builder.templateModeSize());
        assertEquals(HtmlTreeBuilderState.InTemplate, builder.currentTemplateMode());
        assertEquals(HtmlTreeBuilderState.InTemplate, builder.popTemplateMode());
        assertEquals(0, builder.templateModeSize());
    }

    @Test
    public void pendingTableCharactersAreClonedAndResettable() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Token.Character character = new Token.Character().data("text");

        builder.addPendingTableCharacters(character);

        assertEquals(1, builder.getPendingTableCharacters().size());
        assertNotSame(character, builder.getPendingTableCharacters().get(0));
        assertEquals("text", builder.getPendingTableCharacters().get(0).getData());

        builder.resetPendingTableCharacters();
        assertTrue(builder.getPendingTableCharacters().isEmpty());
    }

    @Test
    public void specialElementAndContentDataClassification() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertTrue(builder.isSpecial(new Element("div")));
        assertFalse(builder.isSpecial(new Element("custom-tag")));
        assertTrue(builder.isContentForTagData("script"));
        assertTrue(builder.isContentForTagData("style"));
        assertFalse(builder.isContentForTagData("div"));
    }

    @Test
    public void settersExposeBuilderFlagsAndElements() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element head = new Element("head");

        builder.framesetOk(false);
        builder.setFosterInserts(true);
        builder.setHeadElement(head);

        assertFalse(builder.framesetOk());
        assertTrue(builder.isFosterInserts());
        assertSame(head, builder.getHeadElement());
    }

    @Test
    public void documentAndBaseUriAreInitiallyUnavailable() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertNull(builder.getDocument());
        assertNull(builder.getBaseUri());
    }
}
