package top.uhyils.usher.redis.druid.filter;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import top.uhyils.usher.enums.CacheTypeEnum;
import top.uhyils.usher.redis.filter.RpcHotSpotMethodInvoker;
import top.uhyils.usher.rpc.netty.callback.MethodInvokerFactory;
import top.uhyils.usher.util.LogUtil;


/**
 * 解析table的sql filter
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年05月11日 09时01分
 */
public class TableSqlFilter extends FilterEventAdapter {

    /**
     * 工具人
     */
    private final RpcHotSpotMethodInvoker util;

    public TableSqlFilter() {
        try {
            util = (RpcHotSpotMethodInvoker) MethodInvokerFactory.createMethodInvoker();
        } catch (InterruptedException e) {
            LogUtil.error(this, e);
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        super.statementExecuteAfter(statement, sql, result);
        if (!result) {
            return;
        }
        CacheTypeEnum cacheTypeEnum = util.getMarkThreadLocal().get();
        //如果此接口不允许缓存
        if (CacheTypeEnum.NOT_TYPE.equals(cacheTypeEnum)) {
            return;
        }
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        List<String> tableNames = new ArrayList<>();
        statementList.stream().filter(t -> !(t instanceof SQLSelectStatement)).forEach(stmt -> {
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            for (TableStat.Name entry : tables.keySet()) {
                String tableName = entry.getName();
                tableNames.add(tableName);
            }
        });
        if (!CollectionUtils.isEmpty(tableNames)) {
            util.doHotSpotWrite(tableNames, cacheTypeEnum);
        }

    }
}
