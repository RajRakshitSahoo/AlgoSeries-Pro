package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class GPGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        double a=p.length>0?p[0]:1, r2=p.length>1?p[1]:2;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        double cur=a; for(int i=0;i<n;i++){r.add(cur);cur*=r2;}
        return new SeriesResult(SeriesType.GEOMETRIC,r,"Iterative",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,r=%.2f,n=%d",a,r2,n),"");
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        double a=p.length>0?p[0]:1, r2=p.length>1?p[1]:2;
        List<Double> r=new ArrayList<>(); long s=System.nanoTime();
        buildGP(r,a,r2,0,n);
        return new SeriesResult(SeriesType.GEOMETRIC,r,"Recursive",System.nanoTime()-s,r.size()*8L,String.format("a=%.2f,r=%.2f,n=%d",a,r2,n),"");
    }
    private void buildGP(List<Double> l,double cur,double r,int i,int n){if(i>=n)return;l.add(cur);buildGP(l,cur*r,r,i+1,n);}
}
