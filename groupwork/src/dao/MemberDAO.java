package dao;

import entity.Member;
import utils.DBUtil;

import java.sql.*;//JDBC相关
import java.util.ArrayList;//List相关
import java.util.List;//List相关

public class MemberDAO {


    public List<Member> getAllMembers() {
        //创建成员列表对象
        List<Member> members = new ArrayList<>();
        //写入sql语句:选择member中的所有表格
        String sql = "select * from member";

        try {
            //获取数据库的连接
            Connection conn = DBUtil.getConnection();
            //创建preparedStatement对象(预处理对象)
            PreparedStatement pstmt = conn.prepareStatement(sql);
            //执行查询，返回结果集
            ResultSet rs = pstmt.executeQuery();

            //遍历结果并创建MEMBER对象
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String status = rs.getString("status");

                Member member = new Member(memberId, name, phone, email, status);
                members.add(member);
            }
            //关闭资源
            rs.close();
            //关闭预处理对象
            pstmt.close();
            //关闭连接
            conn.close();

        } catch (SQLException e) {
            //打印异常信息
            e.printStackTrace();
        }
        //返回成员列表
        return members;
    }

    /**
     * 添加新成员到数据库
     *
     * @param member 要添加的成员对象
     * @return 是否添加成功（true表示成功，false表示失败）
     */

    public boolean addMember(Member member) {
        String sql = "INSERT INTO member(name,phone,email,status) VALUES(?,?,?,?)";
        //insert into member:向member表中插入
        //(name,phone,email,status):指定要插入的字段
        //VALUES(?,?,?,?):使用占位符表示要插入的值
        try {
            //获取数据库连接
            Connection conn = DBUtil.getConnection();
            //创建预处理对象
            //预处理对象就是先检查语法是否正确再执行SQL语句
            PreparedStatement pstmt = conn.prepareStatement(sql);//先准备好语句后执行
            //设置占位符的值
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getStatus());
            //执行更新操作
            int rows = pstmt.executeUpdate();
            //关闭资源
            pstmt.close();
            conn.close();
            //如果影响的行数大于0，表示添加成功
            return rows > 0;
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
        String sql = "UPDATE member SET name=?, phone=?, email=?, status=? WHERE member_id=?";
        //写sql语句来更新member中的内容--也是用占位符进行处理
        try {
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhone());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getStatus());
            pstmt.setInt(5, member.getMemberId());
            int rows = pstmt.executeUpdate();
            //关闭数据库,预处理
            pstmt.close();
            conn.close();

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
        try {
            Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, member.getMemberId());
            int rows = pstmt.executeUpdate();

            //关闭系统
            pstmt.close();
            conn.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

}
