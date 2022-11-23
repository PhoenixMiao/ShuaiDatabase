package com.phoenix.shuaidatabase.single;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ShuaiZset extends ShuaiObject{

    private ConcurrentHashMap<ShuaiString,Double> dict;

    private ShuaiSkipList ssl;

    public ShuaiZset() {
        this.dict = new ConcurrentHashMap<>();
        this.ssl = new ShuaiSkipList();
        this.objectType = ShuaiObjectType.SHUAI_ZSET;
    }

    public ConcurrentHashMap<ShuaiString, Double> getDict() {
        return dict;
    }

    public ShuaiReply zsetLength(String[] argv, ShuaiDB db) {
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(ssl.getLength()+""));
    }

    public static ShuaiReply zadd(String[] argv, ShuaiDB db) {
        ShuaiZset shuaiZset;
        try {
            //判断key是否已经存在
            if (db.getDict().containsKey(new ShuaiString(argv[1])))
                shuaiZset = (ShuaiZset) db.getDict().get(new ShuaiString(argv[1]));
            else {
                shuaiZset = new ShuaiZset();
                db.getDict().put(new ShuaiString(argv[1]), shuaiZset);
            }
            //result记录成功添加的个数
            int result=0;
            String[] newValue = argv[2].split(" ");
            if(newValue.length%2!=0){
                throw new Exception("can not parse argument");
            }
            for (int i=0;i<newValue.length;i+=2) {
                ShuaiString member = new ShuaiString(newValue[i+1]);
                Double score = Double.valueOf(newValue[i]);
                if(shuaiZset.dict.containsKey(member) && shuaiZset.dict.get(member).equals(score)){
                    continue;
                }
                else if(shuaiZset.dict.containsKey(member)) {
                    shuaiZset.ssl.delete(shuaiZset.dict.get(member), member);
                }
                shuaiZset.dict.put(member, score);
                shuaiZset.ssl.insert(score, member);
                result++;
            }
            return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(result+ ""));
        }catch (ClassCastException e) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.TYPE_FORMAT_FAULT);
        } catch (Exception exception) {
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        }
    }


    public ShuaiReply zcount(String[] argv, ShuaiDB db){
        Double a = Double.valueOf(argv[2]);
        Double b = Double.valueOf(argv[3]);
        Double min = Math.min(a,b);
        Double max = Math.max(a,b);
        ShuaiSkipList.Node firstNode = ssl.firstInRange(new ShuaiSkipList.RangeSpec(min,max,false,false));
        long rank = 0,count = 0;
        if(firstNode!=null){
            rank = ssl.zslGetRank(firstNode);
            count = ssl.getLength()-rank+1;
        }
        ShuaiSkipList.Node lastNode = ssl.lastInRange(new ShuaiSkipList.RangeSpec(min,max,false,false));
        if(lastNode!=null){
            rank = ssl.zslGetRank(lastNode);
            count -= ssl.getLength()-rank;
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(count+""));
    }

    @Override
    public String toString() {
        return dict.toString();
    }

    public ShuaiReply zincrby(String[] argv, ShuaiDB db){
        String[] arguments = argv[2].split(" ");
        if(arguments.length!=2){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        }
        Double newScore = 0.0;
        try{
            newScore = this.dict.get(new ShuaiString(arguments[1])) + Double.valueOf(arguments[0]);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        arguments[0] = newScore.toString();
        argv[2] = String.join(" ",arguments);
        return zadd(argv,db);
    }


    public ShuaiReply zinterstore(String[] argv, ShuaiDB db){
        ShuaiString destination = new ShuaiString(argv[1]);
        Integer numkeys;
        Integer aggregate=0; //0:sum 1:min 2:max default:sum
        //参数校验
        try{
            numkeys = Integer.valueOf(argv[2]);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        String[] otherArguments = argv[3].split(" ");
        if(otherArguments.length!=numkeys && otherArguments.length!=2*numkeys+1 && otherArguments.length!=2*numkeys+3){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.NUMBER_OF_ARGUMENTS_FAULT);
        }
        if(otherArguments.length>numkeys && !otherArguments[numkeys].equalsIgnoreCase("weights")){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        if(otherArguments.length>2*numkeys+1 ){
            if(!otherArguments[2*numkeys+1].equalsIgnoreCase("AGGREGATE"))
                return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
            String agg = otherArguments[2*numkeys+2];
            if(agg.equalsIgnoreCase("sum"))aggregate=0;
            else if(agg.equalsIgnoreCase("min"))aggregate=1;
            else if(agg.equalsIgnoreCase("max"))aggregate=2;
            else return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }

        //记录集合&权重
        ShuaiString zsetName;
        ShuaiZset[] zsets = new ShuaiZset[numkeys];
        Double[] weights = new Double[numkeys];
        Arrays.fill(weights,1.0);
        for(int i=0;i<numkeys;i++){
            zsetName=new ShuaiString(otherArguments[i]);
            if(!db.getDict().containsKey(zsetName)){
                return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(0+""));
            }
            if(!db.getDict().get(zsetName).getClass().equals(ShuaiZset.class)){
                return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
            }
            ShuaiZset temp = (ShuaiZset) db.getDict().get(zsetName);
            if(temp.dict.size()==0){
                if (!db.getDict().containsKey(destination))
                    db.getDict().put(destination, new ShuaiZset());
                return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(0+""));
            }
            zsets[i]=temp;
        }
        if(otherArguments.length>numkeys){
            for(int i=numkeys+1;i<=2*numkeys;i++){
                try{
                    weights[i-numkeys-1]=Double.valueOf(otherArguments[i]);
                }catch (NumberFormatException numberFormatException){
                    return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
                }
            }
        }

        //遍历求key&score
        Set<ShuaiString> keys = new HashSet<>();
        keys.addAll(zsets[0].dict.keySet());
        Map<ShuaiString,Double> key_score = new HashMap<>();
        for(int i=0;i<numkeys;i++){
            keys.retainAll( zsets[i].dict.keySet() );
            for(ShuaiString key : keys){
                double newscore = zsets[i].dict.get(key);
                if(aggregate==0)key_score.put(key,key_score.get(key)+newscore);
                if(aggregate==1)key_score.put(key,Math.min(key_score.get(key),newscore));
                if(aggregate==2)key_score.put(key,Math.max(key_score.get(key),newscore));
            }
        }
        StringBuilder argBuilder = new StringBuilder();
        for(Map.Entry<ShuaiString,Double> entry : key_score.entrySet()){
            argBuilder.append(entry.getValue());
            argBuilder.append(" ");
            argBuilder.append(entry.getKey());
            argBuilder.append(" ");
        }
        String arg = argBuilder.toString().trim();
        String[] args = new String[]{"ZADD",destination.toString(),arg};
        return ShuaiZset.zadd(args,db);
    }

    //按分数返回区间
    public ShuaiReply zrangeByScore(String[] argv, ShuaiDB db){
        double min,max;
        try{
            double a=Double.valueOf(argv[2]);
            double b=Double.valueOf(argv[3]);
            min=Math.min(a,b);
            max=Math.max(a,b);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        long first = this.ssl.zslGetRank(this.ssl.firstInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        long last = this.ssl.zslGetRank(this.ssl.lastInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        String res = this.ssl.traverse(first,last,true,false);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res.equals("")?"nil":res));
    }

    //按照位置返回区间
    public ShuaiReply zrange(String[] argv, ShuaiDB db){
        boolean withScore = false;
        String[] arg3 = argv[3].split(" ");
        if(arg3.length>1 && arg3[1].equalsIgnoreCase("withscores"))withScore=true;
        int start=0,len = this.ssl.getLength(),end=len;
        try {
            start = Integer.valueOf(argv[2]);
            if(start<0)start = len+start+1;
            end = Integer.valueOf(arg3[0]);
            if(end<0)end = len+start+1;
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        String res = this.ssl.traverse(start,end,withScore,false);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res.equals("")?"nil":res));
    }

    public ShuaiReply zrank(String[] argv, ShuaiDB db){
        ShuaiString member = new ShuaiString(argv[2]);
        if(this.dict.get(member)==null)
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.MEMBER_NOT_EXIST );
        ShuaiSkipList.Node node = new ShuaiSkipList.Node(1,this.dict.get(member),member);
        Long rank = this.ssl.zslGetRank(node);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(rank+""));
    }

    public ShuaiReply zrem(String[] argv, ShuaiDB db){
        String[] members = argv[2].split(" ");
        int res=0;
        for(String member:members){
            ShuaiString mem = new ShuaiString(member);
            if(dict.containsKey(mem)){
                this.ssl.delete(dict.get(mem),mem);
                dict.remove(mem);
                res++;
            }
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res+""));
    }

    public ShuaiReply zremRangeByRank(String[] argv, ShuaiDB db){
        Integer start,stop,len=this.ssl.getLength();
        String res;
        int reply=0;
        try{
            start = Integer.valueOf(argv[2]);
            stop = Integer.valueOf(argv[3]);
            if(start<0)start = len+start+1;
            if(stop<0)stop = len+start+1;
            if(start>stop){
                int temp=start; start =stop; stop=temp;
            }
            res = this.ssl.traverse(start,stop,false,false);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        res = res.trim();
        String[] rem = res.split(" ");
        for(int i=0;i<rem.length;i++){
            ShuaiString mem = new ShuaiString(rem[i]);
            if(dict.containsKey(mem)){
                this.ssl.delete(dict.get(mem),mem);
                dict.remove(mem);
                reply++;
            }
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply+""));
    }

    public ShuaiReply zremRangeByScore(String[] argv, ShuaiDB db){
        int reply=0;
        double min,max;
        try{
            double a=Double.valueOf(argv[2]);
            double b=Double.valueOf(argv[3]);
            min=Math.min(a,b);
            max=Math.max(a,b);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        long first = this.ssl.zslGetRank(this.ssl.firstInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        long last = this.ssl.zslGetRank(this.ssl.lastInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        String res = this.ssl.traverse(first,last,false,false);
        res = res.trim();
        String[] rem = res.split(" ");
        for(int i=0;i<rem.length;i++){
            ShuaiString mem = new ShuaiString(rem[i]);
            if(dict.containsKey(mem)){
                this.ssl.delete(dict.get(mem),mem);
                dict.remove(mem);
                reply++;
            }
        }
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(reply+""));
    }

    public ShuaiReply zrevRange(String[] argv, ShuaiDB db){
        boolean withScore = false;
        String[] arg3 = argv[3].split(" ");
        if(arg3.length>1 && arg3[1].equalsIgnoreCase("withscores"))withScore=true;
        int start=0,len = this.ssl.getLength(),end=len;
        try {
            start = Integer.valueOf(argv[2]);
            if(start<0)start = len+start+1;
            end = Integer.valueOf(arg3[0]);
            if(end<0)end = len+start+1;
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        String res = this.ssl.traverse(start,end,withScore,true);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res.equals("")?"nil":res));
    }

    public ShuaiReply zrevRangeByScore(String[] argv, ShuaiDB db){
        double min,max;
        try{
            double a=Double.valueOf(argv[2]);
            double b=Double.valueOf(argv[3]);
            min=Math.min(a,b);
            max=Math.max(a,b);
        }catch (NumberFormatException numberFormatException){
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT, ShuaiErrorCode.ARGUMENT_WRONG);
        }
        long first = this.ssl.zslGetRank(this.ssl.firstInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        long last = this.ssl.zslGetRank(this.ssl.lastInRange(new ShuaiSkipList.RangeSpec(min,max,false,false)));
        String res = this.ssl.traverse(last,first,true,true);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(res.equals("")?"nil":res));
    }

    public ShuaiReply zrevRank(String[] argv, ShuaiDB db){
        ShuaiString member = new ShuaiString(argv[2]);
        if(this.dict.get(member)==null)
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.MEMBER_NOT_EXIST );
        ShuaiSkipList.Node node = new ShuaiSkipList.Node(1,this.dict.get(member),member);
        Long rank = this.ssl.zslGetRank(node);
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString((this.ssl.getLength()-rank+1)+""));
    }

    public ShuaiReply zscore(String[] argv, ShuaiDB db){
        ShuaiString member = new ShuaiString(argv[2]);
        if(this.dict.get(member)==null)
            return new ShuaiReply(ShuaiReplyStatus.INPUT_FAULT,ShuaiErrorCode.MEMBER_NOT_EXIST );
        return new ShuaiReply(ShuaiReplyStatus.OK, new ShuaiString(this.dict.get(member)+""));
    }

}
