package com.fishercoder.solutions;

import com.fishercoder.common.classes.TreeNode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class _235_ArrowTest {

    @Test
    public void testNormalCase() {
        //       6
        //      / \
        //     2   8
        //    / \ / \
        //   0  4 7  9
        //     / \
        //    3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = new TreeNode(2);
        TreeNode q = new TreeNode(8);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertThat(result.val).isEqualTo(6);
    }

    @Test
    public void testOneNodeIsAncestorOfOther() {
        //       6
        //      / \
        //     2   8
        //    / \ / \
        //   0  4 7  9
        //     / \
        //    3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = new TreeNode(2);
        TreeNode q = new TreeNode(4);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertThat(result.val).isEqualTo(2);
    }

    @Test
    public void testSameNode() {
        //       6
        //      / \
        //     2   8
        //    / \ / \
        //   0  4 7  9
        //     / \
        //    3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = new TreeNode(4);
        TreeNode q = new TreeNode(4);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertThat(result.val).isEqualTo(4);
    }

    @Test
    public void testRootNode() {
        //       6
        //      / \
        //     2   8
        //    / \ / \
        //   0  4 7  9
        //     / \
        //    3   5
        TreeNode root = new TreeNode(6);
        root.left = new TreeNode(2);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(0);
        root.left.right = new TreeNode(4);
        root.right.left = new TreeNode(7);
        root.right.right = new TreeNode(9);
        root.left.right.left = new TreeNode(3);
        root.left.right.right = new TreeNode(5);

        TreeNode p = new TreeNode(6);
        TreeNode q = new TreeNode(2);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, p, q);
        assertThat(result.val).isEqualTo(6);
    }

    @Test
    public void testNullRoot() {
        TreeNode result = new _235.Solution1().lowestCommonAncestor(null, new TreeNode(1), new TreeNode(2));
        assertThat(result).isNull();
    }

    @Test
    public void testNullNodes() {
        TreeNode root = new TreeNode(1);
        TreeNode result = new _235.Solution1().lowestCommonAncestor(root, null, new TreeNode(2));
        assertThat(result).isNull();
    }
}
