package daa;
public class DynamicArray implements IntList {
    private int[] data; private int size; private final Metrics metrics;
    public DynamicArray(){ this(10, new Metrics()); }
    public DynamicArray(int capacity){ this(capacity, new Metrics()); }
    public DynamicArray(int capacity, Metrics metrics){ data=new int[Math.max(1,capacity)]; this.metrics=metrics; }
    public int size(){ return size; }
    private void check(int i){ if(i<0||i>=size) throw new IndexOutOfBoundsException("index: "+i); }
    private void checkInsert(int i){ if(i<0||i>size) throw new IndexOutOfBoundsException("index: "+i); }
    private void grow(){ if(size==data.length){ int[] a=new int[data.length*2]; for(int i=0;i<size;i++){a[i]=data[i]; metrics.steps++; metrics.moves++;} data=a; } }
    public void add(int x){ grow(); data[size++]=x; metrics.steps++; }
    public void add(int index,int x){ checkInsert(index); grow(); for(int i=size;i>index;i--){ data[i]=data[i-1]; metrics.steps++; metrics.moves++; } data[index]=x; size++; metrics.steps++; }
    public int remove(int index){ check(index); int old=data[index]; for(int i=index;i<size-1;i++){ data[i]=data[i+1]; metrics.steps++; metrics.moves++; } size--; metrics.steps++; return old; }
    public int get(int index){ check(index); metrics.steps++; return data[index]; }
    public boolean contains(int x){ for(int i=0;i<size;i++){ metrics.steps++; metrics.comparisons++; if(data[i]==x)return true; } return false; }
}
