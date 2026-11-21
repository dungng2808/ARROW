package io.wcm.samples.core.business.navigation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.samples.core.business.navigation.NavigationManager;
import io.wcm.samples.core.business.navigation.NavigationPageItem;

public class NavigationManagerImpl_ArrowTest {

  private NavigationManager navigationManager;
  private SiteRoot siteRoot;
  private LinkHandler linkHandler;
  private Page currentPage;
  private SlingHttpServletRequest request;

  @Before
  public void setUp() {
    request = mock(SlingHttpServletRequest.class);
    siteRoot = mock(SiteRoot.class);
    linkHandler = mock(LinkHandler.class);
    currentPage = mock(Page.class);
    
    navigationManager = new NavigationManagerImpl() {
      // Override the injected fields with mocks
      @Override
      public void setSiteRoot(SiteRoot siteRoot) {
        NavigationManagerImpl.this.siteRoot = siteRoot;
      }
      
      @Override
      public void setLinkHandler(LinkHandler linkHandler) {
        NavigationManagerImpl.this.linkHandler = linkHandler;
      }
      
      @Override
      public void setCurrentPage(Page currentPage) {
        NavigationManagerImpl.this.currentPage = currentPage;
      }
    };
    
    // Set up the injected fields
    ((NavigationManagerImpl) navigationManager).setSiteRoot(siteRoot);
    ((NavigationManagerImpl) navigationManager).setLinkHandler(linkHandler);
    ((NavigationManagerImpl) navigationManager).setCurrentPage(currentPage);
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
    
    // Mock the page structure
    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/site/page1");
    when(childPage.getNavigationTitle()).thenReturn("Page 1");
    when(childPage.getTitle()).thenReturn("Page 1 Title");
    
    // Mock the page iterator
    java.util.Iterator<Page> iterator = java.util.Collections.singletonIterator(childPage).iterator();
    when(siteRootPage.listChildren(new PageFilter(false, false))).thenReturn(iterator);
    
    // Mock link behavior
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(siteRootPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(siteRootPage).build()).thenReturn(link);
    
    NavigationPageItem result = navigationManager.getMainNavigation(1);
    
    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNotNull(result.getChildren());
    assertEquals(1, result.getChildren().size());
  }

  @Test
  public void testGetFooterNavigation_WithFooterNavPageExists() {
    Page footerNavRoot = mock(Page.class);
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(footerNavRoot);
    
    NavigationPageItem result = navigationManager.getFooterNavigation();
    
    assertNotNull(result);
  }

  @Test
  public void testGetFooterNavigation_WithoutFooterNavPageExists() {
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");
    
    NavigationPageItem result = navigationManager.getFooterNavigation();
    
    assertNotNull(result);
  }

  @Test
  public void testCreateStructureItem() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");
    
    NavigationPageItem result = ((NavigationManagerImpl) navigationManager).createStructureItem(page);
    
    assertNotNull(result);
    assertEquals("Navigation Title", result.getTitle());
  }

  @Test
  public void testCreateLinkableItem() {
    Page page = mock(Page.class);
    when(page.getPath()).thenReturn("/content/site/home");
    when(currentPage.getPath()).thenReturn("/content/site/home");
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");
    
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(page)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(page).build()).thenReturn(link);
    
    NavigationPageItem result = ((NavigationManagerImpl) navigationManager).createLinkableItem(page);
    
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
    
    String result = ((NavigationManagerImpl) navigationManager).getItemTitle(page);
    
    assertEquals("Navigation Title", result);
  }

  @Test
  public void testGetItemTitle_WithoutNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn(null);
    when(page.getTitle()).thenReturn("Page Title");
    
    String result = ((NavigationManagerImpl) navigationManager).getItemTitle(page);
    
    assertEquals("Page Title", result);
  }

  @Test
  public void testGetItemTitle_WithEmptyNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("");
    when(page.getTitle()).thenReturn("Page Title");
    
    String result = ((NavigationManagerImpl) navigationManager).getItemTitle(page);
    
    assertEquals("Page Title", result);
  }
}
