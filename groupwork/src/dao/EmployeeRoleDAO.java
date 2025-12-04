package dao;

import entity.EmployeeRole;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工角色数据访问对象
 * 对应数据库 employee_role 表
 */
public class EmployeeRoleDAO {

    public EmployeeRoleDAO() {
    }

    /**
     * 从结果集中提取角色信息
     */
    public EmployeeRole extractRoleFromResultSet(ResultSet rs) throws SQLException {
        EmployeeRole role = new EmployeeRole();
        role.setRoleId(rs.getInt("role_id"));
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        role.setPermissions(rs.getString("permissions"));
        return role;
    }

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    public List<EmployeeRole> getAllRoles() {
        List<EmployeeRole> roles = new ArrayList<>();
        String sql = "SELECT * FROM employee_role";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                roles.add(extractRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色对象，不存在返回null
     */
    public EmployeeRole getRoleById(int roleId) {
        String sql = "SELECT * FROM employee_role WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据角色名称获取角色
     *
     * @param roleName 角色名称 (Trainer, Receptionist, Admin)
     * @return 角色对象，不存在返回null
     */
    public EmployeeRole getRoleByName(String roleName) {
        String sql = "SELECT * FROM employee_role WHERE role_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加新角色
     *
     * @param role 角色对象
     * @return 是否添加成功
     */
    public boolean addRole(EmployeeRole role) {
        String sql = "INSERT INTO employee_role (role_id, role_name, description, permissions) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, role.getRoleId());
            pstmt.setString(2, role.getRoleName());
            pstmt.setString(3, role.getDescription());
            pstmt.setString(4, role.getPermissions());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新角色信息
     *
     * @param role 角色对象
     * @return 是否更新成功
     */
    public boolean updateRole(EmployeeRole role) {
        String sql = "UPDATE employee_role SET role_name = ?, description = ?, permissions = ? WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getRoleName());
            pstmt.setString(2, role.getDescription());
            pstmt.setString(3, role.getPermissions());
            pstmt.setInt(4, role.getRoleId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    public boolean deleteRole(int roleId) {
        String sql = "DELETE FROM employee_role WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查角色是否拥有某个权限
     *
     * @param roleId     角色ID
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(int roleId, String permission) {
        EmployeeRole role = getRoleById(roleId);
        if (role != null && role.getPermissions() != null) {
            String[] permissions = role.getPermissions().split(",");
            for (String p : permissions) {
                if (p.trim().equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}

