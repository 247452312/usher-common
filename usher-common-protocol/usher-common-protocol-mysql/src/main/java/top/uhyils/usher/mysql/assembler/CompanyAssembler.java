package top.uhyils.usher.mysql.assembler;

import org.mapstruct.factory.Mappers;
import top.uhyils.usher.content.CallNodeContent;
import top.uhyils.usher.mysql.pojo.DTO.CompanyInfo;
import top.uhyils.usher.mysql.pojo.DTO.IUserPrivilegesInfo;
import top.uhyils.usher.mysql.pojo.DTO.MUserInfo;

/**
 * @author uhyils <247452312@qq.com>
 * @date 文件创建日期 2025年01月10日 16时32分
 */
@org.mapstruct.Mapper
public class CompanyAssembler {

    public static CompanyAssembler INSTANCE = Mappers.getMapper(CompanyAssembler.class);


    public MUserInfo toMUser(CompanyInfo user) {

        MUserInfo mUserInfo = new MUserInfo();
        mUserInfo.setHost("%");
        mUserInfo.setUser(user.getAk());
        mUserInfo.setSelectPriv(CallNodeContent.SQL_YES);
        mUserInfo.setInsertPriv(CallNodeContent.SQL_YES);
        mUserInfo.setUpdatePriv(CallNodeContent.SQL_YES);
        mUserInfo.setDeletePriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreatePriv(CallNodeContent.SQL_NO);
        mUserInfo.setDropPiv(CallNodeContent.SQL_NO);
        mUserInfo.setReloadPriv(CallNodeContent.SQL_YES);
        mUserInfo.setShutdownPriv(CallNodeContent.SQL_NO);
        mUserInfo.setProcessPriv(CallNodeContent.SQL_YES);
        mUserInfo.setFilePriv(CallNodeContent.SQL_YES);
        mUserInfo.setGrantPriv(CallNodeContent.SQL_YES);
        mUserInfo.setReferencesPriv(CallNodeContent.SQL_YES);
        mUserInfo.setIndexPriv(CallNodeContent.SQL_YES);
        mUserInfo.setAlterPriv(CallNodeContent.SQL_YES);
        mUserInfo.setShowDbPriv(CallNodeContent.SQL_YES);
        mUserInfo.setSuperPriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreateTmpTablePriv(CallNodeContent.SQL_YES);
        mUserInfo.setLockTablesPriv(CallNodeContent.SQL_YES);
        mUserInfo.setExecutePriv(CallNodeContent.SQL_YES);
        mUserInfo.setReplSlavePriv(CallNodeContent.SQL_YES);
        mUserInfo.setReplClientPriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreateViewPriv(CallNodeContent.SQL_YES);
        mUserInfo.setShowViewPriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreateRoutinePriv(CallNodeContent.SQL_YES);
        mUserInfo.setAlterRoutinePriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreateUserPriv(CallNodeContent.SQL_YES);
        mUserInfo.setEventPriv(CallNodeContent.SQL_YES);
        mUserInfo.setTriggerPriv(CallNodeContent.SQL_YES);
        mUserInfo.setCreateTablespacePriv(CallNodeContent.SQL_YES);
        mUserInfo.setSslType(null);
        mUserInfo.setSslCipher(null);
        mUserInfo.setX509Issuer(null);
        mUserInfo.setX509Subject(null);
        mUserInfo.setMaxQuestions(0);
        mUserInfo.setMaxUpdates(0);
        mUserInfo.setMaxConnections(0);
        mUserInfo.setMaxUserConnections(0);
        mUserInfo.setPlugin(null);
        mUserInfo.setAuthenticationString(user.getSk());
        mUserInfo.setPasswordExpired(CallNodeContent.SQL_NO);
        mUserInfo.setPasswordLastChanged(null);
        mUserInfo.setPasswordLifetime(null);
        mUserInfo.setAccountLocked(CallNodeContent.SQL_NO);
        mUserInfo.setCreateRolePriv(CallNodeContent.SQL_YES);
        mUserInfo.setDropRolePriv(CallNodeContent.SQL_YES);
        mUserInfo.setPasswordReuseHistory(null);
        mUserInfo.setPasswordReuseTime(null);
        mUserInfo.setPasswordRequireCurrent(null);
        mUserInfo.setUserAttributes(null);
        return mUserInfo;
    }

    public IUserPrivilegesInfo toIUserPrivileges(CompanyInfo user) {
        IUserPrivilegesInfo iUserPrivilegesInfo = new IUserPrivilegesInfo();
        iUserPrivilegesInfo.setGrantee(user.getAk() + "@%");
        iUserPrivilegesInfo.setTableCatalog(CallNodeContent.CATALOG_NAME);
        iUserPrivilegesInfo.setPrivilegeType("SELECT");
        iUserPrivilegesInfo.setIsGrantable(CallNodeContent.SQL_NO);
        return iUserPrivilegesInfo;
    }

}
