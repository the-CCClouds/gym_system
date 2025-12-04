package dao;

import entity.Course;
import entity.Employee;
import entity.EmployeeRole;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工数据访问对象
 * 对应数据库 employee 表
 */
public class EmployeeDAO {

    private EmployeeRoleDAO roleDAO;

    public EmployeeDAO() {
        this.roleDAO = new EmployeeRoleDAO();
    }

    /**
     * 从结果集中提取员工信息
     */
    public Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setName(rs.getString("name"));
        employee.setRoleId(rs.getInt("role_id"));
        employee.setPhone(rs.getString("phone"));
        employee.setHireDate(rs.getDate("hire_date"));
        return employee;
    }

    /**
     * 从结果集中提取员工信息（包含角色对象）
     */
    public Employee extractEmployeeWithRoleFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = extractEmployeeFromResultSet(rs);
        // 加载关联的角色对象
        EmployeeRole role = roleDAO.getRoleById(employee.getRoleId());
        employee.setEmployeeRole(role);
        return employee;
    }

    // ========== 基础 CRUD ==========

    /**
     * 新增员工
     *
     * @param employee 员工对象
     * @return 是否添加成功
     */
    public boolean addEmployee(Employee employee) {
        // 数据校验
        if (!isValidPhone(employee.getPhone())) {
            System.err.println("无效的手机号: " + employee.getPhone());
            return false;
        }
        if (!isValidRoleId(employee.getRoleId())) {
            System.err.println("无效的角色ID: " + employee.getRoleId());
            return false;
        }

        String sql = "INSERT INTO employee (name, role_id, phone, hire_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRoleId());
            pstmt.setString(3, employee.getPhone());
            pstmt.setDate(4, new java.sql.Date(employee.getHireDate().getTime()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        employee.setEmployeeId(rs.getInt(1));
                    }
                }
            }
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID查询员工
     *
     * @param employeeId 员工ID
     * @return 员工对象，不存在返回null
     */
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employee WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployeeWithRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employee";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                employees.add(extractEmployeeWithRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 更新员工信息
     *
     * @param employee 员工对象
     * @return 是否更新成功
     */
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employee SET name = ?, role_id = ?, phone = ?, hire_date = ? WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getRoleId());
            pstmt.setString(3, employee.getPhone());
            pstmt.setDate(4, new java.sql.Date(employee.getHireDate().getTime()));
            pstmt.setInt(5, employee.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除员工
     *
     * @param employeeId 员工ID
     * @return 是否删除成功
     */
    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employee WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 按角色查询 ==========

    /**
     * 查询所有教练
     *
     * @return 教练列表
     */
    public List<Employee> getTrainers() {
        return getEmployeesByRoleId(1);
    }

    /**
     * 查询所有前台
     *
     * @return 前台列表
     */
    public List<Employee> getReceptionists() {
        return getEmployeesByRoleId(2);
    }

    /**
     * 查询所有管理员
     *
     * @return 管理员列表
     */
    public List<Employee> getAdmins() {
        return getEmployeesByRoleId(3);
    }

    /**
     * 根据角色ID查询员工
     *
     * @param roleId 角色ID
     * @return 员工列表
     */
    public List<Employee> getEmployeesByRoleId(int roleId) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeWithRoleFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // ========== 业务查询 ==========

    /**
     * 根据手机号查询员工（用于登录验证）
     *
     * @param phone 手机号
     * @return 员工对象，不存在返回null
     */
    public Employee getEmployeeByPhone(String phone) {
        String sql = "SELECT * FROM employee WHERE phone = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployeeWithRoleFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据姓名模糊查询员工
     *
     * @param name 姓名关键字
     * @return 员工列表
     */
    public List<Employee> searchEmployeeByName(String name) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE name LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeWithRoleFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * 查询某教练负责的所有课程
     *
     * @param employeeId 教练ID
     * @return 课程列表
     */
    public List<Course> getCoursesByTrainerId(int employeeId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE employee_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course();
                    course.setCourseId(rs.getInt("course_id"));
                    course.setName(rs.getString("name"));
                    course.setType(rs.getString("type"));
                    course.setDuration(rs.getInt("duration"));
                    course.setMaxCapacity(rs.getInt("max_capacity"));
                    course.setEmployeeId(rs.getInt("employee_id"));
                    courses.add(course);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 查询某教练今日的课程预约数
     *
     * @param trainerId 教练ID
     * @return 今日预约数
     */
    public int getTodayBookingCount(int trainerId) {
        String sql = "SELECT COUNT(*) AS count FROM booking b " +
                "JOIN course c ON b.course_id = c.course_id " +
                "WHERE c.employee_id = ? " +
                "AND DATE(b.booking_time) = CURDATE() " +
                "AND b.booking_status IN ('pending', 'confirmed')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检查员工是否拥有某个权限
     *
     * @param employeeId 员工ID
     * @param permission 权限名称
     * @return 是否拥有该权限
     */
    public boolean hasPermission(int employeeId, String permission) {
        Employee employee = getEmployeeById(employeeId);
        if (employee != null) {
            return roleDAO.hasPermission(employee.getRoleId(), permission);
        }
        return false;
    }

    // ========== 校验方法 ==========

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private boolean isValidRoleId(int roleId) {
        return roleId >= 1 && roleId <= 3;
    }
}

