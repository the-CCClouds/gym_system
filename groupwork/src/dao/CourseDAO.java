package dao;

import entity.Course;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    /*
        从结果集中提取课程信息
                */
    public Course extractCourseFromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setName(rs.getString("name"));
        course.setType(rs.getString("type"));
        course.setDuration(rs.getInt("duration"));
        course.setMaxCapacity(rs.getInt("max_capacity"));
        course.setEmployeeId(rs.getInt("employee_id"));
        return course;
    }
    /*
        查询所有课程
                */
    public List<Course> getAllCourse() {
        List<Course> courses = new ArrayList<>();
        String sql = "select * from course";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
    /*
        根据ID来查询课程
                */
    public Course getCourseById(int courseId) {
        Course course = null;
        String sql = "select * from course where course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    course = extractCourseFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }
    /*
        根据名字查询课程
                */

    public List<Course> searchCourseByName(String name) {
        List<Course> courses = new ArrayList<>();
        String sql = "select * from course where name like ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
    /*
       根据类型来查询课程
               */
    public List<Course> getCourseByType(String type) {
        List<Course> courses = new ArrayList<>();
        String sql = "select * from course where type = ?";
        return getCourses(type, sql, courses);
    }
    /*
        通用的根据条件查询课程方法
                */
    private List<Course> getCourses(String type, String sql, List<Course> course) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    course.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }
    /*
        根据员工ID来查询课程
                */
    public List<Course> getCourseByEmployeeId(String employeeId) {
        List<Course> courses = new ArrayList<>();
        String sql = "select * from course where employee_id = ?";
        return getCourses(employeeId, sql, courses);
    }
    /*
        添加课程
                */
    public boolean addCourse(Course course) {
        // ✅ 添加类型验证
        String[] validTypes = {"yoga", "spinning", "pilates", "aerobics", "strength", "other"};
        boolean validType = false;
        for (String type : validTypes) {
            if (type.equals(course.getType())) {
                validType = true;
                break;
            }
        }

        // ✅ 如果类型无效,直接返回 false
        if (!validType) {
            System.err.println("无效的课程类型: " + course.getType());
            return false;
        }

        // 添加数据校验
        if (course.getDuration() <= 0 || course.getMaxCapacity() <= 0) {
            System.err.println("无效的课程参数");
            return false;
        }
            String sql = "insert into course (name, type, duration, max_capacity, employee_id) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getType());
            pstmt.setInt(3, course.getDuration());
            pstmt.setInt(4, course.getMaxCapacity());
            pstmt.setInt(5, course.getEmployeeId());
            int rowsAffected = pstmt.executeUpdate();
            // ✅ 获取生成的主键
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course.setCourseId(generatedKeys.getInt(1));
                    }
                }
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /*
        更新课程信息
                */
    public boolean updateCourse(Course course) {
        String sql = "update course set name = ?, type = ?, duration = ?, max_capacity = ?, employee_id = ? where course_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getType());
            pstmt.setInt(3, course.getDuration());
            pstmt.setInt(4, course.getMaxCapacity());
            pstmt.setInt(5, course.getEmployeeId());
            pstmt.setInt(6, course.getCourseId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /*
        删除课程
                */
    public boolean deleteCourse(int courseId) {
        String sql = "delete from course where course_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
