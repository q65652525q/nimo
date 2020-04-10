package com.itheima.travel.dao.impl;

import com.itheima.travel.dao.RouteDao;
import com.itheima.travel.model.Route;
import com.itheima.travel.model.RouteImg;
import com.itheima.travel.util.JdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 旅游线路数据访问类
 */
@Repository("routeDao")
public class RouteDaoImpl implements RouteDao {
    //jdbcTemplate
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 查询人气旅游线路
     * @return List<Route>
     * @throws SQLException
     */
    public List<Route> getPopularityRouteList()throws SQLException {
        String sql="select * from tab_route where rflag='1' order by count desc limit 0,4";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<Route>(Route.class));
    }

    /**
     * 获取最新线路列表
     * @return
     * @throws SQLException
     */
    public List<Route> getNewestRouteList()throws SQLException {
        String sql="SELECT * FROM tab_route WHERE rflag='1' ORDER BY rdate DESC LIMIT 0,4";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<Route>(Route.class));
    }

    /**
     * 获取最新主题列表
     * @return
     * @throws SQLException
     */
    public List<Route> getThemeRouteList() throws SQLException {
        String sql="SELECT * FROM tab_route WHERE rflag='1' and isThemeTour='1' ORDER BY rdate DESC LIMIT 0,4";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<Route>(Route.class));
    }

    /**
     * 分页获取国内游线路列表数据
     * @param cid
     * @param curPage
     * @param pageSize
     * @return
     * @throws SQLException
     */
    public List<Route> findRouteListByPage(int cid, int curPage, int pageSize,String rname)throws SQLException {
//        -- 分页的sql语句格式：
//        --  SELECT * FROM 表名 c WHERE 条件  LIMIT start,length
//        --      start,查询数据的开始索引，索引从0开始,就是从查询结果集中从第几条数据开始截取
//        --      length,截取多少条，就是每一页多少条数据，就是pageSize每页大小
//        -- sql分页实例1:每页3条数据，查询第1页sql语句
//        SELECT * FROM tab_route  WHERE  rflag='1'  LIMIT 0,3
//        -- sql分页实例2:每页3条数据，查询第2页sql语句
//        SELECT * FROM tab_route  WHERE  rflag='1'  LIMIT 3,3
//        -- sql分页实例3:每页3条数据，查询第3页sql语句
//        SELECT * FROM tab_route  WHERE  rflag='1'  LIMIT 6,3
//        -- 疑问：start的值分别为0,3,6，我们可以口算出来，那有没有什么公式计算出来呢？
//        -- 答：0,3,6是等差数列，start=(curPage-1)*pageSize
//        -- sql分页实例4:每页3条数据，查询第curPage页sql语句
//        SELECT * FROM tab_route  WHERE  rflag='1' and cid=? LIMIT ?,?

        String sql = "SELECT * FROM tab_route  WHERE  rflag='1' and cid=? ";
        //定义动态的参数集合
        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(cid);
        if(rname!=null && !"".equals(rname)){
            sql+=" and rname like ? ";
            paramsList.add("%"+rname+"%");
        }

        sql+=" LIMIT ?,?";
        int start = (curPage-1)*pageSize;
        paramsList.add(start);
        paramsList.add(pageSize);

        //jdbcTemplate需要数组,可以将集合转换数组
        Object[] params = paramsList.toArray();

        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<Route>(Route.class),params);
    }

    /**
     * 获取指定分类的总记录数
     * @param cid
     * @return int
     * @throws SQLException
     */
    public int getCountByCid(int cid,String rname)throws SQLException {
        String sql = "SELECT COUNT(*) FROM tab_route WHERE rflag='1' AND cid=? ";
        //定义动态的参数集合
        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(cid);
        if(rname!=null && !"".equals(rname)){
            sql+=" and rname like ? ";
            paramsList.add("%"+rname+"%");
        }
        return jdbcTemplate.queryForObject(sql,paramsList.toArray(),Integer.class);
    }

    /**
     * 根据rid获取旅游线路\所属分类\所属商家
     * @param rid
     * @return Map<String,Object>
     * @throws SQLException
     */
    public Map<String,Object> findRouteByRid(String rid)throws SQLException {
        try {
            String sql="SELECT * FROM tab_route r,tab_category c,tab_seller s WHERE r.cid=c.cid AND r.sid=s.sid AND r.rflag='1' AND r.rid=?";
            return jdbcTemplate.queryForMap(sql,new Object[]{rid});
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据rid获取图片集合
     * @param rid
     * @return List<RouteImg>
     * @throws SQLException
     */
    public List<RouteImg> findRouteImgsByRid(String rid)throws SQLException {
        String sql="SELECT * FROM tab_route_img WHERE rid=?";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<RouteImg>(RouteImg.class),rid);
    }

    /**
     * 获取旅游线路收藏排行榜总记录数
     * @return int
     * @throws SQLException
     */
    public int getCountByFavoriteRank(Map<String,Object> conditionMap) throws SQLException {
        //按照收藏数量降序的分页sql语句
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM tab_route WHERE rflag='1'");
        //定义动态参数列表集合
        List<Object> paramList = new ArrayList<Object>();
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object rnameObj = conditionMap.get("rname");
        if(rnameObj!=null && !rnameObj.toString().trim().equals("")){
            sqlBuilder.append(" and rname like ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add("%"+rnameObj.toString().trim()+"%");
        }
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object startPriceObj = conditionMap.get("startPrice");
        if(startPriceObj!=null && !startPriceObj.toString().trim().equals("")){
            sqlBuilder.append(" and price >= ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add(startPriceObj.toString().trim());
        }
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object endPriceObj = conditionMap.get("endPrice");
        if(endPriceObj!=null && !endPriceObj.toString().trim().equals("")){
            sqlBuilder.append(" and price <= ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add(endPriceObj.toString().trim());
        }

        //将参数动态列表结合List<Object>转换为参数数组Object[]
        Object[] params = paramList.toArray();
        //执行sql返回一个int整型数据
        return jdbcTemplate.queryForObject(sqlBuilder.toString(),params,Integer.class);
    }

    /**
     * 获取旅游线路收藏数量降序的排行榜当前页数据列表
     * @param curPage
     * @param pageSize
     * @return List<Route>
     */
    public List<Route> getRoutesFavoriteRankByPage(
            int curPage,int pageSize,Map<String,Object> conditionMap)throws SQLException {
        //按照收藏数量降序的分页sql语句
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM tab_route WHERE rflag='1'");
        //定义动态参数列表集合
        List<Object> paramList = new ArrayList<Object>();
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object rnameObj = conditionMap.get("rname");
        if(rnameObj!=null && !rnameObj.toString().trim().equals("")){
            sqlBuilder.append(" and rname like ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add("%"+rnameObj.toString().trim()+"%");
        }
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object startPriceObj = conditionMap.get("startPrice");
        if(startPriceObj!=null && !startPriceObj.toString().trim().equals("")){
            sqlBuilder.append(" and price >= ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add(startPriceObj.toString());
        }
        //判断搜索条件rname,如果有效就进行拼接过滤条件
        Object endPriceObj = conditionMap.get("endPrice");
        if(endPriceObj!=null && !endPriceObj.toString().trim().equals("")){
            sqlBuilder.append(" and price <= ?");
            //将占位符的参数值加入动态参数列表集合
            paramList.add(endPriceObj.toString().trim());
        }
        sqlBuilder.append(" ORDER BY COUNT DESC LIMIT ?,?");
        int start = (curPage-1)*pageSize;
        int length = pageSize;
        //将start,length加入动态参数列表集合
        paramList.add(start);
        paramList.add(length);
        //将参数动态列表结合List<Object>转换为参数数组Object[]
        Object[] params = paramList.toArray();
        //执行sql语句
        return  jdbcTemplate.query(sqlBuilder.toString(), new BeanPropertyRowMapper<Route>(Route.class),params);
    }


}
