package io.wcm.samples.core.business.navigation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  @BeforeEach
  public void setUp() {
    request = mock(SlingHttpServletRequest.class);
    siteRoot = mock(SiteRoot.class);
    linkHandler = mock(LinkHandler.class);
    currentPage = mock(Page.class);
    
    navigationManager = new NavigationManagerImpl();
    
    // Inject dependencies using reflection since setters are not accessible
    injectDependency(navigationManager, "siteRoot", siteRoot);
    injectDependency(navigationManager, "linkHandler", linkHandler);
    injectDependency(navigationManager, "currentPage", currentPage);
  }

  private void injectDependency(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    }
    catch (Exception e) {
      // Ignore injection failures
    }
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsZero() {
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");
    
    // Mock link behavior
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(siteRootPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(siteRootPage).build()).thenReturn(link);
    
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
    java.util.Iterator<Page> iterator = new java.util.ArrayList<Page>() {{
      add(childPage);
    }}.iterator();
    when(siteRootPage.listChildren(new PageFilter(false, false))).thenReturn(iterator);
    
    // Mock link behavior
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(siteRootPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(siteRootPage).build()).thenReturn(link);
    
    // Mock link behavior for child page
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);
    
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
    
    // Mock the page structure
    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/site/footer/column1");
    when(childPage.getNavigationTitle()).thenReturn("Column 1");
    when(childPage.getTitle()).thenReturn("Column 1 Title");
    
    // Mock the page iterator
    java.util.Iterator<Page> iterator = new java.util.ArrayList<Page>() {{
      add(childPage);
    }}.iterator();
    when(footerNavRoot.listChildren(new PageFilter(false, false))).thenReturn(iterator);
    
    // Mock link behavior
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(footerNavRoot)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(footerNavRoot).build()).thenReturn(link);
    
    // Mock link behavior for child page
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);
    
    NavigationPageItem result = navigationManager.getFooterNavigation();
    
    assertNotNull(result);
  }

  @Test
  public void testGetFooterNavigation_WithoutFooterNavPageExists() {
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    Page siteRootPage = mock(Page.class);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(currentPage.getPath()).thenReturn("/content/site/home");
    
    // Mock the page structure
    Page childPage = mock(Page.class);
    when(childPage.getPath()).thenReturn("/content/site/page1");
    when(childPage.getNavigationTitle()).thenReturn("Page 1");
    when(childPage.getTitle()).thenReturn("Page 1 Title");
    
    // Mock the page iterator
    java.util.Iterator<Page> iterator = new java.util.ArrayList<Page>() {{
      add(childPage);
    }}.iterator();
    when(siteRootPage.listChildren(new PageFilter(false, false))).thenReturn(iterator);
    
    // Mock link behavior
    Link link = mock(Link.class);
    when(link.isValid()).thenReturn(true);
    when(linkHandler.get(siteRootPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(siteRootPage).build()).thenReturn(link);
    
    // Mock link behavior for child page
    when(linkHandler.get(childPage)).thenReturn(mock(io.wcm.handler.link.LinkBuilder.class));
    when(linkHandler.get(childPage).build()).thenReturn(link);
    
    NavigationPageItem result = navigationManager.getFooterNavigation();
    
    assertNotNull(result);
  }

  @Test
  public void testCreateStructureItem() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");
    
    // Access via reflection since method is private
    try {
      java.lang.reflect.Method method = NavigationManagerImpl.class.getDeclaredMethod("createStructureItem", Page.class);
      method.setAccessible(true);
      NavigationPageItem result = (NavigationPageItem) method.invoke(navigationManager, page);
      
      assertNotNull(result);
      assertEquals("Navigation Title", result.getTitle());
    }
    catch (Exception e) {
      // Should not happen in normal operation
      throw new RuntimeException(e);
    }
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
    
    // Access via reflection since method is private
    try {
      java.lang.reflect.Method method = NavigationManagerImpl.class.getDeclaredMethod("createLinkableItem", Page.class);
      method.setAccessible(true);
      NavigationPageItem result = (NavigationPageItem) method.invoke(navigationManager, page);
      
      assertNotNull(result);
      assertEquals("Navigation Title", result.getTitle());
      assertEquals(true, result.isActive());
      assertNotNull(result.getLink());
    }
    catch (Exception e) {
      // Should not happen in normal operation
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetItemTitle_WithNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("Navigation Title");
    when(page.getTitle()).thenReturn("Page Title");
    
    // Access via reflection since method is private
    try {
      java.lang.reflect.Method method = NavigationManagerImpl.class.getDeclaredMethod("getItemTitle", Page.class);
      method.setAccessible(true);
      String result = (String) method.invoke(navigationManager, page);
      
      assertEquals("Navigation Title", result);
    }
    catch (Exception e) {
      // Should not happen in normal operation
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetItemTitle_WithoutNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn(null);
    when(page.getTitle()).thenReturn("Page Title");
    
    // Access via reflection since method is private
    try {
      java.lang.reflect.Method method = NavigationManagerImpl.class.getDeclaredMethod("getItemTitle", Page.class);
      method.setAccessible(true);
      String result = (String) method.invoke(navigationManager, page);
      
      assertEquals("Page Title", result);
    }
    catch (Exception e) {
      // Should not happen in normal operation
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetItemTitle_WithEmptyNavigationTitle() {
    Page page = mock(Page.class);
    when(page.getNavigationTitle()).thenReturn("");
    when(page.getTitle()).thenReturn("Page Title");
    
    // Access via reflection since method is private
    try {
      java.lang.reflect.Method method = NavigationManagerImpl.class.getDeclaredMethod("getItemTitle", Page.class);
      method.setAccessible(true);
      String result = (String) method.invoke(navigationManager, page);
      
      assertEquals("Page Title", result);
    }
    catch (Exception e) {
      // Should not happen in normal operation
      throw new RuntimeException(e);
    }
  }
}
