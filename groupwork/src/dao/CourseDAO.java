package dao;

import entity.Course;
import entity.Member;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public Course QueryAllCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        try {
            course.setCourseId(rs.getInt("course_id"));
            course.setName(rs.getString("name"));
            course.setType(rs.getString("type"));
            course.setDuration(rs.getInt("duration"));
            course.setMaxCapacity(rs.getInt("max_capacity"));
            course.setEmployeeId(rs.getInt("employee_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    public List<Course> getAllCourse() {
        List<Course> course = new ArrayList<>();
        String sql = "select * from course";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                course.add(QueryAllCourse(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    public Course getCourseById(int courseId) {
        Course course = new Course();
        String sql = "select * from course where course_id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                course = QueryAllCourse(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    public List<Course> getCourseByType(String type) {
        List<Course> course = new ArrayList<>();
        String sql = "select * from course where type = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                course.add(QueryAllCourse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    public boolean addCourse(Course course) {
        String sql = "insert into course (name, type, duration, max_capacity, employee_id) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getType());
            pstmt.setInt(3, course.getDuration());
            pstmt.setInt(4, course.getMaxCapacity());
            pstmt.setInt(5, course.getEmployeeId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
