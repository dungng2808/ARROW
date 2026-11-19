package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;
import static org.junit.Assert.*;

public class _235_ArrowTest {

    @Test
    public void testLowestCommonAncestorWithNormalCase() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root.left; // Node with value 2
        TreeNode q = root.right; // Node with value 8
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(6, result.val);
    }

    @Test
    public void testLowestCommonAncestorWhenOneNodeIsRoot() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root; // Node with value 6 (root)
        TreeNode q = root.left; // Node with value 2
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(6, result.val);
    }

    @Test
    public void testLowestCommonAncestorWhenNodesAreOnDifferentSides() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root.left.left; // Node with value 0
        TreeNode q = root.right.right; // Node with value 9
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(6, result.val);
    }

    @Test
    public void testLowestCommonAncestorWhenBothNodesAreSame() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root.left.right; // Node with value 4
        TreeNode q = root.left.right; // Node with value 4
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(4, result.val);
    }

    @Test
    public void testLowestCommonAncestorWhenPAndQAreInLeftSubtree() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root.left.left; // Node with value 0
        TreeNode q = root.left.right; // Node with value 4
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(2, result.val);
    }

    @Test
    public void testLowestCommonAncestorWhenPAndQAreInRightSubtree() {
        // Create a BST:       6
        //                    / \
        //                   2   8
        //                  / \ / \
        //                 0  4 7  9
        //                   / \
        //                  3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = root.right.left; // Node with value 7
        TreeNode q = root.right.right; // Node with value 9
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertEquals(8, result.val);
    }

    @Test
    public void testLowestCommonAncestorWithNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(1), new TreeNode(2));
        assertNull(result);
    }

    @Test
    public void testLowestCommonAncestorWithNullNodes() {
        TreeNode root = new TreeNode(1);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, null, null);
        assertEquals(root, result);
    }
}
