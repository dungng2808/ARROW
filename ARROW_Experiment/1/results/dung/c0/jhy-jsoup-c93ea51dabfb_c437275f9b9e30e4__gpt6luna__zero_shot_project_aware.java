package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlTreeBuilder_ArrowTest {
    @Test
    public void defaultSettingsAreHtmlDefaults() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertSame(ParseSettings.htmlDefault, builder.defaultSettings());
    }

    @Test
    public void newInstanceCreatesAnotherBuilder() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        HtmlTreeBuilder another = builder.newInstance();

        assertNotNull(another);
        assertNotSame(builder, another);
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
    public void stackSearchReturnsNullWhenNameIsAbsent() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertNull(builder.getFromStack("body"));
        assertFalse(builder.onStack("body"));
    }

    @Test
    public void scopeSearchStopsAtScopeBoundary() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));
        builder.push(new Element("div"));
        builder.push(new Element("p"));

        assertTrue(builder.inScope("p"));
        assertFalse(builder.inScope("table"));
        assertTrue(builder.inListItemScope("p"));
        assertFalse(builder.inButtonScope("p"));
        assertTrue(builder.inTableScope("html"));
    }

    @Test
    public void selectScopeOnlyTraversesOptions() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.push(new Element("html"));
        builder.push(new Element("select"));
        builder.push(new Element("optgroup"));
        builder.push(new Element("option"));

        assertTrue(builder.inSelectScope("select"));
        assertTrue(builder.inSelectScope("optgroup"));
        assertFalse(builder.inSelectScope("html"));
    }

    @Test
    public void insertionModeResetsFromStack() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.transition(HtmlTreeBuilderState.Initial);
        builder.push(new Element("html"));
        builder.push(new Element("table"));

        assertTrue(builder.resetInsertionMode());
        assertSame(HtmlTreeBuilderState.InTable, builder.state());
    }

    @Test
    public void resetInsertionModeUsesBodyForEmptyStack() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.transition(HtmlTreeBuilderState.Initial);

        assertTrue(builder.resetInsertionMode());
        assertSame(HtmlTreeBuilderState.InBody, builder.state());
    }

    @Test
    public void formattingElementsCanBeAddedFoundAndRemoved() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");

        builder.pushActiveFormattingElements(bold);

        assertSame(bold, builder.lastFormattingElement());
        assertSame(bold, builder.getActiveFormattingElement("b"));
        assertTrue(builder.isInActiveFormattingElements(bold));
        assertEquals(0, builder.positionOfElement(bold));
        assertSame(bold, builder.removeLastFormattingElement());
        assertNull(builder.lastFormattingElement());
    }

    @Test
    public void formattingElementLookupStopsAtMarker() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element bold = new Element("b");
        builder.pushActiveFormattingElements(bold);
        builder.insertMarkerToFormattingElements();

        assertNull(builder.getActiveFormattingElement("b"));
        builder.clearFormattingElementsToLastMarker();
        assertSame(bold, builder.lastFormattingElement());
    }

    @Test
    public void templateModesAreStackedAndPopped() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertEquals(0, builder.templateModeSize());
        assertNull(builder.currentTemplateMode());
        assertNull(builder.popTemplateMode());

        builder.pushTemplateMode(HtmlTreeBuilderState.InTemplate);
        assertEquals(1, builder.templateModeSize());
        assertSame(HtmlTreeBuilderState.InTemplate, builder.currentTemplateMode());
        assertSame(HtmlTreeBuilderState.InTemplate, builder.popTemplateMode());
        assertEquals(0, builder.templateModeSize());
    }

    @Test
    public void pendingTableCharactersAreClonedAndResettable() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Token.Character character = new Token.Character().data("x");

        builder.addPendingTableCharacters(character);

        assertEquals(1, builder.getPendingTableCharacters().size());
        assertNotSame(character, builder.getPendingTableCharacters().get(0));
        assertEquals("x", builder.getPendingTableCharacters().get(0).getData());

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

        assertTrue(builder.framesetOk());
        builder.framesetOk(false);
        assertFalse(builder.framesetOk());
    }

    @Test
    public void contentForTagDataIsLimitedToScriptAndStyle() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertTrue(builder.isContentForTagData("script"));
        assertTrue(builder.isContentForTagData("style"));
        assertFalse(builder.isContentForTagData("textarea"));
    }

    @Test
    public void insertCharacterIntoElementCreatesTextNode() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element element = new Element("div");
        Token.Character character = new Token.Character().data("hello");

        builder.insert(character, element);

        assertEquals(1, element.childNodeSize());
        assertTrue(element.childNode(0) instanceof TextNode);
        assertEquals("hello", ((TextNode) element.childNode(0)).getWholeText());
    }

    @Test
    public void insertCharacterIntoScriptCreatesDataNode() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element element = new Element("script");
        Token.Character character = new Token.Character().data("code");

        builder.insert(character, element);

        assertEquals("code", element.childNode(0).outerHtml());
    }

    @Test
    public void specialTagDetectionUsesKnownSpecialNames() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();

        assertTrue(builder.isSpecial(new Element("div")));
        assertFalse(builder.isSpecial(new Element("custom-element")));
    }

    @Test
    public void stackCanBeRetrievedAsMutableList() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        Element element = new Element("section");

        ArrayList<Element> stack = builder.getStack();
        stack.add(element);

        assertSame(element, builder.getStack().get(0));
        assertSame(element, builder.pop());
    }

    @Test
    public void toStringIncludesCurrentState() {
        HtmlTreeBuilder builder = new HtmlTreeBuilder();
        builder.transition(HtmlTreeBuilderState.InBody);

        assertTrue(builder.toString().contains("state=InBody"));
    }
}
