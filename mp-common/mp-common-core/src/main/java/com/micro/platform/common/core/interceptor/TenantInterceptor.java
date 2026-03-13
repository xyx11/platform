package com.micro.platform.common.core.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

/**
 * 多租户拦截器
 *
 * 使用 MyBatis-Plus 的租户插件实现租户数据隔离
 */
@Component
public class TenantInterceptor implements TenantLineHandler {

    /**
     * 获取租户 ID
     */
    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        return null;
    }

    /**
     * 获取租户字段名称
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 是否忽略某张表的租户过滤
     */
    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        
        String lowerTableName = tableName.toLowerCase();

        // 系统表不需要租户过滤
        String[] ignoreTables = {
            "sys_tenant",           // 租户表本身
            "sys_config",           // 系统配置
            "sys_dict_type",        // 字典类型
            "sys_dict_data",        // 字典数据
        };

        for (String ignoreTable : ignoreTables) {
            if (lowerTableName.equals(ignoreTable)) {
                return true;
            }
        }

        // 如果当前没有租户 ID，也不进行过滤
        return TenantContext.getCurrentTenantId() == null;
    }
}
