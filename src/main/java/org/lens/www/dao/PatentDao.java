package org.lens.www.dao;

import org.lens.www.entity.PatentEntity;
import org.lens.www.utils.DBUtilLocal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟dao
 */
public class PatentDao {

    public PatentDao(){System.out.println("* 持久层已经就绪.");}
    /**
     * 查询数据库，获取无摘要url
     */
    public List<String> findNoAb(int start, int size){
        List<String> idList = new ArrayList<String>();
        String sql = "select lens_id from patent_lens_data_distinct where abstract is null limit ?,?";
        Connection con = DBUtilLocal.getConnection();
        PreparedStatement ps = null;
        try{
            ps = con.prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String url = rs.getString("lens_id");
                idList.add(url);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return idList;
    }

    /**
     * 信息入库
     */
    public void save_data(PatentEntity patentEntity){
        Connection con = DBUtilLocal.getConnection();
        String sql = "update patent_lens_data_distinct set abstract=?, cites_works=?, cites_parents=?, family_info=?";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, patentEntity.getAb());
            ps.setString(2, patentEntity.getCites_by_parents());
            ps.setString(3, patentEntity.getCites_parents());
            ps.setString(4, patentEntity.getFamily_info());
            ps.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
