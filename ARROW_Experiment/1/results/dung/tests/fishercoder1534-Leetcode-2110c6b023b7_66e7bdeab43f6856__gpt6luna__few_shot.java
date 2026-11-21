package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class _235_ArrowTest {

    @Test
    public void returnsRootWhenItIsOneOfTheTargets() {
        TreeNode root = new TreeNode(6);
        TreeNode p = new TreeNode(2);
        TreeNode q = new TreeNode(8);

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, root, q);

        assertSame(root, result);
    }

    @Test
    public void findsAncestorWhenBothTargetsAreInLeftSubtree() {
        TreeNode root = new TreeNode(6);
        TreeNode ancestor = new TreeNode(2);
        ancestor.left = new TreeNode(0);
        ancestor.right = new TreeNode(4);
        ancestor.right.left = new TreeNode(3);
        ancestor.right.right = new TreeNode(5);
        root.left = ancestor;
        root.right = new TreeNode(8);
        TreeNode p = ancestor.left;
        TreeNode q = ancestor.right.right;

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(ancestor, result);
    }

    @Test
    public void findsAncestorWhenBothTargetsAreInRightSubtree() {
        TreeNode root = new TreeNode(6);
        TreeNode ancestor = new TreeNode(8);
        ancestor.left = new TreeNode(7);
        ancestor.right = new TreeNode(9);
        root.left = new TreeNode(2);
        root.right = ancestor;
        TreeNode p = ancestor.left;
        TreeNode q = ancestor.right;

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(ancestor, result);
    }

    @Test
    public void returnsRootWhenTargetsAreOnOppositeSides() {
        TreeNode root = new TreeNode(6);
        TreeNode p = new TreeNode(2);
        TreeNode q = new TreeNode(8);
        root.left = p;
        root.right = q;

        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);

        assertSame(root, result);
    }

    @Test
    public void returnsNullForNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(2), new TreeNode(8));

        assertSame(null, result);
    }
}
