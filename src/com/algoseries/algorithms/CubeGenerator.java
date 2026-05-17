package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class CubeGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        for(int i=1;i<=n;i++) r.add((double)(i*i*i));
        return new SeriesResult(SeriesType.CUBE,r,"Iterative",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        List<Double> r=new ArrayList<>(); long s=System.nanoTime(); buildCube(r,1,n);
        return new SeriesResult(SeriesType.CUBE,r,"Recursive",System.nanoTime()-s,r.size()*8L,"n="+n,"");
    }
    private void buildCube(List<Double> l,int i,int n){if(i>n)return;l.add((double)(i*i*i));buildCube(l,i+1,n);}
}
