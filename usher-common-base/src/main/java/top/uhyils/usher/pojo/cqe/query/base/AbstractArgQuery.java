package top.uhyils.usher.pojo.cqe.query.base;

import java.util.List;
import top.uhyils.usher.pojo.cqe.DefaultCQE;
import top.uhyils.usher.pojo.cqe.query.demo.Arg;
import top.uhyils.usher.pojo.cqe.query.demo.Limit;
import top.uhyils.usher.pojo.cqe.query.demo.Order;

/**
 * 请求
 *
 * @author uhyils <247452312@qq.com>
 * @version 1.0
 * @date 文件创建日期 2021年08月25日 08时45分
 */
public abstract class AbstractArgQuery extends DefaultCQE implements BaseArgQuery {

    private static final long serialVersionUID = -1L;

    protected List<Arg> args;

    protected Order order;

    protected Limit limit;

    protected AbstractArgQuery() {
    }

    protected AbstractArgQuery(List<Arg> args) {
        this(args, new Order(), new Limit());
    }

    protected AbstractArgQuery(List<Arg> args, Order order, Limit limit) {
        this.args = args;
        this.order = order;
        this.limit = limit;
    }

    @Override
    public List<Arg> getArgs() {
        return args;
    }

    public void setArgs(List<Arg> args) {
        this.args = args;
    }

    @Override
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }
}
