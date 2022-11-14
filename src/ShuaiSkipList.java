import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ShuaiSkipList {

    public static final int SHUAISKIPLIST_MAXLEVEL = 32;

    public static final double SHUAISKIPLIST_P = 0.25;

    private volatile Node header;
    private volatile Node tail;
    private volatile int length;
    private volatile int level;
    final transient ReentrantLock lock = new ReentrantLock();

    static class Node {
        private volatile ShuaiString obj;
        private volatile Double score;
        private volatile Node backward;
        private volatile Level[] level;

        static class Level {
            private volatile Node forward;
            private volatile int span;

            public Level(Node forward, int span) {
                this.forward = forward;
                this.span = span;
            }
        }

        public Node(int level,double score,ShuaiString obj) {
            this.level = new Level[level];
            for(int i = 0;i<level;i++) this .level[i] = new Level(null,0);
            this.score = score;
            this.obj = obj;
        }

        public Node(Node node) {
            this.level = node.level;
            this.score = node.score;
            this.obj = node.obj;
            this.backward = node.backward==null?null:new Node(node.backward);
        }

    }

    public int getLength() {
        return length;
    }

    static class RangeSpec {
        private volatile double min;
        private volatile double max;
        private volatile boolean minex;
        private volatile boolean maxex;

        public RangeSpec(double min, double max, boolean minex, boolean maxex) {
            this.min = min;
            this.max = max;
            this.minex = minex;
            this.maxex = maxex;
        }
    }

    public ShuaiSkipList() {
        this.level = 1;
        this.length = 0;
        this.header = new Node(SHUAISKIPLIST_MAXLEVEL,0,null);
        for(int i = 0;i<SHUAISKIPLIST_MAXLEVEL;i++) this.header.level[i] = new Node.Level(null,0);
        this.header.backward = null;
        this.tail = null;
    }

    public ShuaiSkipList(Node header, Node tail, int length, int level) {
        this.header = header;
        this.tail = tail;
        this.length = length;
        this.level = level;
    }

    public int randomLevel() {
        int level = 1;
        while(Math.random() < SHUAISKIPLIST_P) level++;
        return Math.min(level, SHUAISKIPLIST_MAXLEVEL);
    }

    public Node insert(double score,ShuaiString obj) {
        Node[] update = new Node[SHUAISKIPLIST_MAXLEVEL];
        int[] rank = new int[SHUAISKIPLIST_MAXLEVEL];
        final ReentrantLock lock = this.lock;
        lock.lock();
        Node x;
        try {
            Node header = new Node(this.header);
            int length = this.length;
            int level = this.level;
            Node.Level[] snapshot = header.level;
            x = header;
            for (int i = level - 1; i >= 0; i--) {
                rank[i] = i == level - 1 ? 0 : rank[i + 1];
                while (snapshot[i].forward != null && (snapshot[i].forward.score < score
                        || (snapshot[i].forward.score == score && snapshot[i].forward.obj.compareTo(obj) < 0))) {
                    rank[i] += snapshot[i].span;
                    x = snapshot[i].forward;
                }
                update[i] = x;
            }
            int newLevel = randomLevel();
            if (newLevel > level) {
                for (int i = level; i < newLevel; i++) {
                    rank[i] = 0;
                    update[i] = this.header;
                    update[i].level[i].span = length;
                }
                level = newLevel;
            }
            x = new Node(newLevel, score, obj);
            for (int i = 0; i < newLevel; i++) {
                x.level[i].forward = update[i].level[i].forward;
                update[i].level[i].forward = x;
                x.level[i].span = update[i].level[i].span - (rank[0] - rank[i]);
                update[i].level[i].span = (rank[0] - rank[i]) + 1;
            }
            
            for(int i=newLevel;i < level;i++) update[i].level[i].span++;
            x.backward = update[0] == header ? null : update[0];
            if(x.level[0].forward!=null) x.level[0].forward.backward = x;
            else tail = x;
            this.header = header;
            this.level = level;
            this.length = length+1;
        }finally {
            lock.unlock();
        }
        return x;
    }

    private ShuaiSkipList deleteNode(Node x,Node[] update,int level,Node header,int length) {
        for(int i=0;i<level;i++) {
            if(update[i].level[i].forward == x) {
                update[i].level[i].span += x.level[i].span - 1;
                update[i].level[i].forward = x.level[i].forward;
            }else {
                update[i].level[i].span -= 1;
            }
        }
        Node tail = null;
        if(x.level[0].forward != null) x.level[0].forward.backward = x.backward;
        else tail = x.backward;
        while(level >1 && header.level[level-1].forward == null) level--;
        length--;
        return new ShuaiSkipList(header,tail,length,level);
    }

    public boolean delete(double score,ShuaiString obj) {
        Node[] update = new Node[SHUAISKIPLIST_MAXLEVEL];
        final ReentrantLock lock = this.lock;
        lock.lock();
        Node x;
        try{
            Node header = new Node(this.header);
            int level = this.level;
            x = header;
            for(int i=level-1;i>=0;i--) {
                while(x.level[i].forward!=null && (x.level[i].forward.score < score ||
                        (x.level[i].forward.score == score && x.level[i].forward.obj.compareTo(obj) < 0))) {
                    x = x.level[i].forward;
                }
                update[i] = x;
            }
            x = x.level[0].forward;
            if(x!=null && score == x.score && x.obj.equals(obj)) {
                ShuaiSkipList ans = deleteNode(x,update,level,header,length);
                this.length = ans.length;
                this.level = ans.level;
                this.header = ans.header;
                this.tail = ans.tail;
                return true;
            }

        }finally {
            lock.unlock();
        }
        return false;
    }

    private boolean valueGteMin(double value,RangeSpec spec) {
        return spec.minex ? (value > spec.min) : (value >= spec.min);
    }

    private boolean valueLteMax(double value,RangeSpec spec) {
        return spec.maxex ? (value < spec.max) : (value <= spec.max);
    }

    private boolean isInRange(RangeSpec range) {
        if(range.min > range.max || ( range.min == range.max && (range.minex || range.maxex))) return false;
        if(tail == null || !valueGteMin(tail.score,range)) return false;
        if(header.level[0].forward == null || !valueLteMax(header.level[0].forward.score,range)) return false;
        return true;
    }

    private Node firstInRange(RangeSpec range) {
        if(!isInRange(range)) return null;
        Node x = header;
        for(int i = level-1 ; i>=0 ; i--) {
            while(x.level[i].forward!=null && !valueGteMin(x.level[i].forward.score,range))
                x = x.level[i].forward;
        }

        x = x.level[0].forward;
        assert x!=null;

        if(valueLteMax(x.score,range)) return null;
        return x;
    }

    private Node lastInRange(RangeSpec range) {
        if(!isInRange(range)) return null;
        Node x = header;
        for(int i = level-1;i>=0;i--) {
            while(x.level[i].forward!=null && valueLteMax(x.level[i].forward.score,range))
                x = x.level[i].forward;
        }
        assert x!=null;
        if(valueLteMax(x.score,range)) return null;
        return x;
    }

    public long deleteRangeByScore(RangeSpec range,ConcurrentHashMap<ShuaiString,ShuaiString> dict) {
        long removed = 0;
        Node[] update = new Node[SHUAISKIPLIST_MAXLEVEL];
        final ReentrantLock lock = this.lock;
        lock.lock();
        Node x;
        try{
            ConcurrentHashMap<ShuaiString,ShuaiString> y = new ConcurrentHashMap<>(dict);
            Node header = new Node(this.header);
            int level = this.level;
            int length = this.length;
            x = header;
            for(int i = level-1;i>=0;i--) {
                while(x.level[i].forward!=null && ( range.minex ?
                    x.level[i].forward.score <= range.min : x.level[i].forward.score < range.min)) {
                        x = x.level[i].forward;
                }
                update[i] = x;
            }

            x = x.level[0].forward;
            ShuaiSkipList list = null;
            while(x!=null && (range.maxex ? x.score < range.max : x.score <= range.max)) {
                Node next = x.level[0].forward;
                list = deleteNode(x,update,level,header,length);
                level = list.level;
                length = list.length;
                y.remove(x.obj);
                removed++;
                x = next;
            }

            this.header = header;
            if(list!=null) this.tail = list.tail;
            this.level = level;
            this.length = length;
        }finally {
            lock.unlock();
        }
        return removed;
    }
}
