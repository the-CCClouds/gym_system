package test;

import dao.EmployeeDAO;
import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class EmployeeDAOTest {
    EmployeeDAO employeeDAO = new EmployeeDAO();

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = employeeDAO.getAllEmployees();
        assertNotNull(employees);
        assertTrue(employees.size() >= 7); // 数据库有7个员工
    }

    @Test
    public void testGetEmployeeById() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);
        assertEquals("张教练", employee.getName());
        assertEquals(1, employee.getRoleId());
        assertEquals("13800001111", employee.getPhone());

        // 验证关联的角色对象
        EmployeeRole role = employee.getEmployeeRole();
        assertNotNull(role);
        assertEquals("Trainer", role.getRoleName());
    }

    @Test
    public void testGetEmployeeByIdNotFound() {
        Employee employee = employeeDAO.getEmployeeById(999);
        assertNull(employee);
    }

    @Test
    public void testGetTrainers() {
        List<Employee> trainers = employeeDAO.getTrainers();
        assertNotNull(trainers);
        assertTrue(trainers.size() >= 4); // 数据库有4个教练

        for (Employee trainer : trainers) {
            assertEquals(1, trainer.getRoleId());
            assertEquals("Trainer", trainer.getRole());
        }
    }

    @Test
    public void testGetReceptionists() {
        List<Employee> receptionists = employeeDAO.getReceptionists();
        assertNotNull(receptionists);
        assertTrue(receptionists.size() >= 2); // 数据库有2个前台

        for (Employee receptionist : receptionists) {
            assertEquals(2, receptionist.getRoleId());
            assertEquals("Receptionist", receptionist.getRole());
        }
    }

    @Test
    public void testGetAdmins() {
        List<Employee> admins = employeeDAO.getAdmins();
        assertNotNull(admins);
        assertTrue(admins.size() >= 1); // 数据库有1个管理员

        for (Employee admin : admins) {
            assertEquals(3, admin.getRoleId());
            assertEquals("Admin", admin.getRole());
        }
    }

    @Test
    public void testGetEmployeesByRoleId() {
        // 测试获取教练
        List<Employee> trainers = employeeDAO.getEmployeesByRoleId(1);
        assertNotNull(trainers);
        assertTrue(trainers.size() > 0);

        // 测试获取前台
        List<Employee> receptionists = employeeDAO.getEmployeesByRoleId(2);
        assertNotNull(receptionists);
        assertTrue(receptionists.size() > 0);

        // 测试获取管理员
        List<Employee> admins = employeeDAO.getEmployeesByRoleId(3);
        assertNotNull(admins);
        assertTrue(admins.size() > 0);
    }

    @Test
    public void testGetEmployeeByPhone() {
        Employee employee = employeeDAO.getEmployeeByPhone("13800001111");
        assertNotNull(employee);
        assertEquals("张教练", employee.getName());
        assertEquals(1, employee.getRoleId());
    }

    @Test
    public void testGetEmployeeByPhoneNotFound() {
        Employee employee = employeeDAO.getEmployeeByPhone("00000000000");
        assertNull(employee);
    }

    @Test
    public void testSearchEmployeeByName() {
        List<Employee> employees = employeeDAO.searchEmployeeByName("教练");
        assertNotNull(employees);
        assertTrue(employees.size() >= 4);

        for (Employee employee : employees) {
            assertTrue(employee.getName().contains("教练"));
        }
    }

    @Test
    public void testSearchEmployeeByNameNotFound() {
        List<Employee> employees = employeeDAO.searchEmployeeByName("不存在的名字999");
        assertNotNull(employees);
        assertEquals(0, employees.size());
    }

    @Test
    public void testAddEmployee() {
        Employee employee = new Employee();
        employee.setName("测试员工");
        employee.setRoleId(1); // 教练
        employee.setPhone("13899998888");
        employee.setHireDate(new Date());

        assertTrue(employeeDAO.addEmployee(employee));
        assertTrue(employee.getId() > 0);

        // 验证添加成功
        Employee added = employeeDAO.getEmployeeById(employee.getId());
        assertNotNull(added);
        assertEquals("测试员工", added.getName());

        // 清理测试数据
        assertTrue(employeeDAO.deleteEmployee(employee.getId()));
    }

    @Test
    public void testAddEmployeeInvalidPhone() {
        Employee employee = new Employee();
        employee.setName("无效手机号员工");
        employee.setRoleId(1);
        employee.setPhone("123"); // 无效手机号
        employee.setHireDate(new Date());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    @Test
    public void testAddEmployeeInvalidRoleId() {
        Employee employee = new Employee();
        employee.setName("无效角色员工");
        employee.setRoleId(99); // 无效角色ID
        employee.setPhone("13899997777");
        employee.setHireDate(new Date());

        assertFalse(employeeDAO.addEmployee(employee));
    }

    @Test
    public void testUpdateEmployee() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String originalName = employee.getName();
        String originalPhone = employee.getPhone();

        employee.setName("张教练updated");
        employee.setPhone("13811112222");

        assertTrue(employeeDAO.updateEmployee(employee));

        Employee updated = employeeDAO.getEmployeeById(1);
        assertEquals("张教练updated", updated.getName());
        assertEquals("13811112222", updated.getPhone());

        // 恢复原始数据
        employee.setName(originalName);
        employee.setPhone(originalPhone);
        employeeDAO.updateEmployee(employee);
    }

    @Test
    public void testDeleteEmployee() {
        // 先添加一个测试员工
        Employee employee = new Employee();
        employee.setName("待删除员工");
        employee.setRoleId(2); // 前台
        employee.setPhone("13866665555");
        employee.setHireDate(new Date());

        assertTrue(employeeDAO.addEmployee(employee));
        int employeeId = employee.getId();

        // 删除员工
        assertTrue(employeeDAO.deleteEmployee(employeeId));

        // 验证已删除
        assertNull(employeeDAO.getEmployeeById(employeeId));
    }

    @Test
    public void testGetCoursesByTrainerId() {
        // 张教练(ID=1)负责多个瑜伽课程
        List<Course> courses = employeeDAO.getCoursesByTrainerId(1);
        assertNotNull(courses);
        assertTrue(courses.size() > 0);

        for (Course course : courses) {
            assertEquals(1, course.getEmployeeId());
        }
    }

    @Test
    public void testGetCoursesByTrainerIdNotFound() {
        List<Course> courses = employeeDAO.getCoursesByTrainerId(999);
        assertNotNull(courses);
        assertEquals(0, courses.size());
    }

    @Test
    public void testGetTodayBookingCount() {
        // 测试今日预约数（可能为0，取决于测试数据）
        int count = employeeDAO.getTodayBookingCount(1);
        assertTrue(count >= 0);
    }

    @Test
    public void testHasPermission() {
        // 测试教练权限
        assertTrue(employeeDAO.hasPermission(1, "course_view"));
        assertTrue(employeeDAO.hasPermission(1, "member_view"));
        assertFalse(employeeDAO.hasPermission(1, "member_add"));

        // 测试前台权限
        assertTrue(employeeDAO.hasPermission(4, "member_add"));
        assertTrue(employeeDAO.hasPermission(4, "checkin_manage"));
        assertFalse(employeeDAO.hasPermission(4, "employee_delete"));

        // 测试管理员权限
        assertTrue(employeeDAO.hasPermission(5, "employee_delete"));
        assertTrue(employeeDAO.hasPermission(5, "member_delete"));
    }

    @Test
    public void testHasPermissionInvalidEmployee() {
        assertFalse(employeeDAO.hasPermission(999, "member_view"));
    }

    @Test
    public void testEmployeeRoleRelation() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        // 测试 getRole() 方法返回角色名称
        assertEquals("Trainer", employee.getRole());

        // 测试关联的 EmployeeRole 对象
        EmployeeRole role = employee.getEmployeeRole();
        assertNotNull(role);
        assertEquals(1, role.getRoleId());
        assertEquals("Trainer", role.getRoleName());
        assertNotNull(role.getPermissions());
    }

    @Test
    public void testEmployeeBasicInfo() {
        Employee employee = employeeDAO.getEmployeeById(1);
        assertNotNull(employee);

        String basicInfo = employee.getBasicInfo();
        assertNotNull(basicInfo);
        assertTrue(basicInfo.contains("张教练"));
        assertTrue(basicInfo.contains("Trainer"));
        assertTrue(basicInfo.contains("13800001111"));
    }
}

