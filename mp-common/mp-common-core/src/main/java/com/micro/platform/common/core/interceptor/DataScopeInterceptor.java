package com.micro.platform.common.core.interceptor;

import com.micro.platform.common.core.aspect.DataScopeContext;
import com.micro.platform.common.core.enums.DataScopeType;
import com.micro.platform.common.core.util.DataScopeUtil;
import com.micro.platform.common.security.entity.LoginUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.interceptor.Interceptor;
import org.apache.ibatis.interceptor.InterceptorChain;
import org.apache.ibatis.interceptor.Invocation;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * 数据权限拦截器
 *
 * 拦截 MyBatis 查询语句，自动添加数据权限过滤条件
 */
@Component
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取参数
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];

        // 获取 BoundSql
        BoundSql boundSql = ms.getBoundSql(parameter);

        // 获取登录用户信息
        LoginUser loginUser = DataScopeContext.getLoginUser();
        if (loginUser == null) {
            return invocation.proceed();
        }

        // 如果是超级管理员，不添加数据权限过滤
        if (loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("admin")) {
            return invocation.proceed();
        }

        // 获取数据权限范围
        Integer dataScopeType = loginUser.getDataScope();
        if (dataScopeType == null || dataScopeType.equals(DataScopeType.ALL.getCode())) {
            return invocation.proceed();
        }

        // 获取表别名
        String deptAlias = DataScopeContext.getDeptAlias();
        String userAlias = DataScopeContext.getUserAlias();

        // 生成数据权限 SQL 片段
        String dataScopeSql = generateDataScopeSql(loginUser, dataScopeType, deptAlias, userAlias);

        // 如果不需要添加数据权限过滤，直接执行
        if (dataScopeSql == null || dataScopeSql.trim().isEmpty()) {
            return invocation.proceed();
        }

        // 重新执行查询
        Executor executor = (Executor) invocation.getTarget();
        return executor.query(ms, parameter, rowBounds, resultHandler, boundSql, boundSql.getSql() + " " + dataScopeSql);
    }

    /**
     * 生成数据权限 SQL 片段
     */
    private String generateDataScopeSql(LoginUser loginUser, Integer dataScopeType, String deptAlias, String userAlias) {
        StringBuilder sql = new StringBuilder();

        switch (DataScopeType.getByCode(dataScopeType)) {
            case DEPT_AND_CHILD:
                // 本部门及以下
                sql.append(" AND (d.id = ").append(loginUser.getDeptId())
                        .append(" OR d.parent_id = ").append(loginUser.getDeptId()).append(")");
                break;
            case DEPT_ONLY:
                // 仅本部门
                sql.append(" AND d.id = ").append(loginUser.getDeptId());
                break;
            case SELF:
                // 仅本人
                sql.append(" AND t.id = ").append(loginUser.getUserId());
                break;
            case CUSTOM:
                // 自定义数据权限
                if (loginUser.getRoleIds() != null && !loginUser.getRoleIds().isEmpty()) {
                    sql.append(" AND EXISTS (SELECT 1 FROM sys_user_role sur WHERE sur.user_id = t.id AND sur.role_id IN (");
                    for (int i = 0; i < loginUser.getRoleIds().size(); i++) {
                        if (i > 0) {
                            sql.append(",");
                        }
                        sql.append(loginUser.getRoleIds().get(i));
                    }
                    sql.append("))");
                }
                break;
            default:
                return "";
        }

        return sql.toString();
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无配置
    }
}