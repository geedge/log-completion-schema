package com.zdjizhi.tools.zookeeper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author qidaijie
 * @Package cn.ac.iie.utils.zookeeper
 * @Description:
 * @date 2020/11/1411:28
 */
public class ZookeeperUtils implements Watcher {
    private static final Log logger = LogFactory.get();
    private static final int ID_MAX = 255;

    private ZooKeeper zookeeper;

    private static final int SESSION_TIME_OUT = 20000;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            countDownLatch.countDown();
        }
    }


    /**
     * 修改节点信息
     *
     * @param path 节点路径
     */
    public int modifyNode(String path, String zookeeperIp) {
        createNode(path, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, zookeeperIp);
        int workerId = 0;
        try {
            connectZookeeper(zookeeperIp);
            Stat stat = zookeeper.exists(path, true);
            workerId = Integer.parseInt(getNodeDate(path));
            if (workerId > ID_MAX) {
                workerId = 0;
                zookeeper.setData(path, "1".getBytes(), stat.getVersion());
            } else {
                String result = String.valueOf(workerId + 1);
                if (stat != null) {
                    zookeeper.setData(path, result.getBytes(), stat.getVersion());
                } else {
                    logger.error("Node does not exist!,Can't modify");
                }
            }
        } catch (KeeperException | InterruptedException e) {
            logger.error("modify error Can't modify," + e);
        } finally {
            closeConn();
        }
        logger.warn("workerID is：" + workerId);
        return workerId;
    }

    /**
     * 连接zookeeper
     *
     * @param host 地址
     */
    public void connectZookeeper(String host) {
        try {
            zookeeper = new ZooKeeper(host, SESSION_TIME_OUT, this);
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            logger.error("Connection to the Zookeeper Exception! message:" + e);
        }
    }

    /**
     * 关闭连接
     */
    public void closeConn() {
        try {
            if (zookeeper != null) {
                zookeeper.close();
            }
        } catch (InterruptedException e) {
            logger.error("Close the Zookeeper connection Exception! message:" + e);
        }
    }

    /**
     * 获取节点内容
     *
     * @param path 节点路径
     * @return 内容/异常null
     */
    public String getNodeDate(String path) {
        String result = null;
        Stat stat = new Stat();
        try {
            byte[] resByte = zookeeper.getData(path, true, stat);

            result = StrUtil.str(resByte, "UTF-8");
        } catch (KeeperException | InterruptedException e) {
            logger.error("Get node information exception" + e);
        }
        return result;
    }

    /**
     * @param path 节点创建的路径
     * @param date 节点所存储的数据的byte[]
     * @param acls 控制权限策略
     */
    public void createNode(String path, byte[] date, List<ACL> acls, String zookeeperIp) {
        try {
            connectZookeeper(zookeeperIp);
            Stat exists = zookeeper.exists(path, true);
            if (exists == null) {
                Stat existsSnowflakeld = zookeeper.exists("/Snowflake", true);
                if (existsSnowflakeld == null) {
                    zookeeper.create("/Snowflake", null, acls, CreateMode.PERSISTENT);
                }
                zookeeper.create(path, date, acls, CreateMode.PERSISTENT);
            } else {
                logger.warn("Node already exists ! Don't need to create");
            }
        } catch (KeeperException | InterruptedException e) {
            logger.error(e);
        } finally {
            closeConn();
        }
    }
}
