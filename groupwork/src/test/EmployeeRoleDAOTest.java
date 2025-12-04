package test;

import dao.EmployeeRoleDAO;
import entity.EmployeeRole;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class EmployeeRoleDAOTest {
    EmployeeRoleDAO roleDAO = new EmployeeRoleDAO();

    @Test
    public void testGetAllRoles() {
        List<EmployeeRole> roles = roleDAO.getAllRoles();
        assertNotNull(roles);
        assertEquals(3, roles.size()); // 数据库有3个角色
    }

    @Test
    public void testGetRoleById() {
        // 测试获取教练角色
        EmployeeRole trainer = roleDAO.getRoleById(1);
        assertNotNull(trainer);
        assertEquals("Trainer", trainer.getRoleName());
        assertEquals("健身教练，负责课程教学", trainer.getDescription());
        assertNotNull(trainer.getPermissions());

        // 测试获取前台角色
        EmployeeRole receptionist = roleDAO.getRoleById(2);
        assertNotNull(receptionist);
        assertEquals("Receptionist", receptionist.getRoleName());

        // 测试获取管理员角色
        EmployeeRole admin = roleDAO.getRoleById(3);
        assertNotNull(admin);
        assertEquals("Admin", admin.getRoleName());
    }

    @Test
    public void testGetRoleByIdNotFound() {
        EmployeeRole role = roleDAO.getRoleById(999);
        assertNull(role);
    }

    @Test
    public void testGetRoleByName() {
        EmployeeRole trainer = roleDAO.getRoleByName("Trainer");
        assertNotNull(trainer);
        assertEquals(1, trainer.getRoleId());

        EmployeeRole receptionist = roleDAO.getRoleByName("Receptionist");
        assertNotNull(receptionist);
        assertEquals(2, receptionist.getRoleId());

        EmployeeRole admin = roleDAO.getRoleByName("Admin");
        assertNotNull(admin);
        assertEquals(3, admin.getRoleId());
    }

    @Test
    public void testGetRoleByNameNotFound() {
        EmployeeRole role = roleDAO.getRoleByName("NotExist");
        assertNull(role);
    }

    @Test
    public void testHasPermission() {
        // 教练权限测试
        assertTrue(roleDAO.hasPermission(1, "course_view"));
        assertTrue(roleDAO.hasPermission(1, "member_view"));
        assertFalse(roleDAO.hasPermission(1, "member_add"));
        assertFalse(roleDAO.hasPermission(1, "employee_delete"));

        // 前台权限测试
        assertTrue(roleDAO.hasPermission(2, "member_view"));
        assertTrue(roleDAO.hasPermission(2, "member_add"));
        assertTrue(roleDAO.hasPermission(2, "checkin_manage"));
        assertFalse(roleDAO.hasPermission(2, "employee_delete"));

        // 管理员权限测试
        assertTrue(roleDAO.hasPermission(3, "member_view"));
        assertTrue(roleDAO.hasPermission(3, "member_delete"));
        assertTrue(roleDAO.hasPermission(3, "employee_delete"));
    }

    @Test
    public void testHasPermissionInvalidRole() {
        assertFalse(roleDAO.hasPermission(999, "member_view"));
    }

    @Test
    public void testRolePermissionList() {
        EmployeeRole trainer = roleDAO.getRoleById(1);
        assertNotNull(trainer);

        String[] permissions = trainer.getPermissionList();
        assertNotNull(permissions);
        assertTrue(permissions.length > 0);

        // 验证权限列表包含预期权限
        boolean hasCourseView = false;
        for (String p : permissions) {
            if (p.equals("course_view")) {
                hasCourseView = true;
                break;
            }
        }
        assertTrue(hasCourseView);
    }

    @Test
    public void testAddAndDeleteRole() {
        // 添加新角色
        EmployeeRole newRole = new EmployeeRole();
        newRole.setRoleId(99);
        newRole.setRoleName("TestRole");
        newRole.setDescription("测试角色");
        newRole.setPermissions("test_permission1,test_permission2");

        assertTrue(roleDAO.addRole(newRole));

        // 验证添加成功
        EmployeeRole added = roleDAO.getRoleById(99);
        assertNotNull(added);
        assertEquals("TestRole", added.getRoleName());
        assertEquals("测试角色", added.getDescription());
        assertTrue(added.hasPermission("test_permission1"));

        // 删除测试角色
        assertTrue(roleDAO.deleteRole(99));

        // 验证删除成功
        assertNull(roleDAO.getRoleById(99));
    }

    @Test
    public void testUpdateRole() {
        // 先添加一个测试角色
        EmployeeRole testRole = new EmployeeRole();
        testRole.setRoleId(98);
        testRole.setRoleName("UpdateTestRole");
        testRole.setDescription("更新测试角色");
        testRole.setPermissions("perm1");

        assertTrue(roleDAO.addRole(testRole));

        // 更新角色
        testRole.setRoleName("UpdatedRole");
        testRole.setDescription("已更新的角色");
        testRole.setPermissions("perm1,perm2,perm3");

        assertTrue(roleDAO.updateRole(testRole));

        // 验证更新成功
        EmployeeRole updated = roleDAO.getRoleById(98);
        assertNotNull(updated);
        assertEquals("UpdatedRole", updated.getRoleName());
        assertEquals("已更新的角色", updated.getDescription());
        assertTrue(updated.hasPermission("perm2"));

        // 清理测试数据
        assertTrue(roleDAO.deleteRole(98));
    }

    @Test
    public void testRoleHasPermissionMethod() {
        EmployeeRole admin = roleDAO.getRoleById(3);
        assertNotNull(admin);

        // 测试实体类的 hasPermission 方法
        assertTrue(admin.hasPermission("member_view"));
        assertTrue(admin.hasPermission("employee_delete"));
        assertFalse(admin.hasPermission("not_exist_permission"));
    }
}

