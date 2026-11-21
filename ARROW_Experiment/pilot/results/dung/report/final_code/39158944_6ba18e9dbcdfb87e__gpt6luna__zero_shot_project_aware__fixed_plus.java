package io.wcm.samples.core.business.navigation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.samples.core.business.navigation.NavigationPageItem;

class NavigationManagerImpl_ArrowTest {

  @Test
  public void getMainNavigationUsesCurrentPageWhenSiteRootIsMissing() {
    Page currentPage = page("/content/site", "Home", "Site");
    when(currentPage.listChildren(ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRootPage()).thenReturn(null);

    Link link = validLink();
    LinkHandler linkHandler = linkHandlerFor(currentPage, link);

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, currentPage);

    NavigationPageItem result = manager.getMainNavigation(0);

    assertNotNull(result);
    assertEquals("Home", result.getTitle());
    assertEquals(link, result.getLink());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void getMainNavigationIncludesOnlyValidChildrenUpToMaximumLevel() {
    Page root = page("/content/site", "Root", "Root");
    Page child = page("/content/site/child", "Child", "Child");
    Page grandchild = page("/content/site/child/grandchild", "Grandchild", "Grandchild");
    when(root.listChildren(ArgumentMatchers.any())).thenReturn(Arrays.asList(child).iterator());
    when(child.listChildren(ArgumentMatchers.any())).thenReturn(Arrays.asList(grandchild).iterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRootPage()).thenReturn(root);

    Link rootLink = validLink();
    Link childLink = validLink();
    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(root)).thenReturn(linkBuilder(rootLink));
    when(linkHandler.get(child)).thenReturn(linkBuilder(childLink));

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, root);

    NavigationPageItem result = manager.getMainNavigation(1);

    assertEquals("Root", result.getTitle());
    assertEquals(1, result.getChildren().size());
    assertEquals("Child", result.getChildren().get(0).getTitle());
    assertTrue(result.getChildren().get(0).getChildren().isEmpty());
  }

  @Test
  public void getFooterNavigationUsesSpecificFooterStructureAndOmitsEmptyColumns() {
    Page footerRoot = page("/content/site/tools/navigation/footernav", "Footer", "Footer");
    Page column = page("/content/site/tools/navigation/footernav/column", "Column", "Column");
    Page emptyColumn = page("/content/site/tools/navigation/footernav/empty", "Empty", "Empty");
    Page entry = page("/content/site/tools/navigation/footernav/column/entry", "Entry", "Entry");
    when(footerRoot.listChildren(ArgumentMatchers.any()))
        .thenReturn(Arrays.asList(column, emptyColumn).iterator());
    when(column.listChildren(ArgumentMatchers.any())).thenReturn(Arrays.asList(entry).iterator());
    when(emptyColumn.listChildren(ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(footerRoot);

    Link entryLink = validLink();
    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(entry)).thenReturn(linkBuilder(entryLink));

    Page currentPage = page("/content/site", "Site", "Site");
    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, currentPage);

    NavigationPageItem result = manager.getFooterNavigation();

    assertEquals("Footer", result.getTitle());
    assertEquals(1, result.getChildren().size());
    assertEquals("Column", result.getChildren().get(0).getTitle());
    assertEquals("Entry", result.getChildren().get(0).getChildren().get(0).getTitle());
  }

  @Test
  public void getFooterNavigationFallsBackToMainNavigationWhenSpecificRootIsMissing() {
    Page root = page("/content/site", "Site", "Site");
    Page section = page("/content/site/section", "Section", "Section");
    Page child = page("/content/site/section/child", "Child", "Child");
    when(root.listChildren(ArgumentMatchers.any())).thenReturn(Arrays.asList(section).iterator());
    when(section.listChildren(ArgumentMatchers.any())).thenReturn(Arrays.asList(child).iterator());
    when(child.listChildren(ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    when(siteRoot.getRootPage()).thenReturn(root);

    Link sectionLink = validLink();
    Link childLink = validLink();
    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(section)).thenReturn(linkBuilder(sectionLink));
    when(linkHandler.get(child)).thenReturn(linkBuilder(childLink));

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, root);

    NavigationPageItem result = manager.getFooterNavigation();

    assertEquals("Site", result.getTitle());
    assertEquals(1, result.getChildren().size());
    NavigationPageItem column = result.getChildren().get(0);
    assertEquals("Section", column.getTitle());
    assertEquals(2, column.getChildren().size());
    assertEquals("Section", column.getChildren().get(0).getTitle());
    assertEquals("Child", column.getChildren().get(1).getTitle());
    assertFalse(column.getChildren().get(0).getLink().isValid());
  }

  private static Page page(String path, String navigationTitle, String title) {
    Page page = mock(Page.class);
    when(page.getPath()).thenReturn(path);
    when(page.getNavigationTitle()).thenReturn(navigationTitle);
    when(page.getTitle()).thenReturn(title);
    return page;
  }

  private static Link validLink() {
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    return link;
  }

  private static LinkHandler.LinkBuilder linkBuilder(Link link) {
    LinkHandler.LinkBuilder builder = mock(LinkHandler.LinkBuilder.class);
    when(builder.build()).thenReturn(link);
    return builder;
  }

  private static LinkHandler linkHandlerFor(Page page, Link link) {
    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(page)).thenReturn(linkBuilder(link));
    return linkHandler;
  }

  private static void setDependencies(NavigationManagerImpl manager, SiteRoot siteRoot,
      LinkHandler linkHandler, Page currentPage) {
    try {
      java.lang.reflect.Field siteRootField = NavigationManagerImpl.class.getDeclaredField("siteRoot");
      siteRootField.setAccessible(true);
      siteRootField.set(manager, siteRoot);
      java.lang.reflect.Field linkHandlerField = NavigationManagerImpl.class.getDeclaredField("linkHandler");
      linkHandlerField.setAccessible(true);
      linkHandlerField.set(manager, linkHandler);
      java.lang.reflect.Field currentPageField = NavigationManagerImpl.class.getDeclaredField("currentPage");
      currentPageField.setAccessible(true);
      currentPageField.set(manager, currentPage);
    }
    catch (ReflectiveOperationException ex) {
      throw new AssertionError(ex);
    }
  }
}
