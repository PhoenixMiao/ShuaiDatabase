public class ShuaiRedBlackTree {
    
    private Node root;
    private final Node nil;

    public ShuaiRedBlackTree() {
        this.nil = new Node(new ShuaiEntry(new ShuaiString("nil"),new ShuaiString("nil")),root,root,root,ShuaiColor.BLACK,false);
        this.root = nil;
        nil.left = root;
        nil.right = root;
        nil.p = root;
    }

    static class Node {
        private ShuaiEntry entry;
        private Node p;
        private Node left;
        private Node right;
        private ShuaiColor color;
        private boolean deleted;

        public Node(ShuaiEntry entry, boolean deleted,Node p) {
            this.entry = entry;
            this.deleted = deleted;
            this.p = p;
        }

        public Node(ShuaiEntry entry, Node p, Node left, Node right, ShuaiColor color, boolean deleted) {
            this.entry = entry;
            this.p = p;
            this.left = left;
            this.right = right;
            this.color = color;
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public ShuaiEntry getEntry() {
            return entry;
        }

        public void setEntry(ShuaiEntry entry) {
            this.entry = entry;
        }

        public Node getP() {
            return p;
        }

        public void setP(Node p) {
            this.p = p;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public ShuaiColor getColor() {
            return color;
        }

        public void setColor(ShuaiColor color) {
            this.color = color;
        }
    }
    
    public enum ShuaiColor {
        RED,
        BLACK;
    }

    private synchronized void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if(y.left != nil) y.left.p = x;
        y.p = x.p;
        if(x.p == nil) root = y;
        else if(x==x.p.left) x.p.left = y;
        else x.p.left = y;
        y.left = x;
        x.p = y;
    }

    private synchronized void rightRotate(Node x) {
        Node y = x.right;
        x.right = y.right;
        if(y.right != nil) y.right.p = x;
        y.p = x.p;
        if(x.p == nil) root = y;
        else if(x==x.p.right) x.p.right = y;
        else x.p.right = y;
        y.right = x;
        x.p = y;
    }
    
    public synchronized void rbInsert(Node z) {
        Node y = nil;
        Node x = root;
        while(x != nil) {
            y = x;
            if(z.entry.compareTo(x.entry)<0) x = x.left;
            else x = x.right;
        }
        z.p = y;
        if(y==nil) root = z;
        else if(z.entry.compareTo(y.entry)<0) y.left = z;
        else y.right = z;
        z.left = nil;
        z.right = nil;
        z.color = ShuaiColor.RED;
        rbInsertFixUp(z);
    }
    
    private synchronized void rbInsertFixUp(Node z) {
        while(z.p.color == ShuaiColor.RED) {
            if(z.p == z.p.p.left) {
                Node y = z.p.p.right;
                if(y.color == ShuaiColor.RED) {
                    z.p.color = ShuaiColor.BLACK;
                    y.color = ShuaiColor.BLACK;
                    z.p.p.color = ShuaiColor.RED;
                    z = z.p.p;
                }else if(z == z.p.right) {
                    z = z.p;
                    leftRotate(z);
                }
                z.color = ShuaiColor.BLACK;
                z.p.p.color = ShuaiColor.RED;
                rightRotate(z.p.p);
            }else {
                Node y = z.p.p.left;
                if(y.color == ShuaiColor.RED) {
                    z.p.color = ShuaiColor.BLACK;
                    y.color = ShuaiColor.BLACK;
                    z.p.p.color = ShuaiColor.RED;
                    z = z.p.p;
                }else if(z == z.p.left) {
                    z = z.p;
                    rightRotate(z);
                }
                z.color = ShuaiColor.BLACK;
                z.p.p.color = ShuaiColor.RED;
                leftRotate(z.p.p);
            }
        }
        root.color = ShuaiColor.BLACK;
    }

    private synchronized void rbTransplant(Node u,Node v) {
        if(u.p == nil) root = v;
        else if(u==u.p.left) u.p.left = v;
        else u.p.right = v;
        v.p = u.p;
    }
    
    private synchronized Node treeMinimum(Node x) {
        while(x.left != nil) x = x.left;
        return x;
    }

    public synchronized int getHeight() {
        Node x = root;
        int height = 1;
        while(x.left != nil) {
            x = x.left;
            if(x.color == ShuaiColor.BLACK) height ++;
        }
        return height;
    }
    
    private synchronized void rbDelete(Node z) {
        Node y = z;
        ShuaiColor yOriginalColor = y.color;
        Node x;
        if(z.left == nil) {
            x = z.right;
            rbTransplant(z,z.right);
        }else if(z.right == nil) { 
            x = z.left;
            rbTransplant(z,z.left);
        }else {
            y = treeMinimum(z.right);
            yOriginalColor = y.color;
            x = y.right;
            if(y.p == z) x.p = y;
            else {
                rbTransplant(y,y.right);
                y.right = z.right;
                y.right.p = y;
            }
            rbTransplant(z,y);
            y.left = z.left;
            y.left.p = y;
            y.color = z.color;
        }
        if(yOriginalColor == ShuaiColor.BLACK) rbDeleteFixUp(x);
    }
    
    private synchronized void rbDeleteFixUp(Node x) {
        while(x!=root && x.color == ShuaiColor.BLACK) {
            if(x==x.p.left) {
                Node w = x.p.right;
                if(w.color == ShuaiColor.RED) {
                    w.color = ShuaiColor.BLACK;
                    x.p.color = ShuaiColor.RED;
                    leftRotate(x.p);
                    w = x.p.right;
                }
                if(w.left.color == ShuaiColor.BLACK && w.right.color == ShuaiColor.BLACK) {
                    w.color = ShuaiColor.RED;
                    x = x.p;
                }else if(w.right.color == ShuaiColor.BLACK) {
                    w.left.color = ShuaiColor.BLACK;
                    w.color = ShuaiColor.RED;
                    rightRotate(w);
                    w = x.p.right;
                }
                w.color = x.p.color;
                x.p.color = ShuaiColor.BLACK;
                x.right.color = ShuaiColor.BLACK;
                leftRotate(x.p);
                x = root;
            }else {
                Node w = x.p.left;
                if(w.color == ShuaiColor.RED) {
                    w.color = ShuaiColor.BLACK;
                    x.p.color = ShuaiColor.RED;
                    rightRotate(x.p);
                    w = x.p.left;
                }
                if(w.right.color == ShuaiColor.BLACK && w.left.color == ShuaiColor.BLACK) {
                    w.color = ShuaiColor.RED;
                    x = x.p;
                }else if(w.left.color == ShuaiColor.BLACK) {
                    w.right.color = ShuaiColor.BLACK;
                    w.color = ShuaiColor.RED;
                    leftRotate(w);
                    w = x.p.left;
                }
                w.color = x.p.color;
                x.p.color = ShuaiColor.BLACK;
                x.left.color = ShuaiColor.BLACK;
                rightRotate(x.p);
                x = root;
            }
        }
        x.color = ShuaiColor.BLACK;
    }

    public synchronized Node deleteRoot() {
        Node root = this.root;
        rbDelete(root);
        return root;
    }

    public Node getNil() {
        return nil;
    }
}
