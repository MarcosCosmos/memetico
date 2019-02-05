package org.marcos.uon.tspaidemo.util.tree;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeNode<T> {
    private T data;
    private TreeNode<T> parent;
    private List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }
    public void attachTo(@NotNull TreeNode<T> parent) {
        parent.attach(this);
    }
    public void detach() {
        if(this.parent != null) {
            this.parent.children.remove(this);
        }
    }
    public void attach(@NotNull TreeNode<T> child) {
        child.detach();
        child.parent = this;
        children.add(child);
    }

    @SafeVarargs
    public final void attach(@NotNull TreeNode<T>... children) {
        for (TreeNode<T> each : children) {
            attach(each);
        }
    }

    @SafeVarargs
    public final void adoptAll(@NotNull TreeNode<T>... children) {
        for (TreeNode<T> each : children) {
            attach(each);
        }
    }

    public void clear() {
        children.forEach(TreeNode::detach);
    }

    public List<TreeNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
