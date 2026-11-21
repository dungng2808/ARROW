package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.parser.HtmlTreeBuilderState;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class HtmlTreeBuilder_ArrowTest {

    @Test
    public void defaultStateIsNull() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertNull(builder.state());
    }

    @Test
    public void templateModesCanBePushedAndPopped() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertEquals(0, builder.templateModeSize());
        assertNull(builder.currentTemplateMode());

        builder.pushTemplateMode(HtmlTreeBuilderState.InTemplate);
        assertEquals(1, builder.templateModeSize());
        assertSame(HtmlTreeBuilderState.InTemplate, builder.currentTemplateMode());
        assertSame(HtmlTreeBuilderState.InTemplate, builder.popTemplateMode());
        assertEquals(0, builder.templateModeSize());
        assertNull(builder.popTemplateMode());
    }

    @Test
    public void stackOperationsFindAndRemoveElements() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element first = new Element("html");
        Element second = new Element("body");
        builder.push(first);
        builder.push(second);

        assertTrue(builder.onStack(first));
        assertTrue(builder.onStack("body"));
        assertSame(second, builder.getFromStack("body"));
        assertSame(first, builder.aboveOnStack(second));
        assertTrue(builder.removeFromStack(first));
        assertFalse(builder.onStack(first));
        assertFalse(builder.removeFromStack(first));
        assertSame(second, builder.pop());
    }

    @Test
    public void stackSearchReturnsNullWhenNameIsAbsent() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));
        assertNull(builder.getFromStack("table"));
        assertFalse(builder.onStack("table"));
    }

    @Test
    public void insertionModeCanBeMarkedAndReset() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.transition(HtmlTreeBuilderState.InTable);
        builder.markInsertionMode();
        assertSame(HtmlTreeBuilderState.InTable, builder.originalState());

        builder.transition(HtmlTreeBuilderState.InBody);
        assertTrue(builder.resetInsertionMode());
        assertSame(HtmlTreeBuilderState.BeforeHead, builder.state());
    }

    @Test
    public void resetInsertionModeUsesBodyOnEmptyStack() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertTrue(builder.resetInsertionMode());
        assertSame(HtmlTreeBuilderState.InBody, builder.state());
    }

    @Test
    public void formattingElementsCanBeAddedAndRemoved() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");

        assertNull(builder.lastFormattingElement());
        builder.pushActiveFormattingElements(bold);
        assertSame(bold, builder.lastFormattingElement());
        assertEquals(0, builder.positionOfElement(bold));
        assertTrue(builder.isInActiveFormattingElements(bold));
        assertSame(bold, builder.getActiveFormattingElement("b"));
        assertSame(bold, builder.removeLastFormattingElement());
        assertNull(builder.lastFormattingElement());
        assertEquals(-1, builder.positionOfElement(bold));
    }

    @Test
    public void formattingElementMarkerStopsLookupAndCanBeCleared() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");
        builder.pushActiveFormattingElements(bold);
        builder.insertMarkerToFormattingElements();

        assertNull(builder.getActiveFormattingElement("b"));
        builder.clearFormattingElementsToLastMarker();
        assertSame(bold, builder.lastFormattingElement());
        builder.removeFromActiveFormattingElements(bold);
        assertNull(builder.lastFormattingElement());
    }

    @Test
    public void pendingTableCharactersAreClonedAndReset() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Token.Character character = new Token.Character().data("text");

        builder.addPendingTableCharacters(character);
        assertEquals(1, builder.getPendingTableCharacters().size());
        assertEquals("text", builder.getPendingTableCharacters().get(0).getData());
        assertNotSame(character, builder.getPendingTableCharacters().get(0));

        builder.resetPendingTableCharacters();
        assertTrue(builder.getPendingTableCharacters().isEmpty());
    }

    @Test
    public void fosterInsertFlagCanBeChanged() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertFalse(builder.isFosterInserts());
        builder.setFosterInserts(true);
        assertTrue(builder.isFosterInserts());
    }

    @Test
    public void framesetFlagCanBeChanged() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertFalse(builder.framesetOk());
        builder.framesetOk(true);
        assertTrue(builder.framesetOk());
    }

    @Test
    public void contentForScriptAndStyleIsData() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertTrue(builder.isContentForTagData("script"));
        assertTrue(builder.isContentForTagData("style"));
        assertFalse(builder.isContentForTagData("div"));
    }

    @Test
    public void specialElementNamesAreRecognized() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertTrue(builder.isSpecial(new Element("div")));
        assertFalse(builder.isSpecial(new Element("custom-element")));
    }

    @Test
    public void stackCanBeRetrievedAndModified() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element html = new Element("html");
        builder.getStack().add(html);
        assertEquals(1, builder.getStack().size());
        assertSame(html, builder.getStack().get(0));
    }

    @Test
    public void headElementCanBeSetAndRetrieved() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element head = new Element("head");
        builder.setHeadElement(head);
        assertSame(head, builder.getHeadElement());
    }

    @Test
    public void formElementCanBeSetAndRetrieved() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        assertNull(builder.getFormElement());
        builder.setFormElement(null);
        assertNull(builder.getFormElement());
    }
}
