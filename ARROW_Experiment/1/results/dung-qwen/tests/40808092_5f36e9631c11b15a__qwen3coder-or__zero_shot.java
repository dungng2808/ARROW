package com.iluwatar.singleton;

import org.junit.Test;
import static org.junit.Assert.*;

public class App_ArrowTest {

    @Test
    public void testEagerSingleton() {
        IvoryTower ivoryTower1 = IvoryTower.getInstance();
        IvoryTower ivoryTower2 = IvoryTower.getInstance();
        assertEquals(ivoryTower1, ivoryTower2);
    }

    @Test
    public void testLazySingleton() {
        ThreadSafeLazyLoadedIvoryTower threadSafeIvoryTower1 = ThreadSafeLazyLoadedIvoryTower.getInstance();
        ThreadSafeLazyLoadedIvoryTower threadSafeIvoryTower2 = ThreadSafeLazyLoadedIvoryTower.getInstance();
        assertEquals(threadSafeIvoryTower1, threadSafeIvoryTower2);
    }

    @Test
    public void testEnumSingleton() {
        EnumIvoryTower enumIvoryTower1 = EnumIvoryTower.INSTANCE;
        EnumIvoryTower enumIvoryTower2 = EnumIvoryTower.INSTANCE;
        assertEquals(enumIvoryTower1, enumIvoryTower2);
    }

    @Test
    public void testInitializingOnDemandHolderIdiom() {
        InitializingOnDemandHolderIdiom demandHolderIdiom1 = InitializingOnDemandHolderIdiom.getInstance();
        InitializingOnDemandHolderIdiom demandHolderIdiom2 = InitializingOnDemandHolderIdiom.getInstance();
        assertEquals(demandHolderIdiom1, demandHolderIdiom2);
    }

    @Test
    public void testThreadSafeDoubleCheckLocking() {
        ThreadSafeDoubleCheckLocking dcl1 = ThreadSafeDoubleCheckLocking.getInstance();
        ThreadSafeDoubleCheckLocking dcl2 = ThreadSafeDoubleCheckLocking.getInstance();
        assertEquals(dcl1, dcl2);
    }
}
