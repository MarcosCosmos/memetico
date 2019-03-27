package org.marcos.uon.tspaidemo.util.tree;

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

    public TreeNode<T> parent() {
        return parent;
    }
    public void attachTo(TreeNode<T> parent) {
        parent.attach(this);
    }
    public void detach() {
        if(this.parent != null) {
            this.parent.children.remove(this);
        }
    }
    public void attach(TreeNode<T> child) {
        child.detach();
        child.parent = this;
        children.add(child);
    }

    @SafeVarargs
    public final void attach(TreeNode<T>... children) {
        for (TreeNode<T> each : children) {
            attach(each);
        }
    }

    @SafeVarargs
    public final void adoptAll(TreeNode<T>... children) {
        for (TreeNode<T> each : children) {
            attach(each);
        }
    }

    public void clear() {
        children.forEach(TreeNode::detach);
    }

    public List<TreeNode<T>> children() {
        return Collections.unmodifiableList(children);
    }

    public TreeNode<T> previousSibling() {
        if(isRoot()) {
            return null;
        } else {
            int curIndex = parent.children.indexOf(this);
            if(curIndex > 0) {
                return parent.children.get(curIndex+1);
            } else {
                return null;
            }
        }
    }
    public boolean hasPreviousSibling() {
        return previousSibling() != null;
    }

    public TreeNode<T> nextSibling() {
        if(isRoot()) {
            return null;
        } else {
            int curIndex = parent.children.indexOf(this);
            if(curIndex < parent.children.size()-1) {
                return parent.children.get(curIndex+1);
            } else {
                return null;
            }
        }
    }

    public boolean hasNextSibling() {
        return nextSibling() != null;
    }
}
