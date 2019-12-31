package kerberos;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

public class HiveKerberos {
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";// jdbc驱动路径
    private static String url = "jdbc:hive2://hadoop01.com:10000/asmp;principal=hive/_HOST@CSVW.COM";// hive库地址+库名
    //	private static String user = "";// 用户名
//	private static String password = "";// 密码
    private static String sql = "";
    private static ResultSet res;


    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConn();
            System.out.println(conn);
            stmt = conn.createStatement();
            String tableName = "test_count_day";// hive表名
            sql = "select * from " + tableName;
            System.out.println("Running:" + sql);
            res = stmt.executeQuery(sql);
            System.out.println("执行 select * query 运行结果:");
            while (res.next()) {
                System.out.println(res.getString(1) + "\t" + res.getString(2));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void authKrb5() {
        // 设置jvm启动时krb5的读取路径参数
        System.setProperty("java.security.krb5.conf","src/main/resources/krb5.conf");
        // 配置kerberos认证
        Configuration conf = new Configuration();
        conf.setBoolean("hadoop.security.authorization", true);
        conf.set("hadoop.security.authentication", "kerberos");
        // System.out.println(System.getProperty("java.security.krb5.conf"));
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab(
                    "asmp@CSVW.COM",
                    "src/main/resources/asmp.keytab");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Succeeded in authenticating through Kerberos!");
    }

    private static Connection getConn() throws ClassNotFoundException,
            SQLException {
        //认证kerberos
        authKrb5();
        Class.forName(driverName);
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }


}


