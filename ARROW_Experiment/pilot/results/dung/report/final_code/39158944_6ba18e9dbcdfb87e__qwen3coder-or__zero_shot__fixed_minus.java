package io.wcm.samples.core.business.navigation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.samples.core.business.navigation.NavigationPageItem;

public class NavigationManagerImpl_ArrowTest {

  private NavigationManagerImpl navigationManager;
  private SiteRoot siteRoot;
  private LinkHandler linkHandler;
  private Page currentPage;
  private SlingHttpServletRequest request;

  @Before
  public void setUp() {
    navigationManager = new NavigationManagerImpl();
    siteRoot = mock(SiteRoot.class);
    linkHandler = mock(LinkHandler.class);
    currentPage = mock(Page.class);
    request = mock(SlingHttpServletRequest.class);

    // Inject dependencies using reflection since they are annotated with @Self and @AemObject
    injectField(navigationManager, "siteRoot", siteRoot);
    injectField(navigationManager, "linkHandler", linkHandler);
    injectField(navigationManager, "currentPage", currentPage);
  }

  @SuppressWarnings("unchecked")
  private void injectField(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    }
    catch (Exception e) {
      // ignore
    }
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsZero() {
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");

    NavigationPageItem result = navigationManager.getMainNavigation(0);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getChildren());
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsGreaterThanZero() {
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");

    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/site/page1");
    when(childPage.getNavigationTitle()).thenReturn("Page 1");
    when(childPage.getTitle()).thenReturn("Page 1 Title");

    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);

    when(siteRootPage.listChildren(new PageFilter(false, false))).thenReturn(java.util.Collections.singletonList(childPage).iterator());

    NavigationPageItem result = navigationManager.getMainNavigation(1);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNotNull(result.getChildren());
    assertEquals(1, result.getChildren().size());
    assertEquals("Page 1", result.getChildren().get(0).getTitle());
  }

  @Test
  public void testGetFooterNavigation_WithFooterNavPageExists() {
    Page footerNavRoot = mock(Page.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(footerNavRoot);
    when(footerNavRoot.getPath()).thenReturn("/content/tools/navigation/footernav");
    when(footerNavRoot.getNavigationTitle()).thenReturn("Footer Nav");
    when(footerNavRoot.getTitle()).thenReturn("Footer Navigation");

    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/tools/navigation/column1");
    when(childPage.getNavigationTitle()).thenReturn("Column 1");
    when(childPage.getTitle()).thenReturn("Column 1 Title");

    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);

    when(footerNavRoot.listChildren(new PageFilter(false, false))).thenReturn(java.util.Collections.singletonList(childPage).iterator());

    NavigationPageItem result = navigationManager.getFooterNavigation();

    assertNotNull(result);
    assertEquals("Footer Nav", result.getTitle());
    assertNotNull(result.getChildren());
    assertEquals(1, result.getChildren().size());
    assertEquals("Column 1", result.getChildren().get(0).getTitle());
  }

  @Test
  public void testGetFooterNavigation_WithoutFooterNavPageExists() {
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");

    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/site/page1");
    when(childPage.getNavigationTitle()).thenReturn("Page 1");
    when(childPage.getTitle()).thenReturn("Page 1 Title");

    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);

    when(siteRootPage.listChildren(new PageFilter(false, false))).thenReturn(java.util.Collections.singletonList(childPage).iterator());

    NavigationPageItem result = navigationManager.getFooterNavigation();

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNotNull(result.getChildren());
    assertEquals(1, result.getChildren().size());
    assertEquals("Page 1", result.getChildren().get(0).getTitle());
  }

  @Test
  public void testCreateStructureItem() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");

    NavigationPageItem result = navigationManager.createStructureItem(page);

    assertNotNull(result);
    assertEquals("Navigation Title", result.getTitle());
  }

  @Test
  public void testCreateLinkableItem() {
    Page page = mock(Page.class);
    when(page.getPath()).thenReturn("/content/site/page1");
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");

    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(page)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(page).build()).thenReturn(link);

    NavigationPageItem result = navigationManager.createLinkableItem(page);

    assertNotNull(result);
    assertEquals("Navigation Title", result.getTitle());
    assertEquals(true, result.isCurrentPage());
    assertNotNull(result.getLink());
  }

  @Test
  public void testGetItemTitle_WithNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");

    String result = navigationManager.getItemTitle(page);

    assertEquals("Navigation Title", result);
  }

  @Test
  public void testGetItemTitle_WithoutNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn(null);
    when(page.getTitle()).thenReturn("Page Title");

    String result = navigationManager.getItemTitle(page);

    assertEquals("Page Title", result);
  }

  @Test
  public void testGetItemTitle_WithEmptyNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("");
    when(page.getTitle()).thenReturn("Page Title");

    String result = navigationManager.getItemTitle(page);

    assertEquals("Page Title", result);
  }

}
