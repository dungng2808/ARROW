package com.iluwatar.singleton;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class App_ArrowTest {

    @Test
    public void testEagerSingleton() {
        IvoryTower ivoryTower1 = IvoryTower.getInstance();
        IvoryTower ivoryTower2 = IvoryTower.getInstance();
        assertSame(ivoryTower1, ivoryTower2);
    }

    @Test
    public void testThreadSafeLazyLoadedSingleton() {
        ThreadSafeLazyLoadedIvoryTower threadSafeIvoryTower1 = ThreadSafeLazyLoadedIvoryTower.getInstance();
        ThreadSafeLazyLoadedIvoryTower threadSafeIvoryTower2 = ThreadSafeLazyLoadedIvoryTower.getInstance();
        assertSame(threadSafeIvoryTower1, threadSafeIvoryTower2);
    }

    @Test
    public void testEnumSingleton() {
        EnumIvoryTower enumIvoryTower1 = EnumIvoryTower.INSTANCE;
        EnumIvoryTower enumIvoryTower2 = EnumIvoryTower.INSTANCE;
        assertSame(enumIvoryTower1, enumIvoryTower2);
    }

    @Test
    public void testInitializingOnDemandHolderIdiom() {
        InitializingOnDemandHolderIdiom demandHolderIdiom1 = InitializingOnDemandHolderIdiom.getInstance();
        InitializingOnDemandHolderIdiom demandHolderIdiom2 = InitializingOnDemandHolderIdiom.getInstance();
        assertSame(demandHolderIdiom1, demandHolderIdiom2);
    }

    @Test
    public void testThreadSafeDoubleCheckLocking() {
        ThreadSafeDoubleCheckLocking dcl1 = ThreadSafeDoubleCheckLocking.getInstance();
        ThreadSafeDoubleCheckLocking dcl2 = ThreadSafeDoubleCheckLocking.getInstance();
        assertSame(dcl1, dcl2);
    }
}
