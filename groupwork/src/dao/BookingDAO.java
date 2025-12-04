package dao;

import entity.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BookingDAO {


    public Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setMemberId(rs.getInt("member_id"));
        booking.setCourseId(rs.getInt("course_id"));
        booking.setBookingTime(rs.getDate("booking_time"));
        booking.setBookingStatus(rs.getString("booking_status"));
        return booking;
    }

    public Booking getBookingById(int bookingId) {
        Booking booking = null;
        String sql = "select * from booking where booking_id = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    booking = extractBookingFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booking;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "select * from booking";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getAllBookingsByMemberId(int memberId) {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "select * from booking where member_id = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getAllBookingsByCourseId(int courseId) {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "select * from booking where course_id = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getAllBookingsByBookingStatus(String bookingStatus) {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "select * from booking where booking_status = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookingStatus);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean addBooking(Booking booking) {
        // ✅ 第1步:检查会员卡是否有效
        if (!checkMembershipCardValid(booking.getMemberId())) {
            return false; // 会员卡无效/过期
        }

        // ✅ 第2步:检查是否重复预约
        if (checkDuplicateBooking(booking.getMemberId(), booking.getCourseId())) {
            return false; // 已预约过该课程
        }
        // ✅ 第3步:检查课程容量
        if (!checkCourseCapacity(booking.getCourseId())) {
            return false; // 课程已满
        }
        String sql = "insert into booking (member_id, course_id, booking_time, booking_status) values (?, ?, ?, ?)";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, booking.getMemberId());
            pstmt.setInt(2, booking.getCourseId());
            pstmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.setString(4, booking.getBookingStatus());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkMembershipCardValid(int memberId) {
        String sql = "SELECT COUNT(*) AS count FROM membership_card " +
                "WHERE member_id = ? AND card_status = 'active' AND end_date >= CURDATE()";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkCourseCapacity(int courseId) {
        String sql = "SELECT COUNT(*) AS booking_count, c.max_capacity " +
                "FROM booking b JOIN course c ON b.course_id = c.course_id " +
                "WHERE b.course_id = ? AND b.booking_status = 'confirmed' " + // ✅ 只统计已确认的
                "GROUP BY c.max_capacity";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int bookingCount = rs.getInt("booking_count");
                    int maxCapacity = rs.getInt("max_capacity");
                    return bookingCount < maxCapacity;
                } else {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean confirmBooking(int bookingId) {
        String querysql = "SELECT course_id, booking_status FROM booking WHERE booking_id=? ";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(querysql)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // ✅ 第1步: 检查状态是否为 pending
                    String status = rs.getString("booking_status");
                    if (!"pending".equals(status)) {
                        return false;
                    }
                    // ✅ 第2步: 获取 course_id 并检查容量
                    int courseId = rs.getInt("course_id");
                    if (!checkCourseCapacity(courseId)) {
                        return false;
                    }
                    // ✅ 第3步: 更新状态为 confirmed
                    String updatesql = "UPDATE booking SET booking_status = 'confirmed' " +
                            "WHERE booking_id = ? AND booking_status = 'pending'";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updatesql)) {
                        updatePstmt.setInt(1, bookingId);
                        int affectedRows = updatePstmt.executeUpdate();
                        return affectedRows > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkDuplicateBooking(int memberId, int courseId) {
        String sql = "SELECT COUNT(*) AS count FROM booking " +
                "WHERE member_id = ? AND course_id = ? " +
                "AND booking_status IN ('pending', 'confirmed')";

        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, courseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count > 0; // true = 重复
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelBooking(int bookingId){
        String sql = "UPDATE booking SET booking_status = 'cancelled' WHERE booking_id = ? AND booking_status != 'cancelled'";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /*
    状态更行
     */
    public boolean updateBookingStatus(int bookingId, String newStatus){
        String sql = "UPDATE booking SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    统计已经确认的预约数量
     */
    public int getComfirmedBookingCount(int courseId) {
        String sql = "SELECT COUNT(*) AS count FROM booking WHERE course_id = ? AND booking_status = 'confirmed'";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
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

    public List<Booking> getUpcomingBookingsForMember(int memberId) {
        List<Booking> bookings = new java.util.ArrayList<>();
        String sql = "SELECT b.* FROM booking b " +
                "JOIN course c ON b.course_id = c.course_id " +
                "WHERE b.member_id = ? AND c.schedule_time > NOW() " +
                "AND b.booking_status IN ('pending', 'confirmed')";
        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
// 放在 groupwork/src/dao/BookingDAO.java 中

    public List<Booking> getBookingHistory(int memberId, java.sql.Date startDate, java.sql.Date endDate) {
        List<Booking> bookings = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM booking WHERE member_id = ?");
        if (startDate != null && endDate != null) {
            sql.append(" AND booking_time BETWEEN ? AND ?");
        } else if (startDate != null) {
            sql.append(" AND booking_time >= ?");
        } else if (endDate != null) {
            sql.append(" AND booking_time <= ?");
        }

        try (Connection conn = utils.DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            pstmt.setInt(idx++, memberId);
            if (startDate != null && endDate != null) {
                pstmt.setDate(idx++, startDate);
                pstmt.setDate(idx++, endDate);
            } else if (startDate != null) {
                pstmt.setDate(idx++, startDate);
            } else if (endDate != null) {
                pstmt.setDate(idx++, endDate);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }



    public List<Booking> getBookingHistory(int memberId, String startDateStr, String endDateStr) {
        java.sql.Date startDate = null;
        java.sql.Date endDate = null;
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = utils.DateUtils.toSqlDate(startDateStr);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = utils.DateUtils.toSqlDate(endDateStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
        return getBookingHistory(memberId, startDate, endDate);
    }


}

