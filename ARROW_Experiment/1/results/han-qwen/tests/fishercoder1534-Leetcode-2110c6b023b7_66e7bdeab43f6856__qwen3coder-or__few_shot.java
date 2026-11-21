package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class _235_ArrowTest {

    @Test
    public void testLowestCommonAncestorWithNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(1), new TreeNode(2));
        assertNull(result);
    }

    @Test
    public void testLowestCommonAncestorWithRootEqualsP() {
        TreeNode p = new TreeNode(2);
        TreeNode root = new TreeNode(2);
        TreeNode q = new TreeNode(3);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertSame(root, result);
    }

    @Test
    public void testLowestCommonAncestorWithRootEqualsQ() {
        TreeNode q = new TreeNode(2);
        TreeNode root = new TreeNode(2);
        TreeNode p = new TreeNode(3);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertSame(root, result);
    }

    @Test
    public void testLowestCommonAncestorBothInLeftSubtree() {
        TreeNode p = new TreeNode(1);
        TreeNode q = new TreeNode(2);
        TreeNode root = new TreeNode(4);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(1);
        root.left.right = new TreeNode(3);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertSame(root.left, result);
    }

    @Test
    public void testLowestCommonAncestorBothInRightSubtree() {
        TreeNode p = new TreeNode(5);
        TreeNode q = new TreeNode(6);
        TreeNode root = new TreeNode(4);
        root.right = new TreeNode(6);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(7);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertSame(root.right, result);
    }

    @Test
    public void testLowestCommonAncestorDifferentSubtrees() {
        TreeNode p = new TreeNode(1);
        TreeNode q = new TreeNode(6);
        TreeNode root = new TreeNode(4);
        root.left = new TreeNode(2);
        root.left.left = new TreeNode(1);
        root.left.right = new TreeNode(3);
        root.right = new TreeNode(6);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(7);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertSame(root, result);
    }
}
