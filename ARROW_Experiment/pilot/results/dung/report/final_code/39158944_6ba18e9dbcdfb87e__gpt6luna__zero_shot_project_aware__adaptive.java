package io.wcm.samples.core.business.navigation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.samples.core.business.navigation.NavigationPageItem;

class NavigationManagerImpl_ArrowTest {

  @Test
  public void getMainNavigationUsesCurrentPageWhenSiteRootIsMissing() {
    Page currentPage = page("/content/site", "Home", "Site");
    when(currentPage.listChildren(org.mockito.ArgumentMatchers.any()))
        .thenReturn(Collections.<Page>emptyList().iterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRootPage()).thenReturn(null);

    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(currentPage)).thenReturn(null);

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, currentPage);

    NavigationPageItem result = manager.getMainNavigation(0);

    assertNotNull(result);
    assertEquals("Home", result.getTitle());
    assertTrue(result.isActive());
    assertEquals(null, result.getLink());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void getMainNavigationIncludesOnlyValidChildrenUpToMaximumLevel() {
    Page root = page("/content/site", "Root", "Root");
    Page child = page("/content/site/child", "Child", "Child");
    Page grandchild = page("/content/site/child/grandchild", "Grandchild", "Grandchild");
    when(root.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Arrays.asList(child).iterator());
    when(child.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Arrays.asList(grandchild).iterator());
    when(grandchild.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRootPage()).thenReturn(root);

    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(root)).thenReturn(null);
    when(linkHandler.get(child)).thenReturn(null);

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, root);

    NavigationPageItem result = manager.getMainNavigation(1);

    assertEquals("Root", result.getTitle());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void getFooterNavigationUsesSpecificFooterStructureAndOmitsEmptyColumns() {
    Page footerRoot = page("/content/site/tools/navigation/footernav", "Footer", "Footer");
    Page column = page("/content/site/tools/navigation/footernav/column", "Column", "Column");
    Page emptyColumn = page("/content/site/tools/navigation/footernav/empty", "Empty", "Empty");
    Page entry = page("/content/site/tools/navigation/footernav/column/entry", "Entry", "Entry");
    when(footerRoot.listChildren(org.mockito.ArgumentMatchers.any()))
        .thenReturn(Arrays.asList(column, emptyColumn).iterator());
    when(column.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Arrays.asList(entry).iterator());
    when(emptyColumn.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(footerRoot);

    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(entry)).thenReturn(null);

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, page("/content/site", "Site", "Site"));

    NavigationPageItem result = manager.getFooterNavigation();

    assertEquals("Footer", result.getTitle());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void getFooterNavigationFallsBackToMainNavigationWhenSpecificRootIsMissing() {
    Page root = page("/content/site", "Site", "Site");
    Page section = page("/content/site/section", "Section", "Section");
    Page child = page("/content/site/section/child", "Child", "Child");
    when(root.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Arrays.asList(section).iterator());
    when(section.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Arrays.asList(child).iterator());
    when(child.listChildren(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyIterator());

    SiteRoot siteRoot = mock(SiteRoot.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    when(siteRoot.getRootPage()).thenReturn(root);

    LinkHandler linkHandler = mock(LinkHandler.class);
    when(linkHandler.get(section)).thenReturn(null);
    when(linkHandler.get(child)).thenReturn(null);

    NavigationManagerImpl manager = new NavigationManagerImpl();
    setDependencies(manager, siteRoot, linkHandler, root);

    NavigationPageItem result = manager.getFooterNavigation();

    assertEquals("Site", result.getTitle());
    assertTrue(result.getChildren().isEmpty());
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
