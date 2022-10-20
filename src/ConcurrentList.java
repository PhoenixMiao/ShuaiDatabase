//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//import java.util.function.UnaryOperator;
//import java.util.stream.Stream;
//
//public class ConcurrentList<E> extends AbstractList<E>
//    implements List<E>, RandomAccess {
//    private static final long serialVersionUID = 196831693267521676L;
//
//    private static final int DEFAULT_CAPACITY = 40;
//
//    private static final int COMPACT_THRESHOLD = 5;
//
//    private volatile int size;
//
//    transient volatile Node<E>[] elementData;
//
//    static class Slot<E> {
//        final int id;
//        volatile Node<E> head;
//        volatile int realPos;
//        volatile int headPos;
//
//        public final Node<E> getHead() { return head;}
//
//        public final void setHead(Node<E> head) { this.head = head; }
//
//        public final int getRealPos() { return realPos; }
//
//        public final void setRealPos(int realPos) { this.realPos = realPos; }
//
//        public final int getHeadPos() { return headPos; }
//
//        public final void setHeadPos(int headPos) { this.headPos = headPos; }
//
//        Slot(Node<E> head, int id, int realPos, int headPos) {
//            this.head = head;
//            this.id = id;
//            this.realPos = realPos;
//            this.headPos = headPos;
//        }
//    }
//
//    static class Node<E> {
//        volatile E ele;
//        volatile Node<E> next;
//    }
//
//
////     Unsafe mechanics
//    private static final sun.misc.Unsafe U;
//    private static final long SIZECTL;
//    private static final long TRANSFERINDEX;
//    private static final long BASECOUNT;
//    private static final long CELLSBUSY;
////    private static final long CELLVALUE;
//    private static final long ABASE;
//    private static final int ASHIFT;
//
//    static {
//        try {
//            U = sun.misc.Unsafe.getUnsafe();
//            Class<?> k = ConcurrentHashMap.class;
//            SIZECTL = U.objectFieldOffset
//                    (k.getDeclaredField("sizeCtl"));
//            TRANSFERINDEX = U.objectFieldOffset
//                    (k.getDeclaredField("transferIndex"));
//            BASECOUNT = U.objectFieldOffset
//                    (k.getDeclaredField("baseCount"));
//            CELLSBUSY = U.objectFieldOffset
//                    (k.getDeclaredField("cellsBusy"));
////            Class<?> ck = CounterCell.class;
////            CELLVALUE = U.objectFieldOffset
////                    (ck.getDeclaredField("value"));
//            Class<?> ak = Node[].class;
//            ABASE = U.arrayBaseOffset(ak);
//            int scale = U.arrayIndexScale(ak);
//            if ((scale & (scale - 1)) != 0)
//                throw new Error("data type scale not a power of two");
//            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
//        } catch (Exception e) {
//            throw new Error(e);
//        }
//    }
//
//    static final <E> boolean casTabAt(Node<E>[] tab, int i,
//                                        Node<E> c, Node<E> v) {
//        return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
//    }
//
//    @Override
//    public E get(int index) {
//        if(index<0 || index>)
//    }
//
//    @Override
//    public void forEach(Consumer<? super E> action) {
//
//    }
//
//    @Override
//    public Spliterator<E> spliterator() {
//        return null;
//    }
//
//    @Override
//    public Stream<E> stream() {
//        return null;
//    }
//
//    @Override
//    public Stream<E> parallelStream() {
//        return null;
//    }
//
//    @Override
//    public int size() {
//        return 0;
//    }
//
//    @Override
//    public boolean removeIf(Predicate<? super E> filter) {
//        return false;
//    }
//
//    @Override
//    public void replaceAll(UnaryOperator<E> operator) {
//
//    }
//
//    @Override
//    public void sort(Comparator<? super E> c) {
//
//    }
//}
