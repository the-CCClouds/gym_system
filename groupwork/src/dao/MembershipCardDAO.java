package dao;

import entity.MembershipCard;
import entity.MembershipType;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembershipCardDAO {

    private MembershipTypeDAO typeDAO;

    public MembershipCardDAO() {
        this.typeDAO = new MembershipTypeDAO();
    }

    /**
     * 从结果集映射为实体类
     */
    private MembershipCard mapResultSetToEntity(ResultSet rs) throws SQLException {
        MembershipCard mc = new MembershipCard();
        mc.setCardId(rs.getInt("card_id"));
        mc.setMemberId(rs.getInt("member_id"));
        mc.setTypeId(rs.getInt("type_id"));
        mc.setStartDate(rs.getDate("start_date"));
        mc.setEndDate(rs.getDate("end_date"));
        mc.setCardStatus(rs.getString("card_status"));
        return mc;
    }

    /**
     * 从结果集映射为实体类（包含类型对象）
     */
    private MembershipCard mapResultSetToEntityWithType(ResultSet rs) throws SQLException {
        MembershipCard mc = mapResultSetToEntity(rs);
        // 加载关联的类型对象
        MembershipType type = typeDAO.getTypeById(mc.getTypeId());
        mc.setMembershipType(type);
        return mc;
    }

    /**
     * 查询所有会员卡
     */
    public List<MembershipCard> getAll() {
        List<MembershipCard> membershipCards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                membershipCards.add(mapResultSetToEntityWithType(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membershipCards;
    }

    /**
     * 通过id查询会员卡
     */
    public MembershipCard getById(int cardId) {
        String sql = "SELECT * FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntityWithType(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过会员ID查询会员卡
     */
    public List<MembershipCard> getByMemberId(int memberId) {
        List<MembershipCard> membershipCards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    membershipCards.add(mapResultSetToEntityWithType(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membershipCards;
    }

    /**
     * 按照状态查询会员卡
     */
    public List<MembershipCard> getByStatus(String status) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE card_status = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 按照类型ID查询会员卡
     */
    public List<MembershipCard> getByTypeId(int typeId) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM membership_card WHERE type_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, typeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToEntityWithType(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    /**
     * 获取所有月卡
     */
    public List<MembershipCard> getMonthlyCards() {
        return getByTypeId(1);
    }

    /**
     * 获取所有年卡
     */
    public List<MembershipCard> getYearlyCards() {
        return getByTypeId(2);
    }

    /**
     * 计算剩余天数
     */
    public int getRemainingDays(int cardId) {
        String sql = "SELECT DATEDIFF(end_date, CURDATE()) FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 更新过期会员卡状态
     */
    public int updateExpiredCards() {
        String sql = "UPDATE membership_card SET card_status = 'expired' WHERE end_date < CURDATE() AND card_status = 'active'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 添加会员卡
     */
    public boolean addMembershipCard(MembershipCard membershipCard) {
        // 添加校验逻辑
        if (!validateMemberExists(membershipCard.getMemberId())) {
            System.err.println("会员ID不存在: " + membershipCard.getMemberId());
            return false;
        }
        if (!isValidTypeId(membershipCard.getTypeId())) {
            System.err.println("无效的卡类型ID: " + membershipCard.getTypeId());
            return false;
        }
        if (!isValidCardStatus(membershipCard.getCardStatus())) {
            System.err.println("无效的卡状态: " + membershipCard.getCardStatus());
            return false;
        }
        if (!isValidDateRange(membershipCard.getStartDate(), membershipCard.getEndDate())) {
            System.err.println("日期范围错误: start_date 必须早于 end_date");
            return false;
        }

        String sql = "INSERT INTO membership_card (member_id, type_id, start_date, end_date, card_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, membershipCard.getMemberId());
            pstmt.setInt(2, membershipCard.getTypeId());
            pstmt.setDate(3, new java.sql.Date(membershipCard.getStartDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(membershipCard.getEndDate().getTime()));
            pstmt.setString(5, membershipCard.getCardStatus());

            int affectedRows = pstmt.executeUpdate();

            // 获取数据库自动生成的 card_id
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        membershipCard.setCardId(rs.getInt(1));
                    }
                }
            }
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新会员卡信息
     */
    public boolean updateMembershipCard(MembershipCard membershipCard) {
        String sql = "UPDATE membership_card SET member_id = ?, type_id = ?, start_date = ?, end_date = ?, card_status = ? WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, membershipCard.getMemberId());
            pstmt.setInt(2, membershipCard.getTypeId());
            pstmt.setDate(3, new java.sql.Date(membershipCard.getStartDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(membershipCard.getEndDate().getTime()));
            pstmt.setString(5, membershipCard.getCardStatus());
            pstmt.setInt(6, membershipCard.getCardId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除会员卡
     */
    public boolean deleteMembershipCard(int cardId) {
        String sql = "DELETE FROM membership_card WHERE card_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cardId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 校验方法 ====================

    /**
     * 校验会员ID是否存在
     */
    private boolean validateMemberExists(int memberId) {
        String sql = "SELECT COUNT(*) FROM member WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 校验类型ID是否合法
     */
    private boolean isValidTypeId(int typeId) {
        return typeId == 1 || typeId == 2;
    }

    /**
     * 校验卡状态是否合法
     */
    private boolean isValidCardStatus(String status) {
        return "active".equals(status) || "inactive".equals(status) || "expired".equals(status);
    }

    /**
     * 校验日期范围是否合法
     */
    private boolean isValidDateRange(java.util.Date startDate, java.util.Date endDate) {
        return startDate != null && endDate != null && startDate.before(endDate);
    }
}
