package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class APGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        double a=p.length>0?p[0]:1, d=p.length>1?p[1]:1;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        for(int i=0;i<n;i++) r.add(a+i*d);
        return new SeriesResult(SeriesType.ARITHMETIC,r,"Iterative",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,d=%.2f,n=%d",a,d,n),"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        double a=p.length>0?p[0]:1, d=p.length>1?p[1]:1;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        buildAP(r,a,d,0,n);
        return new SeriesResult(SeriesType.ARITHMETIC,r,"Recursive",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,d=%.2f,n=%d",a,d,n),"");
    }
    private void buildAP(List<Double> l,double a,double d,int i,int n){if(i>=n)return;l.add(a+i*d);buildAP(l,a,d,i+1,n);}
}
 
