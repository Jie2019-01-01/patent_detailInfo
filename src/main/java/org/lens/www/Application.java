package org.lens.www;

import org.lens.www.crawl.Downloader;
import org.lens.www.dao.PatentDao;
import org.lens.www.entity.PatentEntity;
import org.lens.www.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取专利数据的abstract、cite works、cite parents、family info
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("\t\t\t\t\t\t\t---------- 开始启动，patent爬虫 ----------");
        int start = 0;
        int size = 10;
        int page = 1;
        boolean b = true;
        PatentDao patentDao = new PatentDao();
        while (b){
            List<String> idList = patentDao.findNoAb(start, size);
            if (idList==null || idList.size()==0)
                break;
//        List<String> idList = new ArrayList<String>(){{add("000-000-241-677-072");add("063-596-049-675-945");}};
            Downloader downloader = new Downloader();
            // 循环lens_id
            for(String id: idList){
                PatentEntity patentEntity = downloader.execute(id);
                if(patentEntity == null){
                    LogUtil.error("---》patentEntity返回的结果为null，退出循环!");
                    b = false;
                    break;
                }
                System.out.println("结果输出：" + patentEntity + "\r\n");
                // 初始化模型，传递管道层
//          PatentEntity entity = new PatentEntity(ab, cites_by_parents, cites_parents, family_info);
//          patentDao.save_data(entity);
            }
            System.out.println("===== 第" + page + "页结束=======");
            start += size;
        }
    }
}
