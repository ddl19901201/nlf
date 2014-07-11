package nc.liat6.frame.db.custom.mongo;

import java.util.ArrayList;
import java.util.List;
import nc.liat6.frame.db.entity.Bean;
import nc.liat6.frame.db.exception.DaoException;
import nc.liat6.frame.db.plugin.ISelecter;
import nc.liat6.frame.db.plugin.Rule;
import nc.liat6.frame.json.JSON;
import nc.liat6.frame.locale.L;
import nc.liat6.frame.locale.LocaleFactory;
import nc.liat6.frame.log.Logger;
import nc.liat6.frame.paging.PageData;
import nc.liat6.frame.util.Stringer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * MONGO��ѯ��
 * 
 * @author 6tail
 * 
 */
public class MongoSelecter extends MongoExecuter implements ISelecter{

  public ISelecter table(String tableName){
    initTable(tableName);
    return this;
  }

  public ISelecter column(String... column){
    for(String c:column){
      Rule rule = new Rule();
      rule.setColumn(c);
      cols.add(rule);
    }
    return this;
  }

  public ISelecter where(String sql){
    Logger.getLog().warn(Stringer.print("??",L.get(LocaleFactory.locale,"sql.cond_not_support"),sql));
    return this;
  }

  public ISelecter whereSql(String sql,Object[] values){
    Logger.getLog().warn(Stringer.print("??",L.get(LocaleFactory.locale,"sql.cond_not_support"),sql));
    return this;
  }

  public ISelecter where(String column,Object value){
    Rule r = new Rule();
    r.setColumn(column);
    r.setOpStart("");
    r.setOpEnd("");
    r.setTag("");
    wheres.add(r);
    paramWheres.add(value);
    return this;
  }

  public ISelecter whereLike(String column,Object value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereLike"));
    return where(column,value);
  }

  public ISelecter whereLeftLike(String column,Object value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereLeftLike"));
    return where(column,value);
  }

  public ISelecter whereRightLike(String column,Object value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereRightLike"));
    return where(column,value);
  }

  public ISelecter whereNq(String column,Object value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereNq"));
    return where(column,value);
  }

  public ISelecter whereIn(String column,Object... value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereIn"));
    return where(column,value);
  }

  public ISelecter whereNotIn(String column,Object... value){
    Logger.getLog().warn(Stringer.print("?? ?",L.get(LocaleFactory.locale,"sql.cond_not_support"),column,"whereNotIn"));
    return where(column,value);
  }

  public ISelecter asc(String... column){
    for(String c:column){
      orders.add(c+":asc");
    }
    return this;
  }

  public ISelecter desc(String... column){
    for(String c:column){
      orders.add(c+":desc");
    }
    return this;
  }

  public List<Bean> select(){
    if(null==tableName){
      throw new DaoException(Stringer.print("??.?",L.get("sql.table_not_found"),template.getConnVar().getAlias(),tableName));
    }
    MongoConnection conn = (MongoConnection)template.getConnVar().getConnection();
    DBObject ref = new BasicDBObject();
    DBObject keys = new BasicDBObject();
    DBObject orderBy = new BasicDBObject();
    for(int i = 0;i<cols.size();i++){
      keys.put(cols.get(i).getColumn(),1);
    }
    for(int i = 0;i<wheres.size();i++){
      Rule r = wheres.get(i);
      Object v = paramWheres.get(i);
      ref.put(r.getColumn(),v);
    }
    for(String os:orders){
      String k = Stringer.cut(os,"",":").trim();
      String v = Stringer.cut(os,":").trim();
      keys.put(k,v.equalsIgnoreCase("asc")?1:-1);
    }
    DBCursor cur = conn.getDb().getCollection(tableName).find(ref,keys).sort(orderBy);
    List<Bean> l = new ArrayList<Bean>(cur.count());
    while(cur.hasNext()){
      DBObject o = cur.next();
      Bean b = JSON.toBean(o.toString());
      l.add(b);
    }
    reset();
    return l;
  }

  public PageData page(int pageNumber,int pageSize){
    if(null==tableName){
      throw new DaoException(Stringer.print("??.?",L.get("sql.table_not_found"),template.getConnVar().getAlias(),tableName));
    }
    MongoConnection conn = (MongoConnection)template.getConnVar().getConnection();
    DBObject ref = new BasicDBObject();
    DBObject keys = new BasicDBObject();
    DBObject orderBy = new BasicDBObject();
    for(int i = 0;i<cols.size();i++){
      keys.put(cols.get(i).getColumn(),1);
    }
    for(int i = 0;i<wheres.size();i++){
      Rule r = wheres.get(i);
      Object v = paramWheres.get(i);
      ref.put(r.getColumn(),v);
    }
    for(String os:orders){
      String k = Stringer.cut(os,"",":").trim();
      String v = Stringer.cut(os,":").trim();
      keys.put(k,v.equalsIgnoreCase("asc")?1:-1);
    }
    int from = (pageNumber-1)*pageSize;
    DBCursor cur = conn.getDb().getCollection(tableName).find(ref,keys).sort(orderBy).skip(from).limit(pageSize);
    List<Bean> l = new ArrayList<Bean>(cur.size());
    while(cur.hasNext()){
      DBObject o = cur.next();
      Bean b = JSON.toBean(o.toString());
      l.add(b);
    }
    reset();
    PageData pd = new PageData();
    pd.setPageNumber(pageNumber);
    pd.setPageSize(pageSize);
    pd.setRecordCount(conn.getDb().getCollection(tableName).find(ref,keys).count());
    pd.setData(l);
    return pd;
  }

  public Bean one(){
    List<Bean> l = select();
    if(l.size()>1){
      throw new DaoException(L.get("sql.record_too_many"));
    }
    if(l.size()<1){
      throw new DaoException(L.get("sql.record_not_found"));
    }
    return l.get(0);
  }
}
