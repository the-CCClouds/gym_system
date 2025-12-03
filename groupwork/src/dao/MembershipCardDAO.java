package dao;

import entity.MembershipCard;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembershipCardDAO {

    /*
        查询所有会员卡
            */
    public List<MembershipCard> getAll() {
        List<MembershipCard> membershipCards = new ArrayList<>();
        String sql = "select * from membership_card";
        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
        ) {
            while (rs.next()) {
                MembershipCard mc = mapResultSsetToEntity(rs);
                membershipCards.add(mc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membershipCards;
    }
    /*
        通过id进行查询会员卡
                */
    public MembershipCard getById(int cardId) {
        MembershipCard mc = null;
        String sql = "select * from membership_card where card_id=?";
        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery();) {
                if (rs.next()) {
                    mc = mapResultSsetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mc;
    }
    /*
        通过id进行查询会员卡
             */
    public List<MembershipCard> getByMemberId(int memberId) {
        List<MembershipCard> membershipCards = new ArrayList<>();
        String sql = "select * from membership_card where member_id=?";
        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery();) {
                while (rs.next()) {
                    MembershipCard mc = mapResultSsetToEntity(rs);
                    membershipCards.add(mc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return membershipCards;
    }
    /*
    将结果集映射为实体类
         */
    private static MembershipCard mapResultSsetToEntity(ResultSet rs) throws SQLException {
        MembershipCard mc = new MembershipCard();
        mc.setCardId(rs.getInt("card_id"));
        mc.setMemberId(rs.getInt("member_id"));
        mc.setCardType(rs.getString("card_type"));
        mc.setStartDate(rs.getDate("start_date"));
        mc.setEndDate(rs.getDate("end_date"));
        mc.setCardStatus(rs.getString("card_status"));
        return mc;
    }
    /*
    按照状态查询会员卡
     */
    public List<MembershipCard> getByStatus(String status) {
        List<MembershipCard> cards = new ArrayList<>();
        String sql = "select * from membership_card where card_status=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSsetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }
    /*
    计算剩余天数
     */
    public int getRemainingDays(int cardId) {
        String sql = "select DATEDIFF(end_date, CURDATE()) from membership_card where card_id=?";
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

    /*
    更新过期会员卡状态
                   */
    public int updateExpiredCards() {
        String sql = "update membership_card set card_status='expired' where end_date < CURDATE() and card_status='active'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }


    /*
    添加会员卡
                */
    public boolean addMembershipCard(MembershipCard membershipCard) {
        // 添加校验逻辑
        if (!validateMemberExists(membershipCard.getMemberId())) {
            System.err.println("会员ID不存在: " + membershipCard.getMemberId());
            return false;
        }
        if (!isValidCardType(membershipCard.getCardType())) {
            System.err.println("无效的卡类型: " + membershipCard.getCardType());
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


        String sql = "insert into membership_card (member_id, card_type, start_date, end_date, card_status) values (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, membershipCard.getMemberId());
            pstmt.setString(2, membershipCard.getCardType());
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

     /*
    更新会员卡信息
             */

    public boolean updateMembershipCard(MembershipCard membershipCard) {
        String sql = "update membership_card set member_id=?, card_type=?, start_date=?, end_date=?, card_status=? where card_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, membershipCard.getMemberId());
            pstmt.setString(2, membershipCard.getCardType());
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

     /*
    删除会员卡
             */

    public boolean deleteMembershipCard(int cardId) {
        String sql = "delete from membership_card where card_id=?";
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

    /*
    校验会员ID是否存在
                */
    private boolean validateMemberExists(int memberId) {
        String sql = "select count(*) from member where member_id=?";
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
    /*
    校验卡类型是否合法
                */
    private boolean isValidCardType(String cardType) {
        return "yearly".equals(cardType) || "monthly".equals(cardType);
    }
    /*
    校验卡状态是否合法
                */
    private boolean isValidCardStatus(String status) {
        return "active".equals(status) || "inactive".equals(status) || "expired".equals(status);
    }
    /*
    校验日期范围是否合法
                */
    private boolean isValidDateRange(java.util.Date startDate, java.util.Date endDate) {
        return startDate != null && endDate != null && startDate.before(endDate);
    }

}