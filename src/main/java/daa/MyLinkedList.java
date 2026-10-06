package daa;
public class MyLinkedList implements IntList {
    private static class Node { int value; Node next; Node(int v){value=v;} }
    private Node head, tail; private int size; private final Metrics metrics;
    public MyLinkedList(){ this(new Metrics()); }
    public MyLinkedList(Metrics metrics){this.metrics=metrics;}
    public int size(){return size;}
    private void check(int i){if(i<0||i>=size)throw new IndexOutOfBoundsException("index: "+i);}
    private void checkInsert(int i){if(i<0||i>size)throw new IndexOutOfBoundsException("index: "+i);}
    private Node nodeAt(int index){ Node cur=head; for(int i=0;i<index;i++){cur=cur.next;metrics.steps++;} if(index>=0)metrics.steps++; return cur; }
    public void add(int x){Node n=new Node(x); if(tail==null)head=tail=n; else {tail.next=n;metrics.moves++;tail=n;} size++;metrics.steps++;}
    public void add(int index,int x){checkInsert(index); if(index==size){add(x);return;} Node n=new Node(x); if(index==0){n.next=head;head=n;if(tail==null)tail=n;metrics.moves++;size++;metrics.steps++;return;} Node p=nodeAt(index-1);n.next=p.next;p.next=n;metrics.moves+=2;size++;metrics.steps++;}
    public int remove(int index){check(index); if(index==0){int v=head.value;head=head.next;if(--size==0)tail=null;metrics.moves++;metrics.steps++;return v;} Node p=nodeAt(index-1);int v=p.next.value;p.next=p.next.next;metrics.moves++;if(index==size-1)tail=p;size--;metrics.steps++;return v;}
    public int get(int index){check(index);return nodeAt(index).value;}
    public boolean contains(int x){Node cur=head;while(cur!=null){metrics.steps++;metrics.comparisons++;if(cur.value==x)return true;cur=cur.next;}return false;}
}
