package dao;

import entity.Member;
import utils.DBUtil;

import java.sql.*;//JDBC相关
import java.util.ArrayList;//List相关
import java.util.List;//List相关

public class MemberDAO {


    public MemberDAO() {
    }

    public Member extractMemberFromResultSet(ResultSet rs) throws SQLException{
        Member member = new Member();
            member.setId(rs.getInt("member_id"));
            member.setName(rs.getString("name"));
            member.setPhone(rs.getString("phone"));
            member.setEmail(rs.getString("email"));
            member.setGender(rs.getString("gender"));
            member.setBirthDate(rs.getDate("birth_date"));
            member.setRegisterDate(rs.getDate("register_date"));
            member.setStatus(rs.getString("status"));
        return member;
    }

    public List<Member> getAllMembers() {
        //创建成员列表对象
        List<Member> members = new ArrayList<>();
        //写入sql语句:选择member中的所有表格
        String sql = "select * from member";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }


        } catch (SQLException e) {
            //打印异常信息
            e.printStackTrace();
        }
        //返回成员列表
        return members;
    }

    /**
     * 根据ID来查询
     *
     * @param memberId 输入你要查询的id
     * @return 返回查询结果
     */
    public Member getMemberById(int memberId) {
        Member member = new Member();
        String sql = "select * from member where member_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
               return extractMemberFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 根据名字来查询
     *
     * @param name 输入你要查询的id
     * @return 返回查询结果
     */
    public List<Member> getMemberByName(String name) {
        List<Member> member = new ArrayList<>();
        String sql = "select * from member where name = ?";
        return getStringMember(name,sql,member);
    }

    /**
     * 根据名字来查询
     *
     * @param gender 输入你要查询的id
     * @return 返回查询结果
     */
    public List<Member> getMemberByGender(String gender) {
        List<Member> member = new ArrayList<>();
        String sql = "select * from member where gender = ?";
        return  getStringMember(gender, sql, member);
    }


    private List<Member> getStringMember(String gender, String sql, List<Member> member) {
        try(Connection conn=DBUtil.getConnection(); PreparedStatement pstmt=conn.prepareStatement(sql);){
            pstmt.setString(1, gender);
            try(ResultSet rs=pstmt.executeQuery()){
                while(rs.next()){
                    member.add(extractMemberFromResultSet(rs));
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return member;
    }

    public List<Member> getMemberByBirthDate(java.util.Date birthDate) {
       List<Member> member = new ArrayList<>();
       String sql = "select * from member where birth_date = ?";

       try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
           pstmt.setDate(1, new java.sql.Date(birthDate.getTime()));
           try (ResultSet rs = pstmt.executeQuery()) {
               while (rs.next()) {
                   member.add(extractMemberFromResultSet(rs));
               }
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return member;
   }

    public List<Member> getMemberByStatus(String status) {
        List<Member> member = new ArrayList<>();
        String sql = "select * from member where status = ?";
        return getStringMember(status,sql,member);
    }

    // 6. 根据手机号搜索
    public List<Member> searchByPhone(String phone) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE phone LIKE ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + phone + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }



    /**
     * 添加新成员到数据库
     *
     * @param member 要添加的成员对象
     * @return 是否添加成功（true表示成功，false表示失败）
     */

    // 3. 添加会员
    public boolean addMember(Member member) {
        // 数据校验
        if (!isValidEmail(member.getEmail())) {
            System.err.println("无效的邮箱格式: " + member.getEmail());
            return false;
        }
        if (!isValidPhone(member.getPhone())) {
            System.err.println("无效的手机号: " + member.getPhone());
            return false;
        }
        if (!isValidStatus(member.getStatus())) {
            System.err.println("无效的状态: " + member.getStatus());
            return false;
        }

        String sql = "INSERT INTO member (name, phone, email, gender, birth_date, register_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getGender());
            pstmt.setDate(5, new java.sql.Date(member.getBirthDate().getTime()));
            pstmt.setTimestamp(6, new Timestamp(member.getRegisterDate().getTime()));
            pstmt.setString(7, member.getStatus());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setId(rs.getInt(1));
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
     * 更新成员到数据库
     *
     * @param member 要更新的成员对象
     * @return 是否更新成功（true表示成功，false表示失败）
     */
    public boolean updateMember(Member member) {
        String sql = "UPDATE member SET name=?, phone=?, email=?, gender=?, " +
                "birth_date=?,register_date=?, status=? WHERE member_id=?";
        //写sql语句来更新member中的内容--也是用占位符进行处理
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getGender());
            pstmt.setDate(5, new Date(member.getBirthDate().getTime()));
            pstmt.setTimestamp(6, new Timestamp(member.getRegisterDate().getTime()));
            pstmt.setString(7, member.getStatus());
            pstmt.setInt(8, member.getId());
            int rows = pstmt.executeUpdate();


            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 删除表格在数据库中
     *
     * @param member 要删除的成员对象
     * @return 是否删除成功（true表示成功，false表示失败）
     */

    public boolean deleteMember(Member member) {
        String sql = "DELETE FROM member WHERE member_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, member.getId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
    // 校验方法
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private boolean isValidStatus(String status) {
        return "active".equals(status) || "inactive".equals(status) || "frozen".equals(status);
    }

}
