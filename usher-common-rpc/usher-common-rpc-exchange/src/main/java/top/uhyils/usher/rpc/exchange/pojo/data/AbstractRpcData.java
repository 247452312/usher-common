package top.uhyils.usher.rpc.exchange.pojo.data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import top.uhyils.usher.rpc.exception.RpcSpiInitException;
import top.uhyils.usher.rpc.exception.RpcTypeNotSupportedException;
import top.uhyils.usher.rpc.exception.RpcVersionNotSupportedException;
import top.uhyils.usher.rpc.exception.UsherRpcException;
import top.uhyils.usher.rpc.exchange.content.UsherRpcContent;
import top.uhyils.usher.rpc.exchange.pojo.content.RpcContent;
import top.uhyils.usher.rpc.exchange.pojo.head.RpcHeader;
import top.uhyils.usher.rpc.exchange.pojo.head.RpcHeaderFactory;
import top.uhyils.usher.util.BytesUtil;
import top.uhyils.usher.util.CollectionUtil;
import top.uhyils.usher.util.LogUtil;

/**
 * rpc体模板,用来规定rpc应该有的东西
 *
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2020年12月18日 11时16分
 */
public abstract class AbstractRpcData implements RpcData {

    /**
     * 换行符
     */
    protected static final byte ENTER = '\n';

    /**
     * 此类支持的最大版本
     */
    private static final Integer MAX_VERSION = 1;

    /**
     * 版本
     */
    protected Integer version;

    /**
     * 数据体类型 0->请求 1->响应
     */
    protected Integer type;

    /**
     * 内容大小
     */
    protected Integer size;

    /**
     * 状态{@link top.uhyils.usher.rpc.enums.RpcStatusEnum}
     */
    protected byte status;

    /**
     * 唯一标识
     */
    protected Long unique;

    /**
     * head
     */
    protected RpcHeader[] headers;

    /**
     * 拆出来的content字符串
     */
    protected String[] contentArray;

    /**
     * 内容
     */
    protected RpcContent content;

    protected AbstractRpcData() {
    }

    @Override
    public void init(final Object... params) {
        byte[] data = (byte[]) params[0];
        doInit(data);
    }

    /**
     * 获取rpc全部
     *
     * @return
     */
    @Override
    public byte[] toBytes() {
        //头部
        byte[] previousBytes = new byte[UsherRpcContent.RPC_DATA_ITEM_SIZE.stream().mapToInt(t -> t).sum()];
        // 写索引
        AtomicInteger writeIndex = new AtomicInteger(0);
        // 写入mark头
        System.arraycopy(
            UsherRpcContent.AGREEMENT_START,
            0,
            previousBytes,
            writeIndex.getAndAdd(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_MARK_INDEX)),
            UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_MARK_INDEX));

        // 写入version and type
        byte[] src = {(byte) ((rpcVersion() << 2) + (type() << 1))};
        int andAdd = writeIndex.getAndAdd(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_VERSION_REQ_RES_INDEX));
        System.arraycopy(src, 0, previousBytes, andAdd, UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_VERSION_REQ_RES_INDEX));

        //获取head 和 content 的压缩后的数组 并写入size
        byte[] headAndContent = headerAndContent().getBytes(StandardCharsets.UTF_8);
        headAndContent = BytesUtil.compress(headAndContent);
        System.arraycopy(BytesUtil.changeIntegerToByte(headAndContent.length), 0, previousBytes, writeIndex.getAndAdd(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_SIZE_INDEX)),
                         UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_SIZE_INDEX));

        // 写入状态
        previousBytes[writeIndex.getAndAdd(1)] = getStatus();

        //写入唯一标示
        byte[] uniqueBytes = BytesUtil.changeLongToByte(getUnique());
        System.arraycopy(uniqueBytes,
                         0,
                         previousBytes,
                         writeIndex.getAndAdd(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_UNIQUE_INDEX)),
                         UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_UNIQUE_INDEX));

        return BytesUtil.concat(previousBytes, headAndContent);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public RpcHeader[] getHeaders() {
        return headers;
    }

    public void setHeaders(RpcHeader[] headers) {
        this.headers = headers;
    }

    public RpcContent getContent() {
        return content;
    }

    public void setContent(RpcContent content) {
        this.content = content;
    }

    @Override
    public Long unique() {
        return unique;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Long getUnique() {
        return unique;
    }

    public void setUnique(Long unique) {
        this.unique = unique;
    }

    public String[] getContentArray() {
        return contentArray;
    }

    public void setContentArray(String[] contentArray) {
        this.contentArray = contentArray;
    }

    /**
     * RPC版本
     *
     * @return
     */
    @Override
    public Integer rpcVersion() {
        return version;
    }

    /**
     * RPC类型,0->请求 1->响应
     *
     * @return
     */
    @Override
    public Integer type() {
        return type;
    }

    /**
     * RPC内容的size,最大值{@link Integer#MAX_VALUE}
     *
     * @return
     */
    @Override
    public Integer size() {
        return size;
    }

    /**
     * 获取RPC中的header
     *
     * @return
     */
    @Override
    public RpcHeader[] rpcHeaders() {
        return headers;
    }

    /**
     * 内容
     *
     * @return
     */
    @Override
    public RpcContent content() {
        return content;
    }

    @Override
    public String headerAndContent() {
        StringBuilder sb = new StringBuilder();
        RpcHeader[] rpcHeaders = rpcHeaders();
        if (CollectionUtil.isNotEmpty(rpcHeaders)) {
            for (RpcHeader rpcHeader : rpcHeaders) {
                sb.append("\n");
                sb.append(String.format("%s:%s", rpcHeader.getName(), rpcHeader.getValue()));
            }
        }
        sb.append("\n");
        sb.append("\n");
        sb.append(this.content().contentString());
        return sb.toString();
    }

    public RpcHeader getHeader(String name) {
        RpcHeader[] rpcHeaders = rpcHeaders();
        if (rpcHeaders == null || rpcHeaders.length == 0) {
            return null;
        }
        for (RpcHeader rpcHeader : rpcHeaders) {
            if (StringUtils.equals(rpcHeader.getName(), name)) {
                return rpcHeader;
            }
        }
        return null;
    }

    /**
     * 初始化内容
     */
    protected abstract void initContent();

    /**
     * 填充size
     *
     * @param data
     * @param readIndex
     */
    protected void initSize(byte[] data, AtomicInteger readIndex) {
        int sizeSize = UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_SIZE_INDEX);
        int startIndex = readIndex.get();
        byte[] sizeBytes = Arrays.copyOfRange(data, startIndex, startIndex + sizeSize);
        this.size = BytesUtil.changeByteToInteger(sizeBytes);
        readIndex.addAndGet(sizeSize);
    }

    protected void initUnique(byte[] data, AtomicInteger readIndex) {
        int uniqueSize = UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_UNIQUE_INDEX);
        int startIndex = readIndex.get();
        byte[] uniqueBytes = Arrays.copyOfRange(data, startIndex, startIndex + uniqueSize);
        this.unique = BytesUtil.changeByteToLong(uniqueBytes);
        readIndex.addAndGet(uniqueSize);
    }

    protected void initStatus(byte[] data, AtomicInteger readIndex) {
        int statusSize = UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_STATUS_INDEX);
        assert statusSize == 1;
        int startIndex = readIndex.get();
        byte[] dataStatus = Arrays.copyOfRange(data, startIndex, startIndex + statusSize);
        // 先这么搞..
        this.setStatus(dataStatus[0]);
        readIndex.addAndGet(statusSize);
    }

    protected void initContentArray(byte[] data, AtomicInteger readIndex) {
        byte[] bytes = Arrays.copyOfRange(data, readIndex.get(), data.length);
        String contentStr = new String(bytes, StandardCharsets.UTF_8);
        this.contentArray = contentStr.split("\n");
    }

    protected void initHeader(byte[] data, AtomicInteger readIndex) {
        boolean lastByteIsEnter = Boolean.FALSE;
        List<RpcHeader> rpcHeaders = new ArrayList<>();
        StringBuilder headerStr = new StringBuilder();
        int headerEnd = 0;
        for (int i = readIndex.get(); i < data.length; i++) {
            headerEnd++;
            if (Objects.equals(data[i], ENTER)) {
                if (lastByteIsEnter) {
                    break;
                }
                lastByteIsEnter = Boolean.TRUE;
                RpcHeader rpcHeader = RpcHeaderFactory.newHeader(headerStr.toString());
                headerStr.delete(0, headerStr.length());
                if (rpcHeader != null) {
                    rpcHeaders.add(rpcHeader);
                }
            } else {
                headerStr.append((char) data[i]);
                lastByteIsEnter = Boolean.FALSE;
            }
        }
        this.headers = rpcHeaders.toArray(new RpcHeader[]{new RpcHeader()});
        readIndex.addAndGet(headerEnd);
    }

    /**
     * 确定版本以及类型是否兼容(正确)
     *
     * @param data
     * @param readIndex
     *
     * @throws RpcVersionNotSupportedException
     */
    protected void initVersionAndType(byte[] data, AtomicInteger readIndex) {
        int dataVersion = (data[readIndex.get()] >> 2) & 0b111111;
        if (dataVersion > MAX_VERSION) {
            throw new RpcVersionNotSupportedException(dataVersion, MAX_VERSION);
        }
        int dataType = (data[readIndex.get()] & 0b10) >> 1;
        if (!Objects.equals(dataType, type())) {
            throw new RpcTypeNotSupportedException(dataType, type());
        }
        this.version = dataVersion;
        readIndex.addAndGet(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_VERSION_REQ_RES_INDEX));

    }

    private void doInit(final byte[] data) {
        try {
            AtomicInteger readIndex = new AtomicInteger(0);
            // 判断是不是myRpc的协议
            isUsherRpc(data, readIndex);

            // 确定版本以及类型是否兼容(正确)
            initVersionAndType(data, readIndex);

            // 填充size
            initSize(data, readIndex);

            //填充状态
            initStatus(data, readIndex);

            //填充唯一标识
            initUnique(data, readIndex);

            // 获取剩余的部分 解压缩 指针重置到0
            byte[] lastBytes = Arrays.copyOfRange(data, readIndex.get(), data.length);
            lastBytes = BytesUtil.uncompress(lastBytes);
            readIndex.set(0);

            // 获取header
            initHeader(lastBytes, readIndex);

            // 获取内容字符串
            initContentArray(lastBytes, readIndex);

            // 处理内容
            initContent();
        } catch (Exception e) {
            LogUtil.error(this, e);
            throw new RpcSpiInitException(e, this.getClass());
        }
    }

    /**
     * 判断是不是myRpc的协议
     *
     * @param data
     * @param readIndex
     *
     * @throws UsherRpcException
     */
    private void isUsherRpc(byte[] data, AtomicInteger readIndex) throws UsherRpcException {
        int from = readIndex.get();
        byte[] bytes = Arrays.copyOfRange(data, from, from + UsherRpcContent.AGREEMENT_START.length);
        boolean startByteEquals = Arrays.equals(bytes, UsherRpcContent.AGREEMENT_START);
        if (!startByteEquals) {
            throw new UsherRpcException();
        }
        readIndex.addAndGet(UsherRpcContent.RPC_DATA_ITEM_SIZE.get(UsherRpcContent.RPC_DATA_MARK_INDEX));
    }
}
