package top.uhyils.usher.protocol.rpc.base;

import java.util.List;
import top.uhyils.usher.pojo.DTO.base.BaseDbDTO;
import top.uhyils.usher.pojo.DTO.base.Page;
import top.uhyils.usher.pojo.cqe.command.ChangeCommand;
import top.uhyils.usher.pojo.cqe.command.IdCommand;
import top.uhyils.usher.pojo.cqe.command.RemoveCommand;
import top.uhyils.usher.pojo.cqe.command.base.AddCommand;
import top.uhyils.usher.pojo.cqe.query.BlackQuery;
import top.uhyils.usher.pojo.cqe.query.IdQuery;
import top.uhyils.usher.pojo.cqe.query.IdsQuery;

/**
 * 如果是一个EntityService 就应该继承这个类,包含增删改以及
 * 根据id查询
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年04月23日 14时29分
 */
public interface DTOProvider<T extends BaseDbDTO> extends BaseProvider {

    /**
     * 根据某几列获取数据
     *
     * @param query 根据列名获取信息
     *
     * @return 分页数据(也可以设置不分页)
     */
    Page<T> query(BlackQuery query);

    /**
     * 根据某几列获取数据
     *
     * @param query 根据列名获取信息
     *
     * @return 数据
     */
    List<T> queryNoPage(BlackQuery query);

    /**
     * 根据id查询
     *
     * @param query id
     *
     * @return 单条
     */
    T queryById(IdQuery query);

    /**
     * 根据id查询
     *
     * @param query id
     *
     * @return 单条
     */
    List<T> queryByIds(IdsQuery query);

    /**
     * 插入
     *
     * @param addCommand 插入信息,要求是entity
     *
     * @return 插入条数
     */
    Long add(AddCommand<T> addCommand);

    /**
     * 修改
     *
     * @param changeCommand 修改信息,要求是entity,并且要求有id
     *
     * @return 修改条数
     */
    Integer change(ChangeCommand<T> changeCommand);

    /**
     * 删除
     *
     * @param removeCommand 要删除的东西
     *
     * @return 删除条数
     */
    Integer remove(RemoveCommand removeCommand);

    /**
     * 删除
     *
     * @param id 要删除的东西
     *
     * @return 删除条数
     */
    Integer remove(IdCommand id);

    /**
     * 数量
     *
     * @param order 默认不分页,不可配置是否分页
     *
     * @return 根据条件查询出来的数量
     */
    Long count(BlackQuery order);


}
