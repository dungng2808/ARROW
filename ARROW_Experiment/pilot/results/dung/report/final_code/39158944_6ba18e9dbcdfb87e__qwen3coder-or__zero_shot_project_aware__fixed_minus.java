package io.wcm.samples.core.business.navigation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.handler.link.Link;
import io.wcm.handler.link.LinkBuilder;
import io.wcm.handler.link.LinkHandler;
import io.wcm.handler.url.ui.SiteRoot;
import io.wcm.samples.core.business.navigation.NavigationPageItem;

@ExtendWith(MockitoExtension.class)
public class NavigationManagerImpl_ArrowTest {

  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private SiteRoot siteRoot;
  @Mock
  private LinkHandler linkHandler;
  @Mock
  private Page currentPage;
  @Mock
  private Page siteRootPage;
  @Mock
  private Page childPage1;
  @Mock
  private Page childPage2;
  @Mock
  private Link link1;
  @Mock
  private Link link2;
  @Mock
  private Resource resource;

  private NavigationManagerImpl navigationManager;

  @BeforeEach
  public void setUp() throws IllegalAccessException {
    navigationManager = new NavigationManagerImpl();
    FieldUtils.writeField(navigationManager, "siteRoot", siteRoot, true);
    FieldUtils.writeField(navigationManager, "linkHandler", linkHandler, true);
    FieldUtils.writeField(navigationManager, "currentPage", currentPage, true);
  }

  @Test
  public void testGetMainNavigation_Empty() {
    when(siteRoot.getRootPage()).thenReturn(null);
    when(currentPage.getPath()).thenReturn("/content/site/home");

    NavigationPageItem result = navigationManager.getMainNavigation(0);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsZero() {
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(siteRootPage.getPath()).thenReturn("/content/site");
    when(currentPage.getPath()).thenReturn("/content/site/home");

    NavigationPageItem result = navigationManager.getMainNavigation(0);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertTrue(result.getChildren().isEmpty());
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsOne() {
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(siteRootPage.getPath()).thenReturn("/content/site");
    when(currentPage.getPath()).thenReturn("/content/site/home");
    when(siteRootPage.listChildren(any(PageFilter.class))).thenReturn(getChildPagesIterator());

    NavigationPageItem result = navigationManager.getMainNavigation(1);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertEquals(2, result.getChildren().size());
    assertEquals("Child 1", result.getChildren().get(0).getTitle());
    assertEquals("Child 2", result.getChildren().get(1).getTitle());
  }

  @Test
  public void testGetMainNavigation_WithMaxLevelsTwo() {
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(siteRootPage.getPath()).thenReturn("/content/site");
    when(currentPage.getPath()).thenReturn("/content/site/home");
    when(siteRootPage.listChildren(any(PageFilter.class))).thenReturn(getChildPagesIterator());

    NavigationPageItem result = navigationManager.getMainNavigation(2);

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertEquals(2, result.getChildren().size());
    assertEquals("Child 1", result.getChildren().get(0).getTitle());
    assertEquals("Child 2", result.getChildren().get(1).getTitle());
  }

  @Test
  public void testGetFooterNavigation_WithFooterNavPage() {
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(siteRootPage);
    when(siteRootPage.getPath()).thenReturn("/content/site/tools/navigation/footernav");
    when(siteRootPage.listChildren(any(PageFilter.class))).thenReturn(getChildPagesIterator());

    NavigationPageItem result = navigationManager.getFooterNavigation();

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertEquals(2, result.getChildren().size());
    assertEquals("Child 1", result.getChildren().get(0).getTitle());
    assertEquals("Child 2", result.getChildren().get(1).getTitle());
  }

  @Test
  public void testGetFooterNavigation_WithoutFooterNavPage() {
    when(siteRoot.getRelativePage("/tools/navigation/footernav")).thenReturn(null);
    when(siteRoot.getRootPage()).thenReturn(siteRootPage);
    when(siteRootPage.getPath()).thenReturn("/content/site");
    when(currentPage.getPath()).thenReturn("/content/site/home");
    when(siteRootPage.listChildren(any(PageFilter.class))).thenReturn(getChildPagesIterator());

    NavigationPageItem result = navigationManager.getFooterNavigation();

    assertNotNull(result);
    assertEquals("", result.getTitle());
    assertNull(result.getLink());
    assertEquals(2, result.getChildren().size());
    assertEquals("Child 1", result.getChildren().get(0).getTitle());
    assertEquals("Child 2", result.getChildren().get(1).getTitle());
  }

  private java.util.Iterator<Page> getChildPagesIterator() {
    when(childPage1.getPath()).thenReturn("/content/site/child1");
    when(childPage1.getNavigationTitle()).thenReturn("Child 1");
    when(childPage1.getTitle()).thenReturn("Child 1 Title");
    when(childPage2.getPath()).thenReturn("/content/site/child2");
    when(childPage2.getNavigationTitle()).thenReturn("Child 2");
    when(childPage2.getTitle()).thenReturn("Child 2 Title");

    LinkBuilder linkBuilder1 = mock(LinkBuilder.class);
    LinkBuilder linkBuilder2 = mock(LinkBuilder.class);
    when(linkHandler.get(childPage1)).thenReturn(linkBuilder1);
    when(linkHandler.get(childPage2)).thenReturn(linkBuilder2);
    when(linkBuilder1.build()).thenReturn(link1);
    when(linkBuilder2.build()).thenReturn(link2);
    when(link1.isValid()).thenReturn(true);
    when(link2.isValid()).thenReturn(true);

    java.util.List<Page> pages = java.util.Arrays.asList(childPage1, childPage2);
    return pages.iterator();
  }

}
