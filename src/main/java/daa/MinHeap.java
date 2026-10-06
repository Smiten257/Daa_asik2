package daa;
public class MinHeap {
    private int[] heap; private int size; private final Metrics metrics;
    public MinHeap(){this(10,new Metrics());}
    public MinHeap(int capacity,Metrics metrics){heap=new int[Math.max(1,capacity)];this.metrics=metrics;}
    public int size(){return size;}
    /** Package-private test hook: verifies heap order without exposing internal storage. */
    boolean isHeapPropertyValid(){
        for(int i=1;i<size;i++){
            int parent=(i-1)/2;
            if(heap[parent]>heap[i]) return false;
        }
        return true;
    }
    private void grow(){if(size==heap.length){int[] a=new int[heap.length*2];for(int i=0;i<size;i++){a[i]=heap[i];metrics.steps++;metrics.moves++;}heap=a;}}
    public void insert(int x){grow();int i=size++;heap[i]=x;metrics.steps++;while(i>0){int p=(i-1)/2;metrics.steps++;metrics.comparisons++;if(heap[p]<=heap[i])break;int t=heap[p];heap[p]=heap[i];heap[i]=t;metrics.moves++;i=p;}}
    public int peekMin(){if(size==0)throw new IllegalStateException("empty heap");metrics.steps++;return heap[0];}
    public int extractMin(){if(size==0)throw new IllegalStateException("empty heap");int result=heap[0];heap[0]=heap[--size];metrics.moves++;int i=0;while(true){int l=2*i+1,r=l+1,small=i;if(l<size){metrics.steps++;metrics.comparisons++;if(heap[l]<heap[small])small=l;}if(r<size){metrics.steps++;metrics.comparisons++;if(heap[r]<heap[small])small=r;}if(small==i)break;int t=heap[i];heap[i]=heap[small];heap[small]=t;metrics.moves++;i=small;}metrics.steps++;return result;}
}
