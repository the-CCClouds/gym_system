package dao;

import entity.MembershipType;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员卡类型数据访问对象
 * 对应数据库 membership_type 表
 */
public class MembershipTypeDAO {

    public MembershipTypeDAO() {
    }

    /**
     * 从结果集中提取类型信息
     */
    public MembershipType extractTypeFromResultSet(ResultSet rs) throws SQLException {
        MembershipType type = new MembershipType();
        type.setTypeId(rs.getInt("type_id"));
        type.setTypeName(rs.getString("type_name"));
        type.setDurationDays(rs.getInt("duration_days"));
        type.setPrice(rs.getDouble("price"));
        type.setDescription(rs.getString("description"));
        return type;
    }

    /**
     * 获取所有会员卡类型
     *
     * @return 类型列表
     */
    public List<MembershipType> getAllTypes() {
        List<MembershipType> types = new ArrayList<>();
        String sql = "SELECT * FROM membership_type";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                types.add(extractTypeFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    /**
     * 根据ID获取类型
     *
     * @param typeId 类型ID
     * @return 类型对象，不存在返回null
     */
    public MembershipType getTypeById(int typeId) {
        String sql = "SELECT * FROM membership_type WHERE type_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTypeFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类型名称获取类型
     *
     * @param typeName 类型名称 (Monthly, Yearly)
     * @return 类型对象，不存在返回null
     */
    public MembershipType getTypeByName(String typeName) {
        String sql = "SELECT * FROM membership_type WHERE type_name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, typeName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTypeFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取月卡类型
     *
     * @return 月卡类型对象
     */
    public MembershipType getMonthlyType() {
        return getTypeById(1);
    }

    /**
     * 获取年卡类型
     *
     * @return 年卡类型对象
     */
    public MembershipType getYearlyType() {
        return getTypeById(2);
    }

    /**
     * 添加新类型
     *
     * @param type 类型对象
     * @return 是否添加成功
     */
    public boolean addType(MembershipType type) {
        String sql = "INSERT INTO membership_type (type_id, type_name, duration_days, price, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, type.getTypeId());
            pstmt.setString(2, type.getTypeName());
            pstmt.setInt(3, type.getDurationDays());
            pstmt.setDouble(4, type.getPrice());
            pstmt.setString(5, type.getDescription());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新类型信息
     *
     * @param type 类型对象
     * @return 是否更新成功
     */
    public boolean updateType(MembershipType type) {
        String sql = "UPDATE membership_type SET type_name = ?, duration_days = ?, price = ?, description = ? WHERE type_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.getTypeName());
            pstmt.setInt(2, type.getDurationDays());
            pstmt.setDouble(3, type.getPrice());
            pstmt.setString(4, type.getDescription());
            pstmt.setInt(5, type.getTypeId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除类型
     *
     * @param typeId 类型ID
     * @return 是否删除成功
     */
    public boolean deleteType(int typeId) {
        String sql = "DELETE FROM membership_type WHERE type_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据类型ID获取价格
     *
     * @param typeId 类型ID
     * @return 价格
     */
    public double getPriceByTypeId(int typeId) {
        MembershipType type = getTypeById(typeId);
        return type != null ? type.getPrice() : 0.0;
    }

    /**
     * 根据类型ID获取有效期天数
     *
     * @param typeId 类型ID
     * @return 有效期天数
     */
    public int getDurationDaysByTypeId(int typeId) {
        MembershipType type = getTypeById(typeId);
        return type != null ? type.getDurationDays() : 0;
    }
}

