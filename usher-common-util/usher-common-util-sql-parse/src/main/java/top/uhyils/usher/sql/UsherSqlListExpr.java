package top.uhyils.usher.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.fastjson.JSON;
import java.util.List;
import java.util.Objects;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2021年11月25日 09时24分
 */
public class UsherSqlListExpr extends SQLExprImpl implements MySqlExpr {

    private List<SQLExpr> values;

    public UsherSqlListExpr() {
    }

    public UsherSqlListExpr(List<SQLExpr> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UsherSqlListExpr)) {
            return false;
        }
        UsherSqlListExpr other = (UsherSqlListExpr) o;
        List<SQLExpr> values = other.getValues();
        return Objects.equals(this.values, values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public List<SQLExpr> getValues() {
        return values;
    }

    public void setValues(List<SQLExpr> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(values);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {

    }
}
