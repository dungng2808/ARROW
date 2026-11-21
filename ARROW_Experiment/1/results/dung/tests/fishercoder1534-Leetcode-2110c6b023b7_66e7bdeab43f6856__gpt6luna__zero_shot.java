package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class _235_ArrowTest {

    @Test
    public void returnsRootWhenRootIsLowestCommonAncestor() {
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        TreeNode p = new TreeNode(2);
        TreeNode q = new TreeNode(8);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(root, result);
    }

    @Test
    public void searchesLeftSubtreeWhenBothValuesAreSmaller() {
        TreeNode root = new TreeNode(6);
        TreeNode left = new TreeNode(2);
        left.left = new TreeNode(0);
        left.right = new TreeNode(4);
        root.left = left;
        root.right = new TreeNode(8);
        TreeNode p = new TreeNode(0);
        TreeNode q = new TreeNode(4);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(left, result);
    }

    @Test
    public void searchesRightSubtreeWhenBothValuesAreLarger() {
        TreeNode root = new TreeNode(6);
        TreeNode right = new TreeNode(8);
        right.left = new TreeNode(7);
        right.right = new TreeNode(9);
        root.left = new TreeNode(2);
        root.right = right;
        TreeNode p = new TreeNode(7);
        TreeNode q = new TreeNode(9);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(right, result);
    }

    @Test
    public void returnsNullForNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(1), new TreeNode(2));

        assertSame(null, result);
    }
}
