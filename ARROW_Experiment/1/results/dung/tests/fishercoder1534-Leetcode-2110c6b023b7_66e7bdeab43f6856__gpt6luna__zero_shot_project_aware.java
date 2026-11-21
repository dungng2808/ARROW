package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class _235_ArrowTest {

    @Test
    public void lowestCommonAncestorReturnsRootWhenRootIsOneOfTheNodes() {
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, root.left, root.right);

        assertSame(root, result);
    }

    @Test
    public void lowestCommonAncestorFindsAncestorInLeftSubtree() {
        TreeNode root = new TreeNode(6);
        TreeNode left = new TreeNode(2);
        left.left = new TreeNode(0);
        left.right = new TreeNode(4);
        root.left = left;
        root.right = new TreeNode(8);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, left.left, left.right);

        assertSame(left, result);
    }

    @Test
    public void lowestCommonAncestorFindsAncestorInRightSubtree() {
        TreeNode root = new TreeNode(6);
        TreeNode right = new TreeNode(8);
        right.left = new TreeNode(7);
        right.right = new TreeNode(9);
        root.left = new TreeNode(2);
        root.right = right;

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, right.left, right.right);

        assertSame(right, result);
    }

    @Test
    public void lowestCommonAncestorReturnsRootWhenNodesAreOnOppositeSides() {
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, root.left, root.right);

        assertSame(root, result);
    }

    @Test
    public void lowestCommonAncestorReturnsNullForNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(1), new TreeNode(2));

        assertSame(null, result);
    }
}
