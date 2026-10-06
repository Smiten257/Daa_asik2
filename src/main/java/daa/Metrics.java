package daa;
public class Metrics {
    public long steps, moves, comparisons;
    public void reset(){ steps=0; moves=0; comparisons=0; }
    public Metrics copy(){ Metrics m=new Metrics(); m.steps=steps; m.moves=moves; m.comparisons=comparisons; return m; }
}
