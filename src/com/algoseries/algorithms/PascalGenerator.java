package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import java.util.*;
public class PascalGenerator extends AbstractGenerator {
    @Override public SeriesResult generateIterative(int n,double... p){
        List<Double> flat=new ArrayList<>(); List<List<Long>> tri=new ArrayList<>(); long s=System.nanoTime();
        for(int row=0;row<n;row++){
            List<Long> r=new ArrayList<>(); r.add(1L);
            List<Long> prev=row>0?tri.get(row-1):null;
            for(int col=1;col<row;col++) r.add(prev.get(col-1)+prev.get(col));
            if(row>0)r.add(1L); tri.add(r); for(Long v:r) flat.add(v.doubleValue());
        }
        return new SeriesResult(SeriesType.PASCAL,flat,"Iterative",System.nanoTime()-s,flat.size()*8L,"rows="+n,fmt(tri));
    }
    @Override public SeriesResult generateRecursive(int n,double... p){
        List<Double> flat=new ArrayList<>(); List<List<Long>> tri=new ArrayList<>(); long s=System.nanoTime();
        buildRow(tri,0,n); for(List<Long> row:tri) for(Long v:row) flat.add(v.doubleValue());
        return new SeriesResult(SeriesType.PASCAL,flat,"Recursive",System.nanoTime()-s,flat.size()*8L,"rows="+n,fmt(tri));
    }
    private void buildRow(List<List<Long>> tri,int row,int n){
        if(row>=n)return; List<Long> r=new ArrayList<>(); r.add(1L);
        if(row>0){List<Long> prev=tri.get(row-1);for(int c=1;c<row;c++)r.add(prev.get(c-1)+prev.get(c));r.add(1L);}
        tri.add(r); buildRow(tri,row+1,n);
    }
    private String fmt(List<List<Long>> tri){
        if(tri.isEmpty())return "";
        int mx=tri.get(tri.size()-1).size()*6; StringBuilder sb=new StringBuilder();
        for(List<Long> row:tri){int pad=(mx-row.size()*6)/2;sb.append(" ".repeat(Math.max(0,pad)));for(Long v:row)sb.append(String.format("%-6d",v));sb.append("\n");}
        return sb.toString();
    }
}
